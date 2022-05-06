package za.co.woolworths.financial.services.android.geolocation.view


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.geo_location_delivery_address.*
import kotlinx.android.synthetic.main.no_connection.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import za.co.woolworths.financial.services.android.checkout.service.network.Address
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.checkout.view.*
import za.co.woolworths.financial.services.android.checkout.viewmodel.WhoIsCollectingDetails
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.geolocation.model.request.ConfirmLocationRequest
import za.co.woolworths.financial.services.android.geolocation.model.response.ConfirmLocationAddress
import za.co.woolworths.financial.services.android.geolocation.network.apihelper.GeoLocationApiHelper
import za.co.woolworths.financial.services.android.geolocation.network.model.Store
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidateLocationResponse
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.geolocation.viewmodel.GeoLocationViewModelFactory
import za.co.woolworths.financial.services.android.geolocation.viewmodel.UnSellableItemsLiveData
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.Province
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation
import za.co.woolworths.financial.services.android.models.dto.Suburb
import za.co.woolworths.financial.services.android.models.dto.UnSellableCommerceItem
import za.co.woolworths.financial.services.android.models.network.StorePickupInfoBody
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.click_and_collect.UnsellableItemsFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CartFragment
import za.co.woolworths.financial.services.android.ui.views.CustomBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.VtoErrorBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.listener.VtoTryAgainListener
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_OK
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.BUNDLE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.CNC
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.DASH
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.DEFAULT_ADDRESS
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.DELIVERY_TYPE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.FULLFILLMENT_REQUEST_CODE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_CONFIRM_ADD
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_FROM_CHECKOUT
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_FROM_CNC_SELETION
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_FROM_SLOT_SELECTION
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.KEY_LATITUDE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.KEY_LONGITUDE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.KEY_PLACE_ID
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.REQUEST_CODE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.SAVED_ADDRESS_RESPONSE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.STANDARD
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.STANDARD_DELIVERY
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.VALIDATE_RESPONSE
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import javax.inject.Inject

/**
 * Created by Kunal Uttarwar on 24/02/22.
 */
@AndroidEntryPoint
class DeliveryAddressConfirmationFragment : Fragment(), View.OnClickListener, VtoTryAgainListener {

    private var isUnSellableItemsRemoved: Boolean? = false
    private lateinit var confirmAddressViewModel: ConfirmAddressViewModel
    private var placeId: String? = null
    private var isComingFromSlotSelection: Boolean = false
    private var isComingFromCheckout: Boolean = false
    private var latitude: String? = null
    private var longitude: String? = null
    private var validateLocationResponse: ValidateLocationResponse? = null
    private var deliveryType: String? = STANDARD_DELIVERY
    private var lastDeliveryType: String? = STANDARD_DELIVERY
    private var mStoreName: String? = null
    private var mStoreId: String? = null
    private var bundle: Bundle? = null
    private var defaultAddress: Address? = null
    private var savedAddressResponse: SavedAddressResponse? = null
    private var whoIsCollecting: WhoIsCollectingDetails? = null
    private var customBottomSheetDialogFragment: CustomBottomSheetDialogFragment? = null
    var store: Store? = null

    @Inject
    lateinit var vtoErrorBottomSheetDialog: VtoErrorBottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.geo_location_delivery_address, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViewModel()
        addFragmentListner()
        initView()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle(BUNDLE)

