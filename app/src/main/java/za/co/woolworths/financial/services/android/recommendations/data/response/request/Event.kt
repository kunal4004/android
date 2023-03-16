package za.co.woolworths.financial.services.android.recommendations.data.response.request

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Event(
    val eventType: String?,
    val url: String?,
    val pageType: String?,
    val categories: List<String>?,
    val products: List<ProductX>?,
    val cartLines: List<CartProducts?>?,
    val orderId: String? = null,
    val purchaseLines: List<CartProducts?>? = null,
) : Parcelable