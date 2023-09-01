package za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request

data class Context(
    val device: Device? = null,
    val page: Page? = null,
    val channel: String? = null,
    val pageAttributes: PageAttributes? = null
)