        bundle?.apply {
            latitude = getString(KEY_LATITUDE, "")
            longitude = this.getString(KEY_LONGITUDE, "")
            placeId = this.getString(KEY_PLACE_ID, "")
            isComingFromSlotSelection = this.getBoolean(IS_COMING_FROM_SLOT_SELECTION, false)
            isComingFromCheckout = this.getBoolean(IS_COMING_FROM_CHECKOUT, false)
            deliveryType = this.getString(DELIVERY_TYPE, Delivery.STANDARD.name)
            lastDeliveryType = this.getString(DELIVERY_TYPE, Delivery.STANDARD.name)
            getString(CheckoutReturningUserCollectionFragment.KEY_COLLECTING_DETAILS)?.let {
                whoIsCollecting =
                    Gson().fromJson(it, object : TypeToken<WhoIsCollectingDetails>() {}.type)
            }
            if (this.containsKey(DEFAULT_ADDRESS)
                && this.getSerializable(DEFAULT_ADDRESS) != null
            ) {
                defaultAddress =
                    this.getSerializable(DEFAULT_ADDRESS) as Address
            }

            if (bundle?.containsKey(SAVED_ADDRESS_RESPONSE) == true
                && this.getSerializable(SAVED_ADDRESS_RESPONSE) != null
            ) {
                savedAddressResponse =
                    this.getSerializable(SAVED_ADDRESS_RESPONSE) as SavedAddressResponse
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgDelBack -> {
                activity?.onBackPressed()
            }
            R.id.editDelivery -> {
                when (deliveryType) {

                    Delivery.CNC.name -> {
                        moveToCnCEditStore()
                        return
                    }

                    Delivery.STANDARD.name -> {
                        Utils.triggerFireBaseEvents(
                            FirebaseManagerAnalyticsProperties.SHOP_STANDARD_EDIT,
                            hashMapOf(
                                FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                                        FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_SHOP_STANDARD_EDIT
                            ),
                            activity)
                        navigateToConfirmAddressScreen()
                        return
                    }

                    Delivery.DASH.name -> {
                        navigateToConfirmAddressScreen()
                        return
                    }
                }
            }
            R.id.btnConfirmAddress -> {
                sendConfirmLocation()
            }

            R.id.geoCollectTab -> {
                if (progressBar?.visibility == View.VISIBLE)
                    return
                else {
                    lastDeliveryType = deliveryType
                    openCollectionTab()
                }
            }
            R.id.geoDeliveryTab -> {
                if (progressBar?.visibility == View.VISIBLE)
                    return
                else {
                    lastDeliveryType = deliveryType
                    openGeoDeliveryTab()
                }
            }
            R.id.geoDashTab -> {
                if (progressBar?.visibility == View.VISIBLE)
                    return
                else {
                    lastDeliveryType = deliveryType
                    openDashTab()
                }
            }
            R.id.btnRetryConnection -> {
                initView()
            }
        }
    }

    private fun moveToCnCEditStore() {
        Utils.triggerFireBaseEvents(
            FirebaseManagerAnalyticsProperties.SHOP_CLICK_COLLECT_EDIT,
            hashMapOf(
                FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                        FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_SHOP_CLICK_COLLECT_EDIT
            ),
            activity)
        bundle?.putSerializable(
            VALIDATE_RESPONSE, validateLocationResponse)
        bundle?.putBoolean(
            IS_COMING_CONFIRM_ADD, false)
        bundle?.putString(DELIVERY_TYPE, deliveryType)
        findNavController().navigate(
            R.id.action_deliveryAddressConfirmationFragment_to_clickAndCollectStoresFragment,
            bundleOf(BUNDLE to bundle)
        )
    }

    private fun addFragmentListner() {
        setFragmentResultListener(STORE_LOCATOR_REQUEST_CODE) { _, bundle ->
            store = bundle.get(BUNDLE) as Store
            store?.let {
                if (it?.storeName != null) {
                    geoDeliveryText?.text = it?.storeName
                }
                editDelivery?.text = bindString(R.string.edit)
                btnConfirmAddress?.isEnabled = true
                btnConfirmAddress?.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
                mStoreName = it?.storeName.toString()
                mStoreId = it?.storeId.toString()
            }
        }

        setFragmentResultListener(CustomBottomSheetDialogFragment.DIALOG_BUTTON_CLICK_RESULT) { _, _ ->
            // change location button clicked as address is not deliverable.
            when (deliveryType) {
                Delivery.STANDARD.name, Delivery.DASH.name -> {
                    navigateToConfirmAddressScreen()
                }
                Delivery.CNC.name -> {
                    moveToCnCEditStore()
                }
            }
        }
        setFragmentResultListener(MAP_LOCATION_RESULT) { _, bundle ->
            // Assign new lat long and Reload the fragment.
            val localBundle = bundle.getBundle(BUNDLE)
            localBundle.apply {
                latitude = this?.getString(KEY_LATITUDE, "")
                longitude = this?.getString(KEY_LONGITUDE, "")
                placeId = this?.getString(KEY_PLACE_ID, "")
            }
            placeId?.let { getDeliveryDetailsFromValidateLocation(it) }
        }
        setFragmentResultListener(CustomBottomSheetDialogFragment.DIALOG_BUTTON_DISMISS_RESULT) { _, _ ->
            // change location dismiss button clicked so land back on last delivery location tab.
            when (lastDeliveryType) {
                Delivery.STANDARD.name -> {
                    openGeoDeliveryTab()
                }
                Delivery.CNC.name -> {
                    openCollectionTab()
                }
                Delivery.DASH.name -> {
                    openDashTab()
                }
            }
        }
    }

