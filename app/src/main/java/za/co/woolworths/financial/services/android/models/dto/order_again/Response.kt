package za.co.woolworths.financial.services.android.models.dto.order_again

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Response(
    val requestId: String? = null,
    val actions: List<Action>? = null,
    val code: String? = null,
    val desc: String? = null
) : Parcelable