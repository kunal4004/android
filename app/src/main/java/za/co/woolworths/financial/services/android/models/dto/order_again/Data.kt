package za.co.woolworths.financial.services.android.models.dto.order_again

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Data(
     val responses: List<Response>? = null
) : Parcelable