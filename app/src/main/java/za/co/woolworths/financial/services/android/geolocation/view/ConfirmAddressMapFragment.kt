package za.co.woolworths.financial.services.android.geolocation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
import kotlinx.android.synthetic.main.geolocation_confirm_address.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.checkout.view.adapter.GooglePlacesAdapter
import za.co.woolworths.financial.services.android.checkout.view.adapter.PlaceAutocomplete
import za.co.woolworths.financial.services.android.checkout.viewmodel.AddressComponentEnum.ROUTE
import za.co.woolworths.financial.services.android.checkout.viewmodel.AddressComponentEnum.STREET_NUMBER
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.geolocation.model.request.SaveAddressLocationRequest
import za.co.woolworths.financial.services.android.geolocation.network.apihelper.GeoLocationApiHelper
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.geolocation.viewmodel.GeoLocationViewModelFactory
import za.co.woolworths.financial.services.android.ui.views.maps.DynamicMapDelegate
import za.co.woolworths.financial.services.android.ui.views.maps.model.DynamicMapMarker
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.VtoErrorBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.listener.VtoTryAgainListener
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.BUNDLE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_CONFIRM_ADD
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.KEY_LATITUDE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.KEY_LONGITUDE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.KEY_PLACE_ID
import za.co.woolworths.financial.services.android.util.ConnectivityLiveData
import za.co.woolworths.financial.services.android.util.FirebaseManager
import za.co.woolworths.financial.services.android.util.KeyboardUtils.Companion.hideKeyboard
import za.co.woolworths.financial.services.android.util.LocalConstant.Companion.DEFAULT_LATITUDE
import za.co.woolworths.financial.services.android.util.LocalConstant.Companion.DEFAULT_LONGITUDE
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.location.DynamicGeocoder
import javax.inject.Inject

@AndroidEntryPoint
class ConfirmAddressMapFragment :
    Fragment(), DynamicMapDelegate, VtoTryAgainListener {

    private var mAddress: String? = null
    private var placeId: String? = null
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
    private var isAddressFromSearch: Boolean = false
    private var isMoveMapCameraFirstTime: Boolean? = true
    private var isAddressSearch: Boolean? = false
    private lateinit var confirmAddressViewModel: ConfirmAddressViewModel
    @Inject
    lateinit var vtoErrorBottomSheetDialog: VtoErrorBottomSheetDialog
    private var placeName: String? = null
    private var isMainPlaceName: Boolean? = false
    private var isStreetNumberAndRouteFromSearch: Boolean? = false
    private var _binding: GeolocationConfirmAddressBinding? = null
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
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
        val bundle = arguments ?: return

        val args = ConfirmAddressMapFragmentArgs.fromBundle(bundle)
        latitude = args.mapData.latitude
        longitude = args.mapData.longitude
        isAddAddress = args.mapData.isAddAddress
        isComingFromCheckout = args.mapData.isComingFromCheckout

        setUpViewModel()
        clearAddress()
        confirmAddressClick()

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

    private fun checkNetwork() {
        activity?.let {
            ConnectivityLiveData.observe(viewLifecycleOwner, { isNetworkAvailable ->
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
            })
        }
    }

    private fun clearAddress() {
        binding?.apply {
            imgRemoveAddress.setOnClickListener {
                autoCompleteTextView.setText("")
                errorMassageDivider.visibility = View.GONE
                errorMessage.visibility = View.GONE
            }
        }
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
            if(isComingFromCheckout==true) {
                bundle?.apply {
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
            } else {
                // normal geo flow
                if (mLatitude != null && mLongitude != null && placeId != null) {

                    bundle?.apply {
                        putString(
                           KEY_LATITUDE, mLatitude
                        )
                        putString(
                            KEY_LONGITUDE, mLongitude
                        )
                        putString(
                           KEY_PLACE_ID, placeId
                        )
                    }
                    findNavController().navigate(
                        R.id.action_confirmAddressMapFragment_to_deliveryAddressConfirmationFragment,
                        bundleOf(BUNDLE to bundle)
                    )
                }
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
                            FetchPlaceRequest.builder(placeId.toString(), it).setSessionToken(item?.token).build()
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
                                showErrorDialog()
                            }
                    }
                }
        }
    }

    private fun showSelectedLocationError(result: Boolean?) {
        binding?.apply {
            if (result == true) {
                errorMassageDivider?.visibility = View.VISIBLE
                errorMessage?.visibility = View.VISIBLE
                confirmAddress?.isEnabled = false
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
            if (isMoveMapCameraFirstTime == true){
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

    override fun onMarkerClicked(marker: DynamicMapMarker) { }

    private fun moveMapCamera(latitude: Double?, longitude: Double?) {
        if(latitude!=null && longitude!=null) {
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
                    binding?.autoCompleteTextView?.setText(getString(R.string.geo_map_address,address1,city,state))
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
        )
        val request =
            placeFields.let {
                FetchPlaceRequest.builder(placeId.toString(), it).build()
            }
         var streetNumber:String? = null
         var routeName:String? = null
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
                    if(streetNumber.isNullOrEmpty()){
                        streetNumber=""
                    }
                    if(routeName.isNullOrEmpty()){
                        routeName=""
                    }
                    placeName?.let {
                        if (!it.equals("$streetNumber $routeName",
                                true) && isMainPlaceName == true
                        ) {
                            sendAddressData(it,"$streetNumber $routeName")
                            isMainPlaceName = false
                        } else {
                            sendAddressData("$streetNumber $routeName")
                            isMainPlaceName = false
                        }
                    } ?: sendAddressData("$streetNumber $routeName")

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
                    showErrorDialog()
                }
        }

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

    private fun sendAddressData(placeName: String?,apiAddress1:String?="") {
        val saveAddressLocationRequest = SaveAddressLocationRequest("$placeName",
            city,
            country,
            mAddress,
            mLatitude,
            mLongitude,
            placeId,
            postalCode,
            state,
            suburb,
            apiAddress1)
        try {
            view?.let{
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
}

