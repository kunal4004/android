package za.co.woolworths.financial.services.android.checkout.view

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
import kotlinx.android.synthetic.main.checkout_address_confirmation.*
import kotlinx.android.synthetic.main.checkout_address_confirmation_delivery.*
import za.co.woolworths.financial.services.android.checkout.interactor.CheckoutAddAddressNewUserInteractor
import za.co.woolworths.financial.services.android.checkout.service.network.CheckoutAddAddressNewUserApiHelper
import za.co.woolworths.financial.services.android.checkout.service.network.CheckoutMockApiHelper
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.checkout.view.adapter.CheckoutAddressConfirmationListAdapter
import za.co.woolworths.financial.services.android.checkout.viewmodel.CheckoutAddAddressNewUserViewModel
import za.co.woolworths.financial.services.android.checkout.viewmodel.ViewModelFactory
import za.co.woolworths.financial.services.android.service.network.ResponseStatus
import za.co.woolworths.financial.services.android.util.Utils


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
                val bundle = Bundle()
                bundle.putBoolean("addNewAddress", true)
                navController?.navigate(
                    R.id.action_checkoutAddressConfirmationFragment_to_CheckoutAddAddressNewUserFragment,
                    bundleOf("bundle" to bundle)
                )
            }
            R.id.btnAddressConfirmation -> {
                if (checkoutAddressConfirmationListAdapter?.checkedItemPosition == -1)
                    addNewAddressErrorMsg.visibility = View.VISIBLE
            }
        }
    }

    private fun addFragmentResultListener() {
        // Use the Kotlin extension in the fragment-ktx artifact
        setFragmentResultListener(UPDATE_SAVED_ADDRESS_REQUEST_KEY) { requestKey, bundle ->
            updateSavedAddress(bundle)
            checkoutAddressConfirmationListAdapter?.notifyDataSetChanged()
        }
        setFragmentResultListener(DELETE_SAVED_ADDRESS_REQUEST_KEY){ requestKey, bundle ->
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
        checkoutAddressConfirmationListAdapter =
            CheckoutAddressConfirmationListAdapter(savedAddress, navController, this)
        saveAddressRecyclerView?.apply {
            addItemDecoration(object : ItemDecoration() {})
            layoutManager = activity?.let { LinearLayoutManager(it) }
            checkoutAddressConfirmationListAdapter?.let { adapter = it }
        }
        deliveryTab.setOnClickListener(this)
        collectionTab.setOnClickListener(this)
        plusImgAddAddress.setOnClickListener(this)
        addNewAddressTextView.setOnClickListener(this)
        btnAddressConfirmation.setOnClickListener(this)
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

    override fun hideErrorView() {
        addNewAddressErrorMsg.visibility = View.GONE
    }

    override fun changeAddress(nickName: String) {
        checkoutAddAddressNewUserViewModel.changeAddress(nickName).observe(viewLifecycleOwner, {
            when (it.responseStatus) {
                ResponseStatus.SUCCESS -> {
                    if (it?.data != null && it?.data.deliverable) {

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