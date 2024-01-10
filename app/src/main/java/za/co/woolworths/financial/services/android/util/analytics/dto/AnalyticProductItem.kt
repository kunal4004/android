package za.co.woolworths.financial.services.android.util.analytics.dto

import android.os.Bundle
import android.os.Parcelable
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.cart.OrderItem


private const val BREAD_CRUMBS_CATEGORY_ZERO = 0
private const val BREAD_CRUMBS_CATEGORY_FIRST = 1
private const val BREAD_CRUMBS_CATEGORY_SECOND = 2
private const val BREAD_CRUMBS_CATEGORY_THIRD = 3
const val FIREBASE_VALUE_MAX_CHARACTER = 100

@Parcelize
data class AnalyticProductItem(
    val itemId: String? = null,
    val itemName: String? = null,
    val category: String? = null,
    var category2: String? = null,
    var category3: String? = null,
    var category4: String? = null,
    var category5: String? = null,
    val itemBrand: String? = null,
    val itemListName: String? = null,
    val itemVariant: String? = null,
    val quantity: Int = 1,
    val price: Double? = 0.0,
    val affiliation: String? = null,
    val index: Int = 1,
    val productType: String? = null,
) : Parcelable

fun ProductDetails.toAnalyticItem(quantity: Int = 1): AnalyticProductItem {
    return AnalyticProductItem(
        itemId = productId,
        itemName = productName,
        category = productType,
        productType = productType,
        itemBrand = brandText,
        itemListName = categoryName,
        itemVariant = colourSizeVariants,
        quantity = quantity,
        price = price?.toDouble(),
        affiliation = FirebaseManagerAnalyticsProperties.PropertyValues.AFFILIATION_VALUE,
        index = FirebaseManagerAnalyticsProperties.PropertyValues.INDEX_VALUE.toInt(),
    )
}

fun ProductList.toAnalyticItem(category: String?,quantity: Int = 1,index: Int = FirebaseManagerAnalyticsProperties.PropertyValues.INDEX_VALUE.toInt()): AnalyticProductItem {
    return AnalyticProductItem(
        itemId = productId,
        itemName = productName,
        category = category,
        itemBrand = brandText,
        itemListName = category,
        itemVariant = productVariants,
        quantity = quantity, // Required quantity set to 1
        price = price?.toDouble(),
        productType = productType,
        affiliation = FirebaseManagerAnalyticsProperties.PropertyValues.AFFILIATION_VALUE,
        index = index,
    )
}

fun AnalyticProductItem.fillOtherCategories(breadCrumbs: List<String>?) {
    breadCrumbs?.forEachIndexed { index, breadCrumb ->
        when (index) {
            BREAD_CRUMBS_CATEGORY_ZERO -> {
                category2 = breadCrumb
            }
            BREAD_CRUMBS_CATEGORY_FIRST -> {
                category3 = breadCrumb
            }
            BREAD_CRUMBS_CATEGORY_SECOND -> {
                category4 = breadCrumb
            }
            BREAD_CRUMBS_CATEGORY_THIRD -> {
                category5 = breadCrumb
            }
            else -> {
                return
            }
        }
    }
}


fun UnSellableCommerceItem.toAnalyticItem(): AnalyticProductItem {
    return AnalyticProductItem(
        itemId = productId,
        itemName = productDisplayName,
        category = null,
        itemBrand = null,
        itemListName = null,
        itemVariant = productVariant,
        quantity = quantity ?: 1,
        price = price.listPrice,
        affiliation = FirebaseManagerAnalyticsProperties.PropertyValues.AFFILIATION_VALUE,
        index = FirebaseManagerAnalyticsProperties.PropertyValues.INDEX_VALUE.toInt(),
    )
}

fun CommerceItem.toAnalyticItem(): AnalyticProductItem {
    return AnalyticProductItem(
        itemId = commerceItemInfo.productId,
        itemName = commerceItemInfo.getProductDisplayName(),
        category = null,
        itemBrand = null,
        itemListName = null,
        itemVariant = commerceItemInfo.color,
        quantity = commerceItemInfo.quantity,
        price = priceInfo.amount,
        affiliation = FirebaseManagerAnalyticsProperties.PropertyValues.AFFILIATION_VALUE,
        index = FirebaseManagerAnalyticsProperties.PropertyValues.INDEX_VALUE.toInt(),
    )
}

fun OrderItem.toAnalyticItem(): AnalyticProductItem {
    return AnalyticProductItem(
        itemId = productId,
        itemName = productDisplayName,
        category = null,
        itemBrand = brandName,
        itemListName = null,
        itemVariant = commerceItemInfo?.color,
        quantity = quantity,
        price = priceInfo?.amount,
        affiliation = FirebaseManagerAnalyticsProperties.PropertyValues.AFFILIATION_VALUE,
        index = FirebaseManagerAnalyticsProperties.PropertyValues.INDEX_VALUE.toInt(),
    )
}

fun List<OrderDetailsItem>.toAnalyticItemList(): List<AnalyticProductItem> {
    return this.filter { it.type.name == OrderDetailsItem.ViewType.COMMERCE_ITEM.name && it.item is CommerceItem }
        .map { (it.item as CommerceItem).toAnalyticItem() }
}

fun String?.valueOrNone(): String {
    return if (!this.isNullOrEmpty()) {
        this
    } else {
        FirebaseManagerAnalyticsProperties.PropertyValues.NONE
    }
}

fun AnalyticProductItem.toBundle(): Bundle {
    val bundle = Bundle()
    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemId.valueOrNone())
    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName.valueOrNone())
    bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, productType.valueOrNone())
    bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY2, category2.valueOrNone())
    bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY3, category3.valueOrNone())
    bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY4, category4.valueOrNone())
    bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY5, category5.valueOrNone())
    bundle.putString(FirebaseAnalytics.Param.ITEM_BRAND, itemBrand.valueOrNone())
    bundle.putString(FirebaseAnalytics.Param.ITEM_LIST_NAME, category.valueOrNone())
    bundle.putString(FirebaseAnalytics.Param.ITEM_VARIANT, itemVariant.valueOrNone())
    price?.let {
        bundle.putDouble(FirebaseAnalytics.Param.PRICE, it)
    }
    bundle.putInt(FirebaseAnalytics.Param.QUANTITY, quantity)
    bundle.putString(FirebaseAnalytics.Param.AFFILIATION, affiliation.valueOrNone())
    bundle.putString(FirebaseAnalytics.Param.INDEX, index.toString())
    return bundle
}