    private fun navigateToConfirmAddressScreen() {
        bundle?.putBoolean(IS_COMING_FROM_CHECKOUT, isComingFromCheckout)
        bundle?.putBoolean(IS_COMING_FROM_SLOT_SELECTION, isComingFromSlotSelection)
        bundle?.putString(DELIVERY_TYPE, deliveryType)
        findNavController().navigate(
            R.id.action_deliveryAddressConfirmationFragment_to_confirmDeliveryLocationFragment,
            bundleOf(BUNDLE to bundle)
        )
    }

    private fun sendConfirmLocation() {
        var unSellableCommerceItems: MutableList<UnSellableCommerceItem>? = null
        validateLocationResponse?.validatePlace?.stores?.forEach {
            if (it.storeName.equals(mStoreName)) {
                unSellableCommerceItems = it.unSellableCommerceItems
            }
        }
        if (unSellableCommerceItems?.isNullOrEmpty() == false && isUnSellableItemsRemoved == false) {
            // show unsellable items
            unSellableCommerceItems?.let {
                navigateToUnsellableItemsFragment(it)

            }

        } else {
            if (placeId == null) {
                return
            }
            val confirmLocationAddress = ConfirmLocationAddress(placeId)
            val confirmLocationRequest = when (deliveryType) {
                Delivery.STANDARD.name -> {
                    ConfirmLocationRequest(STANDARD, confirmLocationAddress, "")
                }
                Delivery.CNC.name -> {
                    ConfirmLocationRequest(CNC, confirmLocationAddress, mStoreId)
                }
                Delivery.DASH.name -> {
                    ConfirmLocationRequest(DASH, confirmLocationAddress, mStoreId)
                }
                else -> {
                    ConfirmLocationRequest(STANDARD, confirmLocationAddress, "")
                }
            }

            lifecycleScope.launch {
                progressBar?.visibility = View.VISIBLE
                try {
                    val confirmLocationResponse =
                        confirmAddressViewModel.postConfirmAddress(confirmLocationRequest)
                    progressBar?.visibility = View.GONE
                    if (confirmLocationResponse != null) {
                        when (confirmLocationResponse.httpCode) {
                            HTTP_OK -> {
                                // save details in cache
                                if (SessionUtilities.getInstance().isUserAuthenticated) {
                                    KotlinUtils.placeId = placeId
                                    KotlinUtils.isLocationSame =
                                        placeId?.equals(Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId)
                                    KotlinUtils.isDeliveryLocationTabClicked =
                                        placeId?.equals(Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId)
                                    KotlinUtils.isCncTabClicked =
                                        placeId?.equals(Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId)
                                    KotlinUtils.isDashTabClicked =
                                        placeId?.equals(Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId)
                                    Utils.savePreferredDeliveryLocation(
                                        ShoppingDeliveryLocation(
                                            confirmLocationResponse.orderSummary?.fulfillmentDetails
                                        )
                                    )
                                    if (KotlinUtils.getAnonymousUserLocationDetails() != null)
                                        KotlinUtils.clearAnonymousUserLocationDetails()
                                } else {
                                    KotlinUtils.placeId = placeId
                                    KotlinUtils.isLocationSame =
                                        placeId?.equals(KotlinUtils.getAnonymousUserLocationDetails()?.fulfillmentDetails?.address?.placeId)
                                    KotlinUtils.isDeliveryLocationTabClicked =
                                        placeId?.equals(KotlinUtils.getAnonymousUserLocationDetails()?.fulfillmentDetails?.address?.placeId)
                                    KotlinUtils.isCncTabClicked =
                                        placeId?.equals(KotlinUtils.getAnonymousUserLocationDetails()?.fulfillmentDetails?.address?.placeId)
                                    KotlinUtils.isDashTabClicked =
                                        placeId?.equals(KotlinUtils.getAnonymousUserLocationDetails()?.fulfillmentDetails?.address?.placeId)
                                    KotlinUtils.saveAnonymousUserLocationDetails(
                                        ShoppingDeliveryLocation(
                                            confirmLocationResponse.orderSummary?.fulfillmentDetails
                                        )
                                    )
                                }

                                WoolworthsApplication.setValidatedSuburbProducts(
                                    validateLocationResponse?.validatePlace)

                                savedAddressResponse?.defaultAddressNickname =
                                    defaultAddress?.nickname

                                if (deliveryType == Delivery.STANDARD.name) {
                                    Utils.triggerFireBaseEvents(
                                        FirebaseManagerAnalyticsProperties.SHOP_STANDARD_CONFIRM,
                                        hashMapOf(
                                            FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                                                    FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_SHOP_STANDARD_CONFIRM),

                                        activity)
                                }

                                if (isComingFromCheckout) {
                                    if (deliveryType == Delivery.STANDARD.name) {
                                        if (isComingFromSlotSelection) {
                                            /*Navigate to slot selection page with updated saved address*/

                                            val checkoutActivityIntent =
                                                Intent(activity,
                                                    CheckoutActivity::class.java
                                                )
                                            checkoutActivityIntent.putExtra(
                                                CheckoutAddressConfirmationFragment.SAVED_ADDRESS_KEY,
                                                savedAddressResponse)

                                            checkoutActivityIntent.putExtra(
                                                CheckoutAddressManagementBaseFragment.GEO_SLOT_SELECTION,
                                                true
                                            )
                                            activity?.apply {
                                                startActivityForResult(
                                                    checkoutActivityIntent,
                                                    FULLFILLMENT_REQUEST_CODE
                                                )

                                                overridePendingTransition(
                                                    R.anim.slide_from_right,
                                                    R.anim.slide_out_to_left
                                                )

                                            }
                                            activity?.finish()
                                        }
                                    } else if (isComingFromSlotSelection) {
                                        if (whoIsCollecting != null) {
                                            StorePickupInfoBody().apply {
                                                firstName = whoIsCollecting?.recipientName
                                                primaryContactNo = whoIsCollecting?.phoneNumber
                                                storeId = mStoreId
                                                vehicleModel = whoIsCollecting?.vehicleModel ?: ""
                                                vehicleColour = whoIsCollecting?.vehicleColor ?: ""
                                                vehicleRegistration =
                                                    whoIsCollecting?.vehicleRegistration ?: ""
                                                taxiOpted = whoIsCollecting?.isMyVehicle != true
                                                lastDeliveryType = deliveryType
                                                deliveryType = Delivery.CNC.name
                                                address =
                                                    ConfirmLocationAddress(validateLocationResponse?.validatePlace?.placeDetails?.placeId)
                                            }
                                            startCheckoutActivity(Utils.toJson(whoIsCollecting))
                                        } else {
                                            // Navigate to who is collecting
                                            bundle?.putBoolean(
                                                IS_COMING_FROM_CNC_SELETION, true)
                                            findNavController().navigate(
                                                R.id.action_deliveryAddressConfirmationFragment_to_geoCheckoutCollectingFragment,
                                                bundleOf(BUNDLE to bundle))
                                        }
                                    }

                                } else {
                                    // navigate to shop/list/cart tab
                                    activity?.setResult(Activity.RESULT_OK)
                                    activity?.finish()
                                }
                            }
                        }
                    }
                } catch (e: HttpException) {
                    e.printStackTrace()
                    progressBar?.visibility = View.GONE
                    // navigate to shop tab with error scenario
                    activity?.setResult(REQUEST_CODE)
                    activity?.finish()
                }
            }
        }
    }

