package za.co.woolworths.financial.services.android.ui.activities.product.dynamicyield.response.getresponse

data class DyKeywordSearchResponse(
    val choices: List<Any>,
    val cookies: List<Cooky>,
    val httpCode: Int,
    val response: Response
)