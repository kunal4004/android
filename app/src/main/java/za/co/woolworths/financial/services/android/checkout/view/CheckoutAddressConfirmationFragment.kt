package za.co.woolworths.financial.services.android.checkout.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
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
import kotlinx.android.synthetic.main.checkout_address_confirmation.*
import kotlinx.android.synthetic.main.checkout_address_confirmation_click_and_collect.*
import kotlinx.android.synthetic.main.checkout_address_confirmation_delivery.*
import za.co.woolworths.financial.services.android.checkout.interactor.CheckoutAddAddressNewUserInteractor
import za.co.woolworths.financial.services.android.checkout.service.network.*
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressReturningUserFragment.FulfillmentsType.FOOD
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressReturningUserFragment.FulfillmentsType.OTHER
import za.co.woolworths.financial.services.android.checkout.view.CheckoutReturningUserCollectionFragment.Companion.KEY_IS_WHO_IS_COLLECTING
import za.co.woolworths.financial.services.android.checkout.view.adapter.CheckoutAddressConfirmationListAdapter
import za.co.woolworths.financial.services.android.checkout.view.adapter.CheckoutStoreSelectionAdapter
import za.co.woolworths.financial.services.android.checkout.viewmodel.CheckoutAddAddressNewUserViewModel
import za.co.woolworths.financial.services.android.checkout.viewmodel.ViewModelFactory
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.ValidateSelectedSuburbResponse
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.service.network.ResponseStatus
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity
import za.co.woolworths.financial.services.android.ui.adapters.SuburbListAdapter
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CheckOutFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.BUNDLE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.DELIVERY_TYPE
import java.net.HttpURLConnection


