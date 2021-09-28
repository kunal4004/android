package za.co.woolworths.financial.services.android.checkout.view

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.add_to_list_content.*
import kotlinx.android.synthetic.main.checkout_address_confirmation.*
import kotlinx.android.synthetic.main.checkout_address_confirmation_click_and_collect.*
import kotlinx.android.synthetic.main.checkout_address_confirmation_delivery.*
import kotlinx.android.synthetic.main.checkout_new_user_address_details.*
import kotlinx.android.synthetic.main.suburb_selector_fragment.*
import za.co.woolworths.financial.services.android.checkout.interactor.CheckoutAddAddressNewUserInteractor
import za.co.woolworths.financial.services.android.checkout.interactor.CheckoutAddressConfirmationInteractor
import za.co.woolworths.financial.services.android.checkout.service.network.*
import za.co.woolworths.financial.services.android.checkout.view.adapter.CheckoutAddressConfirmationListAdapter
import za.co.woolworths.financial.services.android.checkout.view.adapter.CheckoutStoreSelectionAdapter
import za.co.woolworths.financial.services.android.checkout.viewmodel.CheckoutAddAddressNewUserViewModel
import za.co.woolworths.financial.services.android.checkout.viewmodel.CheckoutAddressConfirmationViewModel
import za.co.woolworths.financial.services.android.checkout.viewmodel.ViewModelFactory
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.ValidateSelectedSuburbResponse
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.service.network.ResponseStatus
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity
import za.co.woolworths.financial.services.android.ui.activities.click_and_collect.EditDeliveryLocationActivity
import za.co.woolworths.financial.services.android.ui.adapters.SuburbListAdapter
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.setDivider
import za.co.woolworths.financial.services.android.ui.fragments.click_and_collect.EditDeliveryLocationFragment
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.DeliveryType
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import java.net.HttpURLConnection


/**
 * Created by Kunal Uttarwar on 16/06/21.
 */
