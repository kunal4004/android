package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.SerializedName
import za.co.woolworths.financial.services.android.models.dto.OrderSummary

class ConfirmDeliveryAddressResponse {
    @SerializedName("timedDeliveryCosts")
    var timedDeliveryCosts: TimedDeliveryCosts? = null

    @SerializedName("outlyingAreaAvailableDays")
    var outlyingAreaAvailableDays: OutlyingAreaAvailableDays? = null

    @SerializedName("hasOnlyODDAvailableWithFood")
    var hasOnlyODDAvailableWithFood: Boolean? = null

    @SerializedName("fulfillmentTypes")
    var fulfillmentTypes: FulfillmentTypes? = null

    @SerializedName("orderSummary")
    var orderSummary: OrderSummary? = null

    @SerializedName("requiredToDisplayODD")
    var requiredToDisplayODD: Boolean? = null

    @SerializedName("sortedFoodDeliverySlots")
    var sortedFoodDeliverySlots: List<SortedFoodDeliverySlot>? = null

    @SerializedName("sortedJoinDeliverySlots")
    var sortedJoinDeliverySlots: List<SortedJoinDeliverySlot>? = null

    @SerializedName("openDayDeliverySlots")
    var openDayDeliverySlots: ArrayList<OpenDayDeliverySlot>? = null

    @SerializedName("requiredToDisplayOnlyODD")
    var requiredToDisplayOnlyODD: Boolean? = null

    @SerializedName("outlyingArea")
    var outlyingArea: Boolean? = null

    @SerializedName("links")
    var links: List<Any>? = null

    @SerializedName("sortedOtherDeliverySlots")
    var sortedOtherDeliverySlots: List<SortedOtherDeliverySlot>? = null

    @SerializedName("splitEnabled")
    var splitEnabled: Boolean? = null

    @SerializedName("status")
    var status: Boolean? = null

    @SerializedName("timedDeliveryStartDates")
    var timedDeliveryStartDates: TimedDeliveryStartDates? = null

    @SerializedName("timedDeliveryFirstAvailableDates")
    var timedDeliveryFirstAvailableDates: TimedDeliveryFirstAvailableDates? = null

    @SerializedName("response")
    var response: Response? = null

    @SerializedName("httpCode")
    var httpCode: Int? = null

    @SerializedName("hasMinimumBasketAmount")
    var hasMinimumBasketAmount:Boolean?= false

    @SerializedName("minimumBasketAmount")
    var minimumBasketAmount:Double?= 0.0
}