    private fun startCheckoutActivity(toJson: String) {
        val checkoutActivityIntent = Intent(activity, CheckoutActivity::class.java)
        checkoutActivityIntent.putExtra(
            CheckoutReturningUserCollectionFragment.KEY_COLLECTING_DETAILS,
            toJson
        )
        activity?.let {
            startActivityForResult(
                checkoutActivityIntent,
                CartFragment.REQUEST_PAYMENT_STATUS
            )

            it.overridePendingTransition(
                R.anim.slide_from_right,
                R.anim.slide_out_to_left
            )
        }
        activity?.finish()
    }

    companion object {

        const val STORE_LOCATOR_REQUEST_CODE = "543"
        const val MAP_LOCATION_RESULT = "8472"

        fun newInstance(latitude: String, longitude: String, placesId: String) =
            DeliveryAddressConfirmationFragment().withArgs {
                putString(KEY_LATITUDE, latitude)
                putString(KEY_LONGITUDE, longitude)
                putString(KEY_PLACE_ID, placesId)
            }

        @JvmStatic
        fun newInstance(placesId: String?, deliveryType: Delivery? = Delivery.STANDARD) =
            DeliveryAddressConfirmationFragment().withArgs {
                putString(KEY_PLACE_ID, placesId)
                putString(DELIVERY_TYPE, deliveryType?.name)
            }
    }

