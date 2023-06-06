package za.co.woolworths.financial.services.android.dynamicyield.data.response.request

import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.Context
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.Session
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.User

data class DynamicVariantRequestEvent(
    val contextDY: Context,
    val options: Options,
    val session: Session,
    val user: User
)