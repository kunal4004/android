package za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request

data class Page(
    val data: ArrayList<String>? = null,
    val location: String? = null,
    val type: String? = null,
    val dataProduct: ArrayList<DataProduct>? = null,
    val dataOther: ArrayList<DataOther>? = null
)