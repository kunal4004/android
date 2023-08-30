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
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.DURATION_0_MS
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.DURATION_120000_MS
import za.co.woolworths.financial.services.android.util.UnIndexedAddressIdentifiedListener
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException


class GooglePlacesAdapter(context: Activity, geoData: PlacesClient,unIndexedAddressIdentifiedListener: UnIndexedAddressIdentifiedListener) : BaseAdapter(),
    Filterable {

    companion object {
        const val SEARCH_LENGTH = 3
    }

    private var token: AutocompleteSessionToken? = null
    private var mResultList = arrayListOf<PlaceAutocomplete>()
    private val placesClient = geoData
    private val mContext = context
    private var startingTime: Long = System.currentTimeMillis()
    private var currentTime: Long = DURATION_0_MS
    private var mUnIndexedAddressIdentifiedListener:UnIndexedAddressIdentifiedListener=unIndexedAddressIdentifiedListener


    override fun getCount(): Int {
        return mResultList.size
    }

    override fun getItem(position: Int): PlaceAutocomplete {
        return mResultList[position]
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
            mHolder.primaryTextView =
                convrtView.findViewById<View>(R.id.primaryTextView) as? TextView
            mHolder.secondaryTextView =
                convrtView.findViewById<View>(R.id.SecondaryTextView) as? TextView
            convrtView.tag = mHolder
        } else {
            mHolder = convrtView.tag as ViewHolder
        }
        mHolder.dividerRecyclerView =
            convrtView?.findViewById(R.id.dividerRecyclerView) as? View
        mHolder.dividerRecyclerView?.visibility = View.VISIBLE
        mHolder.primaryTextView?.visibility = View.VISIBLE
        mHolder.secondaryTextView?.visibility = View.VISIBLE
        val item = getItem(position)
        mHolder.primaryTextView?.text = item.primaryText
        mHolder.secondaryTextView?.text = item.secondaryText
        return convrtView
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                // Skip the autocomplete query if no constraints or less than 3 char is given.
                if (constraint != null && constraint.toString().trim().length >= SEARCH_LENGTH) {
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
                    if(constraint != null && constraint.toString().trim().length >= SEARCH_LENGTH){
                        mUnIndexedAddressIdentifiedListener.unIndexedAddressIdentified()
                        notifyDataSetInvalidated()
                    }
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
        //this logic added for Google Api's cost optimization.
        if (token == null && currentTime == DURATION_0_MS) {
            token = AutocompleteSessionToken.newInstance()
        } else {
            currentTime = System.currentTimeMillis()
            //this logic added for Google Api's cost optimization.
            //after 2 min token need to be change
            if (currentTime - startingTime >= DURATION_120000_MS) {
                token = AutocompleteSessionToken.newInstance()
                startingTime = currentTime
            }
        }
        val request = FindAutocompletePredictionsRequest.builder()
            .setCountry("ZA")
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
                        prediction.getPrimaryText(null).toString(),
                        prediction.getSecondaryText(null).toString(),
                        token
                    )
                )
            }
        }
        return resultList
    }
}

internal class ViewHolder {
    var primaryTextView: TextView? = null
    var secondaryTextView: TextView? = null
    var dividerRecyclerView: View? = null
}

class PlaceAutocomplete(
    var placeId: CharSequence,
    var primaryText: CharSequence,
    var secondaryText: CharSequence,
    var token:AutocompleteSessionToken?
) {

    override fun toString(): String {
        return placeId.toString()
    }
}