class CheckoutAddressConfirmationFragment : CheckoutAddressManagementBaseFragment(),
    View.OnClickListener,
    CheckoutAddressConfirmationListAdapter.EventListner, SuburbListAdapter.ISuburbSelector {

    private var savedAddress: SavedAddressResponse? = null
    private var selectedAddress: Address? = null
    private var checkoutAddressConfirmationListAdapter: CheckoutAddressConfirmationListAdapter? =
        null
    private var storeListAdapter: CheckoutStoreSelectionAdapter? = null
    private lateinit var checkoutAddAddressNewUserViewModel: CheckoutAddAddressNewUserViewModel
    private var navController: NavController? = null
    private var localSuburbId: String = DEFAULT_STORE_ID
    private var validatedSuburbProductResponse: ValidatedSuburbProducts? = null
    private var suburbListAdapter: SuburbListAdapter? = null
    private lateinit var checkoutAddressConfirmationViewModel: CheckoutAddressConfirmationViewModel
    private var selectedSuburb = Suburb()
    private var selectedProvince = Province()
    private var isDeliverySelected: Boolean = true
    private var validateStoreList: ValidateStoreList? = null

    companion object {
        const val UPDATE_SAVED_ADDRESS_REQUEST_KEY = "updateSavedAddress"
        const val DELETE_SAVED_ADDRESS_REQUEST_KEY = "deleteSavedAddress"
        const val ADD_A_NEW_ADDRESS_REQUEST_KEY = "addNewAddress"
        const val ADD_NEW_ADDRESS_KEY = "addNewAddress"
        const val SAVED_ADDRESS_KEY = "savedAddress"
        const val UNSELLABLE_CHANGE_STORE_REQUEST_KEY = "unsellableChangeStore"
        const val STORE_SELECTION_REQUEST_KEY = "storeSelectionResponse"
        const val DEFAULT_STORE_ID = "-1"
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
        updateSavedAddress(baseFragBundle)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.deliveryTab -> {
                if (loadingProgressBar.visibility == View.GONE) {
                    Utils.triggerFireBaseEvents(
                        FirebaseManagerAnalyticsProperties.CHANGE_FULFILLMENT_DELIVERY,
                        activity
                    )
                    showDeliveryTab()
                    showDeliveryAddressListView()
                    initialiseDeliveryAddressRecyclerView()
                }
            }
            R.id.collectionTab -> {
                if (loadingProgressBar.visibility == View.GONE) {
                    Utils.triggerFireBaseEvents(
                        FirebaseManagerAnalyticsProperties.CHANGE_FULFILLMENT_COLLECTION,
                        activity
                    )
                    showCollectionTab(localSuburbId)
                }
            }
            R.id.plusImgAddAddress, R.id.addNewAddressTextView -> {
                Utils.triggerFireBaseEvents(
                    FirebaseManagerAnalyticsProperties.CHANGE_FULFILLMENT_ADD_NEW_ADDRESS,
                    activity
                )
                navigateToAddAddress()
            }
            R.id.btnAddressConfirmation -> {
                if (loadingProgressBar.visibility == View.GONE) {
                    if (isDeliverySelected) {
                        if (checkoutAddressConfirmationListAdapter?.checkedItemPosition == -1)
                            addNewAddressErrorMsg.visibility = View.VISIBLE
                        else {
                            Utils.triggerFireBaseEvents(
                                FirebaseManagerAnalyticsProperties.CHANGE_FULFILLMENT_DELIVERY_CONFIRM_BTN,
                                activity
                            )
                            callChangeAddressApi()
                        }
                    } else {
                        // This is when user clicks on collection journey.
                        if (btnAddressConfirmation.text.equals(getString(R.string.change_suburb))) {
                            //Zero stores and user clicks on change suburb.
                            getSuburb(selectedProvince)
                        } else if (selectedSuburb.storeAddress != null) {
                            Utils.triggerFireBaseEvents(
                                FirebaseManagerAnalyticsProperties.CHECKOUT_CONFIRM_NEW_STORE,
                                activity
                            )
                            checkUnsellableItems()
                        }
                    }
                }
            }
            R.id.changeProvinceTextView -> {
                if (loadingProgressBar.visibility == View.GONE) {
                    changeLocation()
                }
            }
            R.id.changeTextView -> {
                if (loadingProgressBar.visibility == View.GONE) {
                    changeLocation()
                }
            }
        }
    }

    private fun checkUnsellableItems() {
        if (validateStoreList != null && validateStoreList?.deliverable == true) {
            if (validateStoreList?.unSellableCommerceItems?.size!! > 0) {
                navigateToUnsellableItemsFragment(
                    validateStoreList?.unSellableCommerceItems as List<UnSellableCommerceItem>,
                    selectedAddress!!,
                    validateStoreList?.deliverable!!,
                    DeliveryType.STORE_PICKUP
                )
            } else {
                // if it is store then call setSuburb API.
                setSuburb()
            }
        }
    }

    private fun setSuburb() {
        selectedSuburb.storeAddress.suburbId?.let { storeId ->
            loadingProgressBar.visibility = View.VISIBLE
            checkoutAddressConfirmationViewModel.setSuburb(storeId)
                .observe(viewLifecycleOwner, { response ->
                    loadingProgressBar.visibility = View.GONE
                    when (response) {
                        is SetDeliveryLocationSuburbResponse -> {
                            when (response.httpCode) {
                                HttpURLConnection.HTTP_OK, AppConstant.HTTP_OK_201 -> {
                                    val store = selectedSuburb.let { suburb ->
                                        Store(
                                            suburb.storeAddress.suburbId,
                                            suburb.storeAddress.suburb,
                                            suburb.fulfillmentStores,
                                            suburb.storeAddress.address1
                                        )
                                    }
                                    val shoppingDeliveryLocation = ShoppingDeliveryLocation(
                                        selectedProvince,
                                        null,
                                        store
                                    )
                                    shoppingDeliveryLocation.storePickup = true
                                    Utils.savePreferredDeliveryLocation(
                                        shoppingDeliveryLocation
                                    )
                                    if (!isDeliverySelected) {
                                        //TODO Add web breakout implementation here instead of slot selection.
                                        navController?.navigate(
                                            R.id.action_checkoutAddressConfirmationFragment_to_CheckoutAddAddressReturningUserFragment,
                                            baseFragBundle
                                        )
                                    }
                                }
                                else -> {
                                    showErrorScreen(
                                        ErrorHandlerActivity.COMMON_WITH_BACK_BUTTON,
                                        getString(R.string.common_error_message_without_contact_info)
                                    )
                                }
                            }
                        }
                        is Throwable -> {
                            showErrorScreen(
                                ErrorHandlerActivity.COMMON_WITH_BACK_BUTTON,
                                getString(R.string.common_error_message_without_contact_info)
                            )
                        }
                    }
                })
        }
    }

    private fun changeLocation() {
        Utils.triggerFireBaseEvents(
            FirebaseManagerAnalyticsProperties.CHECKOUT_COLECTION_CHANGE_BTN,
            activity
        )
        val bundle = Bundle()
        bundle.apply {
            putString(
                "ProvinceList",
                Utils.toJson(WoolworthsApplication.getNativeCheckout()?.regions)
            )
        }
        navController?.navigate(
            R.id.action_provinceSelectorFragment,
            bundleOf("bundle" to bundle)
        )
    }

    private fun showCollectionTab(suburbId: String?) {
        isDeliverySelected = false
        collectionTab.setBackgroundResource(R.drawable.delivery_round_btn_white)
        deliveryTab.setBackgroundResource(R.drawable.rounded_view_grey_tab_bg)
        addressConfirmationDelivery.visibility = View.GONE
        suburbSelectionLayout.visibility = View.GONE
        if (selectedSuburb.storeAddress == null) {
            removeMarginToStoreListView()
        } else
            setMarginToStoreListView()
        fetchStoreListFromValidateSelectedSuburb(suburbId)
        if (!earliestDateValue?.text.isNullOrEmpty()) {
            earliestDateTitleLayout.visibility = View.VISIBLE
        }
    }

    private fun showDeliveryTab() {
        isDeliverySelected = true
        deliveryTab.setBackgroundResource(R.drawable.delivery_round_btn_white)
        collectionTab.setBackgroundResource(R.drawable.rounded_view_grey_tab_bg)
        addressConfirmationDelivery.visibility = View.VISIBLE
        btnConfirmLayout.visibility = View.VISIBLE
        suburbSelectionLayout.visibility = View.GONE
        addressConfirmationClicknCollect.visibility = View.GONE
        earliestDateTitleLayout.visibility = View.GONE
    }

    private fun navigateToAddAddress() {
        val bundle = Bundle()
        bundle.putBoolean(ADD_NEW_ADDRESS_KEY, true)
        bundle.putString(SAVED_ADDRESS_KEY, Utils.toJson(savedAddress))
        baseFragBundle?.putString(SAVED_ADDRESS_KEY, Utils.toJson(savedAddress))
        navController?.navigate(
            R.id.action_checkoutAddressConfirmationFragment_to_CheckoutAddAddressNewUserFragment,
            bundle
        )
    }

    private fun addFragmentResultListener() {
        // Use the Kotlin extension in the fragment-ktx artifact
        setFragmentResultListener(UPDATE_SAVED_ADDRESS_REQUEST_KEY) { _, bundle ->
            updateSavedAddress(bundle)
            initialiseDeliveryAddressRecyclerView()
        }
        setFragmentResultListener(DELETE_SAVED_ADDRESS_REQUEST_KEY) { _, bundle ->
            updateSavedAddress(bundle)
            initialiseDeliveryAddressRecyclerView()
        }
        setFragmentResultListener(ADD_A_NEW_ADDRESS_REQUEST_KEY) { _, bundle ->
            updateSavedAddress(bundle)
            initialiseDeliveryAddressRecyclerView()
        }
        setFragmentResultListener(UNSELLABLE_CHANGE_STORE_REQUEST_KEY) { _, _ ->
            Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.CHECKOUT_REMOVE_UNSELLABLE_ITEMS,
                activity
            )
            if (isDeliverySelected) {
                view?.findNavController()?.navigate(
                    R.id.action_checkoutAddressConfirmationFragment_to_CheckoutAddAddressReturningUserFragment,
                    baseFragBundle
                )
            } else {
                showCollectionTab(localSuburbId)
            }
        }

        setFragmentResultListener(EditDeliveryLocationFragment.SUBURB_SELECTOR_REQUEST_CODE) { _, bundle ->
            val suburb = bundle.getString("Suburb")
            selectedSuburb = Utils.strToJson(suburb, Suburb::class.java) as Suburb
            selectedSuburb.id?.let {
                storeListAdapter =
                    null // setting null to update selected store position in list to -1
                showCollectionTab(it)
            }
        }
        setFragmentResultListener(EditDeliveryLocationFragment.PROVINCE_SELECTOR_REQUEST_CODE) { _, bundle ->
            val province = bundle.getString("Province")
            selectedProvince = Utils.strToJson(province, Province::class.java) as Province
            showCollectionTab(DEFAULT_STORE_ID) // To show suburb selection list
        }
        setFragmentResultListener(STORE_SELECTION_REQUEST_KEY) { _, bundle ->
            val result = bundle.getString(STORE_SELECTION_REQUEST_KEY)
            val validateStoreList: ValidateStoreList? =
                Utils.strToJson(result, ValidateStoreList::class.java) as? ValidateStoreList

            if (selectedAddress == null)
                selectedAddress = Address()
            selectedAddress?.apply {
                suburbId = selectedSuburb.id
                suburb = selectedSuburb.name
                postalCode = selectedSuburb.postalCode
                region = selectedProvince.id
            }

            this.validateStoreList = validateStoreList
            val storeAddress = StoreAddress(
                validateStoreList?.storeAddress,
                "",
                selectedProvince.name,
                "",
                selectedSuburb.postalCode,
                validateStoreList?.storeName,
                validateStoreList?.storeId,
                selectedProvince.name
            )

            selectedSuburb.storeAddress = storeAddress
            setMarginToStoreListView()
            setEarliestDeliveryDates(validateStoreList)
        }
    }

    private fun updateSavedAddress(bundle: Bundle?) {
        bundle?.apply {
            if (containsKey(SAVED_ADDRESS_KEY)) {
                savedAddress = Utils.jsonStringToObject(
                    getString(SAVED_ADDRESS_KEY),
                    SavedAddressResponse::class.java
                ) as? SavedAddressResponse
                    ?: getSerializable(SAVED_ADDRESS_KEY) as? SavedAddressResponse
                baseFragBundle?.putString(SAVED_ADDRESS_KEY, Utils.toJson(savedAddress))
            }
        }
    }

    private fun initView() {
        if (isDeliverySelected) {
            selectedProvince = Utils.getPreferredDeliveryLocation().province
            if (savedAddress?.addresses?.isNullOrEmpty() == true) {
                //Show No Address view
                hideDeliveryAddressListView()
            } else {
                // Show Delivery View
                showDeliveryAddressListView()
                initialiseDeliveryAddressRecyclerView()
            }
        } else {
            showCollectionTab(localSuburbId)
        }
        deliveryTab.setOnClickListener(this)
        collectionTab.setOnClickListener(this)
        plusImgAddAddress.setOnClickListener(this)
        addNewAddressTextView.setOnClickListener(this)
        btnAddressConfirmation.setOnClickListener(this)
        changeProvinceTextView.setOnClickListener(this)
        changeTextView.setOnClickListener(this)

        storeInputValue?.apply {
            addTextChangedListener {
                storeListAdapter?.filter?.filter(it.toString())
            }
        }
    }

    private fun initialiseDeliveryAddressRecyclerView() {
        setRecyclerViewMaximumHeight(
            saveAddressRecyclerView.layoutParams,
            savedAddress?.addresses?.size ?: 0
        )
        checkoutAddressConfirmationListAdapter = null
        checkoutAddressConfirmationListAdapter =
            CheckoutAddressConfirmationListAdapter(savedAddress, navController, this, activity)
        saveAddressRecyclerView?.apply {
            addItemDecoration(object : ItemDecoration() {})
            layoutManager = activity?.let { LinearLayoutManager(it) }
            checkoutAddressConfirmationListAdapter?.let { adapter = it }
        }
    }

    private fun setRecyclerViewMaximumHeight(viewGroupParams: ViewGroup.LayoutParams, size: Int) {
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val rowHeight = 65
        val totalRemovalHeight = 370
        val recyclerViewSpace =
            displayMetrics.heightPixels - KotlinUtils.dpToPxConverter(totalRemovalHeight)
        val totalRowHeight = KotlinUtils.dpToPxConverter(rowHeight * size)
        if (totalRowHeight <= recyclerViewSpace)
            viewGroupParams.height = totalRowHeight
        else
            viewGroupParams.height = recyclerViewSpace
        saveAddressRecyclerView.layoutParams = viewGroupParams
    }

    private fun hideDeliveryAddressListView() {
        btnAddressConfirmation.text = bindString(R.string.add_address)
        whereWeDeliveringTitle.text = bindString(R.string.no_saved_addresses)
        saveAddressRecyclerView.visibility = View.GONE
        addressListPartition.visibility = View.GONE
        plusImgAddAddress.visibility = View.GONE
        confirmAddressPartition.visibility = View.GONE
        addNewAddressTextView.visibility = View.GONE
    }

    private fun showDeliveryAddressListView() {
        saveAddressRecyclerView.visibility = View.VISIBLE
        addressListPartition.visibility = View.VISIBLE
        plusImgAddAddress.visibility = View.VISIBLE
        addNewAddressTextView.visibility = View.VISIBLE
        confirmAddressPartition.visibility = View.VISIBLE
        btnAddressConfirmation.text = bindString(R.string.confirm)
        whereWeDeliveringTitle.text = bindString(R.string.where_should_we_deliver)
    }

    private fun fetchStoreListFromValidateSelectedSuburb(suburbId: String?) {
        if (suburbId.equals(DEFAULT_STORE_ID)) {
            // This means collection tab clicked for the first time.
            getSuburb(selectedProvince)
        } else if (suburbId.isNullOrEmpty()) {
            clickNCollectTitleLayout.visibility = View.VISIBLE
            addressConfirmationClicknCollect.visibility = View.VISIBLE
            showStoreList()
        } else if (!localSuburbId.equals(suburbId)) { //equals means only tab change happens. No suburb changed.
            localSuburbId = suburbId
            localSuburbId.let { it ->
                checkoutAddAddressNewUserViewModel.validateSelectedSuburb(it, false)
                    .observe(viewLifecycleOwner, {
                        when (it.responseStatus) {
                            ResponseStatus.SUCCESS -> {
                                loadingProgressBar.visibility = View.GONE
                                clickNCollectTitleLayout.visibility = View.VISIBLE
                                addressConfirmationClicknCollect.visibility = View.VISIBLE
                                if (it?.data != null) {
                                    validatedSuburbProductResponse =
                                        (it.data as? ValidateSelectedSuburbResponse)?.validatedSuburbProducts
                                    if (validatedSuburbProductResponse != null) {
                                        if (validatedSuburbProductResponse?.stores?.isNotEmpty() == true) {
                                            changeTextView.visibility = View.VISIBLE
                                            changeProvinceTextView.visibility = View.GONE
                                            btnAddressConfirmation.text =
                                                getString(R.string.confirm)
                                        }
                                        if (validatedSuburbProductResponse?.unSellableCommerceItems?.size!! > 0) {
                                            val address = Address()
                                            address.suburbId = localSuburbId
                                            navigateToUnsellableItemsFragment(
                                                validatedSuburbProductResponse?.unSellableCommerceItems as List<UnSellableCommerceItem>,
                                                address,
                                                validatedSuburbProductResponse?.unDeliverableProducts == false,
                                                DeliveryType.STORE_PICKUP
                                            )
                                        } else
                                            showStoreList()
                                    }
                                }
                            }
                            ResponseStatus.LOADING -> {
                                loadingProgressBar.visibility = View.VISIBLE
                                changeTextView.visibility = View.GONE
                                btnAddressConfirmation.text = getString(R.string.change_suburb)
                                changeProvinceTextView.visibility = View.VISIBLE
                                storesFoundTitle.visibility = View.GONE
                            }
                            ResponseStatus.ERROR -> {
                                loadingProgressBar.visibility = View.GONE
                                changeTextView.visibility = View.VISIBLE
                                btnAddressConfirmation.text = getString(R.string.change_suburb)
                            }
                        }
                    })
            }
        } else if (localSuburbId != null && validatedSuburbProductResponse != null) {
            clickNCollectTitleLayout.visibility = View.VISIBLE
            addressConfirmationClicknCollect.visibility = View.VISIBLE
            showStoreList()
        }
    }

    private fun showSuburbSelectionView(suburbList: MutableList<Suburb>) {
        removeMarginToStoreListView()
        suburbSelectionLayout.visibility = View.VISIBLE
        suburbSelectionTitle.visibility = View.VISIBLE
        suburbSelectionSubTitle.visibility = View.VISIBLE
        suburbInputValue.setHint(R.string.hint_search_for_your_suburb)
        suburbInputValue?.apply {
            addTextChangedListener {
                suburbListAdapter?.filter?.filter(it.toString())
            }
        }
        rcvSuburbList?.apply {
            suburbListAdapter = SuburbListAdapter(
                suburbList as ArrayList<Suburb>,
                this@CheckoutAddressConfirmationFragment,
                DeliveryType.DELIVERY
            )
            setDivider(R.drawable.recycler_view_divider_gray_1dp)
            layoutManager = activity?.let { LinearLayoutManager(it) }
            suburbListAdapter?.let { adapter = it }
        }
    }

    private fun getSuburb(province: Province?) {
        clickNCollectTitleLayout.visibility = View.GONE
        addressConfirmationClicknCollect.visibility = View.GONE
        province?.id?.let {
            with(checkoutAddAddressNewUserViewModel) {
                initGetSuburbs(it).observe(viewLifecycleOwner, {
                    when (it.responseStatus) {
                        ResponseStatus.SUCCESS -> {
                            loadingProgressBar.visibility = View.GONE
                            if ((it?.data as? SuburbsResponse)?.suburbs.isNullOrEmpty()) {
                                //showNoStoresError()
                            } else {
                                (it?.data as? SuburbsResponse)?.suburbs?.let { it1 ->
                                    showSuburbSelectionView(it1)
                                    /*val bundle = Bundle()
                                    bundle.apply {
                                        putString("SuburbList", Utils.toJson(it1))
                                        putSerializable("deliveryType", DeliveryType.DELIVERY)
                                    }
                                    navController?.navigate(
                                        R.id.action_getSuburb_suburbSelectorFragment,
                                        bundleOf("bundle" to bundle)
                                    )*/
                                }
                            }
                        }
                        ResponseStatus.LOADING -> {
                            loadingProgressBar.visibility = View.VISIBLE
                            removeMarginToStoreListView()
                        }
                        ResponseStatus.ERROR -> {
                            loadingProgressBar.visibility = View.GONE
                        }
                    }
                })
            }
        }
    }

    private fun setMarginToStoreListView() {
        btnConfirmLayout.visibility = View.VISIBLE
        val param = addressConfirmationClicknCollect.layoutParams as ViewGroup.MarginLayoutParams
        param.setMargins(0, 192, 0, 104)
        addressConfirmationClicknCollect.layoutParams = param
    }

    private fun removeMarginToStoreListView() {
        btnConfirmLayout.visibility = View.GONE
        val param = addressConfirmationClicknCollect.layoutParams as ViewGroup.MarginLayoutParams
        param.setMargins(0, 192, 0, 10)
        addressConfirmationClicknCollect.layoutParams = param
    }

    private fun showStoreList() {
        if (!validatedSuburbProductResponse?.stores.isNullOrEmpty()) {
            searchLayout.visibility = View.VISIBLE
            storeInputValue.text.clear()
            changeTextView.visibility = View.VISIBLE
            changeProvinceTextView.visibility = View.GONE
            btnAddressConfirmation.text = getString(R.string.confirm)
        } else {
            changeTextView.visibility = View.GONE
            changeProvinceTextView.visibility = View.VISIBLE
            btnAddressConfirmation.text = getString(R.string.change_suburb)
        }
        setEarliestDeliveryDates(validateStoreList)
        storeListAdapter =
            validatedSuburbProductResponse?.stores?.let { it1 ->
                CheckoutStoreSelectionAdapter(
                    it1,
                    this,
                    storeListAdapter?.checkedItemPosition ?: -1
                )
            }
        rcvStoreRecyclerView?.apply {
            val storesCount = (validatedSuburbProductResponse?.stores?.size ?: 0)
            if (storesCount == 0) {
                setMarginToStoreListView()
                changeTextView.visibility = View.GONE
                btnAddressConfirmation.text = getString(R.string.change_suburb)
                changeProvinceTextView.visibility = View.VISIBLE
            } else
                changeProvinceTextView.visibility = View.GONE
            storesFoundTitle.visibility = View.VISIBLE
            storesFoundTitle.text =
                resources.getQuantityString(R.plurals.stores_near_me, storesCount, storesCount)
            layoutManager = activity?.let { LinearLayoutManager(it) }
            storeListAdapter?.let { adapter = it }
        }
    }

    private fun setEarliestDeliveryDates(validateStoreList: ValidateStoreList?) {
        earliestDateValue?.text =
            validateStoreList?.firstAvailableFoodDeliveryDate ?: ""
        if (earliestDateValue?.text.isNullOrEmpty()) {
            earliestDateTitleLayout.visibility = View.GONE
        } else
            earliestDateTitleLayout.visibility = View.VISIBLE
    }

    private fun setupViewModel() {
        checkoutAddAddressNewUserViewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(
                CheckoutAddAddressNewUserInteractor(
                    CheckoutAddAddressNewUserApiHelper()
                )
            )
        ).get(CheckoutAddAddressNewUserViewModel::class.java)

        checkoutAddressConfirmationViewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(
                CheckoutAddressConfirmationInteractor(
                    CheckoutAddressConfirmationApiHelper()
                )
            )
        ).get(CheckoutAddressConfirmationViewModel::class.java)
    }

    private fun navigateToUnsellableItemsFragment(
        unSellableCommerceItems: List<UnSellableCommerceItem>,
        address: Address,
        deliverable: Boolean,
        deliveryType: DeliveryType
    ) {
        val suburb = Suburb()
        when (deliveryType) {
            DeliveryType.DELIVERY -> {
                suburb.apply {
                    id = address.suburbId
                    name = address.suburb
                    postalCode = address.postalCode
                    suburbDeliverable = deliverable
                }
            }
            DeliveryType.STORE_PICKUP -> {

                val localStoreAddress = StoreAddress(
                    validateStoreList?.storeAddress,
                    "",
                    selectedProvince.name,
                    "",
                    address.postalCode,
                    validateStoreList?.storeName,
                    validateStoreList?.storeId,
                    selectedProvince.name
                )

                suburb.apply {
                    id = validateStoreList?.storeId
                    name = validateStoreList?.storeName
                    postalCode = address.postalCode
                    suburbDeliverable = deliverable
                    storeAddress = localStoreAddress
                    fulfillmentStores = selectedSuburb.fulfillmentStores
                }
            }
        }

        val province = Province()
        province.apply {
            name = getProvinceName(address.region)
            id = address.region
        }
        val bundle = Bundle()
        bundle.apply {
            putString(EditDeliveryLocationActivity.DELIVERY_TYPE, deliveryType.name)
            putString("SUBURB", Utils.toJson(suburb))
            putString("PROVINCE", Utils.toJson(province))
            putString("UnSellableCommerceItems", Utils.toJson(unSellableCommerceItems))
        }
        navController?.navigate(
            R.id.action_to_unsellableItemsFragment,
            bundleOf("bundle" to bundle)
        )
    }

    private fun getProvinceName(provinceId: String?): String {
        val provinceList =
            WoolworthsApplication.getNativeCheckout()?.regions as MutableList<Province>
        if (!provinceId.isNullOrEmpty()) {
            for (provinces in provinceList) {
                if (provinceId.equals(provinces.id)) {
                    // province id is matching with the province list from config.
                    return provinces.name
                }
            }
        }
        return ""
    }

    override fun hideErrorView() {
        addNewAddressErrorMsg.visibility = View.GONE
    }

    override fun changeAddress(address: Address) {
        // Save instance of selected address to pass to other screens
        selectedAddress = address
    }

    private fun callChangeAddressApi() {
        selectedAddress?.nickname?.let { nickname ->
            loadingProgressBar.visibility = View.VISIBLE
            checkoutAddAddressNewUserViewModel.changeAddress(nickname)
                .observe(viewLifecycleOwner, { response ->
                    loadingProgressBar.visibility = View.GONE
                    when (response) {
                        is ChangeAddressResponse -> {
                            when (response.httpCode) {
                                HttpURLConnection.HTTP_OK, AppConstant.HTTP_OK_201 -> {

                                    // If deliverable false then show cant deliver popup
                                    // Don't allow user to navigate to Checkout page when deliverable : [false].
                                    if (!response.deliverable) {
                                        showSuburbNotDeliverableBottomSheetDialog(
                                            SuburbNotDeliverableBottomsheetDialogFragment.ERROR_CODE_SUBURB_NOT_DELIVERABLE
                                        )
                                        return@observe
                                    }

                                    // Set default address to selected address
                                    savedAddress?.defaultAddressNickname = nickname
                                    baseFragBundle?.putString(
                                        SAVED_ADDRESS_KEY,
                                        Utils.toJson(savedAddress)
                                    )

                                    // Check if any unSellableCommerceItems[ ] > 0 display the items in modal as per the design
                                    if (!response.unSellableCommerceItems.isNullOrEmpty()) {
                                        navigateToUnsellableItemsFragment(
                                            response.unSellableCommerceItems,
                                            selectedAddress!!,
                                            response.deliverable,
                                            DeliveryType.DELIVERY
                                        )
                                        return@observe
                                    }
                                    navigateToReturningUser()
                                }
                                else -> {
                                    showErrorScreen(
                                        ErrorHandlerActivity.COMMON_WITH_BACK_BUTTON,
                                        getString(R.string.common_error_message_without_contact_info)
                                    )
                                }
                            }
                        }
                        is Throwable -> {
                            showErrorScreen(
                                ErrorHandlerActivity.COMMON_WITH_BACK_BUTTON,
                                getString(R.string.common_error_message_without_contact_info)
                            )
                        }
                    }
                })
        }
    }

    private fun showErrorScreen(errorType: Int, errorMessage: String?) {
        activity?.apply {
            val intent = Intent(this, ErrorHandlerActivity::class.java)
            intent.putExtra("errorType", errorType)
            intent.putExtra("errorMessage", errorMessage)
            startActivityForResult(intent, ErrorHandlerActivity.ERROR_PAGE_REQUEST_CODE)
        }
    }

    private fun navigateToReturningUser() {
        view?.findNavController()?.navigate(
            R.id.action_checkoutAddressConfirmationFragment_to_CheckoutAddAddressReturningUserFragment,
            baseFragBundle
        )
    }

    private fun showSuburbNotDeliverableBottomSheetDialog(errorCode: String?) {
        view?.findNavController()?.navigate(
            R.id.action_checkoutAddressConfirmationFragment_to_suburbNotDeliverableBottomsheetDialogFragment,
            bundleOf(
                SuburbNotDeliverableBottomsheetDialogFragment.ERROR_CODE to errorCode
            )
        )
    }

    override fun onSuburbSelected(suburb: Suburb) {
        selectedSuburb = suburb
        storeListAdapter = null // setting null to update selected store position in list to -1
        showCollectionTab(selectedSuburb?.id)
    }
}