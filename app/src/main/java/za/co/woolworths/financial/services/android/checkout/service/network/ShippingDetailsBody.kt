package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.SerializedName
import za.co.woolworths.financial.services.android.geolocation.model.response.ConfirmLocationAddress
import java.io.Serializable

class ShippingDetailsBody : Serializable {
    @SerializedName("ageConsentConfirmed")
    var ageConsentConfirmed: Boolean? = null

    @SerializedName("requestFrom")
    var requestFrom: String? = null

    @SerializedName("joinBasket")
    var joinBasket: Boolean? = null

    @SerializedName("foodShipOnDate")
    var foodShipOnDate: String? = null

    @SerializedName("foodDeliverySlotId")
    var foodDeliverySlotId: String? = null

    @SerializedName("foodDeliveryStartHour")
    var foodDeliveryStartHour: Long? = null

    @SerializedName("otherShipOnDate")
    var otherShipOnDate: String? = null

    @SerializedName("otherDeliverySlotId")
    var otherDeliverySlotId: String? = null

    @SerializedName("oddDeliverySlotId")
    var oddDeliverySlotId: String? = null

    @SerializedName("otherDeliveryStartHour")
    var otherDeliveryStartHour: Long? = null

    @SerializedName("shipToAddressName")
    var shipToAddressName: String? = null

    @SerializedName("substituesAllowed")
    var substituesAllowed: String? = null

    @SerializedName("deliverySpecialInstructions")
    var deliverySpecialInstructions: String? = null

    @SerializedName("plasticBags")
    var plasticBags: Boolean = false

    @SerializedName("shoppingBagType")
    var shoppingBagType: Double? = null

    @SerializedName("giftNoteSelected")
    var giftNoteSelected: Boolean = false

    @SerializedName("giftMessage")
    var giftMessage: String? = null

    @SerializedName("suburbId")
    var suburbId: String? = null

    @SerializedName("storeId")
    var storeId: String? = null

    @SerializedName("deliveryType")
    var deliveryType: String = ""

    @SerializedName("address")
    var address: ConfirmLocationAddress? =null

    @SerializedName("driverTip")
    var driverTip: Double = 0.0

    var appInstanceId: String? = null
    var pushNotificationToken: String? = null
}