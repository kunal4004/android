package za.co.woolworths.financial.services.android.models.dto

data class RefinementCrumb(var count: Int, var label: String, var dimensionName: String, var displayName: String,
                           var navigationState: String, var multiSelect: Boolean) {
    var isMultiSelectTrueForRefinementCrumbs: Boolean = false
}