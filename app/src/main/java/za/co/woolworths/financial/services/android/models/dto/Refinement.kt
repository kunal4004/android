package za.co.woolworths.financial.services.android.models.dto

data class Refinement(var count: String, var label: String, var navigationType: String, var displayName: String,
                      var navigationState: String, var multiSelect
                      : Boolean, var subRefinements: ArrayList<SubRefinement>) {
}