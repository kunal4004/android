package za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DataOther(
    val orderId: String?,
    val value: Double?,
    val currency: String?,
    val paymentOption: String?,
    val orderValue: Double?,
    val deliveryType: String?
): Parcelable
