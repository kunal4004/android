package za.co.woolworths.financial.services.android.models.network

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Parameter(
    val orderID: String?
) : Parcelable