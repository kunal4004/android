package za.co.woolworths.financial.services.android.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OutOfStock(
    val minimumSupportedAppBuildNumber: Int?,
    var isOutOfStockEnabled: Boolean? = false
) : Parcelable
