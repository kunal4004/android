package za.co.woolworths.financial.services.android.geolocation.view

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.GeolocationConfirmAddressBinding
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.maps.GeoApiContext
import com.google.maps.GeocodingApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.checkout.view.adapter.GooglePlacesAdapter
import za.co.woolworths.financial.services.android.checkout.view.adapter.PlaceAutocomplete
import za.co.woolworths.financial.services.android.checkout.viewmodel.AddressComponentEnum.ROUTE
import za.co.woolworths.financial.services.android.checkout.viewmodel.AddressComponentEnum.STREET_NUMBER
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.geolocation.LocationProviderBroadcastReceiver
import za.co.woolworths.financial.services.android.geolocation.model.request.ConfirmLocationRequest
import za.co.woolworths.financial.services.android.geolocation.model.request.SaveAddressLocationRequest
import za.co.woolworths.financial.services.android.geolocation.model.response.ConfirmLocationAddress
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidateLocationResponse
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidatePlace
import za.co.woolworths.financial.services.android.geolocation.view.DeliveryAddressConfirmationFragment.Companion.MAP_LOCATION_RESULT
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation
import za.co.woolworths.financial.services.android.ui.extension.afterTextChanged
import za.co.woolworths.financial.services.android.ui.fragments.poi.PoiBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.views.CustomBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.EnableLocationSettingsFragment
import za.co.woolworths.financial.services.android.ui.views.maps.DynamicMapDelegate
import za.co.woolworths.financial.services.android.ui.views.maps.model.DynamicMapMarker
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.VtoErrorBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.listener.VtoTryAgainListener
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.DELAY_500_MS
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_OK
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.BUNDLE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_CONFIRM_ADD
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_FROM_NEW_TOGGLE_FULFILMENT_SCREEN
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.KEY_ADDRESS2
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.KEY_LATITUDE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.KEY_LONGITUDE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.KEY_PLACE_ID
import za.co.woolworths.financial.services.android.util.Constant.Companion.POI
import za.co.woolworths.financial.services.android.util.KeyboardUtils.Companion.hideKeyboard
import za.co.woolworths.financial.services.android.util.LocalConstant.Companion.DEFAULT_LATITUDE
import za.co.woolworths.financial.services.android.util.LocalConstant.Companion.DEFAULT_LONGITUDE
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.location.DynamicGeocoder
import za.co.woolworths.financial.services.android.util.location.Event
import za.co.woolworths.financial.services.android.util.location.EventType
import za.co.woolworths.financial.services.android.util.location.Locator
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import javax.inject.Inject

