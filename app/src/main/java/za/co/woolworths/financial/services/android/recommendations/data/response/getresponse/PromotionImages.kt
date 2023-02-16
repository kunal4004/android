package za.co.woolworths.financial.services.android.recommendations.data.response.getresponse

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PromotionImages(
    val reduced: String?,
    val save: String?,
    val wRewards: String?,
    var vitality: String?
) : Parcelable