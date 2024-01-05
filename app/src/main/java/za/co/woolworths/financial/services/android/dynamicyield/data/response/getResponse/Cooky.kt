package za.co.woolworths.financial.services.android.dynamicyield.data.response.getResponse

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Cooky(
    val maxAge: String,
    val name: String,
    val value: String
): Parcelable