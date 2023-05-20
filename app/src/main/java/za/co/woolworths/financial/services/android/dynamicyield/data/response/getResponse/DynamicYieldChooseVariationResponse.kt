package za.co.woolworths.financial.services.android.dynamicyield.data.response.getResponse

data class DynamicYieldChooseVariationResponse(
    val choices: List<Any>,
    val cookies: List<Cooky>,
    val httpCode: Int,
    val response: Response
)