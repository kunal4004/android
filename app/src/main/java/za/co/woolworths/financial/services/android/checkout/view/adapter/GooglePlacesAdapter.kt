package za.co.woolworths.financial.services.android.checkout.view.adapter

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.collections.ArrayList


class GooglePlacesAdapter(
    context: Context,
    resourceId: Int,
    geoData: PlacesClient
) :
    ArrayAdapter<PlaceAutocomplete>(context, resourceId),
    Filterable {

    private val TAG = "PlaceAutoAdapter"
    val mContext = context

    var mResultList = arrayListOf<PlaceAutocomplete>()
    val placesClient = geoData
    override fun getCount(): Int {
        return mResultList.size
    }

    override fun getItem(position: Int): PlaceAutocomplete {
        return mResultList.get(position)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val row = super.getView(position, convertView, parent)
        val item = getItem(position)
        val textView1 = row.findViewById<View>(android.R.id.text1) as TextView
        textView1.text = item?.description

        return row
    }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {
                val results = Filter.FilterResults()
                // Skip the autocomplete query if no constraints are given.
                if (constraint != null && constraint.length>=3) {
                    mResultList = getPredictions(constraint)
                    results.values = mResultList
                    results.count = mResultList.size
                }

                return results
            }

            override fun publishResults(constraint: CharSequence?, results: Filter.FilterResults?) {
                Log.v("results", "results==" + results);
                if (results != null && results.count > 0) {
                    // The API returned at least one result, update the data.
                    mResultList = results.values as ArrayList<PlaceAutocomplete>
                    Log.v("resultList", "resultList==" + mResultList);
                    notifyDataSetChanged()
                } else {
                    // The API did not return any results, invalidate the data set.
                    notifyDataSetInvalidated()
                }
            }

            override fun convertResultToString(resultValue: Any): CharSequence {
                // Override this method to display a readable result in the AutocompleteTextView
                // when clicked.
                return if (resultValue is AutocompletePrediction) {
                    resultValue.getFullText(null)
                } else {
                    super.convertResultToString(resultValue)
                }
            }

        }
    }

    fun getPredictions(constraint: CharSequence): ArrayList<PlaceAutocomplete> {
        val resultList = arrayListOf<PlaceAutocomplete>()
        // and once again when the user makes a selection (for example when calling fetchPlace()).
        val token = AutocompleteSessionToken.newInstance()

        // Include address, ID, and phone number.
        val placeFields: List<Place.Field> = listOf(
            Place.Field.ADDRESS_COMPONENTS,
            Place.Field.NAME
        )
        // Use the builder to create a FindAutocompletePredictionsRequest.
        val request = FindAutocompletePredictionsRequest.builder()
            .setCountry("ZA")
            .setTypeFilter(TypeFilter.ADDRESS)
            .setSessionToken(token)
            .setQuery(constraint.toString())
            .build()


        val autocompletePredictions: Task<FindAutocompletePredictionsResponse> =
            placesClient.findAutocompletePredictions(request)
        try {
            Tasks.await(autocompletePredictions, 60, TimeUnit.SECONDS)
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: TimeoutException) {
            e.printStackTrace()
        }


        if (autocompletePredictions.isSuccessful) {
            val findAutocompletePredictionsResponse: FindAutocompletePredictionsResponse =
                autocompletePredictions.result!!
            for (prediction in findAutocompletePredictionsResponse.autocompletePredictions) {
                Log.i(TAG, prediction.placeId)
                Log.i(TAG, prediction.getPrimaryText(null).toString())

                resultList.add(
                    PlaceAutocomplete(
                        prediction.placeId,
                        prediction.getFullText(null).toString()
                    )
                )
            }
            return resultList;

        } else {
            return resultList;
        }
    }

}

class PlaceAutocomplete(var placeId: CharSequence, var description: CharSequence) {

    override fun toString(): String {
        return description.toString()
    }
}