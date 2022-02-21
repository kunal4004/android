package za.co.woolworths.financial.services.android.geolocation.view

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import java.io.IOException
import java.util.*

class NewScreenAddressMap : Fragment(), OnMapReadyCallback {

    companion object {
        fun newInstance() = NewScreenAddressMap()
    }

    var searchView: SearchView? = null
    private var mMap: GoogleMap? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.geolocation_confirm_address, container, false)
    }

    override fun onViewCreated(
        view: View, savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        activity?.applicationContext?.let { context ->
            Places.initialize(context, getString(R.string.maps_api_key))
            val placesClient = Places.createClient(context)
            val mapFragment =
                childFragmentManager.findFragmentById(R.id.geoloc_Map) as SupportMapFragment?
            mapFragment?.getMapAsync(this)
            val token = AutocompleteSessionToken.newInstance()
            val request =
                FindAutocompletePredictionsRequest.builder() // Call either setLocationBias() OR setLocationRestriction().
                    .setCountry("ZA")
                    .setTypeFilter(TypeFilter.ADDRESS)
                    .setSessionToken(token)
                    .build()
            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                    for (prediction in response.autocompletePredictions) {
                        // Log.i(TAG, prediction.getPlaceId());
                        //Log.i(TAG, prediction.getPrimaryText(null).toString());
                    }
                }.addOnFailureListener { exception: Exception? -> }
            val autocompleteFragment =
                childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment?
            autocompleteFragment?.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME))
            autocompleteFragment?.setOnPlaceSelectedListener(object : PlaceSelectionListener {
                override fun onPlaceSelected(place: Place) {
                    val location = place.name
                    var addressList: List<Address>? = null
                    if (location != null || location == "") {
                        val geocoder = Geocoder(context)
                        try {
                            addressList = geocoder.getFromLocationName(location, 1)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        val address = addressList?.get(0)
                        val latLng = address?.latitude?.let { LatLng(it, address?.longitude) }
                        mMap?.addMarker(latLng?.let {
                            MarkerOptions().position(it).title(location)
                        })
                        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
                        Log.i("TAG", "Place: " + place.name + ", " + place.id)
                    }
                }

                override fun onError(status: Status) {
                    Log.i("TAG", "An error occurred: $status")
                }


            })
            val confirmAdd =view.findViewById(R.id.btnConfirmAddress) as Button
            confirmAdd.setOnClickListener {
                (activity as? BottomNavigationActivity)?.pushFragmentSlideUp(
                    DeliveryClickCollectChanges()
                )
            }
        }

    }

    override fun onMapReady(p0: GoogleMap?) {

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap?.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap?.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

}



