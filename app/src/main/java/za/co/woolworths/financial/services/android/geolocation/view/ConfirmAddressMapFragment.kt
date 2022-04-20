package za.co.woolworths.financial.services.android.geolocation.view

import android.app.Activity
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.GeolocationConfirmAddressBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.maps.GeoApiContext
import com.google.maps.GeocodingApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.geolocation_confirm_address.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import za.co.woolworths.financial.services.android.checkout.view.adapter.GooglePlacesAdapter
import za.co.woolworths.financial.services.android.checkout.view.adapter.PlaceAutocomplete
import za.co.woolworths.financial.services.android.checkout.viewmodel.AddressComponentEnum.ROUTE
import za.co.woolworths.financial.services.android.checkout.viewmodel.AddressComponentEnum.STREET_NUMBER
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.geolocation.model.request.ConfirmLocationRequest
import za.co.woolworths.financial.services.android.geolocation.model.request.SaveAddressLocationRequest
import za.co.woolworths.financial.services.android.geolocation.model.response.ConfirmLocationAddress
import za.co.woolworths.financial.services.android.geolocation.network.apihelper.GeoLocationApiHelper
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidateLocationResponse
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.geolocation.viewmodel.GeoLocationViewModelFactory
import za.co.woolworths.financial.services.android.geolocation.viewmodel.LocationErrorLiveData
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation
import za.co.woolworths.financial.services.android.ui.views.CustomBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.VtoErrorBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.listener.VtoTryAgainListener
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_OK
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.BUNDLE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_CONFIRM_ADD
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.KEY_LATITUDE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.KEY_LONGITUDE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.KEY_PLACE_ID
import za.co.woolworths.financial.services.android.util.KeyboardUtils.Companion.hideKeyboard
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ConfirmAddressMapFragment :
    Fragment(), OnMapReadyCallback, VtoTryAgainListener {

    private var mMap: GoogleMap? = null
    private var mAddress: String? = null
    private var placeId: String? = null
    private var deliveryType: String? = null
    private var latLng: LatLng? = null
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
    private var isLocationErrorShowing: Boolean? = true
    private var isAddressSearch: Boolean? = false
    private lateinit var confirmAddressViewModel: ConfirmAddressViewModel

    @Inject
    lateinit var vtoErrorBottomSheetDialog: VtoErrorBottomSheetDialog

    private var _binding: GeolocationConfirmAddressBinding? = null
    private val binding get() = _binding

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
        initView()
    }

    private fun initView() {
        val bundle = arguments ?: return
        val args = ConfirmAddressMapFragmentArgs.fromBundle(bundle)
        latitude = args.mapData.latitude
        longitude = args.mapData.longitude
        isAddAddress = args.mapData.isAddAddress
        isComingFromCheckout = args.mapData.isComingFromCheckout
        isFromDashTab = args.mapData.isFromDashTab
        deliveryType = args.mapData.deliveryType
        setUpViewModel()
        clearAddress()
        confirmAddressClick()
        addFragmentListner()

        if (confirmAddressViewModel.isConnectedToInternet(requireActivity())) {
            initMap()
            mMap?.uiSettings?.setAllGesturesEnabled(true)
        } else {
            binding?.apply {
                autoCompleteTextView.isEnabled = false
                mMap?.uiSettings?.setAllGesturesEnabled(false)
                showErrorDialog()
            }
        }
    }

    private fun showErrorDialog() {
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
        setFragmentResultListener(CustomBottomSheetDialogFragment.DIALOG_BUTTON_CLICK_RESULT) { _, _ ->
            // change location button clicked as address is not deliverable.
            clearAddressText()
        }
    }

    private fun checkNetwork(mapFragment: SupportMapFragment?) {
        activity?.let {
            ConnectivityLiveData.observe(viewLifecycleOwner) { isNetworkAvailable ->
                binding?.apply {
                    if (isNetworkAvailable) {
                        mapFragment?.view?.visibility = View.VISIBLE
                        mapFrameLayout.visibility = View.VISIBLE
                        imgMapMarker.visibility = View.VISIBLE
                        autoCompleteTextView.isEnabled = true
                        confirmAddress.isEnabled = true
                        mMap?.uiSettings?.setAllGesturesEnabled(true)
                        if (isAddAddress!! && isAddressSearch == false) {
                            confirmAddress.isEnabled = false
                            imgMapMarker.visibility = View.GONE
                        }
                    } else {
                        autoCompleteTextView.isEnabled = false
                        confirmAddress.isEnabled = false
                        mMap?.uiSettings?.setAllGesturesEnabled(false)
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
        errorMassageDivider.visibility = View.GONE
        errorMessage.visibility = View.GONE
    }

    private fun confirmAddressClick() {

        binding?.confirmAddress?.setOnClickListener {

            Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.SHOP_CONFIRM_ADDRESS,
                hashMapOf(
                    FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                            FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_SHOP_CONFIRM_ADDRESS
                ),
                activity)

            val bundle = Bundle()
            if (isComingFromCheckout == true) {
                bundle.apply {
                    putString(
                        KEY_PLACE_ID, placeId
                    )
                    putBoolean(
                        IS_COMING_CONFIRM_ADD, true)
                    findNavController().navigate(
                        R.id.actionClickAndCollectStoresFragment,
                        bundleOf(BUNDLE to bundle)
                    )
                }
            } else if (isFromDashTab == true) {
                validateLocation()
            } else {
                // normal geo flow
                if (mLatitude != null && mLongitude != null && placeId != null) {

                    bundle.apply {
                        putString(KEY_LATITUDE, mLatitude)
                        putString(KEY_LONGITUDE, mLongitude)
                        putString(KEY_PLACE_ID, placeId)
                        putString(BundleKeysConstants.DELIVERY_TYPE, deliveryType)
                    }
                    findNavController().navigate(
                        R.id.action_confirmAddressMapFragment_to_deliveryAddressConfirmationFragment,
                        bundleOf(BUNDLE to bundle)
                    )
                }
            }
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
                if (validateLocationResponse != null) {
                    when (validateLocationResponse?.httpCode) {
                        HTTP_OK -> {
                            validateLocationResponse.validatePlace?.let { place ->
                                if (place.onDemand != null && place.onDemand!!.deliverable == true) {
                                    if (!SessionUtilities.getInstance().isUserAuthenticated) {
                                        // User not logged in that's why we are setting new location.
                                        confirmSetAddress(validateLocationResponse)
                                    } else {
                                        val savedLocation = Utils.getPreferredDeliveryLocation()
                                        if (savedLocation?.fulfillmentDetails?.deliveryType.isNullOrEmpty()) {
                                            // user logged in but don't have any location that's why we are setting new location.
                                            confirmSetAddress(validateLocationResponse)
                                        } else {
                                            // directly go back to Dash landing screen. Don't call confirm location API as user only wants to browse Dash.
                                                var intent = Intent()
                                            intent.putExtra(BundleKeysConstants.VALIDATE_RESPONSE, validateLocationResponse)
                                            activity?.setResult(Activity.RESULT_OK, intent)
                                            activity?.finish()
                                        }
                                    }
                                } else {
                                    // Show not deliverable Bottom Dialog.
                                    val customBottomSheetDialogFragment =
                                        CustomBottomSheetDialogFragment.newInstance(
                                            getString(R.string.no_location_delivery),
                                            getString(R.string.no_location_desc),
                                            getString(R.string.change_location),
                                            R.drawable.location_disabled)
                                    customBottomSheetDialogFragment.show(requireFragmentManager(),
                                        CustomBottomSheetDialogFragment::class.java.simpleName)
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
                binding?.progressBar?.visibility = View.GONE
                showErrorDialog()
            }
        }
    }

    private fun confirmSetAddress(validateLocationResponse: ValidateLocationResponse) {
        if (placeId.isNullOrEmpty())
            return

        //make confirm Location call
        val confirmLocationAddress = ConfirmLocationAddress(placeId)
        val confirmLocationRequest =
            ConfirmLocationRequest(BundleKeysConstants.DASH,
                confirmLocationAddress,
                validateLocationResponse.validatePlace?.onDemand?.storeId)

        lifecycleScope.launch {
            binding?.progressBar?.visibility = View.VISIBLE
            try {
                val confirmLocationResponse =
                    confirmAddressViewModel.postConfirmAddress(confirmLocationRequest)
                binding?.progressBar?.visibility = View.GONE
                if (confirmLocationResponse != null) {
                    when (confirmLocationResponse.httpCode) {
                        HTTP_OK -> {

                            WoolworthsApplication.setValidatedSuburbProducts(validateLocationResponse.validatePlace)
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
            } catch (e: HttpException) {
                binding?.progressBar?.visibility = View.GONE
                FirebaseManager.logException(e)
                showErrorDialog()
            }
        }

    }

    private fun initMap() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.gMap) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        checkNetwork(mapFragment)
        activity?.applicationContext?.let { context ->
            Places.initialize(context, getString(R.string.maps_api_key))
            val placesClient = Places.createClient(context)
            val placesAdapter =
                GooglePlacesAdapter(requireActivity(), placesClient)
            binding?.autoCompleteTextView?.apply {
                setAdapter(placesAdapter)
            }
            showLocationErrorBanner()
            binding?.autoCompleteTextView?.onItemClickListener =
                AdapterView.OnItemClickListener { parent, _, position, _ ->
                    val item = parent.getItemAtPosition(position) as? PlaceAutocomplete
                    placeId = item?.placeId.toString()
                    val placeName = item?.primaryText.toString()
                    binding?.autoCompleteTextView?.setText(placeName)
                    isAddressFromSearch = true
                    val placeFields: MutableList<Place.Field> = mutableListOf(
                        Place.Field.ID,
                        Place.Field.NAME,
                        Place.Field.LAT_LNG,
                        Place.Field.ADDRESS,
                        Place.Field.ADDRESS_COMPONENTS
                    )
                    val request =
                        placeFields.let {
                            FetchPlaceRequest.builder(placeId.toString(), it).build()
                        }
                    request.let { placeRequest ->
                        placesClient.fetchPlace(placeRequest)
                            .addOnSuccessListener { response ->
                                hideKeyboard(requireActivity())
                                autoCompleteTextView?.clearFocus()
                                val place = response.place
                                val location = place.name
                                val addressList: MutableList<Address>?
                                if (location != null || location == "") {
                                    try {
                                        val geocoder = Geocoder(context)
                                        addressList = geocoder.getFromLocationName(location, 1)
                                        val address = addressList?.getOrNull(0)
                                        latLng = address?.latitude?.let {
                                            LatLng(it, address.longitude)
                                        }
                                        isAddressSearch = true
                                        moveMapCamera(latLng)
                                    } catch (e: Exception) {
                                        FirebaseManager.logException(e)
                                    }
                                }
                            }.addOnFailureListener {
                                showErrorDialog()
                            }
                    }
                }
        }
    }

    private fun showLocationErrorBanner() {
        LocationErrorLiveData.observe(viewLifecycleOwner) { isResult ->
            isLocationErrorShowing = isResult
            showSelectedLocationError(isResult)
        }
    }

    private fun showSelectedLocationError(result: Boolean?) {
        binding?.apply {
            if (result == true) {
                errorMassageDivider.visibility = View.VISIBLE
                errorMessage.visibility = View.VISIBLE
                confirmAddress.isEnabled = false
            } else {
                errorMassageDivider.visibility = View.GONE
                errorMessage.visibility = View.GONE
                confirmAddress.isEnabled = true
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (!isAddAddress!!) {
            latLng = longitude?.let { latitude?.let { it1 -> LatLng(it1, it) } }
            if (isMoveMapCameraFirstTime == true) {
                moveMapCamera(latLng)
            }

        } else {
            binding?.imgMapMarker?.visibility = View.GONE
            binding?.confirmAddress?.isEnabled = false
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(DEFAULT_LATITUDE,
                DEFAULT_LONGITUDE), 10f))

        }

    }

    private fun moveMapCamera(latLng: LatLng?) {
        binding?.imgMapMarker?.visibility = View.VISIBLE
        binding?.confirmAddress?.isEnabled = true
        isAddAddress = false
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
        mMap?.setOnCameraMoveListener {
            mMap?.setOnCameraIdleListener {
                val localLatLng = mMap?.cameraPosition?.target
                val latitude = localLatLng?.latitude
                val longitude = localLatLng?.longitude
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
        try {
            val geocoder = Geocoder(requireActivity(), Locale.getDefault())
            val address: MutableList<Address> = geocoder.getFromLocation(latitude, longitude, 1)
            address.let {
                mAddress = it.getOrNull(0)?.getAddressLine(0)
                address1 = getAddressOne(mAddress)
                city = it.getOrNull(0)?.locality
                state = it.getOrNull(0)?.adminArea
                country = it.getOrNull(0)?.countryName
                postalCode = it.getOrNull(0)?.postalCode
                suburb = it.getOrNull(0)?.subLocality

            }

        } catch (e: Exception) {
            FirebaseManager.logException(e)
        }
        mAddress?.let {
            if (!isAddressFromSearch) {
                binding?.autoCompleteTextView?.setText(getString(R.string.geo_map_address,
                    address1,
                    city,
                    state))
            }
            isAddressFromSearch = false
            binding?.autoCompleteTextView?.dismissDropDown()
        }

    }

    private fun getPlaceId(latitude: Double?, longitude: Double?) {
        val context = GeoApiContext.Builder()
            .apiKey(getString(R.string.maps_api_key))
            .build()
        val results = GeocodingApi.newRequest(context)
            .latlng(latitude?.let {
                longitude?.let { it1 ->
                    com.google.maps.model.LatLng(
                        it, it1
                    )
                }
            }).await()

        placeId = results.getOrNull(0)?.placeId.toString()

        getStreetNumberAndRoute(placeId)

        sendAddressData()
    }

    private fun getStreetNumberAndRoute(placeId: String?) {
        Places.initialize(requireActivity(), getString(R.string.maps_api_key))
        val placesClient = Places.createClient(requireActivity())
        val placeFields: MutableList<Place.Field> = mutableListOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS,
            Place.Field.ADDRESS_COMPONENTS,
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
                    val address = place.addressComponents?.asList()?.getOrNull(0)
                    address?.types?.getOrNull(0)
                    when (address?.types?.getOrNull(0)) {
                        STREET_NUMBER.value -> streetNumber = address.name
                        ROUTE.value -> routeName = address.name
                    }
                    viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                        delay(AppConstant.DELAY_500_MS)
                        if (streetNumber.isNullOrEmpty() && routeName.isNullOrEmpty() && isLocationErrorShowing == false) {
                            showSelectedLocationError(true)
                        }
                    }

                }.addOnFailureListener {
                    showErrorDialog()
                }
        }

    }

    companion object {
        private const val DEFAULT_LATITUDE = -33.918861
        private const val DEFAULT_LONGITUDE = 18.423300
    }

    override fun tryAgain() {
        initView()
    }

    private fun setUpViewModel() {
        confirmAddressViewModel = ViewModelProvider(
            this,
            GeoLocationViewModelFactory(GeoLocationApiHelper())
        ).get(ConfirmAddressViewModel::class.java)
    }

    private fun sendAddressData() {
        val saveAddressLocationRequest = SaveAddressLocationRequest("$address1 $city",
            city,
            country,
            mAddress,
            mLatitude,
            mLongitude,
            placeId,
            postalCode,
            state,
            suburb)
        viewLifecycleOwner.lifecycleScope.launch {
            confirmAddressViewModel.postSaveAddress(saveAddressLocationRequest)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocationErrorLiveData.value = false
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        mLatitude?.let {
            val latLng =
                LatLng(it.toDouble(), mLongitude?.toDouble()!!)
            isMoveMapCameraFirstTime = false
            moveMapCamera(latLng)
        }

    }
}

