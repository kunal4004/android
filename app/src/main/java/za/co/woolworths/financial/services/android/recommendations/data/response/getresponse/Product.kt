package za.co.woolworths.financial.services.android.recommendations.data.response.getresponse

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Product(
    val brandHeaderDescription: String?,
    val brandImage: String?,
    val brandText: String?,
    val externalImageRefV2: String?,
    val isLiquor: Boolean?,
    val isRnREnabled: Boolean?,
    val kilogramPrice: String?,
    val price: String?,
    val priceType: String?,
    val productId: String?,
    val productName: String?,
    val productType: String?,
    val productVariants: String?,
    val promotionImages: PromotionImages?,
    val promotions: List<Promotion>?,
    val saveText: String?,
    val sku: String?,
    val wasPrice: String?,
    var averageRating: String? = null,
    var reviewCount: String? = null,
    val recToken: String? = null

) : Parcelable