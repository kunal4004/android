package za.co.woolworths.financial.services.android.recommendations.data.response.getresponse

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RecommendationResponse(
    val actions: List<Action>?,
    val httpCode: Int?,
    val monetateId: String?,
    val response: ResponseCode?,
    val title: String?,
) : Parcelable