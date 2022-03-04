package za.co.woolworths.financial.services.android.geolocation.view

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
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
import za.co.woolworths.financial.services.android.checkout.view.adapter.GooglePlacesAdapter
import za.co.woolworths.financial.services.android.checkout.view.adapter.PlaceAutocomplete
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.VtoErrorBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.listener.VtoTryAgainListener
import za.co.woolworths.financial.services.android.util.ConnectivityLiveData
import za.co.woolworths.financial.services.android.geolocation.model.GeoLocationAddress
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.util.FirebaseManager
import za.co.woolworths.financial.services.android.util.KeyboardUtils.Companion.hideKeyboard
import za.co.woolworths.financial.services.android.util.NetworkManager
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ConfirmAddressMapFragment(val latitude: Double?, val longitude: Double?,var isAddAddress:Boolean) :
    Fragment(R.layout.geolocation_confirm_address), OnMapReadyCallback, VtoTryAgainListener {

    private var mMap: GoogleMap? = null
    private var mAddress: String? = null
    private var placeId: String? = null
    private var latLng: LatLng? = null
    private var mLatitude: String? = null
    private var mLongitude: String? = null
    @Inject
    lateinit var vtoErrorBottomSheetDialog: VtoErrorBottomSheetDialog


    override fun onViewCreated(
        view: View, savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        if (NetworkManager.getInstance().isConnectedToNetwork(activity)) {
            initMap()
        } else {
            mapFrameLayout?.visibility = View.GONE
            imgMapMarker?.visibility = View.GONE
            autoCompleteTextView?.isEnabled = false
            showErrorDialog()

        }
        clearAddress()
        confirmAddressClick()

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
                    mapFragment?.view?.visibility = View.VISIBLE
                    mapFrameLayout?.visibility = View.VISIBLE
                    imgMapMarker?.visibility = View.VISIBLE
                    autoCompleteTextView?.isEnabled = true
                    confirmAddress?.isEnabled = true
                    if(isAddAddress){
                        confirmAddress?.isEnabled = false
                        imgMapMarker?.visibility = View.GONE

                    }
                } else {
                    mapFragment?.view?.visibility = View.GONE
                    imgMapMarker?.visibility = View.GONE
                    autoCompleteTextView?.isEnabled = false
                    confirmAddress?.isEnabled = false
                    showErrorDialog()
                }

            })
        }
    }

    private fun clearAddress() {
        imgRemoveAddress?.setOnClickListener {
            autoCompleteTextView?.setText("")
        }
    }

    private fun confirmAddressClick() {
        btnConfirmAddress?.setOnClickListener {
            if (mLatitude != null && mLongitude != null && placeId != null) {
                (activity as? BottomNavigationActivity)?.pushFragmentSlideUp(
                    GeolocationDeliveryAddressConfirmationFragment.newInstance(mLatitude!!,
                        mLongitude!!,
                        placeId!!))
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
            autoCompleteTextView?.apply {
                setAdapter(placesAdapter)
            }
            autoCompleteTextView?.onItemClickListener =
                AdapterView.OnItemClickListener { parent, _, position, _ ->
                    val item = parent.getItemAtPosition(position) as? PlaceAutocomplete
                    placeId = item?.placeId.toString()
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
                                        autoCompleteTextView?.setText("")
                                        val address = addressList?.get(0)
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

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (!isAddAddress) {
            mMap?.uiSettings?.setAllGesturesEnabled(true)
            latLng = longitude?.let { latitude?.let { it1 -> LatLng(it1, it) } }
            moveMapCamera(latLng)
        } else {
            imgMapMarker?.visibility = View.GONE
            confirmAddress?.isEnabled = false
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE), 10f))

        }

    }

    private fun moveMapCamera(latLng: LatLng?) {
        imgMapMarker?.visibility = View.VISIBLE
        confirmAddress?.isEnabled = true
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

    private fun getAddressFromLatLng(latitude: Double, longitude: Double) {
        try {
            val geocoder = Geocoder(requireActivity(), Locale.getDefault())
            val address: MutableList<Address> = geocoder.getFromLocation(latitude, longitude, 1)
            address.let {
                mAddress = it[0].getAddressLine(0)
            }
        } catch (e: Exception) {
            FirebaseManager.logException(e)
        }
        mAddress.let {
            autoCompleteTextView?.setText(it)
            autoCompleteTextView.dismissDropDown()
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
        placeId = results[0].placeId.toString()
       GeoLocationAddress(mLatitude,mLongitude,placeId)

    }

    companion object {
        private const val DEFAULT_LATITUDE = -33.918861
        private const val DEFAULT_LONGITUDE = 18.423300
    }

    override fun tryAgain() {
        //Do nothing
    }

}

