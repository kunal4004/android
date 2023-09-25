package za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Device(
    val ip: String? = null,
    val userAgent: String? = null
): Parcelable