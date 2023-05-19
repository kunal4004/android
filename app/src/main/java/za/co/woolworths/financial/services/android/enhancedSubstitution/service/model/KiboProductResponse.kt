package za.co.woolworths.financial.services.android.enhancedSubstitution.service.model

data class KiboProductResponse(
    val data: ItemResponse,
    val response: Response,
    val httpCode: Int,
)