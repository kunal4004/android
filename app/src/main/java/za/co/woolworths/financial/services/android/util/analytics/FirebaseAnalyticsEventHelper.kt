package za.co.woolworths.financial.services.android.util.analytics

import android.app.Activity
import android.os.Bundle
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import za.co.woolworths.financial.services.android.cart.viewmodel.CartViewModel
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.BROWSE_MODE
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.DELIVERY_MODE
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.SET_DELIVERY_BROWSE_MODE
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.SET_Location
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.SWITCH_BROWSE_MODE
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.SWITCH_DELIVERY_MODE
import za.co.woolworths.financial.services.android.models.dto.*
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

    fun cartBeginEventAnalytics(orderSummary: OrderSummary?, viewModel: CartViewModel) {
        val beginCheckoutParams = Bundle()
        beginCheckoutParams.apply {
            putString(
                FirebaseAnalytics.Param.CURRENCY,
                FirebaseManagerAnalyticsProperties.PropertyValues.CURRENCY_VALUE
            )
            orderSummary?.total?.let {
                putDouble(
                    FirebaseAnalytics.Param.VALUE,
                    it
                )
            }

            viewModel?.getCartItemList()?.let {
                val itemArrayEvent = arrayListOf<Bundle>()
                for (cartItem in it) {
                    val beginCheckoutItem = Bundle()
                    beginCheckoutItem.apply {
                        putString(
                            FirebaseAnalytics.Param.ITEM_ID,
                            cartItem.commerceItemInfo?.productId
                        )
                        putString(
                            FirebaseAnalytics.Param.ITEM_NAME,
                            cartItem.commerceItemInfo.productDisplayName
                        )
                        putDouble(
                            FirebaseAnalytics.Param.PRICE,
                            cartItem.priceInfo.amount
                        )

                        putString(
                            FirebaseAnalytics.Param.ITEM_BRAND,
                            cartItem.commerceItemInfo?.productDisplayName
                        )
                        putString(
                            FirebaseAnalytics.Param.ITEM_VARIANT,
                            cartItem.commerceItemInfo?.size
                        )

                        putString(
                            FirebaseAnalytics.Param.ITEM_CATEGORY,
                            cartItem.commerceItemInfo.productDisplayName
                        )
                        putInt(
                            FirebaseAnalytics.Param.QUANTITY,
                            cartItem.commerceItemInfo.quantity
                        )
                        itemArrayEvent.add(this)
                    }
                }
                putParcelableArray(
                    FirebaseAnalytics.Param.ITEMS,
                    itemArrayEvent.toTypedArray()
                )
            }

            AnalyticsManager.logEvent(
                FirebaseManagerAnalyticsProperties.CART_BEGIN_CHECKOUT,
                this
            )
        }
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
        products: List<ProductList>?, category: String?
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

    enum class FirebaseEventAction(val value: Int) { VIEW_VOUCHER(0), VIEW_WREWARDS_VOUCHERS(1),ADD_PROMO_CODE(2) }

    enum class FirebaseEventOption(val value: Int) { VOUCHERS(0), ADD_PROMO(1) }
    fun triggerFirebaseEventVouchersOrPromoCode(actionEnum: Int, optionEnum: Int,activity: Activity) {
        val action = when (actionEnum){
            FirebaseEventAction.VIEW_VOUCHER.value -> FirebaseManagerAnalyticsProperties.PropertyValues.VIEW_VOUCHER
            FirebaseEventAction.VIEW_WREWARDS_VOUCHERS.value -> FirebaseManagerAnalyticsProperties.PropertyValues.VIEW_WREWARDS_VOUCHERS
            FirebaseEventAction.ADD_PROMO_CODE.value -> FirebaseManagerAnalyticsProperties.PropertyValues.ADD_PROMO_CODE
            else -> throw IllegalStateException()
        }

        val option = when (optionEnum){
            FirebaseEventOption.VOUCHERS.value -> FirebaseManagerAnalyticsProperties.PropertyValues.VOUCHERS
            FirebaseEventOption.ADD_PROMO.value -> FirebaseManagerAnalyticsProperties.PropertyValues.ADD_PROMO
            else -> throw IllegalStateException()
        }

        val deliveryType = when(KotlinUtils.getPreferredDeliveryType()){
            Delivery.STANDARD -> FirebaseManagerAnalyticsProperties.PropertyValues.STANDARD
            Delivery.CNC -> FirebaseManagerAnalyticsProperties.PropertyValues.CLICK_AND_COLLECT
            Delivery.DASH -> FirebaseManagerAnalyticsProperties.PropertyValues.DASH
            else ->  throw IllegalStateException()
        }
        val arguments = HashMap<String, String>()
        arguments[FirebaseManagerAnalyticsProperties.PropertyNames.STEP] = FirebaseManagerAnalyticsProperties.PropertyValues.BASKET
        arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE] = action
        arguments[FirebaseManagerAnalyticsProperties.PropertyNames.OPTION] = option
        arguments[FirebaseManagerAnalyticsProperties.PropertyNames.DELIVERY_TYPE] = deliveryType
        KotlinUtils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.CHECKOUT, arguments,activity)

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

    fun outOfStock() {
        val analyticsParams = Bundle()
        analyticsParams.apply {
            putString(
                FirebaseManagerAnalyticsProperties.PropertyNames.MESSAGE_TYPE,
                FirebaseManagerAnalyticsProperties.PropertyValues.OUT_OF_STOCK_MESSAGE
            )
        }
        AnalyticsManager.logEvent(
            FirebaseManagerAnalyticsProperties.IN_APP_POP_UP, analyticsParams
        )
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

    fun setLocationEvent(browseMode:String?,deliveryMode: String?) {
        val analyticsParams = Bundle().apply {
            putString(BROWSE_MODE,browseMode)
            putString(DELIVERY_MODE, deliveryMode)
        }
        AnalyticsManager.logEvent(SET_Location, analyticsParams)
    }

    fun fromShopWithSetDeliveryBrowseMode(browseMode:String?, deliveryMode: String?) {
        val analyticsParams = Bundle().apply {
            putString(BROWSE_MODE,browseMode)
            putString(DELIVERY_MODE, deliveryMode)
        }
        AnalyticsManager.logEvent(SET_DELIVERY_BROWSE_MODE, analyticsParams)
    }

    fun switchDeliverModeEvent(deliveryMode: String?) {
        val analyticsParams = Bundle().apply {
            putString(DELIVERY_MODE, deliveryMode)
        }
         AnalyticsManager.logEvent(SWITCH_DELIVERY_MODE, analyticsParams)
    }

    fun switchBrowseModeEvent(deliveryMode: String) {
        val analyticsParams = Bundle().apply {
            putString(BROWSE_MODE, deliveryMode)
        }
         AnalyticsManager.logEvent(SWITCH_BROWSE_MODE, analyticsParams)
    }

}