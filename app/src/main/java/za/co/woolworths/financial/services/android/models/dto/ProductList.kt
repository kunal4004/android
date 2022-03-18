package za.co.woolworths.financial.services.android.models.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.ui.adapters.holder.ProductListingViewType
import java.util.*

@Parcelize
data class ProductList(
    @JvmField
    @SerializedName("productId")
    var productId: String? = null,

    @JvmField
    var productName: String? = null,

    @JvmField
    @SerializedName("externalImageRefV2")
    var externalImageRefV2: String? = null,

    @SerializedName("imagePath")
    var imagePath: String? = null,

    @JvmField
    @SerializedName("fromPrice")
    var fromPrice: Float? = null,

    @JvmField
    @SerializedName("sku")
    var sku: String? = null,

    @JvmField
    @SerializedName("productType")
    var productType: String? = null,

    @SerializedName("promotionImages")
    var promotionImages: PromotionImages? = null,

    @JvmField
    @SerializedName("otherSkus")
    var otherSkus: List<OtherSkus>? = null,

    @SerializedName("promotions")
    var promotionsList: ArrayList<Promotions>? = null,

    @JvmField
    @SerializedName("saveText")
    var saveText: String? = null,

    @SerializedName("priceType")
    var priceType: String? = null,

    @SerializedName("kilogramPrice")
    var kilogramPrice: Float? = null,

    @SerializedName("price")
    var price: Float? = null,

    @SerializedName("wasPrice")
    var wasPrice: Float? = null,

    @SerializedName("brandText")
    var brandText: String? = null,

    @SerializedName("productVariants")
    var productVariants: String? = null,

    @SerializedName("isLiquor")
    var isLiquor: Boolean? = null,

    @SerializedName("virtualTryOn")
    var virtualTryOn: String? = null,

    @JvmField
    var rowType: ProductListingViewType = ProductListingViewType.PRODUCT,

    @JvmField
    var numberOfItems: Int? = null,
    @JvmField
    var itemWasChecked: Boolean = false,
    @JvmField
    var viewIsLoading: Boolean = false,
    @JvmField
    var displayColorSizeText: String? = null,
    var fulfillmentType: String? = null,
    var quickShopButtonWasTapped: Boolean = false
) : Parcelable