package za.co.woolworths.financial.services.android.models.dto

import android.os.Parcelable
import com.google.gson.JsonElement
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class ProductDetails(
    @JvmField
    var productId: String? = null,

    @JvmField
    var productName: String? = null,

    var range: String? = null,

    var categoryName: String? = null,

    var categoryId: String? = null,

    var isnAvailable: String? = null,

    var auxiliaryImages: JsonElement? = null,

    var promotionImages: PromotionImages? = null,

    var longDescription: String? = null,

    @JvmField
    var otherSkus: ArrayList<OtherSkus>? = null,

    var promotions: ArrayList<Promotions>? = null,

    var checkOutLink: String? = null,

    var productType: String? = null,

    var imagePath: String? = null,

    @JvmField
    var fromPrice: Float? = null,

    @JvmField
    var sku: String? = null,

    @JvmField
    var externalImageRefV2: String? = null,

    var fulfillmentType: String? = null,

    var ingredients: String? = null,

    var dietary: List<String>? = null,

    var allergens: List<String>? = null,

    var saveText: String? = null,

    var price: String? = null,

    var priceType: String? = null,

    var wasPrice: String? = null,

    var kilogramPrice: String? = null,

    var nutritionalInformationDetails: NutritionalInformationDetails? = null,

    var freeGiftText: String? = null,

    var freeGift: String? = null,

    var brandText: String? = null,

    var sizeGuideId: String? = null,

    var colourSizeVariants: String? = null,

    var isLiquor: Boolean = false,

    var virtualTryOn: String? = null,

    var lowStockThreshold: Int? = null
) : Parcelable