    private fun setUpViewModel() {
        confirmAddressViewModel = ViewModelProvider(
            this,
            GeoLocationViewModelFactory(GeoLocationApiHelper())
        ).get(ConfirmAddressViewModel::class.java)
    }

    private fun initView() {
        imgDelBack?.setOnClickListener(this)
        editDelivery?.setOnClickListener(this)
        btnConfirmAddress?.setOnClickListener(this)
        geoDeliveryTab?.setOnClickListener(this)
        geoCollectTab?.setOnClickListener(this)
        geoDashTab?.setOnClickListener(this)
        geoDeliveryTab?.isEnabled = true
        geoCollectTab?.isEnabled = true
        geoDashTab?.isEnabled = true
        isUnSellableItemsRemoved()
        placeId?.let {
            if (confirmAddressViewModel.isConnectedToInternet(requireActivity())) {
                getDeliveryDetailsFromValidateLocation(it)
                connectionLayout?.visibility = View.GONE
            } else {
                connectionLayout?.visibility = View.VISIBLE
            }
        }
        btnRetryConnection?.setOnClickListener(this)
    }

    private fun openGeoDeliveryTab() {
        deliveryType = Delivery.STANDARD.name
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOP_DELIVERY,
            hashMapOf(FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_SHOP_DELIVERY),
            activity)

