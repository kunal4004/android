package za.co.woolworths.financial.services.android.util.analytics.dto

import android.os.Bundle
import android.os.Parcelable
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.cart.OrderItem

@Parcelize
data class AnalyticProductItem(
    val itemId: String? = null,
    val itemName: String? = null,
    val category: String? = null,
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
        category = categoryName,
        itemBrand = brandText,
        itemListName = categoryName,
        itemVariant = colourSizeVariants,
        quantity = quantity,
        price = price?.toDouble(),
        affiliation = FirebaseManagerAnalyticsProperties.PropertyValues.AFFILIATION_VALUE,
        index = FirebaseManagerAnalyticsProperties.PropertyValues.INDEX_VALUE.toInt(),
    )
}

fun ProductList.toAnalyticItem(category: String?): AnalyticProductItem {
    return AnalyticProductItem(
        itemId = productId,
        itemName = productName,
        category = category,
        itemBrand = brandText,
        itemListName = category,
        itemVariant = productVariants,
        quantity = 1, // Required quantity set to 1
        price = price?.toDouble(),
        productType = productType,
        affiliation = FirebaseManagerAnalyticsProperties.PropertyValues.AFFILIATION_VALUE,
        index = FirebaseManagerAnalyticsProperties.PropertyValues.INDEX_VALUE.toInt(),
    )
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

fun AnalyticProductItem.toBundle(): Bundle {
    val bundle = Bundle()
    itemId?.let {
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, it)
    }

    itemName?.let {
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, it)
    }

    category?.let {
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, it)
    }

    productType?.let {
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, it)
    }

    itemBrand?.let {
        bundle.putString(FirebaseAnalytics.Param.ITEM_BRAND, it)
    }

    category?.let {
        bundle.putString(FirebaseAnalytics.Param.ITEM_LIST_NAME, it)
    }

    itemVariant?.let {
        bundle.putString(FirebaseAnalytics.Param.ITEM_VARIANT, itemVariant)
    }

    price?.let {
        bundle.putDouble(FirebaseAnalytics.Param.PRICE, it)
    }

    bundle.putInt(FirebaseAnalytics.Param.QUANTITY, quantity)
    bundle.putString(FirebaseAnalytics.Param.AFFILIATION, affiliation)
    bundle.putString(FirebaseAnalytics.Param.INDEX, index.toString())
    return bundle
}