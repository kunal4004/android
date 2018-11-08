package za.co.woolworths.financial.services.android.models.dto

data class RefinementHistory(var searchCrumbs
                             : ArrayList<SearchCrumb>, var categoryDimensions
                             : ArrayList<CategoryDimension>, var navigationType: String) {
}