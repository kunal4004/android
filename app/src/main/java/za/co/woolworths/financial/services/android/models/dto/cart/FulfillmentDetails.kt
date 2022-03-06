package za.co.woolworths.financial.services.android.models.dto.cart

import com.google.gson.JsonElement
import za.co.woolworths.financial.services.android.checkout.service.network.DeliveryStatus

data class FulfillmentDetails(val foodMaximumQuantity: Int?, val address: Address?, val fulfillmentStores: JsonElement?, val deliveryType: String?, val bulkPromotionEnabled: Boolean?, val deliverable: Boolean?, val storeName: String?, val storeId: String?, val otherMaximumQuantity: Int?, var liquorDeliverable:Boolean)
