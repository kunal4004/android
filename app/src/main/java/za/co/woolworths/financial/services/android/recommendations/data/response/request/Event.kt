package za.co.woolworths.financial.services.android.recommendations.data.response.request

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request.Properties
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.usecase.Constants
import za.co.woolworths.financial.services.android.ui.wfs.common.getIpAddress

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
    val recClicks: List<String>? = null,
    val recImpressions: List<String>? = null,
    val name: String? = null,
    val properties: Properties? = null
) : Parcelable

object CommonRecommendationEvent {

    fun commonRecommendationEvents(): List<Event> {
        return listOf(userAgentEvent(), ipAddressEvent())
    }

    private fun userAgentEvent(): Event {
        return Event(
            eventType = Constants.EVENT_TYPE_USER_AGENT,
            userAgent = System.getProperty("http.agent") ?: ""
        )
    }

    private fun ipAddressEvent(): Event {
        return Event(
            eventType = Constants.Event_TYPE_IP_ADDRESS,
            ipAddress = getIpAddress(WoolworthsApplication.getInstance())
        )
    }
}