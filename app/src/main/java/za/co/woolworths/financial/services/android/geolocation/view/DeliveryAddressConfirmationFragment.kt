package za.co.woolworths.financial.services.android.geolocation.view


import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.GeoLocationDeliveryAddressBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.checkout.service.network.Address
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.checkout.view.CheckoutActivity
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressManagementBaseFragment
import za.co.woolworths.financial.services.android.checkout.view.CheckoutReturningUserCollectionFragment
import za.co.woolworths.financial.services.android.checkout.viewmodel.WhoIsCollectingDetails
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.DASH_SWITCH_DELIVERY_MODE
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.PropertyNames.Companion.BROWSE_MODE
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.PropertyNames.Companion.DELIVERY_MODE
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.geolocation.GeoUtils
import za.co.woolworths.financial.services.android.geolocation.model.request.ConfirmLocationRequest
import za.co.woolworths.financial.services.android.geolocation.model.response.ConfirmLocationAddress
import za.co.woolworths.financial.services.android.geolocation.network.model.Store
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidateLocationResponse
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.geolocation.viewmodel.UnSellableItemsLiveData
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.StorePickupInfoBody
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CartFragment
import za.co.woolworths.financial.services.android.ui.views.CustomBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.ui.views.UnsellableItemsBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.VtoErrorBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.listener.VtoTryAgainListener
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_OK
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_SESSION_TIMEOUT_440
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
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.saveAnonymousUserLocationDetails
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import za.co.woolworths.financial.services.android.viewmodels.ShoppingCartLiveData
import javax.inject.Inject
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.KEY_ADDRESS2
import za.co.woolworths.financial.services.android.util.analytics.AnalyticsManager

/**
 * Created by Kunal Uttarwar on 24/02/22.
 */
@AndroidEntryPoint
class DeliveryAddressConfirmationFragment : Fragment(R.layout.geo_location_delivery_address), View.OnClickListener, VtoTryAgainListener {

    private lateinit var binding: GeoLocationDeliveryAddressBinding
    private var isUnSellableItemsRemoved: Boolean? = false
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
    private  var address2:String?=null

    val confirmAddressViewModel: ConfirmAddressViewModel by activityViewModels()

    @Inject
    lateinit var vtoErrorBottomSheetDialog: VtoErrorBottomSheetDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = GeoLocationDeliveryAddressBinding.bind(view)
        binding.addFragmentListner()
        binding.moveToTabBeforeApiCalls(deliveryType)
        binding.initView()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateBundleValues()

