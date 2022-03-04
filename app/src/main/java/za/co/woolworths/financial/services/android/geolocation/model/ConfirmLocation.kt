package za.co.woolworths.financial.services.android.geolocation.model

data class ConfirmLocation(
    val basketId: String,
    val firstOrderInProfile: Boolean,
    val groupSubTotal: GroupSubTotal,
    val hasReservedSlotConflict: Boolean,
    val httpCode: Int,
    val links: List<Any>,
    val orderSummary: OrderSummary,
    val preReservedDeliverySlots: String,
    val productCountMap: ProductCountMap,
    val response: Response
)