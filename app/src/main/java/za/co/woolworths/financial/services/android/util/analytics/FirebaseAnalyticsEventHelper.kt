package za.co.woolworths.financial.services.android.util.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.CommerceItem
import za.co.woolworths.financial.services.android.models.dto.ProductDetails
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.dto.UnSellableCommerceItem
import za.co.woolworths.financial.services.android.recommendations.data.response.getresponse.Product
import za.co.woolworths.financial.services.android.util.analytics.dto.AnalyticProductItem
import za.co.woolworths.financial.services.android.util.analytics.dto.toAnalyticItem
import za.co.woolworths.financial.services.android.util.analytics.dto.toBundle

object FirebaseAnalyticsEventHelper {

    fun addToCart(productDetail: ProductDetails, quantity: Int = 1) {
        val analyticItem = productDetail.toAnalyticItem(quantity = quantity)

        val addToCartParams = Bundle()
        addToCartParams.putString(
            FirebaseAnalytics.Param.CURRENCY,
            FirebaseManagerAnalyticsProperties.PropertyValues.CURRENCY_VALUE
        )

        analyticItem.price?.let {
            val value = analyticItem.quantity * it
            addToCartParams.putDouble(FirebaseAnalytics.Param.VALUE, value)
        }
        addToCartParams.putParcelableArray(
            FirebaseAnalytics.Param.ITEMS, arrayOf(analyticItem.toBundle())
        )

        AnalyticsManager.logEvent(
            FirebaseManagerAnalyticsProperties.ADD_TO_CART_PDP, addToCartParams
        )
    }

    fun removeFromCart(commerceItems: List<CommerceItem>) {
        val analyticItems = commerceItems.map { it.toAnalyticItem() }
        removeFromCartEvent(analyticItems)
    }

    fun removeFromCartUnsellable(commerceItems: List<UnSellableCommerceItem>) {
        val analyticItems = commerceItems.map { it.toAnalyticItem() }
        removeFromCartEvent(analyticItems)
    }

    private fun removeFromCartEvent(analyticProductItems: List<AnalyticProductItem>) {
        val addToCartParams = Bundle()
        addToCartParams.putString(
            FirebaseAnalytics.Param.CURRENCY,
            FirebaseManagerAnalyticsProperties.PropertyValues.CURRENCY_VALUE
        )

        val value = analyticProductItems.sumOf { it.quantity * (it.price ?: 0.0) }

        addToCartParams.putDouble(FirebaseAnalytics.Param.VALUE, value)
        addToCartParams.putParcelableArray(
            FirebaseAnalytics.Param.ITEMS, analyticProductItems.map { it.toBundle() }.toTypedArray()
        )

        AnalyticsManager.logEvent(
            FirebaseManagerAnalyticsProperties.REMOVE_FROM_CART, addToCartParams
        )
    }

    fun refund(
        commerceItems: List<CommerceItem>?,
        value: Double?,
        transactionId: String?,
        coupon: String? = null,
        shipping: Double? = null
    ) {
        if (transactionId.isNullOrEmpty() || commerceItems.isNullOrEmpty() || value == null) {
            return
        }

        val analyticItems = commerceItems.map { it.toAnalyticItem() }

        val analyticsParams = Bundle()
        analyticsParams.apply {
            putString(
                FirebaseAnalytics.Param.CURRENCY,
                FirebaseManagerAnalyticsProperties.PropertyValues.CURRENCY_VALUE
            )
            putDouble(FirebaseAnalytics.Param.VALUE, value)

            putParcelableArray(
                FirebaseAnalytics.Param.ITEMS, analyticItems.map { it.toBundle() }.toTypedArray()
            )

            putString(
                FirebaseAnalytics.Param.TRANSACTION_ID, transactionId
            )

            coupon?.let {
                putString(
                    FirebaseAnalytics.Param.COUPON, it
                )
            }

            shipping?.let {
                putDouble(
                    FirebaseAnalytics.Param.SHIPPING, it
                )
            }

            putString(
                FirebaseManagerAnalyticsProperties.PropertyNames.AFFILIATION,
                FirebaseManagerAnalyticsProperties.PropertyValues.AFFILIATION_VALUE
            )

            putString(
                FirebaseManagerAnalyticsProperties.PropertyNames.REFUND_TYPE,
                FirebaseManagerAnalyticsProperties.PropertyValues.DASH_CANCELLED_ORDER
            )
        }

        AnalyticsManager.logEvent(
            FirebaseManagerAnalyticsProperties.REFUND, analyticsParams
        )
    }

    fun viewItemList(
        products: List<ProductList>?,
        category: String?
    ) {
        if (products.isNullOrEmpty()) {
            return
        }

        val analyticItems = products.map { it.toAnalyticItem(category = category) }

        val analyticsParams = Bundle()
        analyticsParams.apply {
            putParcelableArray(
                FirebaseAnalytics.Param.ITEMS, analyticItems.map { it.toBundle() }.toTypedArray()
            )
            category?.let {
                putString(FirebaseAnalytics.Param.ITEM_LIST_NAME, category)
            }
        }

        AnalyticsManager.logEvent(FirebaseManagerAnalyticsProperties.VIEW_ITEM_LIST, analyticsParams)
    }
    fun viewItemListEvent(
        products: List<Product>?,
        category: String?
    ) {
        if (products.isNullOrEmpty()) {
            return
        }

        val analyticItems = products.map { it.toAnalyticItem(category = category) }.reversed()

        val analyticsParams = Bundle()
        analyticsParams.apply {
            putParcelableArray(
                FirebaseAnalytics.Param.ITEMS, analyticItems.map { it.toBundle() }.toTypedArray()
            )
            category?.let {
                putString(FirebaseAnalytics.Param.ITEM_LIST_NAME, category)
            }
        }

        AnalyticsManager.logEvent(FirebaseManagerAnalyticsProperties.VIEW_ITEM_LIST, analyticsParams)
    }
}