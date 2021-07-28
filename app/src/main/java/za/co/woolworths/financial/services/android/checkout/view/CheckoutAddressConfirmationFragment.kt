package za.co.woolworths.financial.services.android.checkout.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.awfs.coordination.R
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.android.synthetic.main.checkout_address_confirmation.*
import kotlinx.android.synthetic.main.checkout_address_confirmation_click_and_collect.*
import kotlinx.android.synthetic.main.checkout_address_confirmation_delivery.*
import za.co.woolworths.financial.services.android.checkout.interactor.CheckoutAddAddressNewUserInteractor
import za.co.woolworths.financial.services.android.checkout.service.network.*
import za.co.woolworths.financial.services.android.checkout.view.adapter.CheckoutAddressConfirmationListAdapter
import za.co.woolworths.financial.services.android.checkout.view.adapter.CheckoutStoreSelectionAdapter
import za.co.woolworths.financial.services.android.checkout.viewmodel.CheckoutAddAddressNewUserViewModel
import za.co.woolworths.financial.services.android.checkout.viewmodel.ViewModelFactory
import za.co.woolworths.financial.services.android.models.ValidateSelectedSuburbResponse
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.Province
import za.co.woolworths.financial.services.android.models.dto.Suburb
import za.co.woolworths.financial.services.android.models.dto.UnSellableCommerceItem
import za.co.woolworths.financial.services.android.models.dto.ValidatedSuburbProducts
import za.co.woolworths.financial.services.android.service.network.ResponseStatus
import za.co.woolworths.financial.services.android.ui.activities.click_and_collect.EditDeliveryLocationActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.click_and_collect.EditDeliveryLocationFragment
import za.co.woolworths.financial.services.android.ui.fragments.click_and_collect.ProvinceSelectorFragment.Companion.CHECKOUT_CHANGE_LOCATION_KEY
import za.co.woolworths.financial.services.android.util.DeliveryType
import za.co.woolworths.financial.services.android.util.Utils


/**
 * Created by Kunal Uttarwar on 16/06/21.
 */
