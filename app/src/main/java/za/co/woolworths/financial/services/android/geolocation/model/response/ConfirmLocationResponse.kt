package za.co.woolworths.financial.services.android.geolocation.model.response

data class ConfirmLocationResponse(
    val basketId: String,
    val firstOrderInProfile: Boolean,
    val groupSubTotal: GroupSubTotal,
    val hasReservedSlotConflict: Boolean,
    val httpCode: Int,
    val links: List<Any>,
    val orderSummary: OrderSummary,
    val productCountMap: ProductCountMap,
    val response: Response
)