@AndroidEntryPoint
class ConfirmAddressMapFragment :
    Fragment(R.layout.geolocation_confirm_address), DynamicMapDelegate, VtoTryAgainListener,
    LocationProviderBroadcastReceiver.LocationProviderInterface,
    PoiBottomSheetDialog.ClickListener, UnIndexedAddressIdentifiedListener {

    private lateinit var validateLocationResponse: ValidateLocationResponse
    private lateinit var binding: GeolocationConfirmAddressBinding
    private var mAddress: String? = null
    private var placeId: String? = null
    private var deliveryType: String? = null
    private var mLatitude: String? = null
    private var mLongitude: String? = null
    private var address1: String? = null
    private var city: String? = null
    private var state: String? = null
    private var country: String? = null
    private var postalCode: String? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var suburb: String? = null
    private var isAddAddress: Boolean? = false
    private var isComingFromCheckout: Boolean? = false
    private var isFromDashTab: Boolean? = false
    private var isAddressFromSearch: Boolean = false
    private var isMoveMapCameraFirstTime: Boolean? = true
    private var isAddressSearch: Boolean? = false
    private var unIndexedAddressIdentified: Boolean? = false
    private var unIndexedBottomSheetDialog: PoiBottomSheetDialog? = null
    private var poiBottomSheetDialog: PoiBottomSheetDialog? = null
    private var newDeliveryType: String? = null

    val confirmAddressViewModel: ConfirmAddressViewModel by activityViewModels()

    @Inject
    lateinit var vtoErrorBottomSheetDialog: VtoErrorBottomSheetDialog

    @Inject
    lateinit var connectivityLiveData: ConnectivityLiveData

    private var placeName: String? = null
    private var isStreetNumberAndRouteFromSearch: Boolean? = false
    private var isPoiAddress: Boolean? = false
    private var address2: String? = ""
    private lateinit var locator: Locator
    private lateinit var locationBroadcastReceiver : LocationProviderBroadcastReceiver
    private val unIndexedLiveData = MutableLiveData<Boolean>()
    private var isComingFromNewToggleFulfilment: Boolean? = false
    private var isLocationUpdateRequest: Boolean? = false
    override fun onViewCreated(
        view: View, savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding = GeolocationConfirmAddressBinding.bind(view)
        binding.dynamicMapView?.initializeMap(savedInstanceState, this)
        locator = Locator(activity as AppCompatActivity)
        locationBroadcastReceiver = LocationProviderBroadcastReceiver()
        locationBroadcastReceiver.registerCallback(this)
    }

    private fun initView() {
        if (!isAdded || !isVisible) return

        val bundle = arguments ?: return

        val args = ConfirmAddressMapFragmentArgs.fromBundle(bundle)
        latitude = args.mapData.latitude
        longitude = args.mapData.longitude
        isAddAddress = args.mapData.isAddAddress
        isComingFromCheckout = args.mapData.isComingFromCheckout
        isFromDashTab = args.mapData.isFromDashTab
        deliveryType = args.mapData.deliveryType
        isComingFromNewToggleFulfilment = args.mapData.isFromNewFulfilmentScreen
        isLocationUpdateRequest = args.mapData.isLocationUpdateRequest
        newDeliveryType = args.mapData.newDeliveryType
        clearAddress()
        confirmAddressClick()
        onNavigationMapArrowClicked()
        addFragmentListner()
        turnLocationSettingsOn()
        cancelClick()
        if (confirmAddressViewModel.isConnectedToInternet(requireActivity())) {
            initMap()
            binding.dynamicMapView?.setAllGesturesEnabled(true)
        } else {
            binding?.apply {
                autoCompleteTextView.isEnabled = false
                dynamicMapView?.setAllGesturesEnabled(false)
                showErrorDialog()
            }
        }

        addUnIndexedIdentifiedListener()
    }

    private fun onNavigationMapArrowClicked() {
        binding?.navigationMapArrow?.setOnClickListener {
            startLocationDiscoveryProcess()
        }
    }

    private fun navigateCurrentLocation() {
        Utils.getLastSavedLocation()?.let {
            moveMapCamera(it.latitude, it.longitude)
        }
    }

    private fun showErrorDialog() {
        if (!isAdded || !isVisible) return

        requireActivity().resources?.apply {
            vtoErrorBottomSheetDialog.showErrorBottomSheetDialog(
                this@ConfirmAddressMapFragment,
                requireActivity(),
                getString(R.string.vto_generic_error),
                "",
                getString(R.string.retry_label)
            )
        }
    }

    private fun addFragmentListner() {
        if (!isAdded || !isVisible) return

        setFragmentResultListener(CustomBottomSheetDialogFragment.DIALOG_BUTTON_CLICK_RESULT) { _, _ ->
            // change location button clicked as address is not deliverable.
            initView()
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                delay(AppConstant.DELAY_1500_MS)
                clearAddressText()
                clearMapDetails()
            }
        }
    }

    private fun checkNetwork() {
        activity?.let {
            connectivityLiveData.observe(viewLifecycleOwner) { isNetworkAvailable ->
                if (!isAdded || !isVisible) return@observe

                binding?.apply {
                    if (isNetworkAvailable) {
                        autoCompleteTextView.isEnabled = true
                        if(noLocationLayout?.noLocationRootLayout?.visibility == View.VISIBLE) {
                            return@apply
                        }
                        dynamicMapView?.visibility = View.VISIBLE
                        mapFrameLayout.visibility = View.VISIBLE
                        dynamicMapView?.setAllGesturesEnabled(true)
                        if (isAddAddress!! && isAddressSearch == false) {
                            confirmAddress.isEnabled = false
                            imgMapMarker.visibility = View.GONE
                            tvMarkerHint?.visibility = View.GONE
                        }
                    } else {
                        autoCompleteTextView.isEnabled = false
                        confirmAddress.isEnabled = false
                        dynamicMapView?.setAllGesturesEnabled(false)
                        showErrorDialog()
                    }
                }
            }
        }
    }

    private fun clearAddress() {
        binding?.apply {
            imgRemoveAddress.setOnClickListener {
                clearAddressText()
            }
        }
    }

    private fun cancelClick() {
        binding.cancelText?.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun turnLocationSettingsOn() {
        binding.apply {
            noLocationLayout?.turnOnSubTitle?.setOnClickListener {
                KotlinUtils.openAccessMyLocationDeviceSettings(
                    EnableLocationSettingsFragment.ACCESS_MY_LOCATION_REQUEST_CODE,
                    activity
                )
            }
        }
    }

    private fun clearAddressText() {
        binding.autoCompleteTextView.setText("")
        binding.tvLocationNikName.text = ""
        binding?.confirmAddress?.isEnabled = false
    }

    private fun clearMapDetails() {
        mLatitude = null
        mLongitude = null
        binding?.confirmAddress?.isEnabled = false
    }

    private fun confirmAddressClick() {

        binding?.confirmAddress?.setOnClickListener {

            Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.SHOP_CONFIRM_ADDRESS,
                hashMapOf(
                    FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                            FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_SHOP_CONFIRM_ADDRESS
                ),
                activity
            )

            // first we will call validate Location API after successful we will decide if it is deliverable for that delivery Type.
            validateLocation()
        }
    }

    private fun validateLocation() {
        if (placeId.isNullOrEmpty())
            return

        // Make Validate Location Call
        lifecycleScope.launch {
            binding?.progressBar?.visibility = View.VISIBLE
            try {
                 validateLocationResponse =
                    confirmAddressViewModel.getValidateLocation(placeId!!)
                binding?.progressBar?.visibility = View.GONE
                if (!isAdded || !isVisible) return@launch

                if (validateLocationResponse != null) {
                    when (validateLocationResponse?.httpCode) {
                        HTTP_OK -> {
                            validateLocationResponse.validatePlace?.let { place ->

                                if (isFromDashTab == true) {
                                    if (place.onDemand != null && place.onDemand!!.deliverable == true) {

                                        if (KotlinUtils.getDeliveryType() == null) {
                                            // User don't have any location (signin or signout both) that's why we are setting new location.
                                            confirmSetAddress(validateLocationResponse)
                                        } else {
                                            // User has location. Means only changing browsing location.
                                            // directly go back to Dash landing screen. Don't call confirm location API as user only wants to browse Dash.
                                            val intent = Intent()
                                            intent.putExtra(
                                                BundleKeysConstants.VALIDATE_RESPONSE,
                                                validateLocationResponse
                                            )
                                            activity?.setResult(Activity.RESULT_OK, intent)
                                            activity?.finish()
                                        }
                                    } else {
                                        // Show not deliverable Bottom Dialog.
                                        showChangeLocationDialog()
                                    }
                                    return@let
                                } else if (KotlinUtils.isComingFromCncTab == true) {
                                    /*user is coming from CNC i.e. set Location flow */
                                    // navigate to CNC home tab.
                                    KotlinUtils.isComingFromCncTab = false

                                    /* set cnc browsing data */
                                    WoolworthsApplication.setCncBrowsingValidatePlaceDetails(
                                        validateLocationResponse?.validatePlace
                                    )
                                    activity?.finish()
                                } else if ((isComingFromNewToggleFulfilment == true && !newDeliveryType.isNullOrEmpty()) || (isLocationUpdateRequest == true)) {
                                    // New user journey flow from the new toggle fulfilment screen when there is no place id available initially
                                    validateDeliverableAndNavigate(place)
                                    return@let
                                }

                                when (deliveryType) {
                                    // As per delivery type first we will verify if it is deliverable for that or not.
                                    Delivery.STANDARD.name -> {
                                        if (place.deliverable == true) {
                                            navigateToLastScreen()
                                        } else
                                            showChangeLocationDialog()
                                    }
                                    Delivery.CNC.name -> {
                                        if (place.stores?.getOrNull(0)?.deliverable == true) {
                                            navigateToLastScreen()
                                        } else
                                            showNoCollectionStores()
                                    }
                                    Delivery.DASH.name -> {
                                        if (place.onDemand?.deliverable == true) {
                                            navigateToLastScreen()
                                        } else
                                            showChangeLocationDialog()
                                    }
                                    else -> {
                                        // This happens when there is no location. So delivery type might be null.
                                        if (place.deliverable == true) {
                                            navigateToLastScreen()
                                        } else
                                            showChangeLocationDialog()
                                    }
                                }
                            }
                        }
                        else -> {
                            showErrorDialog()
                        }
                    }
                }
            } catch (e: Exception) {
                if (!isAdded || !isVisible) return@launch

                FirebaseManager.logException(e)
                binding?.progressBar?.visibility = View.GONE
                showErrorDialog()
            }
        }
    }

    private fun validateDeliverableAndNavigate(place: ValidatePlace) {
        when (newDeliveryType) {
            // As per delivery type first we will verify if it is deliverable for that or not.
            Delivery.STANDARD.type -> {
                if (place.deliverable == true) {
                    confirmSetAddress(validateLocationResponse)
                } else {
                    showChangeLocationDialog()
                }
            }

            Delivery.CNC.type -> {
                if (place.stores?.getOrNull(0)?.deliverable == true) {
                    launchStores()
                } else {
                    showNoCollectionStores()
                }
            }

            Delivery.DASH.type -> {
                if (place.onDemand?.deliverable == true) {
                    confirmSetAddress(validateLocationResponse)
                } else {
                    showChangeLocationDialog()
                }
            }
        }
    }

    private fun launchStores() {
        val bundle = Bundle()
        bundle.apply {
            putString(KEY_PLACE_ID, placeId)
            putString(BundleKeysConstants.NEW_DELIVERY_TYPE, newDeliveryType)
            putBoolean(
                IS_COMING_FROM_NEW_TOGGLE_FULFILMENT_SCREEN,
                isComingFromNewToggleFulfilment!!
            )
            putSerializable(
                BundleKeysConstants.VALIDATE_RESPONSE, validateLocationResponse
            )
        }
        findNavController().navigate(
            R.id.actionClickAndCollectStoresFragment,
            bundleOf(BUNDLE to bundle)
        )
    }

    @SuppressLint("RestrictedApi")
    private fun navigateToLastScreen() {
        if (isComingFromCheckout == true) {
            val bundle = Bundle()
            bundle.apply {
                putString(
                    KEY_PLACE_ID, placeId
                )
                putBoolean(
                    IS_COMING_CONFIRM_ADD, true
                )
                findNavController().navigate(
                    R.id.actionClickAndCollectStoresFragment,
                    bundleOf(BUNDLE to bundle)
                )
            }
        }
        // normal geo flow
        else if (mLatitude != null && mLongitude != null && placeId != null) {
            val bundle = Bundle()
            bundle.apply {
                putString(KEY_LATITUDE, mLatitude)
                putString(KEY_LONGITUDE, mLongitude)
                putString(KEY_PLACE_ID, placeId)
                putString(
                    BundleKeysConstants.DELIVERY_TYPE,
                    deliveryType
                )
                if (!address2.isNullOrEmpty()) {
                    putString(
                        KEY_ADDRESS2, address2
                    )
                }
            }
            if(deliveryType==Delivery.CNC.name){
                bundle.apply {
                    putSerializable(
                    BundleKeysConstants.VALIDATE_RESPONSE, validateLocationResponse)
                }
                findNavController().navigate(
                    R.id.actionClickAndCollectStoresFragment,
                    bundleOf(BUNDLE to bundle)
                )
                return
            }

            findNavController().navigateUp() // This will land on confirmAddress fragment.
            if (findNavController().graph.startDestination != findNavController().currentDestination?.id) {
                // if it's not a first time user then it must have change fulfillment screen in nav graph. so again navigateUp() will land on changeFulfillment screen.
                findNavController().navigateUp()
                setFragmentResult(MAP_LOCATION_RESULT, bundleOf(BUNDLE to bundle))
            } else {
                // directly go to change fulfillment screen.
                findNavController().navigate(
                    R.id.actionToDeliveryAddressConfirmationFragment,
                    bundleOf(BUNDLE to bundle)
                )
            }
        }
    }

    private fun showChangeLocationDialog() {
        val customBottomSheetDialogFragment =
            CustomBottomSheetDialogFragment.newInstance(
                getString(R.string.no_location_title),
                getString(R.string.no_location_desc),
                getString(R.string.change_location),
                R.drawable.location_disabled, getString(R.string.dismiss)
            )
        customBottomSheetDialogFragment.show(
            requireFragmentManager(),
            CustomBottomSheetDialogFragment::class.java.simpleName
        )
    }

    private fun showNoCollectionStores() {
        // Show no store available Bottom Dialog.
        val customBottomSheetDialogFragment =
            CustomBottomSheetDialogFragment.newInstance(
                getString(R.string.no_location_collection),
                getString(R.string.no_location_desc),
                getString(R.string.change_location),
                R.drawable.img_collection_bag,
                resources.getString(R.string.cancel_underline_html)
            )
        customBottomSheetDialogFragment.show(
            requireFragmentManager(),
            CustomBottomSheetDialogFragment::class.java.simpleName
        )
    }

    private fun getConfirmAddressRequest(delivery: String?): ConfirmLocationRequest {
        val confirmLocationAddress = ConfirmLocationAddress(placeId, null, address2)
        return if (delivery == Delivery.DASH.type) {
            val storeId = validateLocationResponse.validatePlace?.onDemand?.storeId
            ConfirmLocationRequest(BundleKeysConstants.DASH, confirmLocationAddress, storeId)
        } else {
            ConfirmLocationRequest(BundleKeysConstants.STANDARD, confirmLocationAddress, "")
        }
    }

    private fun confirmSetAddress(validateLocationResponse: ValidateLocationResponse) {
        if (placeId.isNullOrEmpty())
            return

        //make confirm Location call
        val confirmLocationAddress = ConfirmLocationAddress(placeId)
        val confirmLocationRequest =
        if ((isComingFromNewToggleFulfilment == true && !newDeliveryType.isNullOrEmpty()) || (isLocationUpdateRequest == true)) {
            getConfirmAddressRequest(newDeliveryType)
        } else {
            ConfirmLocationRequest(
                BundleKeysConstants.DASH,
                confirmLocationAddress,
                validateLocationResponse.validatePlace?.onDemand?.storeId
            )
        }

        lifecycleScope.launch {
            binding?.progressBar?.visibility = View.VISIBLE
            try {
                val confirmLocationResponse =
                    confirmAddressViewModel.postConfirmAddress(confirmLocationRequest)
                binding?.progressBar?.visibility = View.GONE
                if (!isAdded || !isVisible) return@launch

                if (confirmLocationResponse != null) {
                    when (confirmLocationResponse.httpCode) {
                        HTTP_OK -> {

                            /*reset browsing data for cnc and dash both once fulfillment location is confirmed*/
                            WoolworthsApplication.setCncBrowsingValidatePlaceDetails(
                                validateLocationResponse?.validatePlace
                            )
                            WoolworthsApplication.setDashBrowsingValidatePlaceDetails(
                                validateLocationResponse?.validatePlace
                            )

                            KotlinUtils.placeId = placeId
                            KotlinUtils.isLocationPlaceIdSame =
                                placeId?.equals(Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId)

                            WoolworthsApplication.setValidatedSuburbProducts(
                                validateLocationResponse.validatePlace
                            )

                            // save details in cache
                            if (SessionUtilities.getInstance().isUserAuthenticated) {
                                Utils.savePreferredDeliveryLocation(
                                    ShoppingDeliveryLocation(
                                        confirmLocationResponse.orderSummary?.fulfillmentDetails
                                    )
                                )
                                if (KotlinUtils.getAnonymousUserLocationDetails() != null)
                                    KotlinUtils.clearAnonymousUserLocationDetails()
                            } else {
                                KotlinUtils.saveAnonymousUserLocationDetails(
                                    ShoppingDeliveryLocation(
                                        confirmLocationResponse.orderSummary?.fulfillmentDetails
                                    )
                                )
                            }

                            // navigate to Dash home tab.
                            activity?.setResult(Activity.RESULT_OK)
                            activity?.finish()
                        }
                    }
                }
            } catch (e: Exception) {
                if (!isAdded || !isVisible) return@launch

                binding?.progressBar?.visibility = View.GONE
                FirebaseManager.logException(e)
                showErrorDialog()
            }
        }

    }

    private fun initMap() {
        checkNetwork()
        activity?.applicationContext?.let { context ->
            Places.initialize(context, getString(R.string.maps_google_api_key))
            val placesClient = Places.createClient(context)
            val placesAdapter =
                GooglePlacesAdapter(requireActivity(), placesClient, this@ConfirmAddressMapFragment)
            binding?.autoCompleteTextView?.apply {
                setAdapter(placesAdapter)
            }
            binding.autoCompleteTextView.afterTextChanged {
                if (it.isEmpty()) {
                    binding.imgRemoveAddress.visibility = View.GONE
                } else {
                    binding.imgRemoveAddress.visibility = View.VISIBLE
                }
            }
            binding?.autoCompleteTextView?.onItemClickListener =
                AdapterView.OnItemClickListener { parent, _, position, _ ->
                    val item = parent.getItemAtPosition(position) as? PlaceAutocomplete
                    placeId = item?.placeId.toString()
                    placeName = item?.primaryText.toString()
                    binding?.autoCompleteTextView?.setText(placeName)
                    enableMapView()
                    binding?.tvLocationNikName?.text = placeName
                    isAddressFromSearch = true
                    isStreetNumberAndRouteFromSearch = true
                    val placeFields: MutableList<Place.Field> = mutableListOf(
                        Place.Field.ID,
                        Place.Field.NAME,
                        Place.Field.LAT_LNG,
                        Place.Field.ADDRESS,
                        Place.Field.ADDRESS_COMPONENTS
                    )
                    val request =
                        placeFields.let {
                            FetchPlaceRequest.builder(placeId.toString(), it)
                                .setSessionToken(item?.token).build()
                        }
                    try {
                        request.let { placeRequest ->
                            placesClient.fetchPlace(placeRequest)
                                .addOnSuccessListener { response ->
                                    hideKeyboard(requireActivity())
                                    binding.autoCompleteTextView?.clearFocus()
                                    val place = response.place
                                    try {
                                        isAddressSearch = true
                                        moveMapCamera(
                                            place.latLng?.latitude,
                                            place.latLng?.longitude
                                        )
                                    } catch (e: Exception) {
                                        FirebaseManager.logException(e)
                                    }
                                }.addOnFailureListener {
                                    if (!isAdded || !isVisible) return@addOnFailureListener
                                    showErrorDialog()
                                }
                        }
                    } catch (e: Exception) {
                        FirebaseManager.logException(e)
                    }
                }
        }
    }


    private fun showSelectedLocationError(result: Boolean?) {
        binding?.apply {
            if (result == true) {
                if (isPoiAddress == true) {
                    confirmAddress?.isEnabled = false
                    errorMassageDivider?.visibility = View.GONE
                    errorMessage?.visibility = View.GONE
                    errorMessageTitle?.visibility = View.GONE
                    if (poiBottomSheetDialog == null) {
                        poiBottomSheetDialog =
                            PoiBottomSheetDialog(this@ConfirmAddressMapFragment, true)
                    }
                    if (poiBottomSheetDialog?.isVisible == false) {
                        poiBottomSheetDialog?.show(
                            requireActivity().supportFragmentManager,
                            PoiBottomSheetDialog::class.java.simpleName
                        )
                    }
                } else {
                    errorMassageDivider.visibility = View.VISIBLE
                    errorMessageTitle.visibility = View.VISIBLE
                    errorMessage.visibility = View.VISIBLE
                    confirmAddress.isEnabled = false
                    if (imgMapMarker.visibility == View.VISIBLE)
                        errorMessage.text = getText(R.string.geo_loc_error_msg)
                    else
                        errorMessage.text = getText(R.string.geo_loc_error_msg_with_out_pin)
                }

            } else {
                errorMassageDivider.visibility = View.GONE
                errorMessage.visibility = View.GONE
                errorMessageTitle.visibility = View.GONE
                confirmAddress.isEnabled = true
            }
        }
    }

    override fun onMapReady() {
        initView()

        if (!isAddAddress!!) {
            if (isMoveMapCameraFirstTime == true) {
                moveMapCamera(latitude, longitude)
            }
        } else {
            binding?.imgMapMarker?.visibility = View.GONE
            binding?.tvMarkerHint?.visibility = View.GONE
            binding?.confirmAddress?.isEnabled = false
            binding.dynamicMapView?.moveCamera(
                latitude = DEFAULT_LATITUDE,
                longitude = DEFAULT_LONGITUDE,
                zoom = 5f
            )

        }
    }

    override fun onMarkerClicked(marker: DynamicMapMarker) {}

    private fun moveMapCamera(latitude: Double?, longitude: Double?) {
        binding.apply {
            if (latitude != null && longitude != null) {
                if(noLocationLayout?.noLocationRootLayout?.visibility == View.VISIBLE) {
                    return@apply
                }
                imgMapMarker?.visibility = View.VISIBLE
                tvMarkerHint?.visibility = View.VISIBLE
                if (Utils.isLocationEnabled(requireContext()) && PermissionUtils.hasPermissions(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                    navigationMapArrow?.visibility = View.VISIBLE
                } else navigationMapArrow?.visibility = View.GONE
                confirmAddress?.isEnabled = true
            }
            isAddAddress = false
            dynamicMapView?.animateCamera(latitude, longitude, zoom = 18f)
            dynamicMapView?.setOnCameraMoveListener {
                dynamicMapView?.setOnCameraIdleListener {
                    if (imgMapMarker?.visibility == View.VISIBLE) {
                        val latitude = dynamicMapView?.getCameraPositionTargetLatitude()
                        val longitude = dynamicMapView?.getCameraPositionTargetLongitude()
                        latitude?.let { lat ->
                            longitude?.let { longitude ->
                                getAddressFromLatLng(lat, longitude)
                            }
                        }
                        mLatitude = latitude?.toString()
                        mLongitude = longitude?.toString()
                        getPlaceId(latitude, longitude)


                    }
                }
            }
        }
    }


    private fun getAddressOne(mAddress: String?) =
        mAddress?.split(",")?.getOrNull(0)

    private fun getAddressFromLatLng(latitude: Double, longitude: Double) {
        DynamicGeocoder.getAddressFromLocation(requireActivity(), latitude, longitude) { address ->
            address?.let {
                mAddress = it.addressLine
                address1 = getAddressOne(mAddress)
                city = it.city
                state = it.state
                country = it.countryCode
                postalCode = it.postcode
                suburb = it.suburb

            }
            mAddress?.let {
                if (!isAddressFromSearch) {
                    binding?.autoCompleteTextView?.setText(
                        getString(
                            R.string.geo_map_address,
                            address1,
                            city,
                            state
                        )
                    )
                    binding?.tvLocationNikName?.text = getString(
                        R.string.geo_map_address,
                        address1,
                        "",
                        ""
                    )
                }
                isAddressFromSearch = false
                binding?.autoCompleteTextView?.dismissDropDown()
            }
        }
    }

    private fun getPlaceId(latitude: Double?, longitude: Double?) {
        // https://woolworths.atlassian.net/browse/APP1-1501
        // Crashing on onNavigationMapArrowClicked if no network available.
        if (connectivityLiveData.value == false) {
            return
        }
        val context = GeoApiContext.Builder()
            .apiKey(getString(R.string.maps_google_api_key))
            .build()
        val results = GeocodingApi.newRequest(context)
            .latlng(latitude?.let {
                longitude?.let { it1 ->
                    com.google.maps.model.LatLng(
                        it, it1
                    )
                }
            }).await()

        if (isStreetNumberAndRouteFromSearch == false) {
            placeId = results.getOrNull(0)?.placeId.toString()

            getStreetNumberAndRoute(placeId)
        } else {
            // here place id is d/f coming search address
            getStreetNumberAndRoute(placeId)
        }
        isStreetNumberAndRouteFromSearch = false
    }

    private fun getStreetNumberAndRoute(placeId: String?) {
        isPoiAddress = false
        if (placeId.isNullOrEmpty() || placeId == "null") {
            showSelectedLocationError(true)
            return
        }
        Places.initialize(requireActivity(), getString(R.string.maps_google_api_key))
        val placesClient = Places.createClient(requireActivity())
        val placeFields: MutableList<Place.Field> = mutableListOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS,
            Place.Field.ADDRESS_COMPONENTS,
            Place.Field.TYPES
        )
        val request =
            placeFields.let {
                FetchPlaceRequest.builder(placeId.toString(), it).build()
            }
        var streetNumber: String? = null
        var routeName: String? = null
        request.let { placeRequest ->
            placesClient.fetchPlace(placeRequest)
                .addOnSuccessListener { response ->
                    val place = response.place
                    for (address in place.addressComponents?.asList()!!) {
                        when (address.types[0]) {
                            STREET_NUMBER.value -> streetNumber = address.name
                            ROUTE.value -> routeName = address.name
                        }
                    }
                    var type: String? = ""
                    address2 = ""
                    val placeTypes: MutableList<Place.Type>? = response.place.types
                    if (!placeTypes.isNullOrEmpty()) {
                        for (placeType in placeTypes) {
                            if (placeType == Place.Type.POINT_OF_INTEREST) {
                                isPoiAddress = true

                            }
                        }
                    }

                    if (streetNumber.isNullOrEmpty()) {
                        streetNumber = ""
                    }
                    if (routeName.isNullOrEmpty()) {
                        routeName = ""
                    }

                    if (isPoiAddress == true && streetNumber.isNullOrEmpty() && routeName.isNullOrEmpty()) {
                        type = POI
                    }

                    if (unIndexedAddressIdentified == true) {

                        type = POI
                    }



                    placeName?.let {
                        if (!it.equals(
                                "$streetNumber $routeName",
                                true
                            )
                        ) {
                            sendAddressData(it, "$streetNumber $routeName", type)

                        } else {
                            sendAddressData("$streetNumber $routeName", "", type)

                        }
                    } ?: sendAddressData("$streetNumber $routeName", "", type)

                    try {
                        view?.let {
                            lifecycleScope.launchWhenStarted {
                                delay(DELAY_500_MS)
                                if (streetNumber.isNullOrEmpty() && routeName.isNullOrEmpty()) {
                                    showSelectedLocationError(true)
                                } else {
                                    unIndexedLiveData.value = true
                                    showSelectedLocationError(false)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        FirebaseManager.logException(e)
                    }


                }.addOnFailureListener {
                    if (!isAdded || !isVisible) return@addOnFailureListener
                    showErrorDialog()
                }
        }

    }

    override fun tryAgain() {
        initView()
    }

    private fun sendAddressData(placeName: String?, apiAddress1: String? = "", type: String? = "") {
        val saveAddressLocationRequest = SaveAddressLocationRequest(
            "$placeName",
            city,
            country,
            mAddress,
            mLatitude,
            mLongitude,
            placeId,
            postalCode,
            state,
            suburb,
            apiAddress1,
            type
        )
        view?.let {
            lifecycleScope.launch {
                try {
                    confirmAddressViewModel.postSaveAddress(saveAddressLocationRequest)
                } catch (e: Exception) {
                    // Ignored (for example, HttpException for error 502)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver()
        KeyboardUtils.showSoftKeyboard(binding.autoCompleteTextView, activity)
        checkForLocationPermissionAndSetLocationAddress()
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun checkForLocationPermissionAndSetLocationAddress() {
        activity?.apply {
            //Check if user has location services enabled. If not, notify user as per current store locator functionality.
            if (!Utils.isLocationEnabled(this) || !PermissionUtils.hasPermissions(
                            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                binding.apply {
                  //  autoCompleteTextView.isEnabled = false
                   // clearAddressText()
                    showSelectedLocationError(false)
                    confirmAddress.isEnabled = false
                    dynamicMapView?.setAllGesturesEnabled(false)

                    noLocationLayout?.noLocationRootLayout?.visibility = View.VISIBLE
                    dynamicMapView?.visibility = View.GONE
                    imgMapMarker?.visibility = View.GONE
                    tvMarkerHint?.visibility = View.GONE
                    navigationMapArrow?.visibility = View.GONE
                    confirmAddressLayout?.visibility = View.GONE
                }
                return@apply
            } else {
                enableMapView()
            }
        }
    }

    private fun enableMapView() {
        binding.noLocationLayout?.noLocationRootLayout?.visibility = View.GONE

        binding.dynamicMapView?.onResume()
        if (binding.dynamicMapView?.isMapInstantiated() == true) {
            isMoveMapCameraFirstTime = false
        }
        moveMapCamera(mLatitude?.toDoubleOrNull(), mLongitude?.toDoubleOrNull())
        binding.apply {
            dynamicMapView?.visibility = View.VISIBLE
            mapFrameLayout?.visibility = View.VISIBLE
            confirmAddressLayout?.visibility = View.VISIBLE
            if (Utils.isLocationEnabled(requireContext()) && PermissionUtils.hasPermissions(
                            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                navigationMapArrow?.visibility = View.VISIBLE
            } else navigationMapArrow?.visibility = View.GONE
            autoCompleteTextView?.isEnabled = true
            dynamicMapView?.setAllGesturesEnabled(true)
            if (isAddAddress != null && isAddressSearch == false) {
                confirmAddress?.isEnabled = false
                imgMapMarker?.visibility = View.GONE
                tvMarkerHint?.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        binding.dynamicMapView?.onDestroy()
        super.onDestroyView()
    }

    override fun onPause() {
        binding.dynamicMapView?.onPause()
        super.onPause()
        unregisterReceiver()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.dynamicMapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.dynamicMapView?.onSaveInstanceState(outState)
    }

    override fun onConfirmClick(streetName: String) {
        address2 = streetName
        if (!address2.isNullOrEmpty())
            binding.confirmAddress?.isEnabled = true
    }

    override fun unIndexedAddressIdentified() {
        unIndexedAddressIdentified = true
        showSelectedLocationError(true)
    }

    private fun addUnIndexedIdentifiedListener() {
        unIndexedLiveData.value = false
        unIndexedLiveData.observe(viewLifecycleOwner) {
            if (it == true && unIndexedAddressIdentified == true) {

                if (unIndexedBottomSheetDialog == null) {
                    unIndexedBottomSheetDialog =
                        PoiBottomSheetDialog(this@ConfirmAddressMapFragment, false)
                }
                if (unIndexedBottomSheetDialog?.isVisible == false) {
                    unIndexedBottomSheetDialog?.show(
                        requireActivity().supportFragmentManager,
                        PoiBottomSheetDialog::class.java.simpleName
                    )
                }
            }

        }
    }

    private fun startLocationDiscoveryProcess() {
        locator?.getCurrentLocationSilently { locationEvent ->
            when (locationEvent) {
                is Event.Location -> handleLocationEvent(locationEvent)
                is Event.Permission -> handlePermissionEvent(locationEvent)
            }
        }
    }

    private fun handlePermissionEvent(permissionEvent: Event.Permission) {
        if (permissionEvent.event == EventType.LOCATION_PERMISSION_NOT_GRANTED) {
            Utils.saveLastLocation(null, activity)
            handleLocationEvent(null)
        }
    }

    private fun handleLocationEvent(locationEvent: Event.Location?) {
        Utils.saveLastLocation(locationEvent?.locationData, context)
        navigateCurrentLocation()
    }

    private fun unregisterReceiver() {
        try {
            locationBroadcastReceiver.let {
                requireContext().unregisterReceiver(locationBroadcastReceiver)
            }
        } catch (ex: IllegalArgumentException) {
            FirebaseManager.logException("unregisterReceiver locationBroadcastReceiver $ex")
        }
    }

    private fun registerReceiver() {
        requireContext().registerReceiver(locationBroadcastReceiver, IntentFilter().apply {
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        })
    }

    override fun onDetach() {
        super.onDetach()
        unregisterReceiver()
    }

    override fun onLocationProviderChange(context: Context?, intent: Intent?) {
        if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
            Handler(Looper.getMainLooper()).postDelayed({
                checkForLocationPermissionAndSetLocationAddress()
            }, AppConstant.DELAY_2000_MS)
        }
    }
}

