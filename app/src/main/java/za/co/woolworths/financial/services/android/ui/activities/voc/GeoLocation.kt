package za.co.woolworths.financial.services.android.ui.activities.voc

import android.location.Address
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.GoogleMap
import android.os.Bundle
import com.awfs.coordination.R
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import android.location.Geocoder
import android.util.Log
import androidx.appcompat.widget.SearchView
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import java.io.IOException
import java.lang.Exception
import java.util.*

class GeoLocation : FragmentActivity(), OnMapReadyCallback {
    var searchView: SearchView? = null
    private var mMap: GoogleMap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Places.initialize(applicationContext, getString(R.string.maps_api_key))
        val placesClient = Places.createClient(this)
        try {
            setContentView(R.layout.geolocation_confirm_address)
        } catch (e: Exception) {
            throw e
        }
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.geoloc_Map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
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
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment?
        autocompleteFragment!!.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME))
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val location = place.name
                var addressList: List<Address>? = null
                if (location != null || location == "") {
                    val geocoder = Geocoder(this@GeoLocation)
                    try {
                        addressList = geocoder.getFromLocationName(location, 1)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    val address = addressList!![0]
                    val latLng = LatLng(address.latitude, address.longitude)
                    mMap!!.addMarker(MarkerOptions().position(latLng).title(location))
                    mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
                    Log.i("TAG", "Place: " + place.name + ", " + place.id)
                }
            }

            override fun onError(status: Status) {
                Log.i("TAG", "An error occurred: $status")
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }
}