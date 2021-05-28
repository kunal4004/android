package za.co.woolworths.financial.services.android.checkout.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import com.google.android.gms.location.places.ui.PlaceSelectionListener
import kotlinx.android.synthetic.main.checkout_add_address_new_user.*


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
        /*val autocompleteFragment =
            activity?.supportFragmentManager?.findFragmentById(R.id.place_autocomplete_fragment) as? PlaceAutocompleteFragment

        val filter = AutocompleteFilter.Builder()
            .setCountry("IN")
            .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
            .build()
        autocompleteFragment!!.setFilter(filter)

        autocompleteFragment!!.setOnPlaceSelectedListener(
            object : PlaceSelectionListener {
                override fun onPlaceSelected(place: Place) {
                    placeName.setText(place.getName())
                }

                override fun onError(status: Status) {
                    placeName.setText(status.toString())
                }
            })*/
    }
}