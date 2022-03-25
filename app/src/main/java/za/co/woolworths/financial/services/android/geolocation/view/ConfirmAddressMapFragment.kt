package za.co.woolworths.financial.services.android.geolocation.view

import android.location.Address
import android.location.Geocoder
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
import kotlinx.android.synthetic.main.geolocation_confirm_address.autoCompleteTextView
import kotlinx.android.synthetic.main.no_connection.view.*
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.checkout.view.adapter.GooglePlacesAdapter
import za.co.woolworths.financial.services.android.checkout.view.adapter.PlaceAutocomplete
import za.co.woolworths.financial.services.android.geolocation.model.request.SaveAddressLocationRequest
import za.co.woolworths.financial.services.android.geolocation.network.apihelper.GeoLocationApiHelper
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.geolocation.viewmodel.GeoLocationViewModelFactory
import za.co.woolworths.financial.services.android.geolocation.viewmodel.LocationErrorLiveData
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.VtoErrorBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.listener.VtoTryAgainListener
import za.co.woolworths.financial.services.android.util.ConnectivityLiveData
import za.co.woolworths.financial.services.android.util.FirebaseManager
import za.co.woolworths.financial.services.android.util.KeyboardUtils.Companion.hideKeyboard
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ConfirmAddressMapFragment :
    Fragment(), OnMapReadyCallback, VtoTryAgainListener {

    private var mMap: GoogleMap? = null
    private var mAddress: String? = null
    private var placeId: String? = null
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
    private var isAddressFromSearch: Boolean = false
    private var isMoveMapCameraFirstTime: Boolean? = true
    private lateinit var confirmAddressViewModel: ConfirmAddressViewModel
    @Inject
    lateinit var vtoErrorBottomSheetDialog: VtoErrorBottomSheetDialog

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
        if (savedInstanceState == null) {
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
            } else {
                binding?.mapFrameLayout?.visibility = View.GONE
                binding?.imgMapMarker?.visibility = View.GONE
                binding?.autoCompleteTextView?.isEnabled = false
                noMapConnectionLayout?.no_connection_layout?.visibility = View.VISIBLE

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

    private fun checkNetwork(mapFragment: SupportMapFragment?) {
        activity?.let {
            ConnectivityLiveData.observe(viewLifecycleOwner, { isNetworkAvailable ->
                if (isNetworkAvailable) {
                    noMapConnectionLayout?.no_connection_layout?.visibility = View.GONE
                    mapFragment?.view?.visibility = View.VISIBLE
                    binding?.mapFrameLayout?.visibility = View.VISIBLE
                    binding?.imgMapMarker?.visibility = View.VISIBLE
                    binding?.autoCompleteTextView?.isEnabled = true
                    binding?.confirmAddress?.isEnabled = true
                    if(isAddAddress!!){
                        binding?.confirmAddress?.isEnabled = false
                        binding?.imgMapMarker?.visibility = View.GONE

                    }
                } else {
                    noMapConnectionLayout?.no_connection_layout?.visibility = View.VISIBLE
                    mapFragment?.view?.visibility = View.GONE
                    binding?.imgMapMarker?.visibility = View.GONE
                    binding?.autoCompleteTextView?.isEnabled = false
                    binding?.confirmAddress?.isEnabled = false

                }

            })
        }
    }

    private fun clearAddress() {
        binding?.imgRemoveAddress?.setOnClickListener {
            binding?.autoCompleteTextView?.setText("")
            binding?.errorMassageDivider?.visibility = View.GONE
            binding?.errorMessage?.visibility = View.GONE
        }
    }

    private fun confirmAddressClick() {

        binding?.confirmAddress?.setOnClickListener {
            val bundle = Bundle()
            if(isComingFromCheckout==true) {
                bundle?.apply {
                    putString(
                        DeliveryAddressConfirmationFragment.KEY_PLACE_ID, placeId
                    )
                    putBoolean(
                        ConfirmAddressFragment.IS_COMING_CONFIRM_ADD, true)
                    findNavController().navigate(
                        R.id.actionClickAndCollectStoresFragment,
                        bundleOf("bundle" to bundle)
                    )
                }
            } else {
                // normal geo flow
                if (mLatitude != null && mLongitude != null && placeId != null) {

                    bundle?.apply {
                        putString(
                            DeliveryAddressConfirmationFragment.KEY_LATITUDE, mLatitude
                        )
                        putString(
                            DeliveryAddressConfirmationFragment.KEY_LONGITUDE, mLongitude
                        )
                        putString(
                            DeliveryAddressConfirmationFragment.KEY_PLACE_ID, placeId
                        )
                    }
                    findNavController().navigate(
                        R.id.action_confirmAddressMapFragment_to_deliveryAddressConfirmationFragment,
                        bundleOf("bundle" to bundle)
                    )
                }
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
        LocationErrorLiveData.observe(viewLifecycleOwner, { isResult ->
            if (isResult) {
                binding?.errorMassageDivider?.visibility = View.VISIBLE
                binding?.errorMessage?.visibility = View.VISIBLE
            } else {
                binding?.errorMassageDivider?.visibility = View.GONE
                binding?.errorMessage?.visibility = View.GONE
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (!isAddAddress!!) {
            mMap?.uiSettings?.setAllGesturesEnabled(true)
            latLng = longitude?.let { latitude?.let { it1 -> LatLng(it1, it) } }
            if (isMoveMapCameraFirstTime == true){
                moveMapCamera(latLng)
            }

        } else {
            binding?.imgMapMarker?.visibility = View.GONE
            binding?.confirmAddress?.isEnabled = false
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE), 10f))

        }

    }

    private fun moveMapCamera(latLng: LatLng?) {
        binding?.imgMapMarker?.visibility = View.VISIBLE
        binding?.confirmAddress?.isEnabled = true
        isAddAddress = false
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
        mMap?.setOnCameraMoveListener {
            mMap?.setOnCameraIdleListener {
                val latLng = mMap?.cameraPosition?.target
                val latitude = latLng?.latitude
                val longitude = latLng?.longitude
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
                val streetName = it.getOrNull(0)?.thoroughfare
                if (streetName.isNullOrEmpty()) {
                    binding?.errorMessage?.visibility = View.VISIBLE
                } else {
                    binding?.errorMessage?.visibility = View.GONE
                }
            }

        } catch (e: Exception) {
            FirebaseManager.logException(e)
        }
        mAddress?.let {
            if (!isAddressFromSearch) {
                binding?.autoCompleteTextView?.setText(getString(R.string.geo_map_address,address1,city,state))
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
      sendAddressData()
    }

    companion object {
        private const val DEFAULT_LATITUDE = -33.918861
        private const val DEFAULT_LONGITUDE = 18.423300
    }

    override fun tryAgain() {
        //Do nothing
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

