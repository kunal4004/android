package za.co.woolworths.financial.services.android.checkout.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.checkout_address_confirmation.*
import kotlinx.android.synthetic.main.checkout_address_confirmation_delivery.*
import za.co.woolworths.financial.services.android.checkout.interactor.CheckoutAddAddressNewUserInteractor
import za.co.woolworths.financial.services.android.checkout.service.network.*
import za.co.woolworths.financial.services.android.checkout.view.adapter.CheckoutAddressConfirmationListAdapter
import za.co.woolworths.financial.services.android.checkout.viewmodel.CheckoutAddAddressNewUserViewModel
import za.co.woolworths.financial.services.android.checkout.viewmodel.ViewModelFactory
import za.co.woolworths.financial.services.android.models.dto.Province
import za.co.woolworths.financial.services.android.models.dto.Suburb
import za.co.woolworths.financial.services.android.service.network.ResponseStatus
import za.co.woolworths.financial.services.android.ui.activities.click_and_collect.EditDeliveryLocationActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.DeliveryType
import za.co.woolworths.financial.services.android.util.Utils
import java.io.IOException


/**
 * Created by Kunal Uttarwar on 16/06/21.
 */
class CheckoutAddressConfirmationFragment : Fragment(), View.OnClickListener,
    CheckoutAddressConfirmationListAdapter.EventListner {

    var savedAddress: SavedAddressResponse? = null
    var checkoutAddressConfirmationListAdapter: CheckoutAddressConfirmationListAdapter? = null
    private lateinit var checkoutAddAddressNewUserViewModel: CheckoutAddAddressNewUserViewModel
    private var navController: NavController? = null

    companion object {
        const val UPDATE_SAVED_ADDRESS_REQUEST_KEY = "updateSavedAddress"
        const val DELETE_SAVED_ADDRESS_REQUEST_KEY = "deleteSavedAddress"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.checkout_address_confirmation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (navController == null)
            navController = Navigation.findNavController(view)

        if (activity is CheckoutActivity)
            (activity as? CheckoutActivity)?.showBackArrowWithoutTitle()
        setupViewModel()
        initView()
        addFragmentResultListener()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = arguments?.getBundle("bundle")
        updateSavedAddress(bundle)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.deliveryTab -> {
                deliveryTab.setBackgroundResource(R.drawable.delivery_round_btn_white)
                collectionTab.setBackgroundResource(R.drawable.rounded_view_grey_tab_bg)
                addressConfirmationDelivery.visibility = View.VISIBLE
            }
            R.id.collectionTab -> {
                collectionTab.setBackgroundResource(R.drawable.delivery_round_btn_white)
                deliveryTab.setBackgroundResource(R.drawable.rounded_view_grey_tab_bg)
                addressConfirmationDelivery.visibility = View.GONE
            }
            R.id.plusImgAddAddress, R.id.addNewAddressTextView -> {
                navigateToAddAddress()
            }
            R.id.btnAddressConfirmation -> {
                if (savedAddress?.addresses == null || savedAddress?.addresses?.size == 0) {
                    navigateToAddAddress()
                } else if (checkoutAddressConfirmationListAdapter?.checkedItemPosition == -1)
                    addNewAddressErrorMsg.visibility = View.VISIBLE
                else {
                    //TO DO next screen
                }
            }
        }
    }

    private fun navigateToAddAddress() {
        val bundle = Bundle()
        bundle.putBoolean("addNewAddress", true)
        navController?.navigate(
            R.id.action_checkoutAddressConfirmationFragment_to_CheckoutAddAddressNewUserFragment,
            bundleOf("bundle" to bundle)
        )
    }

    private fun addFragmentResultListener() {
        // Use the Kotlin extension in the fragment-ktx artifact
        setFragmentResultListener(UPDATE_SAVED_ADDRESS_REQUEST_KEY) { requestKey, bundle ->
            updateSavedAddress(bundle)
            checkoutAddressConfirmationListAdapter?.notifyDataSetChanged()
        }
        setFragmentResultListener(DELETE_SAVED_ADDRESS_REQUEST_KEY) { requestKey, bundle ->
            updateSavedAddress(bundle)
            checkoutAddressConfirmationListAdapter?.notifyDataSetChanged()
        }
    }

    private fun updateSavedAddress(bundle: Bundle?) {
        bundle?.apply {
            if (containsKey("savedAddress")) {
                val addressString = getString("savedAddress")
                if (!addressString.isNullOrEmpty() && !addressString.equals("null", true))
                    savedAddress = (Utils.jsonStringToObject(
                        addressString,
                        SavedAddressResponse::class.java
                    ) as? SavedAddressResponse)
            }
        }
    }

    private fun initView() {
        if (savedAddress?.addresses == null || savedAddress?.addresses?.size == 0) {
            hideAddressListView()
        } else {
            showAddressListView()
            checkoutAddressConfirmationListAdapter =
                CheckoutAddressConfirmationListAdapter(savedAddress, navController, this)
            saveAddressRecyclerView?.apply {
                addItemDecoration(object : ItemDecoration() {})
                layoutManager = activity?.let { LinearLayoutManager(it) }
                checkoutAddressConfirmationListAdapter?.let { adapter = it }
            }
        }
        deliveryTab.setOnClickListener(this)
        collectionTab.setOnClickListener(this)
        plusImgAddAddress.setOnClickListener(this)
        addNewAddressTextView.setOnClickListener(this)
        btnAddressConfirmation.setOnClickListener(this)
    }

    private fun hideAddressListView() {
        btnAddressConfirmation.text = bindString(R.string.add_address)
        whereWeDeliveringTitle.text = bindString(R.string.no_saved_addresses)
        saveAddressRecyclerView.visibility = View.GONE
        addressListPartition.visibility = View.GONE
        plusImgAddAddress.visibility = View.GONE
        confirmAddressPartition.visibility = View.GONE
        addNewAddressTextView.visibility = View.GONE
    }

    private fun showAddressListView() {
        saveAddressRecyclerView.visibility = View.VISIBLE
        addressListPartition.visibility = View.VISIBLE
        plusImgAddAddress.visibility = View.VISIBLE
        addNewAddressTextView.visibility = View.VISIBLE
        confirmAddressPartition.visibility = View.VISIBLE
        btnAddressConfirmation.text = bindString(R.string.confirm)
        whereWeDeliveringTitle.text = bindString(R.string.where_should_we_deliver)
    }

    private fun setupViewModel() {
        checkoutAddAddressNewUserViewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(
                CheckoutAddAddressNewUserInteractor(
                    CheckoutAddAddressNewUserApiHelper(),
                    CheckoutMockApiHelper()
                )
            )
        ).get(CheckoutAddAddressNewUserViewModel::class.java)
    }

    fun navigateToUnsellableItemsFragment(
        unSellableCommerceItems: MutableList<UnSellableCommerceItem>,
        address: Address,
        deliverable: Boolean
    ) {
        val suburb = Suburb()
        suburb.apply {
            id = address.suburbId
            name = address.suburb
            postalCode = address.postalCode
            suburbDeliverable = deliverable
        }
        val province = Province()
        province.apply {
            name = address.city
            id = address.region
        }
        val bundle = Bundle()
        bundle.apply {
            putString(EditDeliveryLocationActivity.DELIVERY_TYPE, DeliveryType.DELIVERY.name)
            putString("SUBURB", Utils.toJson(suburb))
            putString("PROVINCE", Utils.toJson(province))
            putString("UnSellableCommerceItems", Utils.toJson(unSellableCommerceItems))
        }
        navController?.navigate(
            R.id.action_to_unsellableItemsFragment,
            bundleOf("bundle" to bundle)
        )
    }

    override fun hideErrorView() {
        addNewAddressErrorMsg.visibility = View.GONE
    }

    override fun changeAddress(address: Address) {
        checkoutAddAddressNewUserViewModel.changeAddress(address.nickname)
            .observe(viewLifecycleOwner, {
                when (it.responseStatus) {
                    ResponseStatus.SUCCESS -> {

                        val jsonFileString = Utils.getJsonDataFromAsset(
                            activity?.applicationContext,
                            "mocks/unsellableItems.json"
                        )
                        var mockChangeAddressResponse: ChangeAddressResponse = Gson().fromJson(
                            jsonFileString,
                            object : TypeToken<ChangeAddressResponse>() {}.type
                        )


                        val changeAddressResponse = mockChangeAddressResponse //it?.data
                        if (changeAddressResponse != null && changeAddressResponse?.deliverable) {
                            if (changeAddressResponse?.unSellableCommerceItems?.size!! > 0) {
                                navigateToUnsellableItemsFragment(
                                    changeAddressResponse?.unSellableCommerceItems,
                                    address,
                                    changeAddressResponse?.deliverable
                                )
                            }
                        }
                    }
                    ResponseStatus.LOADING -> {

                    }
                    ResponseStatus.ERROR -> {

                    }
                }
            })
    }
}