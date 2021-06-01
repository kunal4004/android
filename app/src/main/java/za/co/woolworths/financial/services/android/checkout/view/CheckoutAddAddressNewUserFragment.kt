package za.co.woolworths.financial.services.android.checkout.view

import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import kotlinx.android.synthetic.main.checkout_new_user_address_details.*
import za.co.woolworths.financial.services.android.checkout.view.adapter.GooglePlacesAdapter
import za.co.woolworths.financial.services.android.checkout.view.adapter.PlaceAutocomplete
import za.co.woolworths.financial.services.android.ui.fragments.wtoday.WTodayExtension
import java.util.*


/**
 * Created by Kunal Uttarwar on 26/05/21.
 */
class CheckoutAddAddressNewUserFragment : Fragment() {
    private var navController: NavController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.checkout_add_address_new_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        init()
    }

    private fun init() {
        activity?.applicationContext?.let {
            Places.initialize(it, getString(R.string.maps_api_key))
            var placesClient = Places.createClient(it)
            val placesAdapter =
                GooglePlacesAdapter(it, android.R.layout.simple_list_item_1, placesClient)
            autoCompleteTextView?.apply {
                setAdapter(placesAdapter)
            }
            autoCompleteTextView?.onItemClickListener =
                AdapterView.OnItemClickListener { parent, _, position, _ ->
                    val place = parent.getItemAtPosition(position) as PlaceAutocomplete
                    autoCompleteTextView.apply {
                        setText(place.description)
                        setSelection(autoCompleteTextView.length())
                    }
                }
        }

        /*var autoCompleteSupportFragment =
            childFragmentManager?.findFragmentById(R.id.autocomplete_fragment) as? AutocompleteSupportFragment


        autoCompleteSupportFragment?.setPlaceFields(
            listOf(
                com.google.android.libraries.places.api.model.Place.Field.ID,
                com.google.android.libraries.places.api.model.Place.Field.NAME,
                com.google.android.libraries.places.api.model.Place.Field.ADDRESS,
                com.google.android.libraries.places.api.model.Place.Field.ADDRESS_COMPONENTS,
                com.google.android.libraries.places.api.model.Place.Field.LAT_LNG
            )
        )
        autoCompleteSupportFragment?.setCountry("ZA")

        autoCompleteSupportFragment?.setOnPlaceSelectedListener(object :
            com.google.android.libraries.places.widget.listener.PlaceSelectionListener {
            override fun onPlaceSelected(place: com.google.android.libraries.places.api.model.Place) {
                Log.i(WTodayExtension.TAG, "Place: " + place.addressComponents + ", " + place.id);
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses =
                    place.latLng?.let { geocoder.getFromLocation(it.latitude, it.longitude, 1) }
                Log.i(WTodayExtension.TAG, "Place: " + addresses.toString());
            }

            override fun onError(p0: Status) {

            }
        })*/
    }
}