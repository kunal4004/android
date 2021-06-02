package za.co.woolworths.financial.services.android.checkout.view

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import kotlinx.android.synthetic.main.checkout_add_address_new_user.*
import kotlinx.android.synthetic.main.checkout_new_user_address_details.*
import za.co.woolworths.financial.services.android.checkout.view.adapter.PlacesAutoCompleteAdapter
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by Kunal Uttarwar on 26/05/21.
 */
class CheckoutAddAddressNewUserFragment : Fragment(), PlacesAutoCompleteAdapter.ClickListener {

    private val deliveringOptionsList: ArrayList<String> = ArrayList()
    var mAutoCompleteAdapter: PlacesAutoCompleteAdapter? = null
    private var navController: NavController? = null

    companion object {
        const val SEARCH_LENGTH = 3
    }

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
        /*activity?.applicationContext?.let {
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
        }*/



        activity?.applicationContext?.let {
            Places.initialize(it, getString(R.string.maps_api_key))
            autoCompleteTextView.addTextChangedListener(filterTextWatcher);

            mAutoCompleteAdapter = PlacesAutoCompleteAdapter(it)
            places_recycler_view.setLayoutManager(LinearLayoutManager(activity))
            mAutoCompleteAdapter!!.setClickListener(this);
            places_recycler_view.setAdapter(mAutoCompleteAdapter);
            mAutoCompleteAdapter!!.notifyDataSetChanged();
        }
    }

    private val filterTextWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            if (!s.toString().isNullOrEmpty() && s.toString().length >= SEARCH_LENGTH) {
                mAutoCompleteAdapter!!.filter!!.filter(s.toString())
                if (places_recycler_view.getVisibility() === View.GONE) {
                    places_recycler_view.setVisibility(View.VISIBLE)
                }
            } else {
                if (places_recycler_view.getVisibility() === View.VISIBLE) {
                    places_recycler_view.setVisibility(View.GONE)
                }
            }
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    }

    override fun click(place: Place?) {
        if (places_recycler_view.getVisibility() === View.VISIBLE) {
            places_recycler_view.setVisibility(View.GONE)
        }
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses =
            place?.latLng?.let { geocoder.getFromLocation(it.latitude, it.longitude, 1) }
        addresses?.let { setAddress(it) }
    }

    private fun setAddress(addresses: MutableList<Address>) {
        val address = addresses.get(0)
        autoCompleteTextView.apply {
            setText(address.featureName)
            setSelection(autoCompleteTextView.length())
        }
        provinceEditText.setText(address.countryName)
        postalCode.setText(address.postalCode)
    }


    private fun showWhereAreWeDeliveringView() {
        deliveringOptionsList.add("Home")
        deliveringOptionsList.add("Office")
        deliveringOptionsList.add("Complex/Estate")
        deliveringOptionsList.add("Apartment")

        for ((index, options) in deliveringOptionsList.withIndex()) {
            val view = View.inflate(context, R.layout.where_are_we_delivering_items, null)
            val titleTextView: TextView? = view?.findViewById(R.id.titleTv)
            titleTextView?.tag = index
            titleTextView?.text = options
            titleTextView?.setOnClickListener {
                resetOtherDeliveringTitle(it.tag as Int)
                // change background of selected textView
                it.background =
                    bindDrawable(R.drawable.checkout_delivering_title_round_button_pressed)
                (it as TextView).setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
            }
            delivering_layout?.addView(view)
        }
    }

    fun resetOtherDeliveringTitle(selectedTag: Int) {
        //change background of unselected textview
        for ((indx, option) in deliveringOptionsList.withIndex()) {
            if (indx != selectedTag) {
                val titleTextView: TextView? = view?.findViewWithTag(indx)
                titleTextView?.background =
                    bindDrawable(R.drawable.checkout_delivering_title_round_button)
                titleTextView?.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
            }
        }
    }
}