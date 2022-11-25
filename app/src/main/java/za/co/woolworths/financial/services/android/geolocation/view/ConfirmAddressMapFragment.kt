package za.co.woolworths.financial.services.android.geolocation.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.GeolocationConfirmAddressBinding
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.gson.JsonSyntaxException
import com.google.maps.GeoApiContext
import com.google.maps.GeocodingApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.geolocation_confirm_address.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import za.co.woolworths.financial.services.android.checkout.view.adapter.GooglePlacesAdapter
import za.co.woolworths.financial.services.android.checkout.view.adapter.GooglePlacesAdapter.Companion.SEARCH_LENGTH
import za.co.woolworths.financial.services.android.checkout.view.adapter.PlaceAutocomplete
import za.co.woolworths.financial.services.android.checkout.viewmodel.AddressComponentEnum.ROUTE
import za.co.woolworths.financial.services.android.checkout.viewmodel.AddressComponentEnum.STREET_NUMBER
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.geolocation.GeoUtils
import za.co.woolworths.financial.services.android.geolocation.model.request.ConfirmLocationRequest
import za.co.woolworths.financial.services.android.geolocation.model.request.SaveAddressLocationRequest
import za.co.woolworths.financial.services.android.geolocation.model.response.ConfirmLocationAddress
import za.co.woolworths.financial.services.android.geolocation.network.apihelper.GeoLocationApiHelper
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidateLocationResponse
import za.co.woolworths.financial.services.android.geolocation.view.DeliveryAddressConfirmationFragment.Companion.MAP_LOCATION_RESULT
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.geolocation.viewmodel.GeoLocationViewModelFactory
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation
import za.co.woolworths.financial.services.android.ui.extension.afterTextChanged
import za.co.woolworths.financial.services.android.ui.fragments.poi.MapsPoiBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.views.CustomBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.ui.views.maps.DynamicMapDelegate
import za.co.woolworths.financial.services.android.ui.views.maps.model.DynamicMapMarker
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.VtoErrorBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.listener.VtoTryAgainListener
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_OK
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.BUNDLE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_CONFIRM_ADD
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
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import javax.inject.Inject

