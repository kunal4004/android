package za.co.woolworths.financial.services.android.chanel.utils

import android.view.View
import kotlinx.android.synthetic.main.fragment_brand_landing.view.*

const val SEARCH_TERM = "searchTerm"
const val SEARCH_TYPE = "searchType"
const val FILTER_CONTENT = "filterContent"

fun View.setProgressIndicator(isLoading: Boolean) {
    if (isLoading)
        incCenteredProgress.visibility = View.VISIBLE
    else
        incCenteredProgress.visibility = View.GONE
}