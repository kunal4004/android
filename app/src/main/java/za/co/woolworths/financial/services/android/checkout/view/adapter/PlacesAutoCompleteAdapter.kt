package za.co.woolworths.financial.services.android.checkout.view.adapter

import android.R
import android.content.Context
import android.graphics.Typeface
import android.text.style.CharacterStyle
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.*
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressNewUserFragment
import za.co.woolworths.financial.services.android.util.AuthenticateUtils.mContext
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException


/**
 * Created by Kunal Uttarwar on 02/06/21.
 */
class PlacesAutoCompleteAdapter : RecyclerView.Adapter<PlacesAutoCompleteAdapter.PredictionHolder>,
    Filterable {
    constructor(it: Context) {
        mContext = it
        STYLE_BOLD = StyleSpan(Typeface.BOLD)
        STYLE_NORMAL = StyleSpan(Typeface.NORMAL)
        placesClient = Places.createClient(mContext!!)
    }

    companion object {
        var mResultList: ArrayList<PlaceAutocomplete>? = ArrayList()
        var placesClient: PlacesClient? = null
        var clickListener: ClickListener? = null
    }

    private var mContext: Context? = null
    private var STYLE_BOLD: CharacterStyle? = null
    private var STYLE_NORMAL: CharacterStyle? = null

    fun setClickListener(clickListner: ClickListener?) {
        clickListener = clickListner
    }

    interface ClickListener {
        fun click(place: Place?)
    }

    /**
     * Returns the filter for the current set of autocomplete results.
     */
    override fun getFilter(): Filter? {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults? {
                val results = FilterResults()
                // Skip the autocomplete query if no constraints are given.
                if (constraint != null && constraint.length >= CheckoutAddAddressNewUserFragment.SEARCH_LENGTH) {
                    // Query the autocomplete API for the (constraint) search string.
                    mResultList = getPredictions(constraint)
                    if (mResultList != null) {
                        results.values = mResultList
                        results.count = mResultList!!.size
                    }
                }
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null && results.count > 0) {
                    // The API returned at least one result, update the data.
                    mResultList = results.values as ArrayList<PlaceAutocomplete>
                    notifyDataSetChanged()
                } else {
                    // The API did not return any results, invalidate the data set.
                    //notifyDataSetInvalidated()
                }
            }
        }
    }

    private fun getPredictions(constraint: CharSequence): ArrayList<PlaceAutocomplete>? {
        val resultList: ArrayList<PlaceAutocomplete> = ArrayList()
        val token = AutocompleteSessionToken.newInstance()
        val request =
            FindAutocompletePredictionsRequest.builder() // Call either setLocationBias() OR setLocationRestriction().
                .setCountry("ZA")
                .setTypeFilter(TypeFilter.ADDRESS)
                .setSessionToken(token)
                .setQuery(constraint.toString())
                .build()
        val autocompletePredictions: Task<FindAutocompletePredictionsResponse> =
            placesClient!!.findAutocompletePredictions(request)
        try {
            Tasks.await(autocompletePredictions, 60, TimeUnit.SECONDS)
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: TimeoutException) {
            e.printStackTrace()
        }
        return if (autocompletePredictions.isSuccessful()) {
            val findAutocompletePredictionsResponse: FindAutocompletePredictionsResponse =
                autocompletePredictions.getResult()
            if (findAutocompletePredictionsResponse != null) for (prediction in findAutocompletePredictionsResponse.autocompletePredictions) {
                resultList.add(
                    PlaceAutocomplete(
                        prediction.placeId,
                        prediction.getFullText(null).toString()
                    )
                )
            }
            resultList
        } else {
            resultList
        }
    }

    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, i: Int): PredictionHolder {
        val layoutInflater =
            mContext?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val convertView: View =
            layoutInflater.inflate(R.layout.simple_list_item_1, parent, false)
        return PredictionHolder(convertView)
    }

    override fun onBindViewHolder(@NonNull mPredictionHolder: PredictionHolder, i: Int) {
        mPredictionHolder.address.text = mResultList!![i].address
    }

    override fun getItemCount(): Int {
        return mResultList!!.size
    }

    fun getItem(position: Int): PlaceAutocomplete? {
        return mResultList!![position]
    }

    class PredictionHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val address: TextView
        override fun onClick(v: View) {
            val item: PlaceAutocomplete = mResultList!!.get(adapterPosition)
            if (v.getId() === R.id.text1) {
                val placeId = item.placeId.toString()
                val placeFields: MutableList<Place.Field>? = Arrays.asList(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.LAT_LNG,
                    Place.Field.ADDRESS
                )
                val request = placeFields?.let { FetchPlaceRequest.builder(placeId, it).build() }
                request?.let {
                    placesClient?.fetchPlace(it)
                        ?.addOnSuccessListener(object : OnSuccessListener<FetchPlaceResponse?> {
                            override fun onSuccess(response: FetchPlaceResponse?) {
                                val place = response!!.place
                                clickListener?.click(place)
                            }
                        })?.addOnFailureListener(object : OnFailureListener {
                            override fun onFailure(@NonNull exception: Exception) {
                                if (exception is ApiException) {
                                    Toast.makeText(
                                        mContext,
                                        exception.message + "",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            }
                        })
                }
            }
        }

        init {
            address = itemView.findViewById(R.id.text1)
            itemView.setOnClickListener(this)
        }
    }

    /**
     * Holder for Places Geo Data Autocomplete API results.
     */
    class PlaceAutocomplete internal constructor(
        var placeId: CharSequence,
        var address: CharSequence
    ) {
        override fun toString(): String {
            return address.toString()
        }
    }
}