/**
 * Created by Kunal Uttarwar on 16/06/21.
 *
 * not usefull now
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
    private var selectedSuburb = Suburb()
    private var selectedProvince = Province()
    private var isDeliverySelected: Boolean? = null
    private var isConfirmDeliveryResponse: Boolean = false
    private var validateStoreList: ValidateStoreList? = null

    companion object {
        const val UPDATE_SAVED_ADDRESS_REQUEST_KEY = "updateSavedAddress"
        const val DELETE_SAVED_ADDRESS_REQUEST_KEY = "deleteSavedAddress"
        const val ADD_A_NEW_ADDRESS_REQUEST_KEY = "addNewAddress"
        const val ADD_NEW_ADDRESS_KEY = "addNewAddress"
        const val SAVED_ADDRESS_KEY = "savedAddress"
        const val IS_EDIT_ADDRESS_SCREEN = "isEditAddressScreenNeeded"
        const val UNSELLABLE_CHANGE_STORE_REQUEST_KEY = "unsellableChangeStore"
        const val STORE_SELECTION_REQUEST_KEY = "storeSelectionResponse"
        const val CONFIRM_DELIVERY_ADDRESS_RESPONSE_KEY = "confirmDeliveryAddressResponse"
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
        // Hide keyboard in case it was visible from a previous screen
        KeyboardUtils.hideKeyboardIfVisible(activity)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.deliveryTab -> {
                if (loadingProgressBar.visibility == View.GONE && isDeliverySelected == false) {
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.CHANGE_FULFILLMENT_DELIVERY,
                        hashMapOf(
                            FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                                    FirebaseManagerAnalyticsProperties.PropertyValues.  ACTION_VALUE_NATIVE_CHECKOUT_DELIVERY
                        ),
                        activity)
                    showDeliveryTab()
                    showDeliveryAddressListView()
                    initialiseDeliveryAddressRecyclerView()
                }
            }
            R.id.collectionTab -> {
                if (loadingProgressBar.visibility == View.GONE && isDeliverySelected == true) {
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.CHANGE_FULFILLMENT_COLLECTION,
                        hashMapOf(
                            FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                                    FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_NATIVE_CHECKOUT_COLLECTION
                        ),
                        activity)
                    showCollectionTab(localSuburbId)
                }
            }
            R.id.plusImgAddAddress, R.id.addNewAddressTextView -> {
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.CHANGE_FULFILLMENT_ADD_NEW_ADDRESS,
                    hashMapOf(
                        FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                                FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_NATIVE_CHECKOUT_ADD_NEW_ADDRESS
                    ),
                    activity)
                navigateToAddAddress()
            }
            R.id.btnAddressConfirmation -> {
                if (loadingProgressBar.visibility == View.GONE) {
                    if (isDeliverySelected == true) {
                        if (checkoutAddressConfirmationListAdapter?.checkedItemPosition == -1)
                            addNewAddressErrorMsg.visibility = View.VISIBLE
                        else {
                            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.CHANGE_FULFILLMENT_DELIVERY_CONFIRM_BTN,
                                hashMapOf(
                                    FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                                            FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_NATIVE_CHECKOUT_CONFIRM_ADDRESS
                                ),
                                activity)
                            callChangeAddressApi()
                        }
                    } else {
                        // This is when user clicks on collection journey.
                        if (btnAddressConfirmation.text.equals(getString(R.string.change_suburb))) {
                            //Zero stores and user clicks on change suburb.

                        } else if (selectedSuburb.storeAddress != null) {
                            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.CHECKOUT_CONFIRM_NEW_STORE,
                                hashMapOf(
                                    FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                                            FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_NATIVE_CHECKOUT_CONFIRM_STORE
                                ),
                                activity)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ErrorHandlerActivity.ERROR_EMPTY_REQUEST_CODE -> {
                when (resultCode) {
                    // Cart is empty when removed unsellable items. go to cart and refresh cart screen.
                    Activity.RESULT_CANCELED, ErrorHandlerActivity.RESULT_RETRY -> {
                        (activity as? CheckoutActivity)?.apply {
                            setResult(CheckOutFragment.RESULT_EMPTY_CART)
                            closeActivity()
                        }
                    }
                }
            }
            ErrorHandlerActivity.ERROR_PAGE_REQUEST_CODE -> {
                when (resultCode) {
                    // Cart is empty when removed unsellable items. go to cart and refresh cart screen.
                    Activity.RESULT_CANCELED, ErrorHandlerActivity.RESULT_RETRY -> {
                        (activity as? CheckoutActivity)?.apply {
                            setResult(CheckOutFragment.RESULT_RELOAD_CART)
                            closeActivity()
                        }
                    }
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
                // if it is store then call setConfirmSelection API (same as setSuburb API).
                setConfirmSelection()
            }
        }
    }

    private fun showEmptyCart() {
        activity?.let {
            val intent = Intent(it, ErrorHandlerActivity::class.java)
            intent.putExtra(
                ErrorHandlerActivity.ERROR_TYPE,
                ErrorHandlerActivity.ERROR_TYPE_EMPTY_CART
            )
            it.startActivityForResult(intent, ErrorHandlerActivity.ERROR_EMPTY_REQUEST_CODE)
        }
    }

    private fun setConfirmSelection() {
        selectedSuburb.storeAddress.suburbId?.let { storeId ->
            loadingProgressBar.visibility = View.VISIBLE
            activity?.window?.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
            checkoutAddAddressNewUserViewModel.setConfirmSelection(
                ConfirmSelectionRequestBody(
                    storeId,
                    null
                )
            )
                .observe(viewLifecycleOwner, { response ->
                    loadingProgressBar.visibility = View.GONE
                    activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    when (response) {
                        is ConfirmSelectionResponse -> {
                            when (response.httpCode) {
                                HttpURLConnection.HTTP_OK, AppConstant.HTTP_OK_201 -> {
                                    if (response.productCountMap.totalProductCount <= 0) {
                                        showEmptyCart()
                                        return@observe
                                    }
                                    val store = selectedSuburb.let { suburb ->
                                        Store(
                                            suburb.storeAddress.suburbId,
                                            suburb.storeAddress.suburb,
                                            suburb.fulfillmentStores,
                                            suburb.storeAddress.address1
                                        )
                                    }

                                    if (isDeliverySelected != null && !isDeliverySelected!!) {
                                        // check if it's from collection Change Fullfilments or delivery Change Fullfilments. if collection then nav up else who is collecting.
                                        if (arguments?.containsKey(KEY_IS_WHO_IS_COLLECTING) == true && arguments?.getBoolean(
                                                KEY_IS_WHO_IS_COLLECTING
                                            ) == true
                                        ) {
                                            navController?.navigateUp()
                                        } else
                                            navController?.navigate(R.id.checkoutWhoIsCollectingFragment)
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
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.CHECKOUT_COLECTION_CHANGE_BTN,
            hashMapOf(
                FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                        FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_NATIVE_CHECKOUT_COLLECTION_CHANGE_SUBURB
            ),
            activity)
        val bundle = Bundle()
        bundle.apply {
            putString(
                "ProvinceList",
                Utils.toJson(AppConfigSingleton.nativeCheckout?.regions)
            )
        }

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
            showEarliestCollectionView(earliestDateValue?.text.toString())
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
        if (isConfirmDeliveryResponse) {
            showEarliestDeliveryDates()
        } else
            hideEarliestDeliveryView()
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
            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.CHECKOUT_REMOVE_UNSELLABLE_ITEMS,
                hashMapOf(
                    FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                            FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_NATIVE_CHECKOUT_REMOVE_ITEMS
                ),
                activity)
            if (isDeliverySelected == true) {
                view?.findNavController()?.navigate(
                    R.id.action_checkoutAddressConfirmationFragment_to_CheckoutAddAddressReturningUserFragment,
                    baseFragBundle
                )
            } else {
                localSuburbId =
                    DEFAULT_STORE_ID // setting to default so that it will again call validateSelectedSuburb.
                setConfirmSelection()
            }
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
            showEarliestCollectionView(validateStoreList?.firstAvailableFoodDeliveryDate ?: "")
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
        if (arguments?.containsKey(KEY_IS_WHO_IS_COLLECTING) == true && arguments?.getBoolean(
                KEY_IS_WHO_IS_COLLECTING
            ) == true
        ){
            isDeliverySelected = false
        }
        else if (isDeliverySelected == null) {
            if (baseFragBundle?.containsKey(IS_DELIVERY) == true)
                isDeliverySelected = baseFragBundle?.getBoolean(IS_DELIVERY)
        }
        if (isDeliverySelected != null && isDeliverySelected as Boolean) {
            if (baseFragBundle?.containsKey(CONFIRM_DELIVERY_ADDRESS_RESPONSE_KEY) == true) {
                isConfirmDeliveryResponse = true
                showEarliestDeliveryDates()
            } else {
                isConfirmDeliveryResponse = false
                hideEarliestDeliveryView()
            }
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

    private fun showEarliestDeliveryDates() {
        val confirmDeliveryAddressResponse = Utils.jsonStringToObject(
            baseFragBundle?.getString(CONFIRM_DELIVERY_ADDRESS_RESPONSE_KEY),
            ConfirmDeliveryAddressResponse::class.java
        ) as? ConfirmDeliveryAddressResponse
            ?: baseFragBundle?.getSerializable(CONFIRM_DELIVERY_ADDRESS_RESPONSE_KEY) as? ConfirmDeliveryAddressResponse

        val fulfillmentsType = confirmDeliveryAddressResponse?.fulfillmentTypes
        if (fulfillmentsType?.join == FOOD.type) {
            //Food Basket
            val foodItemDate =
                confirmDeliveryAddressResponse?.timedDeliveryFirstAvailableDates?.join
            if (foodItemDate.isNullOrEmpty()) {
                hideEarliestDeliveryView()
            } else {
                showEarliestDeliveryView()
                foodItemsDeliveryDateLayout.visibility = View.GONE
                otherItemsDeliveryDateLayout.visibility = View.GONE
                earliestDateValue.text = foodItemDate
            }
        } else if (fulfillmentsType?.join == OTHER.type && fulfillmentsType.other == OTHER.type) {
            //Mixed Basket
            showEarliestDeliveryView()
            val foodItemDate =
                confirmDeliveryAddressResponse?.timedDeliveryFirstAvailableDates?.food
            val otherItemDate =
                confirmDeliveryAddressResponse?.timedDeliveryFirstAvailableDates?.other
            if (foodItemDate.isNullOrEmpty() && otherItemDate.isNullOrEmpty()) {
                hideEarliestDeliveryView()
            } else if (foodItemDate.isNullOrEmpty() || otherItemDate.isNullOrEmpty()) {
                foodItemsDeliveryDateLayout.visibility = View.GONE
                otherItemsDeliveryDateLayout.visibility = View.GONE
                earliestDateValue.text =
                    if (foodItemDate?.isEmpty() == true) otherItemDate else foodItemDate
            } else {
                foodItemsDeliveryDateLayout.visibility =
                    if (foodItemDate.isNullOrEmpty()) View.GONE else View.VISIBLE
                otherItemsDeliveryDateLayout.visibility =
                    if (otherItemDate.isNullOrEmpty()) View.GONE else View.VISIBLE
                if (foodItemsDeliveryDateLayout.visibility == View.GONE || otherItemsDeliveryDateLayout.visibility == View.GONE) {
                    earliestDateValue.text = foodItemDate ?: otherItemDate
                } else {
                    foodItemsDeliveryDate.text = foodItemDate
                    otherItemsDeliveryDate.text = otherItemDate
                }
            }

        } else if (fulfillmentsType?.join == OTHER.type) {
            //Other Basket
            val otherItemDate =
                confirmDeliveryAddressResponse?.timedDeliveryFirstAvailableDates?.join
            if (otherItemDate.isNullOrEmpty()) {
                hideEarliestDeliveryView()
            } else {
                showEarliestDeliveryView()
                foodItemsDeliveryDateLayout.visibility = View.GONE
                otherItemsDeliveryDateLayout.visibility = View.GONE
                earliestDateValue.text = otherItemDate
            }
        } else {
            hideEarliestDeliveryView()
        }
    }

    private fun showEarliestDeliveryView() {
        deliveryDateLayout.visibility = View.VISIBLE
        earliestDateTitle.text = bindString(R.string.earliest_delivery_date)
        earliestDateValue.text = ""
    }

    private fun hideEarliestDeliveryView() {
        deliveryDateLayout.visibility = View.GONE
    }

    private fun showEarliestCollectionView(dateValue: String) {
        earliestDateTitle.text = bindString(R.string.earliest_collection_date)
        earliestDateValue?.text = dateValue
        if (dateValue.isNullOrEmpty()) {
            deliveryDateLayout.visibility = View.GONE
        } else
            deliveryDateLayout.visibility = View.VISIBLE
        foodItemsDeliveryDateLayout.visibility = View.GONE
        otherItemsDeliveryDateLayout.visibility = View.GONE
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
        val totalRemovalHeight =
            if (isConfirmDeliveryResponse) 405 else 370
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

        } else if (suburbId.isNullOrEmpty()) {
            clickNCollectTitleLayout.visibility = View.VISIBLE
            addressConfirmationClicknCollect.visibility = View.VISIBLE
            showStoreList()
        } else if (localSuburbId != suburbId) { //equals means only tab change happens. No suburb changed.
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
        showEarliestCollectionView(validateStoreList?.firstAvailableFoodDeliveryDate ?: "")
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

    private fun setupViewModel() {
        checkoutAddAddressNewUserViewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(
                CheckoutAddAddressNewUserInteractor(
                    CheckoutAddAddressNewUserApiHelper()
                )
            )
        ).get(CheckoutAddAddressNewUserViewModel::class.java)
    }

    private fun navigateToUnsellableItemsFragment(
        unSellableCommerceItems: List<UnSellableCommerceItem>,
        address: Address,
        deliverable: Boolean,
        deliveryType: DeliveryType,
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
            putString(DELIVERY_TYPE, deliveryType.name)
            putString("SUBURB", Utils.toJson(suburb))
            putString("PROVINCE", Utils.toJson(province))
            putString("UnSellableCommerceItems", Utils.toJson(unSellableCommerceItems))
        }
        navController?.navigate(
            R.id.action_to_unsellableItemsFragment,
            bundleOf(BUNDLE to bundle)
        )
    }

    private fun getProvinceName(provinceId: String?): String {
        val provinceList =
            AppConfigSingleton.nativeCheckout?.regions as? MutableList<Province>
        if (!provinceId.isNullOrEmpty() && !provinceList.isNullOrEmpty()) {
            for (provinces in provinceList) {
                if (provinceId == provinces.id) {
                    // province id is matching with the province list from config.
                    return provinces.name ?: ""
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
        if (!isConfirmDeliveryResponse || savedAddress?.defaultAddressNickname != address.nickname) {
            deliveryDateLayout.visibility = View.GONE
        } else
            showEarliestDeliveryDates()
    }

    private fun callChangeAddressApi() {
        selectedAddress?.nickname?.let { nickname ->
            loadingProgressBar.visibility = View.VISIBLE
            activity?.window?.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
            checkoutAddAddressNewUserViewModel.changeAddress(nickname)
                .observe(viewLifecycleOwner, { response ->
                    loadingProgressBar.visibility = View.GONE
                    activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    when (response) {
                        is ChangeAddressResponse -> {
                            when (response.httpCode) {
                                HttpURLConnection.HTTP_OK, AppConstant.HTTP_OK_201 -> {

                                    if (response.deliverable == null) {
                                        showErrorScreen(
                                            ErrorHandlerActivity.COMMON_WITH_BACK_BUTTON,
                                            getString(R.string.common_error_message_without_contact_info)
                                        )
                                        return@observe
                                    }

                                    // If deliverable false then show cant deliver popup
                                    // Don't allow user to navigate to Checkout page when deliverable : [false].
                                    if (response.deliverable == false) {
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
                                            response.deliverable ?: false,
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