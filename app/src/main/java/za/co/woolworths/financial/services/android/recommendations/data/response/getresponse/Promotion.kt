package za.co.woolworths.financial.services.android.recommendations.data.response.getresponse

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Promotion(
    val promotionalText: String?,
    val searchTerm: String?,
) : Parcelable