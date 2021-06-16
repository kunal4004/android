package za.co.woolworths.financial.services.android.checkout.view.adapter

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.awfs.coordination.R
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException


class GooglePlacesAdapter(context: Activity, geoData: PlacesClient) : BaseAdapter(),
    Filterable {

    companion object {
        const val SEARCH_LENGTH = 3
    }

    private var mResultList = arrayListOf<PlaceAutocomplete>()
    private val placesClient = geoData
    private val mContext = context


    override fun getCount(): Int {
        return mResultList.size
    }

    override fun getItem(position: Int): PlaceAutocomplete {
        return mResultList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var convrtView = convertView
        val mHolder: ViewHolder
        if (convrtView == null) {
            mHolder = ViewHolder()
            convrtView =
                mContext.layoutInflater.inflate(R.layout.places_simple_item_list, null)
            mHolder.text1 = convrtView.findViewById<View>(R.id.text1) as? TextView
            convrtView.tag = mHolder
        } else {
            mHolder = convrtView.tag as ViewHolder
        }
        /*val shimmer = Shimmer.AlphaHighlightBuilder().build()
        val layout =
            (row.findViewById<View>(R.id.recyclerViewShimmerFrameLayout) as? ShimmerFrameLayout)
        layout?.setShimmer(shimmer)
        layout?.startShimmer()*/
        val item = getItem(position)
        mHolder.text1?.text = item.description
        return convrtView
    }

    internal class ViewHolder {
        var text1: TextView? = null
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                // Skip the autocomplete query if no constraints or less than 3 char is given.
                if (constraint != null && constraint.length >= SEARCH_LENGTH) {
                    mResultList = getPredictions(constraint)
                    results.values = mResultList
                    results.count = mResultList.size
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
                    notifyDataSetInvalidated()
                }
            }

            override fun convertResultToString(resultValue: Any): CharSequence {
                // To display a readable result in the AutocompleteTextView when clicked.
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
        val token = AutocompleteSessionToken.newInstance()
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
                autocompletePredictions.result
            for (prediction in findAutocompletePredictionsResponse.autocompletePredictions) {
                resultList.add(
                    PlaceAutocomplete(
                        prediction.placeId,
                        prediction.getFullText(null).toString()
                    )
                )
            }
        }
        return resultList
    }
}

class PlaceAutocomplete(var placeId: CharSequence, var description: CharSequence) {

    override fun toString(): String {
        return description.toString()
    }
}