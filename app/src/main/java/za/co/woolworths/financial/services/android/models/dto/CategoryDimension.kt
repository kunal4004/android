package za.co.woolworths.financial.services.android.models.dto

data class CategoryDimension(var count: String, var label: String, var displayName: String,
                             var dimensionName: String, var multiSelect
                             : Boolean, var breadCrumbs: ArrayList<BreadCrumb>) {
}