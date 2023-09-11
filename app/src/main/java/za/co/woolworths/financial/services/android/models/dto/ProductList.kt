package za.co.woolworths.financial.services.android.models.dto

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.ui.adapters.holder.ProductListingViewType
import java.util.*

@Parcelize
data class ProductList(
    @JvmField
    var productId: String? = null,

    @JvmField
    var productName: String? = null,

    @JvmField
    var externalImageRefV2: String? = null,

    var imagePath: String? = null,

    @JvmField
    var fromPrice: Float? = null,

    @JvmField
    var sku: String? = null,

    @JvmField
    var productType: String? = null,

    @JvmField
    var brandHeaderDescription: String? = null,

    var promotionImages: PromotionImages? = null,

    @JvmField
    var otherSkus: List<OtherSkus>? = null,

    var promotions: ArrayList<Promotions>? = null,

    @JvmField
    var saveText: String? = null,

    var priceType: String? = null,

    var kilogramPrice: Float? = null,

    var price: Float? = null,

    var wasPrice: Float? = null,

    @JvmField
    var brandText: String? = null,

    var productVariants: String? = null,

    var isLiquor: Boolean? = null,

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
    var quickShopButtonWasTapped: Boolean = false,
    @SerializedName("isRnREnabled")
    var isRnREnabled: Boolean? = null,
    @SerializedName("averageRating")
    @Expose
    var averageRating: String? = null,
    @SerializedName("reviewCount")
    @Expose
    var reviewCount: String? = null,
    @SerializedName("network")
    var network: String? = null
) : Parcelable