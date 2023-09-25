package za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DataProduct(
    val productId: String? = null,
    val skuIds: ArrayList<String>? = null
): Parcelable
