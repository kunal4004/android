package za.co.woolworths.financial.services.android.chanel.model

data class ChanelResponse(
    val dynamicBanners: List<DynamicBanner?>,
    val httpCode: Int,
    val isBanners: Boolean,
    val pageHeading: String?,
    val response: Response
)