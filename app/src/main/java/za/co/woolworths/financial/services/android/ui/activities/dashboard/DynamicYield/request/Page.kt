package za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request

data class Page(
    val `data`: List<Any>,
    val location: String,
    val type: String
)