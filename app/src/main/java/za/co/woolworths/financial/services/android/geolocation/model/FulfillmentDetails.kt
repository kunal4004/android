package za.co.woolworths.financial.services.android.geolocation.model

data class FulfillmentDetails(
    val address: Address,
    val bulkPromotionEnabled: Boolean,
    val deliverable: Boolean,
    val deliveryStatus: DeliveryStatus,
    val deliveryType: String,
    val foodMaximumQuantity: Int,
    val fulfillmentStores: FulfillmentStores,
    val liquorDeliverable: Boolean,
    val otherMaximumQuantity: Int
)