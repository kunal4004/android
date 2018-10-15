package za.co.woolworths.financial.services.android.models.dto

data class RefinementNavigation(var displayName: String, var multiSelect
: Boolean, var refinementCrumbs: ArrayList<RefinementCrumb>, var refinements
                                : ArrayList<Refinement>) {
}