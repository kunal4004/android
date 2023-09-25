package za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.Context
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.Session
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.User
import za.co.woolworths.financial.services.android.recommendations.data.response.request.Event

@Parcelize
data class PrepareChangeAttributeRequestEvent(
    val context: Context? = null,
    val events: List<Event>? = null,
    val session: Session? = null,
    val user: User? = null
): Parcelable