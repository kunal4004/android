package za.co.woolworths.financial.services.android.models.dto.order_again

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OrderAgainResponse(
    val meta: Meta? = null,
    val data: Data? = null,
    val response: Response? = null,
    var httpCode: Int? = null
) : Parcelable