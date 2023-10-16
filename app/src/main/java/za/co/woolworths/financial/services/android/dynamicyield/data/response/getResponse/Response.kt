package za.co.woolworths.financial.services.android.dynamicyield.data.response.getResponse

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Response(
    val code: String,
    val desc: String
): Parcelable