        if (SessionUtilities.getInstance().isUserAuthenticated) {
            Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.apply {
                if (deliveryType == Delivery.CNC.type) {
                    //storeName will only be used in CnC flow. But storeId will be use in CnC or Dash.
                    mStoreName = this.storeName
                }
                mStoreId = this.storeId
            }
        } else {
            KotlinUtils.getAnonymousUserLocationDetails()?.fulfillmentDetails?.apply {
                if (deliveryType == Delivery.CNC.type) {
                    //storeName will only be used in CnC flow. But storeId will be use in CnC or Dash.
                    mStoreName = this.storeName
                }
                mStoreId = this.storeId
            }
        }
    }

    private fun updateBundleValues() {
        bundle = arguments?.getBundle(BUNDLE)

        bundle?.apply {
            latitude = getString(KEY_LATITUDE, "")
            longitude = this.getString(KEY_LONGITUDE, "")
            placeId = this.getString(KEY_PLACE_ID, "")
            address2=this.getString(KEY_ADDRESS2,"")
            isComingFromSlotSelection = this.getBoolean(IS_COMING_FROM_SLOT_SELECTION, false)
            isComingFromCheckout = this.getBoolean(IS_COMING_FROM_CHECKOUT, false)
            lastDeliveryType = this.getString(DELIVERY_TYPE, Delivery.STANDARD.name)
            //added this condition during the app Upgrade
            deliveryType = when (this.getString(DELIVERY_TYPE, Delivery.STANDARD.name)) {
                Delivery.STANDARD.name -> {
                    Delivery.STANDARD.name
                }
                Delivery.CNC.name -> {
                    Delivery.CNC.name
                }
                Delivery.DASH.name -> {
                    Delivery.DASH.name
                }
                else -> {
                    Delivery.STANDARD.name
                }
            }


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
                binding.sendConfirmLocation()
            }

            R.id.geoCollectTab -> {
                if (binding.progressBar.visibility == View.VISIBLE)
                    return
                else {
                    lastDeliveryType = deliveryType
                    binding.openCollectionTab()
                }
            }
            R.id.geoDeliveryTab -> {
                if (binding.progressBar.visibility == View.VISIBLE)
                    return
                else {
                    lastDeliveryType = deliveryType
                    binding.openGeoDeliveryTab()
                }
            }
            R.id.geoDashTab -> {
                if (binding.progressBar.visibility == View.VISIBLE)
                    return
                else {
                    lastDeliveryType = deliveryType
                    binding.openDashTab()
                }
            }
            R.id.btnRetry -> {
                binding.initView()
            }
        }
    }

    private fun setEventsForSwitchingDeliveryType(deliveryType: String) {
        val dashParams = bundleOf(
            DELIVERY_MODE to deliveryType,
            BROWSE_MODE to KotlinUtils.browsingDeliveryType?.name
        )
        AnalyticsManager.setUserProperty(DELIVERY_MODE, deliveryType)
        AnalyticsManager.setUserProperty(BROWSE_MODE, KotlinUtils.browsingDeliveryType?.type)
        AnalyticsManager.logEvent(DASH_SWITCH_DELIVERY_MODE, dashParams)
    }


    private fun GeoLocationDeliveryAddressBinding.moveToTabBeforeApiCalls(receivedDeliveryType: String?) {
        geoDeliveryView.visibility = View.GONE
        when (receivedDeliveryType) {
            Delivery.STANDARD.name -> {
                showDeliveryTabView()
            }
            Delivery.CNC.name -> {
                showCollectionTabView()
            }
            Delivery.DASH.name -> {
                showDashTabView()
            }
            else -> {
                showDeliveryTabView()
            }
        }
    }

    private fun moveToTab(receivedDeliveryType: String?) {
        when (receivedDeliveryType) {
            Delivery.STANDARD.name -> {
                binding.openGeoDeliveryTab()
            }
            Delivery.CNC.name -> {
                binding.openCollectionTab()
            }
            Delivery.DASH.name -> {
                binding.openDashTab()
            }
            else -> {
                binding.openGeoDeliveryTab()
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

    private fun GeoLocationDeliveryAddressBinding.addFragmentListner() {
        setFragmentResultListener(STORE_LOCATOR_REQUEST_CODE) { _, bundle ->
            store = bundle.get(BUNDLE) as Store
            store?.let {
                if (it.storeName != null) {
                    geoDeliveryText?.text = KotlinUtils.capitaliseFirstLetter(it.storeName)
                }
                editDelivery?.text = bindString(R.string.edit)
                btnConfirmAddress?.isEnabled = true
                btnConfirmAddress?.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
                mStoreName = it.storeName.toString()
                mStoreId = it.storeId.toString()
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
            updateBundleValues()
            val localBundle = bundle.getBundle(BUNDLE)
            localBundle.apply {
                latitude = this?.getString(KEY_LATITUDE, "")
                longitude = this?.getString(KEY_LONGITUDE, "")
                placeId = this?.getString(KEY_PLACE_ID, "")
                address2=this?.getString(KEY_ADDRESS2,"")
            }
            placeId?.let { getDeliveryDetailsFromValidateLocation(it, true) }
        }
        setFragmentResultListener(CustomBottomSheetDialogFragment.DIALOG_BUTTON_DISMISS_RESULT) { _, _ ->
            // change location dismiss button clicked so land back on last delivery location tab.
            moveToTab(lastDeliveryType)
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

    private fun GeoLocationDeliveryAddressBinding.sendConfirmLocation() {

        if (!confirmAddressViewModel.isConnectedToInternet(requireActivity())) {
            dashNoConnectionView.visibility = View.GONE
            geoDeliveryView.visibility = View.GONE
            connectionLayout.noConnectionLayout.visibility = View.VISIBLE
            return
        }

        var unSellableCommerceItems: MutableList<UnSellableCommerceItem>? = ArrayList()
        when (deliveryType) {
            Delivery.STANDARD.name -> {
                unSellableCommerceItems =
                    validateLocationResponse?.validatePlace?.unSellableCommerceItems
            }
            Delivery.CNC.name -> {
                validateLocationResponse?.validatePlace?.stores?.forEach {
                    if (it.storeId.equals(mStoreId)) {
                        unSellableCommerceItems = it.unSellableCommerceItems
                    }
                }
            }
            Delivery.DASH.name -> {
                unSellableCommerceItems =
                    validateLocationResponse?.validatePlace?.onDemand?.unSellableCommerceItems
            }
        }

        if (unSellableCommerceItems?.isNullOrEmpty() == false && isUnSellableItemsRemoved == false) {
            // show unsellable items
            unSellableCommerceItems?.let {
                navigateToUnsellableItemsFragment(it as ArrayList<UnSellableCommerceItem>)
            }

        } else {
            if (placeId == null) {
                return
            }
            val confirmLocationAddress = ConfirmLocationAddress(placeId,null,address2)
            val confirmLocationRequest = when (deliveryType) {
                Delivery.STANDARD.name -> {
                    mStoreId = ""
                    ConfirmLocationRequest(STANDARD, confirmLocationAddress, mStoreId)
                }
                Delivery.CNC.name -> {
                    ConfirmLocationRequest(CNC, confirmLocationAddress, mStoreId)
                }
                Delivery.DASH.name -> {
                    mStoreId = validateLocationResponse?.validatePlace?.onDemand?.storeId
                    ConfirmLocationRequest(DASH, confirmLocationAddress, mStoreId)
                }
                else -> {
                    ConfirmLocationRequest(STANDARD, confirmLocationAddress, "")
                }
            }
            setEventsForSwitchingDeliveryType(deliveryType ?: Delivery.STANDARD.name)

            viewLifecycleOwner.lifecycleScope.launch {
                progressBar.visibility = View.VISIBLE
                try {
                    val confirmLocationResponse =
                        confirmAddressViewModel.postConfirmAddress(confirmLocationRequest)
                    progressBar.visibility = View.GONE
                    if(!isAdded || !isVisible) return@launch
                    if (confirmLocationResponse != null) {
                        when (confirmLocationResponse.httpCode) {
                            HTTP_OK -> {
                                // save details in cache
                                if (SessionUtilities.getInstance().isUserAuthenticated) {
                                    val savedPlaceId =
                                        Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId
                                    KotlinUtils.let {
                                        it.placeId = placeId
                                        it.isLocationSame = placeId?.equals(savedPlaceId)

                                        if (it.isLocationSame == false) {
                                            KotlinUtils.isDeliveryLocationTabCrossClicked = false
                                            KotlinUtils.isCncTabCrossClicked = false
                                            KotlinUtils.isDashTabCrossClicked = false
                                            KotlinUtils.isStoreSelectedForBrowsing = false
                                        }
                                    }
                                    Utils.savePreferredDeliveryLocation(
                                        ShoppingDeliveryLocation(
                                            confirmLocationResponse.orderSummary?.fulfillmentDetails
                                        )
                                    )
                                    if (KotlinUtils.getAnonymousUserLocationDetails() != null)
                                        KotlinUtils.clearAnonymousUserLocationDetails()
                                } else {
                                    val anonymousUserPlaceId =
                                        KotlinUtils.getAnonymousUserLocationDetails()?.fulfillmentDetails?.address?.placeId
                                    KotlinUtils.let {
                                        it.placeId = placeId
                                        it.isLocationSame = placeId?.equals(anonymousUserPlaceId)
                                        if (it.isLocationSame == false) {
                                            KotlinUtils.isDeliveryLocationTabCrossClicked = false
                                            KotlinUtils.isCncTabCrossClicked = false
                                            KotlinUtils.isDashTabCrossClicked = false
                                            KotlinUtils.isStoreSelectedForBrowsing = false
                                        }
                                        saveAnonymousUserLocationDetails(ShoppingDeliveryLocation(
                                            confirmLocationResponse.orderSummary?.fulfillmentDetails))
                                    }
                                }

                                /*reset browsing data for cnc and dash both once fulfillment location is confirmed*/
                                WoolworthsApplication.setCncBrowsingValidatePlaceDetails(
                                    validateLocationResponse?.validatePlace)
                                WoolworthsApplication.setDashBrowsingValidatePlaceDetails(
                                    validateLocationResponse?.validatePlace)

                                if (KotlinUtils.isLocationSame == false && deliveryType != Delivery.CNC.name) {
                                    KotlinUtils.browsingCncStore = null
                                }

                                if (deliveryType == Delivery.CNC.name) {
                                    KotlinUtils.browsingCncStore =
                                        GeoUtils.getStoreDetails(mStoreId,
                                            validateLocationResponse?.validatePlace?.stores)
                                    KotlinUtils.isStoreSelectedForBrowsing = false
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
                                    if (deliveryType == Delivery.STANDARD.name || deliveryType == Delivery.DASH.name) {
                                        if (isComingFromSlotSelection) {
                                            /*Navigate to slot selection page with updated saved address*/
                                            val checkoutActivityIntent =
                                                Intent(activity,
                                                    CheckoutActivity::class.java
                                                )
                                            checkoutActivityIntent.putExtra(
                                                CheckoutAddressConfirmationFragment.SAVED_ADDRESS_KEY,
                                                savedAddressResponse
                                            )
                                            val result = when (deliveryType) {
                                                Delivery.STANDARD.name -> CheckoutAddressManagementBaseFragment.GEO_SLOT_SELECTION
                                                else -> CheckoutAddressManagementBaseFragment.DASH_SLOT_SELECTION
                                            }
                                            checkoutActivityIntent.putExtra(result, true)
                                            checkoutActivityIntent.putExtra(Constant.LIQUOR_ORDER, getLiquorOrder())
                                            checkoutActivityIntent.putExtra(Constant.NO_LIQUOR_IMAGE_URL, getLiquorImageUrl())
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
                } catch (e: Exception) {
                    progressBar.visibility = View.GONE
                    // navigate to shop tab with error scenario
                    activity?.setResult(REQUEST_CODE)
                    activity?.finish()
                }
            }
        }
    }

    private fun getLiquorOrder(): Boolean {
        var liquorOrder = false
        bundle?.apply {
            liquorOrder = getBoolean(Constant.LIQUOR_ORDER)
        }
        return liquorOrder
    }

    private fun getLiquorImageUrl(): String {
        var liquorImageUrl = ""
        bundle?.apply {
            liquorImageUrl = getString(Constant.NO_LIQUOR_IMAGE_URL, "")
        }
        return liquorImageUrl
    }

    private fun startCheckoutActivity(toJson: String) {
        val checkoutActivityIntent = Intent(activity, CheckoutActivity::class.java)
        checkoutActivityIntent.putExtra(
            CheckoutReturningUserCollectionFragment.KEY_COLLECTING_DETAILS,
            toJson
        )
        checkoutActivityIntent.putExtra(Constant.LIQUOR_ORDER, getLiquorOrder())
        checkoutActivityIntent.putExtra(Constant.NO_LIQUOR_IMAGE_URL, getLiquorImageUrl())
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
    }

    private fun GeoLocationDeliveryAddressBinding.initView() {
        imgDelBack.setOnClickListener(this@DeliveryAddressConfirmationFragment)
        editDelivery.setOnClickListener(this@DeliveryAddressConfirmationFragment)
        btnConfirmAddress.setOnClickListener(this@DeliveryAddressConfirmationFragment)
        geoDeliveryTab.setOnClickListener(this@DeliveryAddressConfirmationFragment)
        geoCollectTab.setOnClickListener(this@DeliveryAddressConfirmationFragment)
        geoDashTab.setOnClickListener(this@DeliveryAddressConfirmationFragment)
        geoDeliveryTab.isEnabled = true
        geoCollectTab.isEnabled = true
        geoDashTab.isEnabled = true
        isUnSellableItemsRemoved()
        placeId?.let {
            if (confirmAddressViewModel.isConnectedToInternet(requireActivity())) {
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(AppConstant.DELAY_300_MS)
                    getDeliveryDetailsFromValidateLocation(it, false)
                }
                dashNoConnectionView.visibility = View.VISIBLE
                connectionLayout.noConnectionLayout.visibility = View.GONE
            } else {
                dashNoConnectionView.visibility = View.GONE
                connectionLayout.noConnectionLayout.visibility = View.VISIBLE
            }
        }
        connectionLayout.btnRetry.setOnClickListener(this@DeliveryAddressConfirmationFragment)
    }

    private fun GeoLocationDeliveryAddressBinding.showDeliveryTabView() {
        selectATab(geoDeliveryTab)
        deliveryBagIcon.setImageDrawable(ContextCompat.getDrawable(requireActivity(),
            R.drawable.img_delivery_truck))
        changeFulfillmentTitleTextView.text = bindString(R.string.standard_delivery)
        changeFulfillmentSubTitleTextView.text = bindString(R.string.standard_title_text)
    }

    private fun GeoLocationDeliveryAddressBinding.showCollectionTabView() {
        selectATab(geoCollectTab)
        deliveryBagIcon.setImageDrawable(ContextCompat.getDrawable(requireActivity(),
            R.drawable.ic_cnc_set_location))
        changeFulfillmentTitleTextView.text = bindString(R.string.click_and_collect)
        val collectionQuantity =
            validateLocationResponse?.validatePlace?.stores?.getOrNull(0)?.quantityLimit?.foodMaximumQuantity
        changeFulfillmentSubTitleTextView.text =
            if (collectionQuantity != null) bindString(R.string.click_and_collect_title_text,
                collectionQuantity.toString()) else bindString(R.string.empty)
    }

    private fun GeoLocationDeliveryAddressBinding.showDashTabView() {
        selectATab(geoDashTab)
        deliveryBagIcon.setImageDrawable(ContextCompat.getDrawable(requireActivity(),
            R.drawable.img_dash_delivery))
        changeFulfillmentTitleTextView.text = bindString(R.string.dash_delivery)
        val deliveryFee =
            validateLocationResponse?.validatePlace?.onDemand?.deliveryTimeSlots?.getOrNull(0)?.slotCost
        val deliveryQuantity =
            validateLocationResponse?.validatePlace?.onDemand?.quantityLimit?.foodMaximumQuantity

        var titleText = if (deliveryFee != null) bindString(R.string.dash_title_text_1,
            deliveryFee.toString()) else ""
        titleText += if (deliveryQuantity != null) bindString(R.string.dash_title_text_2,
            deliveryQuantity.toString()) else ""
        changeFulfillmentSubTitleTextView.text = titleText
    }

    private fun GeoLocationDeliveryAddressBinding.openGeoDeliveryTab() {
        deliveryType = Delivery.STANDARD.name
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOP_DELIVERY,
            hashMapOf(FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_SHOP_DELIVERY),
            activity)

        selectATab(geoDeliveryTab)
        deliveryBagIcon.setImageDrawable(ContextCompat.getDrawable(requireActivity(),
            R.drawable.img_delivery_truck))
        btnConfirmAddress.isEnabled = true
        btnConfirmAddress.setBackgroundColor(ContextCompat.getColor(requireContext(),
            R.color.black))
        editDelivery.text = getString(R.string.edit)
        changeFulfillmentTitleTextView.text = bindString(R.string.standard_delivery)
        changeFulfillmentSubTitleTextView.text = bindString(R.string.standard_title_text)
        if (validateLocationResponse != null && validateLocationResponse?.validatePlace?.deliverable == false && progressBar.visibility == View.GONE) {
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

    private fun GeoLocationDeliveryAddressBinding.openCollectionTab() {
        deliveryType = Delivery.CNC.name
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOP_CLICK_COLLECT,
            hashMapOf(FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_SHOP_CLICK_COLLECT),
            activity)
        selectATab(geoCollectTab)
        deliveryBagIcon.setImageDrawable(ContextCompat.getDrawable(requireActivity(),
            R.drawable.ic_cnc_set_location))
        changeFulfillmentTitleTextView.text = bindString(R.string.click_and_collect)
        val collectionQuantity =
            validateLocationResponse?.validatePlace?.stores?.getOrNull(0)?.quantityLimit?.foodMaximumQuantity
        changeFulfillmentSubTitleTextView.text =
            if (collectionQuantity != null) bindString(R.string.click_and_collect_title_text,
                collectionQuantity.toString()) else bindString(R.string.empty)
        validateLocationResponse?.validatePlace?.apply {
            if ((this.stores?.isEmpty() == true || this.stores?.getOrNull(0)?.deliverable == false) && progressBar?.visibility == View.GONE) {
                // Show no store available Bottom Dialog.
                showNotDeliverablePopUp(R.string.no_location_collection,
                    R.string.no_location_desc,
                    R.string.change_location,
                    R.drawable.ic_cnc_set_location,
                    null)
            } else {
                if (customBottomSheetDialogFragment != null && customBottomSheetDialogFragment!!.isVisible) {
                    customBottomSheetDialogFragment?.dismiss()
                }
            }
        }
        updateCollectionDetails()
    }

    private fun GeoLocationDeliveryAddressBinding.openDashTab() {
        deliveryType = Delivery.DASH.name
        selectATab(geoDashTab)
        deliveryBagIcon.setImageDrawable(ContextCompat.getDrawable(requireActivity(),
            R.drawable.img_dash_delivery))
        changeFulfillmentTitleTextView.text = bindString(R.string.dash_delivery)
        val deliveryFee =
            validateLocationResponse?.validatePlace?.onDemand?.deliveryTimeSlots?.getOrNull(0)?.slotCost
        val deliveryQuantity =
            validateLocationResponse?.validatePlace?.onDemand?.quantityLimit?.foodMaximumQuantity

        var titleText = if (deliveryFee != null) bindString(R.string.dash_title_text_1,
            deliveryFee.toString()) else ""
        titleText += if (deliveryQuantity != null) bindString(R.string.dash_title_text_2,
            deliveryQuantity.toString()) else ""
        changeFulfillmentSubTitleTextView.text = titleText

        val dashDeliverable = validateLocationResponse?.validatePlace?.onDemand?.deliverable
        if (validateLocationResponse != null && (dashDeliverable == null || dashDeliverable == false) && binding.progressBar.visibility == View.GONE) {
            // Show not deliverable Popup
            showNotDeliverablePopUp(R.string.no_location_title,
                R.string.no_location_desc,
                R.string.change_location,
                R.drawable.location_disabled,
                null)
        } else {
            if (customBottomSheetDialogFragment != null && customBottomSheetDialogFragment!!.isVisible) {
                customBottomSheetDialogFragment?.dismiss()
            }
        }
        updateDashDetails()
    }

    private fun GeoLocationDeliveryAddressBinding.selectATab(selectedTab: AppCompatTextView?) {
        selectedTab?.setBackgroundResource(R.drawable.bg_geo_selected_tab)
        val myRiadSemiBoldFont =
            Typeface.createFromAsset(activity?.assets, "fonts/MyriadPro-Semibold.otf")
        selectedTab?.typeface = myRiadSemiBoldFont
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

    private fun unSelectATab(unSelectedTab: AppCompatTextView?) {
        unSelectedTab?.apply {
            val myriadProRegularFont =
                Typeface.createFromAsset(activity?.assets, "fonts/MyriadPro-Regular.otf")
            typeface = myriadProRegularFont
            setBackgroundResource(R.drawable.bg_geo_unselected_tab)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.color_444444))
        }
    }

    private fun getDeliveryDetailsFromValidateLocation(placeId: String, isNewLocation: Boolean) {
        val oldPlaceId = validateLocationResponse?.validatePlace?.placeDetails?.placeId
        if (placeId.isNullOrEmpty() || (oldPlaceId != null && oldPlaceId == placeId)) {
            moveToTab(deliveryType)
            return
        }

        binding.progressBar?.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            try {
                validateLocationResponse = confirmAddressViewModel.getValidateLocation(placeId)
                binding.progressBar?.visibility = View.GONE
                if (validateLocationResponse != null) {
                    when (validateLocationResponse?.httpCode) {
                        HTTP_OK -> {
                            if (isNewLocation || (mStoreName.isNullOrEmpty() || mStoreId.isNullOrEmpty())) {
                                mStoreName =
                                    getNearestStore(validateLocationResponse?.validatePlace?.stores)
                                mStoreId =
                                    getNearestStoreId(validateLocationResponse?.validatePlace?.stores)
                            }
                            moveToTab(deliveryType)
                        }
                        else -> {
                            if(!isAdded && !isVisible) return@launch
                            binding.showErrorDialog()

                        }
                    }
                }
            } catch (e: Exception) {
                FirebaseManager.logException(e)
                if(!isAdded && !isVisible) return@launch
                binding.progressBar.visibility = View.GONE
                binding.showErrorDialog()
            }
        }
    }

    private fun GeoLocationDeliveryAddressBinding.updateDeliveryDetails() {
        geoDeliveryText?.text =
            KotlinUtils.capitaliseFirstLetter(validateLocationResponse?.validatePlace?.placeDetails?.address1
                ?: getString(R.string.empty))

        var earliestFoodDate =
            validateLocationResponse?.validatePlace?.firstAvailableFoodDeliveryDate
        if (earliestFoodDate.isNullOrEmpty())
            earliestFoodDate = getString(R.string.earliest_delivery_no_date_available)

        var earliestFashionDate =
            validateLocationResponse?.validatePlace?.firstAvailableOtherDeliveryDate
        if (earliestFashionDate.isNullOrEmpty())
            earliestFashionDate = getString(R.string.earliest_delivery_no_date_available)
        geoDeliveryView.visibility = View.VISIBLE
        setVisibilityDeliveryDates(earliestFoodDate, earliestFashionDate, null)
    }

    private fun GeoLocationDeliveryAddressBinding.updateCollectionDetails() {
        binding.setGeoDeliveryTextForCnc()

        var earliestFoodDate =
            validateLocationResponse?.validatePlace?.firstAvailableFoodDeliveryDate
        if (earliestFoodDate.isNullOrEmpty())
            earliestFoodDate = getString(R.string.earliest_delivery_no_date_available)
         geoDeliveryView.visibility = View.VISIBLE
        // Use dash labels
        earliestDeliveryDashLabel.text = requireContext().getString(R.string.earliest_collection_Date)
        binding.setVisibilityDeliveryDates(null, null, earliestFoodDate)
    }

    private fun GeoLocationDeliveryAddressBinding.updateDashDetails() {
        geoDeliveryText.text =
            KotlinUtils.capitaliseFirstLetter(validateLocationResponse?.validatePlace?.placeDetails?.address1
                ?: getString(R.string.empty))
        var earliestDashDate =
            validateLocationResponse?.validatePlace?.onDemand?.firstAvailableFoodDeliveryTime
        if (earliestDashDate.isNullOrEmpty())
            earliestDashDate = getString(R.string.earliest_delivery_no_date_available)
        geoDeliveryView?.visibility = View.VISIBLE
        earliestDeliveryDashLabel?.text = requireContext().getString(R.string.earliest_dash_delivery_timeslot)
        setVisibilityDeliveryDates(null, null, earliestDashDate)
    }

    private fun showNotDeliverablePopUp(
        @StringRes title: Int,
        @StringRes subTitle: Int,
        @StringRes btnText: Int,
        @DrawableRes imgUrl: Int,
        dismissLinkText: String?,
    ) {
        if (customBottomSheetDialogFragment != null && customBottomSheetDialogFragment!!.isVisible) {
            customBottomSheetDialogFragment!!.dismiss()
        }
        customBottomSheetDialogFragment =
            CustomBottomSheetDialogFragment.newInstance(getString(title),
                getString(subTitle),
                getString(btnText),
                imgUrl,
                dismissLinkText)
        customBottomSheetDialogFragment!!.show(requireFragmentManager(),
            CustomBottomSheetDialogFragment::class.java.simpleName)
    }

    private fun GeoLocationDeliveryAddressBinding.setVisibilityDeliveryDates(
        earliestFoodDate: String?,
        earliestFashionDate: String?,
        earliestDashDate: String?,
    ) {
        if (earliestFoodDate.isNullOrEmpty()) {
            earliestDeliveryDateLabel.visibility = View.GONE
            earliestDeliveryDateValue.visibility = View.GONE
        } else {
            earliestDeliveryDateLabel.visibility = View.VISIBLE
            earliestDeliveryDateValue.visibility = View.VISIBLE
            //earliestDeliveryDateValue?.text = WFormatter.getFullMonthWithDate(earliestFoodDate)
            earliestDeliveryDateValue.text = earliestFoodDate
        }

        if (earliestFashionDate.isNullOrEmpty()) {
            earliestFashionDeliveryDateLabel.visibility = View.INVISIBLE
            earliestFashionDeliveryDateValue.visibility = View.INVISIBLE
        } else {
            earliestFashionDeliveryDateLabel.visibility = View.VISIBLE
            earliestFashionDeliveryDateValue.visibility = View.VISIBLE
            //earliestFashionDeliveryDateValue?.text = WFormatter.getFullMonthWithDate(earliestFashionDate)
            earliestFashionDeliveryDateValue.text = earliestFashionDate
        }
        if (earliestDashDate.isNullOrEmpty()) {
            earliestDeliveryDashLabel.visibility = View.GONE
            earliestDeliveryDashValue.visibility = View.GONE
        } else {
            earliestDeliveryDashLabel.visibility = View.VISIBLE
            earliestDeliveryDashValue.visibility = View.VISIBLE
            earliestDeliveryDashValue.text = earliestDashDate
        }
    }

    private fun GeoLocationDeliveryAddressBinding.setGeoDeliveryTextForCnc() {
        val store = GeoUtils.getStoreDetails(
                mStoreId,
                validateLocationResponse?.validatePlace?.stores
        )
        geoDeliveryText.text = KotlinUtils.capitaliseFirstLetter(mStoreName)
        editDelivery.text = bindString(R.string.edit)
        btnConfirmAddress.isEnabled = true
        btnConfirmAddress.setBackgroundColor(
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
     *
     * @see [Suburb]
     * @see [Province]
     * @see [UnSellableCommerceItem]
     */
    private fun navigateToUnsellableItemsFragment(
        unSellableCommerceItems: ArrayList<UnSellableCommerceItem>,
    ) {
        deliveryType?.let {
            val unsellableItemsBottomSheetDialog =
                UnsellableItemsBottomSheetDialog.newInstance(unSellableCommerceItems, it)
            unsellableItemsBottomSheetDialog.show(requireFragmentManager(),
                UnsellableItemsBottomSheetDialog::class.java.simpleName)
        }
    }

    private fun isUnSellableItemsRemoved() {
        UnSellableItemsLiveData.observe(viewLifecycleOwner) {
            isUnSellableItemsRemoved = it
            if (isUnSellableItemsRemoved == true) {
                binding.sendConfirmLocation()
                loadShoppingCart()
                UnSellableItemsLiveData.value = false
            }
        }
    }

    private fun loadShoppingCart() {
        val shoppingCartResponseCall = OneAppService.getShoppingCart()
        shoppingCartResponseCall.enqueue(
                CompletionHandler(
                        (object : IResponseListener<ShoppingCartResponse> {
                            override fun onSuccess(response: ShoppingCartResponse?) {
                                try {
                                    when (response?.httpCode) {
                                        HTTP_OK -> {
                                            val isNoLiquorOrder = response.data[0].liquorOrder
                                            if(isNoLiquorOrder == false)
                                                ShoppingCartLiveData.value = isNoLiquorOrder
                                        }
                                        HTTP_SESSION_TIMEOUT_440 -> {
                                            SessionUtilities.getInstance()
                                                    .setSessionState(SessionDao.SESSION_STATE.INACTIVE)
                                            SessionExpiredUtilities.getInstance().showSessionExpireDialog(
                                                    requireActivity() as AppCompatActivity?,
                                                    this@DeliveryAddressConfirmationFragment
                                            )
                                        }
                                        else -> {
                                            response?.response?.let {
                                                Utils.displayValidationMessage(
                                                        requireActivity(),
                                                        CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                                                        it.desc,
                                                        true
                                                )
                                            }
                                        }
                                    }
                                } catch (ex: Exception) {
                                    FirebaseManager.logException(ex)
                                }
                            }

                            override fun onFailure(error: Throwable?) {

                            }
                        }), ShoppingCartResponse::class.java
                )
        )
    }

    private fun GeoLocationDeliveryAddressBinding.showErrorDialog() {
        if(!isAdded && !isVisible) return
        geoDeliveryTab?.isEnabled = false
        geoCollectTab?.isEnabled = false
        requireActivity().resources?.apply {
            vtoErrorBottomSheetDialog?.showErrorBottomSheetDialog(
                this@DeliveryAddressConfirmationFragment,
                requireActivity(),
                getString(R.string.vto_generic_error),
                "",
                getString(R.string.retry_label)
            )
        }
    }

    override fun tryAgain() {
        binding.initView()
    }
}


