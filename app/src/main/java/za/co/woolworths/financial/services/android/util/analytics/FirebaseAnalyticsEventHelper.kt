package za.co.woolworths.financial.services.android.util.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.CommerceItem
import za.co.woolworths.financial.services.android.models.dto.ProductDetails
import za.co.woolworths.financial.services.android.models.dto.UnSellableCommerceItem
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

    fun viewCart(commerceItems: List<CommerceItem>, value: Double) {
        val analyticItems = commerceItems.map { it.toAnalyticItem() }
        val addToCartParams = Bundle()
        addToCartParams.apply {
            putString(
                FirebaseAnalytics.Param.CURRENCY,
                FirebaseManagerAnalyticsProperties.PropertyValues.CURRENCY_VALUE
            )
            putDouble(FirebaseAnalytics.Param.VALUE, value)
            putParcelableArray(
                FirebaseAnalytics.Param.ITEMS, analyticItems.map { it.toBundle() }.toTypedArray()
            )

        }
        AnalyticsManager.logEvent(
            FirebaseManagerAnalyticsProperties.VIEW_CART, addToCartParams
        )
    }

}