@AndroidEntryPoint
class ConfirmAddressMapFragment :
    Fragment(), DynamicMapDelegate, VtoTryAgainListener, MapsPoiBottomSheetDialog.ClickListner {

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

    val confirmAddressViewModel: ConfirmAddressViewModel by activityViewModels()

    @Inject
    lateinit var vtoErrorBottomSheetDialog: VtoErrorBottomSheetDialog

    @Inject
    lateinit var connectivityLiveData: ConnectivityLiveData

    private var placeName: String? = null
    private var isMainPlaceName: Boolean? = false
    private var isStreetNumberAndRouteFromSearch: Boolean? = false
    private var _binding: GeolocationConfirmAddressBinding? = null
    private val binding get() = _binding
    private var isPoiAddress: Boolean? = false
    private var address2: String? = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = GeolocationConfirmAddressBinding.inflate(inflater, container, false)
        return binding?.root
    }


    override fun onViewCreated(
        view: View, savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        dynamicMapView?.initializeMap(savedInstanceState, this)
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
        clearAddress()
        confirmAddressClick()
        addFragmentListner()

        if (confirmAddressViewModel.isConnectedToInternet(requireActivity())) {
            initMap()
            dynamicMapView?.setAllGesturesEnabled(true)
        } else {
            binding?.apply {
                autoCompleteTextView.isEnabled = false
                dynamicMapView?.setAllGesturesEnabled(false)
                showErrorDialog()
            }
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
                        dynamicMapView?.visibility = View.VISIBLE
                        mapFrameLayout.visibility = View.VISIBLE
                        autoCompleteTextView.isEnabled = true
                        dynamicMapView?.setAllGesturesEnabled(true)
                        if (isAddAddress!! && isAddressSearch == false) {
                            confirmAddress.isEnabled = false
                            imgMapMarker.visibility = View.GONE
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

    private fun clearAddressText() {
        autoCompleteTextView.setText("")
        showSearchBarHint()
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
                val validateLocationResponse =
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
                                            KotlinUtils.isDashTabCrossClicked =
                                                placeId?.equals(KotlinUtils.getDeliveryType()?.address?.placeId) // changing black tooltip flag as user changes in his location.
                                            confirmSetAddress(validateLocationResponse)
                                        } else {
                                            // User has location. Means only changing browsing location.
                                            KotlinUtils.isDashTabCrossClicked =
                                                placeId?.equals(KotlinUtils.getDeliveryType()?.address?.placeId) // changing black tooltip flag as user changes his browsing location.
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

                                    if(placeId != null) {
                                        val store = GeoUtils.getStoreDetails(
                                                placeId,
                                                validateLocationResponse?.validatePlace?.stores
                                        )
                                        if (store?.locationId != "" && store?.storeName?.contains(StoreUtils.PARGO, true) == false) {
                                            Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.storeName = store?.storeName.toString() + "." + StoreUtils.PARGO
                                            Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.locationId =  store?.locationId.toString()
                                        }
                                    }

                                    /*user is coming from CNC i.e. set Location flow */
                                    // navigate to CNC home tab.
                                    KotlinUtils.isComingFromCncTab = false

                                    /* set cnc browsing data */
                                    WoolworthsApplication.setCncBrowsingValidatePlaceDetails(
                                        validateLocationResponse?.validatePlace
                                    )
                                    activity?.finish()
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
                null
            )
        customBottomSheetDialogFragment.show(
            requireFragmentManager(),
            CustomBottomSheetDialogFragment::class.java.simpleName
        )
    }

    private fun confirmSetAddress(validateLocationResponse: ValidateLocationResponse) {
        if (placeId.isNullOrEmpty())
            return

        //make confirm Location call
        val confirmLocationAddress = ConfirmLocationAddress(placeId)
        val confirmLocationRequest =
            ConfirmLocationRequest(
                BundleKeysConstants.DASH,
                confirmLocationAddress,
                validateLocationResponse.validatePlace?.onDemand?.storeId
            )

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
                            KotlinUtils.isLocationSame =
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
                GooglePlacesAdapter(requireActivity(), placesClient)
            binding?.autoCompleteTextView?.apply {
                setAdapter(placesAdapter)
            }
            showSearchBarHint()
            binding?.autoCompleteTextView?.afterTextChanged {
                if (it.length < SEARCH_LENGTH) {
                    showSearchBarHint()
                } else
                    hideSearchBarHint()
            }
            binding?.autoCompleteTextView?.onItemClickListener =
                AdapterView.OnItemClickListener { parent, _, position, _ ->
                    val item = parent.getItemAtPosition(position) as? PlaceAutocomplete
                    placeId = item?.placeId.toString()
                    placeName = item?.primaryText.toString()
                    binding?.autoCompleteTextView?.setText(placeName)
                    isAddressFromSearch = true
                    isMainPlaceName = true
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
                    request.let { placeRequest ->
                        placesClient.fetchPlace(placeRequest)
                            .addOnSuccessListener { response ->
                                hideKeyboard(requireActivity())
                                autoCompleteTextView?.clearFocus()
                                val place = response.place
                                try {
                                    isAddressSearch = true
                                    moveMapCamera(place.latLng?.latitude, place.latLng?.longitude)
                                } catch (e: Exception) {
                                    FirebaseManager.logException(e)
                                }
                            }.addOnFailureListener {
                                if (!isAdded || !isVisible) return@addOnFailureListener
                                showErrorDialog()
                            }
                    }
                }
        }
    }

    private fun showSearchBarHint() {
        errorMessage?.visibility = View.GONE
        errorMassageDivider?.visibility = View.VISIBLE
        searchBarTipHint?.visibility = View.VISIBLE
    }

    private fun hideSearchBarHint() {
        searchBarTipHint?.visibility = View.GONE
        errorMassageDivider?.visibility = View.GONE
    }

    private fun showSelectedLocationError(result: Boolean?) {
        binding?.apply {
            if (result == true) {
                if (isPoiAddress == true) {
                    confirmAddress?.isEnabled = false
                    MapsPoiBottomSheetDialog(this@ConfirmAddressMapFragment).show(
                        requireActivity().supportFragmentManager,
                        MapsPoiBottomSheetDialog::class.java.simpleName
                    )
                } else {
                    errorMassageDivider?.visibility = View.VISIBLE
                    errorMessage?.visibility = View.VISIBLE
                    errorMessage?.text = getString(R.string.geo_loc_error_msg)
                    errorMessage?.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.red
                        )
                    )
                    errorMessage?.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                    confirmAddress?.isEnabled = false
                }

            } else {
                errorMassageDivider?.visibility = View.GONE
                errorMessage?.visibility = View.GONE
                confirmAddress?.isEnabled = true
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
            binding?.confirmAddress?.isEnabled = false
            dynamicMapView?.moveCamera(
                latitude = DEFAULT_LATITUDE,
                longitude = DEFAULT_LONGITUDE,
                zoom = 5f
            )

        }
    }

    override fun onMarkerClicked(marker: DynamicMapMarker) {}

    private fun moveMapCamera(latitude: Double?, longitude: Double?) {
        if (latitude != null && longitude != null) {
            binding?.imgMapMarker?.visibility = View.VISIBLE
            binding?.confirmAddress?.isEnabled = true
        }
        isAddAddress = false
        dynamicMapView?.animateCamera(
            latitude, longitude,
            zoom = 18f
        )
        dynamicMapView?.setOnCameraMoveListener {
            dynamicMapView?.setOnCameraIdleListener {
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
                }
                isAddressFromSearch = false
                binding?.autoCompleteTextView?.dismissDropDown()
            }
        }
    }

    private fun getPlaceId(latitude: Double?, longitude: Double?) {
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
                            if (placeType.equals(Place.Type.POINT_OF_INTEREST)) {
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


                    placeName?.let {
                        if (!it.equals(
                                "$streetNumber $routeName",
                                true
                            ) && isMainPlaceName == true
                        ) {
                            sendAddressData(it, "$streetNumber $routeName", type)
                            isMainPlaceName = false
                        } else {
                            sendAddressData("$streetNumber $routeName", type)
                            isMainPlaceName = false
                        }
                    } ?: sendAddressData("$streetNumber $routeName", type)

                    try {
                        view?.let {
                            lifecycleScope.launchWhenStarted {
                                delay(AppConstant.DELAY_500_MS)
                                if (streetNumber.isNullOrEmpty() && routeName.isNullOrEmpty())
                                    showSelectedLocationError(true)
                                else
                                    showSelectedLocationError(false)
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
        try {
            view?.let {
                lifecycleScope.launch {
                    confirmAddressViewModel.postSaveAddress(saveAddressLocationRequest)
                }
            }
        } catch (e: Exception) {
            FirebaseManager.logException(e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        dynamicMapView?.onResume()
        if (dynamicMapView?.isMapInstantiated() == true) {
            isMoveMapCameraFirstTime = false
        }
        moveMapCamera(mLatitude?.toDoubleOrNull(), mLongitude?.toDoubleOrNull())
    }

    override fun onDestroyView() {
        dynamicMapView?.onDestroy()
        super.onDestroyView()
    }

    override fun onPause() {
        dynamicMapView?.onPause()
        super.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        dynamicMapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        dynamicMapView?.onSaveInstanceState(outState)
    }

    override fun onConfirmClick(streetName: String) {
        address2 = streetName
        if (!address2.isNullOrEmpty())
            confirmAddress?.isEnabled = true
    }
}

