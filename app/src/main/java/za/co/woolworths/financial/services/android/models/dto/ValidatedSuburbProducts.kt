package za.co.woolworths.financial.services.android.models.dto

import com.google.gson.JsonElement

class ValidatedSuburbProducts(val hasDeliverySlotReservations: Boolean?, val unDeliverableCommerceItems: List<UnSellableCommerceItem>?, val stores: List<ValidateStoreList>, val noFulfillmentCentre: Boolean?, val sameSuburb: Boolean?, val deliveryAddressChangeStatus: String?, val suburbAvailable: Boolean?, val message: String?, val deliverySlotsDetails: String?, val firstAvailableFoodDeliveryDate: String?, val firstAvailableOtherDeliveryDate: String?, val addressSuburb: String?, val unDeliverableProducts: Boolean?, val noFulfillmentCentreMessage: String?, val selectedAddress: String?, val quantityLimit: QuantityLimit?, val foodFulfillmentCentre: Boolean?, val links: List<Any>?, val deliverableCommerceItems: List<DeliverableCommerceItem>?, val unSellableCommerceItems: List<UnSellableCommerceItem>?, val deliveryStatus: JsonElement?, val storeClosed: Boolean) {
    var suburbId: String? = null
}