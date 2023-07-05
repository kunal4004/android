package za.co.woolworths.financial.services.android.util.analytics

import android.app.Activity
import android.os.Bundle
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.recommendations.data.response.getresponse.Product
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.analytics.dto.*
import za.co.woolworths.financial.services.android.util.wenum.Delivery

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

    fun viewCartAnalyticsEvent(commerceItems: List<CommerceItem>, value: Double) {
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
        products: List<ProductList>?, category: String?
    ) {
        if (products.isNullOrEmpty()) {
            return
        }

        val analyticItems = products.map { it.toAnalyticItem(category = category) }
        triggerViewItemListEvent(products = analyticItems, category = category)
    }

    fun viewItemListRecommendations(
        products: List<Product>?, category: String?
    ) {
        if (products.isNullOrEmpty()) {
            return
        }

        val analyticItems = products.map { it.toAnalyticItem(category = category) }
        triggerViewItemListEvent(products = analyticItems, category = category)
    }

    private fun triggerViewItemListEvent(products: List<AnalyticProductItem>, category: String?) {
        val analyticsParams = Bundle()
        analyticsParams.apply {
            putParcelableArray(
                FirebaseAnalytics.Param.ITEMS, products.map { it.toBundle() }.toTypedArray()
            )
            category?.let {
                putString(FirebaseAnalytics.Param.ITEM_LIST_NAME, category)
            }
        }

        AnalyticsManager.logEvent(
            FirebaseManagerAnalyticsProperties.VIEW_ITEM_LIST, analyticsParams
        )
    }

    fun viewPromotion(productDetail: ProductDetails, promotionsList: List<Promotions>) {
        if (promotionsList.isEmpty()) {
            return
        }
        val analyticItem = productDetail.toAnalyticItem()
        val promoText = extractPromotionText(promotionsList)

        val analyticsParams = Bundle()
        analyticsParams.apply {
            putParcelableArray(
                FirebaseAnalytics.Param.ITEMS, arrayOf(analyticItem.toBundle())
            )
            putString(FirebaseAnalytics.Param.CREATIVE_NAME, promoText)
            putString(FirebaseAnalytics.Param.PROMOTION_NAME, promoText)
            productDetail.productType?.let { productType ->
                putString(FirebaseManagerAnalyticsProperties.BUSINESS_UNIT, productType)
            }
        }

        AnalyticsManager.logEvent(
            FirebaseManagerAnalyticsProperties.VIEW_PROMOTION, analyticsParams
        )
    }

    private fun extractPromotionText(promotionsList: List<Promotions>): String {
        return promotionsList.map { it.promotionalText }.filterNot { it.isNullOrEmpty() }
            .joinToString()
    }

    fun addToWishlistEvent(addToWishListFirebaseEventData: AddToWishListFirebaseEventData?) {
        val products = addToWishListFirebaseEventData?.products
        if (products.isNullOrEmpty() || addToWishListFirebaseEventData.shoppingListName.isNullOrEmpty()) {
            return
        }

        val value = products.mapNotNull { it.price?.times(it.quantity) }.sum()

        val analyticsParams = Bundle()
        analyticsParams.apply {
            addToWishListFirebaseEventData.itemRating?.let { rating ->
                putFloat(FirebaseManagerAnalyticsProperties.PropertyNames.ITEM_RATING, rating)
            }
            addToWishListFirebaseEventData.businessUnit?.let { businessUnit ->
                putString(FirebaseManagerAnalyticsProperties.BUSINESS_UNIT, businessUnit)
            }
            putDouble(FirebaseAnalytics.Param.VALUE, value)
            putString(
                FirebaseAnalytics.Param.CURRENCY,
                FirebaseManagerAnalyticsProperties.PropertyValues.CURRENCY_VALUE
            )
            putString(
                FirebaseManagerAnalyticsProperties.PropertyNames.SHOPPING_LIST_NAME,
                addToWishListFirebaseEventData.shoppingListName
            )
            putParcelableArray(
                FirebaseAnalytics.Param.ITEMS, products.map { it.toBundle() }.toTypedArray()
            )
        }
        AnalyticsManager.logEvent(
            FirebaseManagerAnalyticsProperties.ADD_TO_WISHLIST, analyticsParams
        )
    }

    fun viewSearchResult(searchTerm: String?) {
        if (searchTerm.isNullOrEmpty()) {
            return
        }

        val analyticsParams = Bundle()
        analyticsParams.apply {
            putString(
                FirebaseManagerAnalyticsProperties.PropertyNames.SEARCH_TERM,
                searchTerm
            )
        }
        AnalyticsManager.logEvent(
            FirebaseAnalytics.Event.VIEW_SEARCH_RESULTS, analyticsParams
        )
    }

    fun viewScreenEventForPLP(activity: Activity?, screenViewEventData: ScreenViewEventData?) {
        val eventName = screenViewEventData?.department
        if (eventName.isNullOrEmpty()) {
            activity?.let {
                za.co.woolworths.financial.services.android.util.Utils.setScreenName(
                    it,
                    FirebaseManagerAnalyticsProperties.ScreenNames.PRODUCT_LISTING_PAGE
                )
            }
            return
        }
        val analyticsParams = Bundle()
        analyticsParams.apply {
            putString(
                FirebaseAnalytics.Param.SCREEN_NAME, FirebaseManagerAnalyticsProperties.ScreenNames.PRODUCT_LISTING_PAGE
            )
            putString(
                FirebaseManagerAnalyticsProperties.PropertyNames.CATEGORY_NAME,
                screenViewEventData.category
            )
            putString(
                FirebaseManagerAnalyticsProperties.PropertyNames.SUB_CATEGORY_NAME,
                screenViewEventData.subCategory
            )
            putString(
                FirebaseManagerAnalyticsProperties.PropertyNames.SUB_SUB_CATEGORY_NAME,
                screenViewEventData.subSubCategory
            )
        }
        AnalyticsManager.logEvent(eventName, analyticsParams)
    }

    fun setFirebaseEventForm(type: String?, eventName: String, isComingFromCheckout: Boolean) {

        var propertyValueForFormType = when (KotlinUtils.getPreferredDeliveryType()) {
            Delivery.DASH -> {
                FirebaseManagerAnalyticsProperties.PropertyValues.DASH
            }
            Delivery.CNC -> {
                FirebaseManagerAnalyticsProperties.PropertyValues.CLICK_AND_COLLECT
            }
            else -> {
                FirebaseManagerAnalyticsProperties.PropertyValues.STANDARD
            }
        }

        val propertyValueForFormLocation = if (isComingFromCheckout) {
            FirebaseManagerAnalyticsProperties.CHECKOUT
        } else {
            FirebaseManagerAnalyticsProperties.PropertyValues.BROWSE
        }

        //Event form type for address checkout
        val formTypeParams = bundleOf(
                FirebaseManagerAnalyticsProperties.PropertyNames.FORM_TYPE to
                        propertyValueForFormType,
                FirebaseManagerAnalyticsProperties.PropertyNames.FORM_NAME to
                        type,
                FirebaseManagerAnalyticsProperties.PropertyNames.FORM_LOCATION to
                        propertyValueForFormLocation
        )
        AnalyticsManager.logEvent(eventName, formTypeParams)
    }

    object Utils {
        private fun stringToFirebaseEventName(string: String?): String? {
            return string?.filter { it.isLetterOrDigit() }?.lowercase()
        }

        fun getPLPScreenViewEventDataForDash(
            headerText: String?,
            bannerDisplayName: String?,
            bannerNavigationState: String?
        ): ScreenViewEventData? {
            val eventName = stringToFirebaseEventName(headerText)
            if (eventName.isNullOrEmpty()) {
                return null
            }

            val subCategory =
                if (bannerDisplayName.isNullOrEmpty()) bannerNavigationState else bannerDisplayName
            return ScreenViewEventData(
                department = FirebaseManagerAnalyticsProperties.DASH_PREFIX.plus(eventName),
                category = headerText,
                subCategory = subCategory
            )
        }

        fun getPLPScreenViewEventDataForStandardAndCnc(
            category: String?,
            subCategory: String?,
            subSubCategory: String?
        ): ScreenViewEventData {
            val department = stringToFirebaseEventName(category)
            val subCat = if(category == subCategory) subSubCategory else subCategory
            val subSubCat = if(subCat == subSubCategory) null else subSubCategory
            return ScreenViewEventData(
                department = department,
                category = category,
                subCategory = subCat,
                subSubCategory = subSubCat
            )
        }
    }
}