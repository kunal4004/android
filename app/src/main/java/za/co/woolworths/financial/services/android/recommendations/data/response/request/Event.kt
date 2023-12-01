package za.co.woolworths.financial.services.android.recommendations.data.response.request

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request.Properties
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.usecase.Constants
import za.co.woolworths.financial.services.android.ui.wfs.common.getIpAddress
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.getDeliveryType
import za.co.woolworths.financial.services.android.util.wenum.Delivery

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
) : RecommendationEvent

interface RecommendationEvent: Parcelable

sealed class Recommendation : RecommendationEvent {
    @Parcelize
    data class PageView(
        val eventType: String?,
        val url: String? = null,
        val pageType: String? = null): Recommendation()

    @Parcelize
    data class ShoppingListEvent(
        val eventType: String?,
        val products: List<Product>? = null,
    ): Recommendation()

    @Parcelize
    data class CustomVariables(
        val eventType: String?,
        val customVariables: List<CustomVariable>? = null,
    ): Recommendation()
}

@Parcelize
data class CustomVariable(val variable: String, val value: String ): Parcelable
@Parcelize
data class Product(val productId: String): Parcelable

object CommonRecommendationEvent {

    fun commonRecommendationEvents(): List<RecommendationEvent> {
        return listOf(userAgentEvent(), ipAddressEvent(), customVariables())
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

    private fun customVariables(): RecommendationEvent {
        return  Recommendation.CustomVariables(
            eventType = Constants.Event_TYPE_CUSTOM_VARIABLES,
            customVariables = listOf(CustomVariable(Constants.DELIVERY_TYPE, deliveryType()))
        )
    }

    private fun deliveryType(): String {
        return when(Delivery.getType(getDeliveryType()?.deliveryType)) {
            Delivery.CNC -> Constants.DELIVERY_TYPE_CNC
            Delivery.DASH -> Constants.DELIVERY_TYPE_DASH
            else -> Constants.DELIVERY_TYPE_STANDARD
        }
    }
}