        selectATab(geoDeliveryTab)
        deliveryBagIcon?.setImageDrawable(ContextCompat.getDrawable(requireActivity(),
            R.drawable.img_delivery_truck))
        btnConfirmAddress?.isEnabled = true
        btnConfirmAddress?.setBackgroundColor(ContextCompat.getColor(requireContext(),
            R.color.black))
        editDelivery?.text = getString(R.string.edit)
        changeFulfillmentTitleTextView?.text = bindString(R.string.standard_delivery)
        changeFulfillmentSubTitleTextView?.text = bindString(R.string.empty)
        if (validateLocationResponse?.validatePlace?.deliverable == false) {
            // Show not deliverable Bottom Dialog.
            showNotDeliverablePopUp(R.string.no_location_title,
                R.string.no_location_desc,
                R.string.change_location,
                R.drawable.location_disabled,
                null)
        } else {
            if (customBottomSheetDialogFragment != null && customBottomSheetDialogFragment!!.isVisible) {
                customBottomSheetDialogFragment!!.dismiss()
            }
        }
        updateDeliveryDetails()
    }

    private fun openCollectionTab() {
        deliveryType = Delivery.CNC.name
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOP_CLICK_COLLECT,
            hashMapOf(FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_SHOP_CLICK_COLLECT),
            activity)
        selectATab(geoCollectTab)
        deliveryBagIcon?.setImageDrawable(ContextCompat.getDrawable(requireActivity(),
            R.drawable.img_collection_bag))
        changeFulfillmentTitleTextView?.text = bindString(R.string.click_and_collect)
        val collectionQuantity =
            validateLocationResponse?.validatePlace?.stores?.getOrNull(0)?.quantityLimit?.foodMaximumQuantity
        changeFulfillmentSubTitleTextView?.text =
            if (collectionQuantity != null) bindString(R.string.click_and_collect_title_text,
                collectionQuantity.toString()) else bindString(R.string.empty)
        validateLocationResponse?.validatePlace?.apply {
            if (this?.stores?.isEmpty() == true || this?.stores?.getOrNull(0)?.deliverable == false) {
                // Show no store available Bottom Dialog.
                showNotDeliverablePopUp(R.string.no_location_collection,
                    R.string.no_location_desc,
                    R.string.change_location,
                    R.drawable.img_collection_bag,
                    null)
            } else {
                if (customBottomSheetDialogFragment != null && customBottomSheetDialogFragment!!.isVisible) {
                    customBottomSheetDialogFragment!!.dismiss()
                }
            }
        }
        updateCollectionDetails()
    }

    private fun openDashTab() {
        deliveryType = Delivery.DASH.name
        selectATab(geoDashTab)
        deliveryBagIcon?.setImageDrawable(ContextCompat.getDrawable(requireActivity(),
            R.drawable.img_dash_delivery))
        changeFulfillmentTitleTextView?.text = bindString(R.string.dash_delivery)
        val deliveryFee =
            validateLocationResponse?.validatePlace?.onDemand?.deliveryTimeSlots?.getOrNull(0)?.slotCost
        val deliveryQuantity =
            validateLocationResponse?.validatePlace?.onDemand?.quantityLimit?.foodMaximumQuantity

        changeFulfillmentSubTitleTextView?.text =
            if (deliveryFee != null || deliveryQuantity != null) bindString(R.string.dash_title_text,
                deliveryFee.toString(),
                deliveryQuantity.toString()) else bindString(R.string.empty)
        val dashDeliverable = validateLocationResponse?.validatePlace?.onDemand?.deliverable
        if (dashDeliverable == null || dashDeliverable == false) {
            // Show not deliverable Popup
            showNotDeliverablePopUp(R.string.no_location_title,
                R.string.no_location_desc,
                R.string.change_location,
                R.drawable.location_disabled,
                null)
        } else {
            if (customBottomSheetDialogFragment != null && customBottomSheetDialogFragment!!.isVisible) {
                customBottomSheetDialogFragment!!.dismiss()
            }
        }
        updateDashDetails()
    }

    private fun selectATab(selectedTab: AppCompatTextView) {
        selectedTab?.setBackgroundResource(R.drawable.bg_geo_selected_tab)
        selectedTab?.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        when (selectedTab) {
            geoDeliveryTab -> {
                unSelectATab(geoCollectTab)
                unSelectATab(geoDashTab)
            }
            geoCollectTab -> {
                unSelectATab(geoDeliveryTab)
                unSelectATab(geoDashTab)
            }
            geoDashTab -> {
                unSelectATab(geoDeliveryTab)
                unSelectATab(geoCollectTab)
            }
        }
    }

    private fun unSelectATab(unSelectedTab: AppCompatTextView) {
        unSelectedTab?.apply {
            setBackgroundResource(R.drawable.bg_geo_unselected_tab)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.color_444444))
        }
    }

    private fun getDeliveryDetailsFromValidateLocation(placeId: String) {
        if (placeId.isNullOrEmpty())
            return

        lifecycleScope.launch {
            progressBar?.visibility = View.VISIBLE
            try {
                validateLocationResponse =
                    confirmAddressViewModel.getValidateLocation(placeId)
                progressBar?.visibility = View.GONE
                geoDeliveryView?.visibility = View.VISIBLE
                if (validateLocationResponse != null) {
                    when (validateLocationResponse?.httpCode) {
                        HTTP_OK -> {
                            when (deliveryType) {
                                Delivery.STANDARD.name -> {
                                    openGeoDeliveryTab()
                                }
                                Delivery.CNC.name -> {
                                    openCollectionTab()
                                }
                                Delivery.DASH.name -> {
                                    openDashTab()
                                }
                                else -> {
                                    openGeoDeliveryTab()
                                }
                            }
                        }
                        else -> {
                            showErrorDialog()

                        }
                    }
                }
            } catch (e: HttpException) {
                FirebaseManager.logException(e)
                progressBar?.visibility = View.GONE
                showErrorDialog()
            }
        }
    }

    private fun updateDeliveryDetails() {
        geoDeliveryView?.visibility = View.VISIBLE
        geoDeliveryText?.text =
            validateLocationResponse?.validatePlace?.placeDetails?.address1
                ?: getString(R.string.empty)

        var earliestFoodDate =
            validateLocationResponse?.validatePlace?.firstAvailableFoodDeliveryDate
        if (earliestFoodDate.isNullOrEmpty())
            earliestFoodDate = getString(R.string.earliest_delivery_no_date_available)

        var earliestFashionDate =
            validateLocationResponse?.validatePlace?.firstAvailableOtherDeliveryDate
        if (earliestFashionDate.isNullOrEmpty())
            earliestFashionDate = getString(R.string.earliest_delivery_no_date_available)
        setVisibilityDeliveryDates(earliestFoodDate, earliestFashionDate, null)
    }

    private fun updateCollectionDetails() {

        geoDeliveryView?.visibility = View.VISIBLE
        setGeoDeliveryTextForCnc()

        var earliestFoodDate =
            validateLocationResponse?.validatePlace?.firstAvailableFoodDeliveryDate
        if (earliestFoodDate.isNullOrEmpty())
            earliestFoodDate = getString(R.string.earliest_delivery_no_date_available)
        setVisibilityDeliveryDates(earliestFoodDate, null, null)
    }

    private fun updateDashDetails() {
        geoDeliveryView?.visibility = View.VISIBLE
        geoDeliveryText?.text =
            validateLocationResponse?.validatePlace?.onDemand?.storeName
                ?: getString(R.string.empty)
        var earliestDashDate =
            validateLocationResponse?.validatePlace?.onDemand?.firstAvailableFoodDeliveryTime
        if (earliestDashDate.isNullOrEmpty())
            earliestDashDate = getString(R.string.earliest_delivery_no_date_available)
        setVisibilityDeliveryDates(null, null, earliestDashDate)
    }

    private fun showNotDeliverablePopUp(
        @StringRes title: Int,
        @StringRes subTitle: Int,
        @StringRes btnText: Int,
        @DrawableRes imgUrl: Int,
        dismissLinkText: String?,
    ) {
        customBottomSheetDialogFragment =
            CustomBottomSheetDialogFragment.newInstance(getString(title),
                getString(subTitle),
                getString(btnText),
                imgUrl,
                dismissLinkText)
        customBottomSheetDialogFragment!!.show(requireFragmentManager(),
            CustomBottomSheetDialogFragment::class.java.simpleName)
    }

    private fun setVisibilityDeliveryDates(
        earliestFoodDate: String?,
        earliestFashionDate: String?,
        earliestDashDate: String?,
    ) {
        if (earliestFoodDate.isNullOrEmpty()) {
            earliestDeliveryDateLabel?.visibility = View.GONE
            earliestDeliveryDateValue?.visibility = View.GONE
        } else {
            earliestDeliveryDateLabel?.visibility = View.VISIBLE
            earliestDeliveryDateValue?.visibility = View.VISIBLE
            //earliestDeliveryDateValue?.text = WFormatter.getFullMonthWithDate(earliestFoodDate)
            earliestDeliveryDateValue?.text = earliestFoodDate
        }

        if (earliestFashionDate.isNullOrEmpty()) {
            earliestFashionDeliveryDateLabel?.visibility = View.GONE
            earliestFashionDeliveryDateValue?.visibility = View.GONE
        } else {
            earliestFashionDeliveryDateLabel?.visibility = View.VISIBLE
            earliestFashionDeliveryDateValue?.visibility = View.VISIBLE
            //earliestFashionDeliveryDateValue?.text = WFormatter.getFullMonthWithDate(earliestFashionDate)
            earliestFashionDeliveryDateValue?.text = earliestFashionDate
        }
        if (earliestDashDate.isNullOrEmpty()) {
            earliestDeliveryDashLabel?.visibility = View.GONE
            earliestDeliveryDashValue?.visibility = View.GONE
        } else {
            earliestDeliveryDashLabel?.visibility = View.VISIBLE
            earliestDeliveryDashValue?.visibility = View.VISIBLE
            earliestDeliveryDashValue?.text = earliestDashDate
        }
    }

    private fun setGeoDeliveryTextForCnc() {

        if (Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.storeName?.isNullOrEmpty() == true
            && KotlinUtils.getAnonymousUserLocationDetails()?.fulfillmentDetails?.storeName?.isNullOrEmpty() == true
        ) {
            whereToCollect()
            return
        } else if (SessionUtilities.getInstance().isUserAuthenticated) {
            if (store != null) {
                geoDeliveryText?.text = store?.storeName
                editDelivery?.text = bindString(R.string.edit)
                btnConfirmAddress?.isEnabled = true
                btnConfirmAddress?.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
                return
            }
            if (Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.storeName == null) {
                whereToCollect()
                return
            }
            Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.let {
                geoDeliveryText?.text = it.storeName
                editDelivery?.text = bindString(R.string.edit)
                btnConfirmAddress?.isEnabled = true
                btnConfirmAddress?.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
                return
            }
        } else {
            if (store != null) {
                geoDeliveryText?.text = store?.storeName
                editDelivery?.text = bindString(R.string.edit)
                btnConfirmAddress?.isEnabled = true
                btnConfirmAddress?.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
                return
            }

            if (KotlinUtils.getAnonymousUserLocationDetails()?.fulfillmentDetails == null) {
                whereToCollect()
                return
            }
            KotlinUtils.getAnonymousUserLocationDetails()?.fulfillmentDetails?.let {
                geoDeliveryText?.text = it.storeName
                editDelivery?.text = bindString(R.string.edit)
                btnConfirmAddress?.isEnabled = true
                btnConfirmAddress?.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
                return
            }
        }
    }

    private fun whereToCollect() {
        geoDeliveryText?.text =
            getNearestStore(validateLocationResponse?.validatePlace?.stores)
        mStoreId = getNearestStoreId(validateLocationResponse?.validatePlace?.stores)
        editDelivery?.text = bindString(R.string.edit)
        btnConfirmAddress?.isEnabled = true
        btnConfirmAddress?.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
    }

    private fun getNearestStore(stores: List<Store>?): String? {
        var shortestDistance: Store? = null
        if (!stores.isNullOrEmpty()) {
            shortestDistance = stores.minByOrNull {
                it.distance!!
            }
        }
        return shortestDistance?.storeName
    }

    private fun getNearestStoreId(stores: List<Store>?): String? {
        var shortestDistance: Store? = null
        if (!stores.isNullOrEmpty()) {
            shortestDistance = stores.minByOrNull {
                it.distance!!
            }
        }
        return shortestDistance?.storeId
    }

    /**
     * This function is to navigate to Unsellable Items screen.
     * @param [unSellableCommerceItems] list of items that are not deliverable in the selected location
     * @param [deliverable] boolean flag to determine if provided list of items are deliverable
     *
     * @see [Suburb]
     * @see [Province]
     * @see [UnSellableCommerceItem]
     */
    private fun navigateToUnsellableItemsFragment(
        unSellableCommerceItems: MutableList<UnSellableCommerceItem>,
    ) {
        findNavController()?.navigate(
            R.id.action_deliveryAddressConfirmationFragment_to_geoUnsellableItemsFragment,
            bundleOf(
                UnsellableItemsFragment.KEY_ARGS_BUNDLE to bundleOf(
                    UnsellableItemsFragment.KEY_ARGS_UNSELLABLE_COMMERCE_ITEMS to Utils.toJson(
                        unSellableCommerceItems),
                )
            )
        )
    }

    private fun isUnSellableItemsRemoved() {
        UnSellableItemsLiveData.observe(viewLifecycleOwner) {
            isUnSellableItemsRemoved = it
            if (isUnSellableItemsRemoved == true) {
                sendConfirmLocation()
                UnSellableItemsLiveData.value = false
            }
        }
    }

    private fun showErrorDialog() {
        geoDeliveryTab?.isEnabled = false
        geoCollectTab?.isEnabled = false
        requireActivity().resources?.apply {
            vtoErrorBottomSheetDialog.showErrorBottomSheetDialog(
                this@DeliveryAddressConfirmationFragment,
                requireActivity(),
                getString(R.string.vto_generic_error),
                "",
                getString(R.string.retry_label)
            )
        }
    }

    override fun tryAgain() {
        initView()
    }
}


