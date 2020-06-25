package za.co.woolworths.financial.services.android.models.dto

import com.google.gson.JsonElement

class OrderDetailsResponse {
    private val basketId: String? = null

    private val productLeadTimeExceeded: String? = null

    private val reservedDeliverySlots: String? = null

    private val itemLevelQualifierMesaages: String? = null

    private val isGiftCardPayment: String? = null

    private val discountDetails: String? = null

    private val message: String? = null

    private val skuIds: Array<String>? = null

    private val vouchers: String? = null

    val orderSummary: OrderSummary? = null

    var items: JsonElement? = null

    var httpCode: Int = 0

    var response: Response? = null
}