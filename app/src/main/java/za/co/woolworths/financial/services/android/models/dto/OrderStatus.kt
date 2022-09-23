package za.co.woolworths.financial.services.android.models.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OrderStatus(
    val deliveryStatus: String?,
    val isDone: Boolean = false
) : Parcelable
