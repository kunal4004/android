package za.co.woolworths.financial.services.android.models.dto.cart

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest

@Parcelize
data class OrderItem(
    var commerceItemInfo: CommerceItemInfo? = null,
    var brandName: String = "",
    var productType: String = "",
    var breadcrumbs: ArrayList<BreadCrumb>?,
    var quantity: Int = 0,
    var productId: String = "",
    var color: String = "",
    var primarySize: String = "",
    var internalSwatchImage: String = "",
    var isGWP: Boolean = false,
    var promotionalMessage: String = "",
    var internalImageURL: String = "",
    var commerceItemClassType: String = "",
    var catalogRefId: String = "",
    var fulfillmentType: String = "",
    var thresholdQuantity: String = "",
    var priceInfo: PriceInfo? = null,
    var size: String = "",
    var productseoURL: String = "",
    var productDisplayName: String = "",
    var siteId: String = "",
    var fulfillerType: String = "",
    var id: String = "",
    var externalSwatchImageURL: String = "",
) : Parcelable

fun OrderItem.toAddToListRequest() = AddToListRequest(
    quantity = this.quantity.toString(),
    catalogRefId = this.catalogRefId,
    giftListId = "",
    skuID = this.catalogRefId,
    isGWP = this.isGWP
)