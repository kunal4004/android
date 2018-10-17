package za.co.woolworths.financial.services.android.models.dto

data class RefinementCrumb(var count: String, var label: String, var dimensionName: String, var displayName: String,
                           var navigationState: String, var multiSelect: Boolean) {
}