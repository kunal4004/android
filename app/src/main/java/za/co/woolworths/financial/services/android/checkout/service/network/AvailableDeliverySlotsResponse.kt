package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.SerializedName

class AvailableDeliverySlotsResponse {
    @SerializedName("timedDeliveryCosts")
    var timedDeliveryCosts: TimedDeliveryCosts? = null

    @SerializedName("outlyingAreaAvailableDays")
    var outlyingAreaAvailableDays: OutlyingAreaAvailableDays? = null

    @SerializedName("hasOnlyODDAvailableWithFood")
    var hasOnlyODDAvailableWithFood: Boolean? = null

    @SerializedName("fulfillmentTypes")
    var fulfillmentTypes: FulfillmentTypes? = null

    @SerializedName("requiredToDisplayODD")
    var requiredToDisplayODD: Boolean? = null

    @SerializedName("hasDeliverySlotsConflict")
    var hasDeliverySlotsConflict: Boolean? = null

    @SerializedName("sortedFoodDeliverySlots")
    var sortedFoodDeliverySlots: List<Any>? = null

    @SerializedName("sortedJoinDeliverySlots")
    var sortedJoinDeliverySlots: List<SortedJoinDeliverySlot>? = null

    @SerializedName("reservedDeliverySlots")
    var reservedDeliverySlots: List<Any>? = null

    @SerializedName("openDayDeliverySlots")
    var openDayDeliverySlots: List<Any>? = null

    @SerializedName("requiredToDisplayOnlyODD")
    var requiredToDisplayOnlyODD: Boolean? = null

    @SerializedName("outlyingArea")
    var outlyingArea: Boolean? = null

    @SerializedName("links")
    var links: List<Any>? = null

    @SerializedName("sortedOtherDeliverySlots")
    var sortedOtherDeliverySlots: List<Any>? = null

    @SerializedName("splitEnabled")
    var splitEnabled: Boolean? = null

    @SerializedName("status")
    var status: Boolean? = null

    @SerializedName("timedDeliveryStartDates")
    var timedDeliveryStartDates: TimedDeliveryStartDates? = null

    @SerializedName("response")
    var response: Response? = null

    @SerializedName("httpCode")
    var httpCode: Int? = null
}