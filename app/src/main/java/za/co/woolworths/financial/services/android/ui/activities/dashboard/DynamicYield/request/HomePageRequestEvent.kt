package za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request

data class HomePageRequestEvent(
    val user: User? = null,
    val session: Session? = null,
    val context: Context? = null,
    val options: Options? = null
)