class CheckoutAddressConfirmationFragment : Fragment(), View.OnClickListener,
    CheckoutAddressConfirmationListAdapter.EventListner {

    var savedAddress: SavedAddressResponse? = null
    var checkoutAddressConfirmationListAdapter: CheckoutAddressConfirmationListAdapter? = null
    private var storeListAdapter: CheckoutStoreSelectionAdapter? = null
    private lateinit var checkoutAddAddressNewUserViewModel: CheckoutAddAddressNewUserViewModel
    private var navController: NavController? = null
    private var localSuburbId: String? = null
    private var validatedSuburbProductResponse: ValidatedSuburbProducts? = null

    companion object {
        const val UPDATE_SAVED_ADDRESS_REQUEST_KEY = "updateSavedAddress"
        const val DELETE_SAVED_ADDRESS_REQUEST_KEY = "deleteSavedAddress"
        const val ADD_A_NEW_ADDRESS_REQUEST_KEY = "addNewAddress"
        const val ADD_NEW_ADDRESS_KEY = "addNewAddress"
        const val SAVED_ADDRESS_KEY = "savedAddress"
        const val SAVED_ADDRESS_RESPONSE_KEY = "savedAddressResponse"
        const val UNSELLABLE_CHANGE_STORE_REQUEST_KEY = "unsellableChangeStore"
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

        updateSavedAddress(arguments)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.deliveryTab -> {
                showDeliveryTab()
            }
            R.id.collectionTab -> {
                showCollectionTab(localSuburbId)
            }
            R.id.plusImgAddAddress, R.id.addNewAddressTextView -> {
                navigateToAddAddress()
            }
            R.id.btnAddressConfirmation -> {
                if (btnAddressConfirmation.text.equals(getString(R.string.change_location))) {
                    changeLocation()
                } else {
                    if (savedAddress?.addresses == null || savedAddress?.addresses?.size == 0) {
                        navigateToAddAddress()
                    } else if (checkoutAddressConfirmationListAdapter?.checkedItemPosition == -1 && addressConfirmationDelivery.visibility == View.VISIBLE)
                        addNewAddressErrorMsg.visibility = View.VISIBLE
                    else {
                        //TO DO next screen
                    }
                }
            }
            R.id.changeTextView -> {
                changeLocation()
            }
        }
    }

    private fun changeLocation() {
        val bundle = Bundle()
        bundle.apply {
            putString(
                "ProvinceList",
                Utils.toJson(WoolworthsApplication.getNativeCheckout()?.regions)
            )
            putBoolean(CHECKOUT_CHANGE_LOCATION_KEY, true)
        }
        navController?.navigate(
            R.id.action_provinceSelectorFragment,
            bundleOf("bundle" to bundle)
        )
    }

    private fun showCollectionTab(suburbId: String?) {
        collectionTab.setBackgroundResource(R.drawable.delivery_round_btn_white)
        deliveryTab.setBackgroundResource(R.drawable.rounded_view_grey_tab_bg)
        addressConfirmationDelivery.visibility = View.GONE
        addressConfirmationClicknCollect.visibility = View.VISIBLE
        showStoreListView(suburbId)
    }

    private fun showDeliveryTab() {
        deliveryTab.setBackgroundResource(R.drawable.delivery_round_btn_white)
        collectionTab.setBackgroundResource(R.drawable.rounded_view_grey_tab_bg)
        addressConfirmationDelivery.visibility = View.VISIBLE
        addressConfirmationClicknCollect.visibility = View.GONE
        earliestDateTitleLayout.visibility = View.GONE
    }

    private fun navigateToAddAddress() {
        val bundle = Bundle()
        bundle.putBoolean(ADD_NEW_ADDRESS_KEY, true)
        bundle.putString(SAVED_ADDRESS_RESPONSE_KEY, Utils.toJson(savedAddress))
        navController?.navigate(
            R.id.action_checkoutAddressConfirmationFragment_to_CheckoutAddAddressNewUserFragment,
            bundleOf("bundle" to bundle)
        )
    }

    private fun addFragmentResultListener() {
        // Use the Kotlin extension in the fragment-ktx artifact
        setFragmentResultListener(UPDATE_SAVED_ADDRESS_REQUEST_KEY) { requestKey, bundle ->
            updateSavedAddress(bundle)
        }
        setFragmentResultListener(DELETE_SAVED_ADDRESS_REQUEST_KEY) { requestKey, bundle ->
            updateSavedAddress(bundle)
        }
        setFragmentResultListener(ADD_A_NEW_ADDRESS_REQUEST_KEY) { requestKey, bundle ->
            updateSavedAddress(bundle)
        }
        setFragmentResultListener(UNSELLABLE_CHANGE_STORE_REQUEST_KEY){ requestKey, bundle ->
            showCollectionTab(localSuburbId)
        }
        setFragmentResultListener(EditDeliveryLocationFragment.SUBURB_SELECTOR_REQUEST_CODE) { requestKey, bundle ->
            val result = bundle.getString("Suburb")
            val suburb: Suburb? = Utils.strToJson(result, Suburb::class.java) as? Suburb
            suburb?.id?.let { showCollectionTab(it) }
        }

        setFragmentResultListener(CheckoutAddAddressNewUserFragment.PROVINCE_SELECTION_BACK_PRESSED) { requestKey, bundle ->
            showCollectionTab(localSuburbId)
        }
        setFragmentResultListener(CheckoutAddAddressNewUserFragment.SUBURB_SELECTION_BACK_PRESSED) { requestKey, bundle ->
            showCollectionTab(localSuburbId)
        }
    }

    private fun updateSavedAddress(bundle: Bundle?) {
        bundle?.apply {
            if (containsKey(SAVED_ADDRESS_KEY)) {
                savedAddress = getSerializable(SAVED_ADDRESS_KEY) as? SavedAddressResponse
            }
        }
        checkoutAddressConfirmationListAdapter?.setData(savedAddress)
        checkoutAddressConfirmationListAdapter?.notifyDataSetChanged()
    }

    private fun initView() {
        if (savedAddress?.addresses == null || savedAddress?.addresses?.size == 0) {
            //Show No Address view
            hideAddressListView()
        } else {
            // Show Delivery View
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
        changeTextView.setOnClickListener(this)

        storeInputValue?.apply {
            addTextChangedListener {
                storeListAdapter?.filter?.filter(it.toString())
            }
        }
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

    private fun showStoreListView(suburbId: String?) {
        var selectedSuburbId = suburbId
        if (selectedSuburbId.isNullOrEmpty())
            selectedSuburbId = Utils.getPreferredDeliveryLocation().suburb?.id.toString()
        if (!localSuburbId.equals(selectedSuburbId)) { //equals means only tab change happens. No suburb changed.
            localSuburbId = selectedSuburbId
            storesFoundTitle.text = resources.getQuantityString(R.plurals.stores_near_me, 0, 0)
            localSuburbId?.let { it ->
                checkoutAddAddressNewUserViewModel.validateSelectedSuburb(it, false)
                    .observe(viewLifecycleOwner, {
                        when (it.responseStatus) {
                            ResponseStatus.SUCCESS -> {
                                loadingProgressBar.visibility = View.GONE
                                changeTextView.visibility = View.VISIBLE
                                btnAddressConfirmation.text = getString(R.string.confirm)
                                if (it?.data != null) {
                                    validatedSuburbProductResponse =
                                        (it.data as? ValidateSelectedSuburbResponse)?.validatedSuburbProducts
                                    /*val jsonFileString = Utils.getJsonDataFromAsset(
                                        activity?.applicationContext,
                                        "mocks/validateSuburbWithUnsellable.json"
                                    )
                                    var mockAddressResponse: ValidatedSuburbProducts = Gson().fromJson(
                                        jsonFileString,
                                        object : TypeToken<ValidatedSuburbProducts>() {}.type
                                    )
                                    validatedSuburbProductResponse= mockAddressResponse*/
                                    if (validatedSuburbProductResponse != null) {
                                        if (validatedSuburbProductResponse?.unSellableCommerceItems?.size!! > 0) {
                                            val address = Address()
                                            address.suburbId = localSuburbId
                                            navigateToUnsellableItemsFragment(
                                                validatedSuburbProductResponse?.unSellableCommerceItems!!,
                                                address,
                                                validatedSuburbProductResponse?.unDeliverableProducts == false
                                            )
                                        } else
                                            showStoreList()
                                    }
                                }
                            }
                            ResponseStatus.LOADING -> {
                                loadingProgressBar.visibility = View.VISIBLE
                                changeTextView.visibility = View.GONE
                                btnAddressConfirmation.text = getString(R.string.change_location)
                            }
                            ResponseStatus.ERROR -> {
                                loadingProgressBar.visibility = View.GONE
                                changeTextView.visibility = View.VISIBLE
                                btnAddressConfirmation.text = getString(R.string.confirm)
                            }
                        }
                    })
            }
        } else if (localSuburbId != null && validatedSuburbProductResponse != null) {
            showStoreList()
        }
    }

    private fun showStoreList() {
        if (!validatedSuburbProductResponse?.stores.isNullOrEmpty()) {
            searchLayout.visibility = View.VISIBLE
            changeTextView.visibility = View.VISIBLE
            btnAddressConfirmation.text = getString(R.string.confirm)
        } else {
            changeTextView.visibility = View.GONE
            btnAddressConfirmation.text = getString(R.string.change_location)
        }
        earliestDateValue?.text =
            validatedSuburbProductResponse?.firstAvailableFoodDeliveryDate ?: ""
        if (!earliestDateValue?.text.isNullOrEmpty()) {
            earliestDateTitleLayout.visibility = View.VISIBLE
        }
        rcvStoreRecyclerView?.apply {
            storeListAdapter =
                validatedSuburbProductResponse?.stores?.let { it1 ->
                    CheckoutStoreSelectionAdapter(it1)
                }
            val storesCount = (validatedSuburbProductResponse?.stores?.size ?: 0)
            storesFoundTitle.text =
                resources.getQuantityString(R.plurals.stores_near_me, storesCount, storesCount)
            layoutManager = activity?.let { LinearLayoutManager(it) }
            storeListAdapter?.let { adapter = it }
        }
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
        unSellableCommerceItems: List<UnSellableCommerceItem>,
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

                        /*val jsonFileString = Utils.getJsonDataFromAsset(
                            activity?.applicationContext,
                            "mocks/unsellableItems.json"
                        )
                        var mockChangeAddressResponse: ChangeAddressResponse = Gson().fromJson(
                            jsonFileString,
                            object : TypeToken<ChangeAddressResponse>() {}.type
                        )*/


                        val changeAddressResponse = it?.data as? ChangeAddressResponse
                        if (changeAddressResponse != null && changeAddressResponse.deliverable) {
                            if (changeAddressResponse.unSellableCommerceItems?.size!! > 0) {
                                navigateToUnsellableItemsFragment(
                                    changeAddressResponse.unSellableCommerceItems,
                                    address,
                                    changeAddressResponse.deliverable
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