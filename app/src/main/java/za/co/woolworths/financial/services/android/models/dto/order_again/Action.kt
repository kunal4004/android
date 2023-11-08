package za.co.woolworths.financial.services.android.models.dto.order_again

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Action(
    val impressionReporting: List<ImpressionReporting>? = null,
    val actionType: String? = null,
    val actionId: Int? = null,
    val isControl: Boolean? = null,
    val items: List<Item>? = null,
    val actionEvents: List<String>? = null,
    val component: String? = null
) : Parcelable