package za.co.woolworths.financial.services.android.models.dto.order_again

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import za.co.woolworths.financial.services.android.util.CurrencyFormatter

@Parcelize
data class Item(
    val affinity: Double? = null,
    val slotIndex: Int? = null,
    val itemGroupId: String? = null,
    val id: String? = null,
    val ratings: Double? = null,
    val title: String? = null,
    val pattern: String? = null,
    val rawAffinity: Double? = null,
    val priceRange: String? = null,
    val reviewEnabled: String? = null,
    val sizeCount: Int? = null,
    val reviewCount: Int? = null,
    val valiant: String? = null,
    val productClassification: String? = null,
    val link: String? = null,
    val recSetId: Int? = null,
    val colorCount: Int? = null,
    val recToken: String? = null,
    val imageLink: String? = null,
    val plist3620006: Double? = null,
    val plist3620006Wp: Double? = null,
    val isNewImagery: Boolean? = null,
    val isLiquor: Boolean? = null,
    val price: Double? = null,
    val wasPrice: Double? = null,
    val badges: String? = null,
    val badgesImgLink: String? = null,
    @SerializedName("PROMOTION")
    val promotion: String? = null,
    val promotionURL: String? = null,
    val bulkpromotion: String? = null,
    val bulkPromotionURL: String? = null,
    val promotion1: String? = null,
    val promotion1URL: String? = null
) : Parcelable

fun Item.toProductItem(): ProductItem {

    var wasPriceString = CurrencyFormatter.formatAmountToRandAndCentWithSpace(wasPrice ?: 0.0)
    if(wasPriceString.equals("0.0", ignoreCase = true)) wasPriceString = ""
    var priceString = CurrencyFormatter.formatAmountToRandAndCentWithSpace( price ?: 0.0)
    if(priceString.equals("0.0", ignoreCase = true)) priceString = ""
    val item = ProductItem(
        id = id ?: "",
        productName = title ?: "",
        promotionalText = promotion?.uppercase() ?: "",
        price = plist3620006 ?: 0.0,
        priceString = priceString,
        wasPrice = plist3620006Wp ?: 0.0,
        wasPriceString = wasPriceString,
        productImage = imageLink ?: ""
    ).apply {
        quantityInStock = -1
        quantity = 1
    }
    return item
}