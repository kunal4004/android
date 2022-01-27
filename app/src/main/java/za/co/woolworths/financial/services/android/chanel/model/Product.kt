package za.co.woolworths.financial.services.android.chanel.model

data class Product(
    val brandHeaderDescription: String?,
    val brandText: String?,
    val externalImageRef: String?,
    val externalImageRefV2: String?,
    val imagePath: String?,
    val isLiquor: Boolean,
    val isRnREnabled: Boolean,
    val price: String?,
    val priceType: String?,
    val productId: String?,
    val productName: String?,
    val productType: String?,
    val productVariants: String?,
    val promotions: List<Any>,
    val sku: String?
)