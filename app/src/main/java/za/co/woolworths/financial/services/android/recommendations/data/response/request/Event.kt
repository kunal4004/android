package za.co.woolworths.financial.services.android.recommendations.data.response.request

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Event(
    val eventType: String?,
    val url: String? = null,
    val pageType: String? = null,
    val categories: List<String>? = null,
    val products: List<ProductX>? = null,
    val cartLines: List<CartProducts?>? = null,
    val orderId: String? = null,
    val purchaseLines: List<CartProducts?>? = null,
    val userAgent: String? = null,
    val ipAddress: String? = null,
) : Parcelable