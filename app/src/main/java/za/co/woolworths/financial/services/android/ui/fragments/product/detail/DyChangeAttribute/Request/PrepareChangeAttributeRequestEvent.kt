package za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request

import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.Context
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.Session
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.User


data class PrepareChangeAttributeRequestEvent(
    val context: Context? = null,
    val events: List<za.co.woolworths.financial.services.android.recommendations.data.response.request.Event>? = null,
    val session: Session? = null,
    val user: User? = null
)