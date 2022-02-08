package za.co.woolworths.financial.services.android.models.dto

data class RefinementHistory(var searchCrumbs
                             : MutableList<SearchCrumb>?, var categoryDimensions
                             : ArrayList<CategoryDimension>, var navigationType: String) {
}