package za.co.woolworths.financial.services.android.ui.fragments.geo_location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
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
import kotlinx.android.synthetic.main.reward_vip_exclusive_fragment.*
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.activities.WRewardBenefitActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.Utils
import java.io.IOException
import java.util.*

class GeoLocNewScreenAddressMap : Fragment() {

    companion object {
        fun newInstance() = GeoLocNewScreenAddressMap()
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
                requireActivity().supportFragmentManager.findFragmentById(R.id.geoloc_Map) as SupportMapFragment?
            mapFragment!!.getMapAsync(context)

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
                requireActivity().supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment?
            autocompleteFragment!!.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME))
            autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
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
        tvTermsCondition?.apply {
            activity?.apply {
                text =
                    WRewardBenefitActivity.convertWRewardCharacter(bindString(R.string.benefits_term_and_condition_link))
                setOnClickListener {
                    Utils.openLinkInInternalWebView(WoolworthsApplication.getWrewardsTCLink())
                }
            }
        }
    }

}

private fun SupportMapFragment.getMapAsync(context: Context) {

}
