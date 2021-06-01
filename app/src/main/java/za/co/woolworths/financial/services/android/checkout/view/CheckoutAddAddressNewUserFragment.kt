package za.co.woolworths.financial.services.android.checkout.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import com.google.android.libraries.places.api.Places
import kotlinx.android.synthetic.main.checkout_add_address_new_user.*
import kotlinx.android.synthetic.main.checkout_new_user_address_details.*
import za.co.woolworths.financial.services.android.checkout.view.adapter.GooglePlacesAdapter
import za.co.woolworths.financial.services.android.checkout.view.adapter.PlaceAutocomplete


/**
 * Created by Kunal Uttarwar on 26/05/21.
 */
class CheckoutAddAddressNewUserFragment : Fragment() {

    private val deliveringOptionsList: ArrayList<String> = ArrayList()
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
        showWhereAreWeDeliveringView()
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
    }

    private fun showWhereAreWeDeliveringView() {
        deliveringOptionsList.add("Home")
        deliveringOptionsList.add("Office")
        deliveringOptionsList.add("Complex/Estate")
        deliveringOptionsList.add("Apartment")

        for (options in deliveringOptionsList) {
            val view = View.inflate(context, R.layout.where_are_we_delivering_items, null)
            val title: TextView? = view?.findViewById(R.id.titleTv)
            title?.text = options
            delivering_layout?.addView(view)
        }
    }
}