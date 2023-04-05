package za.co.woolworths.financial.services.android.checkout.utils

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.CommerceItem
import za.co.woolworths.financial.services.android.util.analytics.AnalyticsManager
import javax.inject.Inject

class AddShippingInfoEventsAnalyticsImpl @Inject constructor() : AddShippingInfoEventsAnalytics {

    override fun sendEventData(
        cartItemList: ArrayList<CommerceItem>,
        shippingTier: String,
        value: Double,
    ) {
        val shoppingItems = Bundle()
        shoppingItems.putString(
            FirebaseAnalytics.Param.CURRENCY,
            FirebaseManagerAnalyticsProperties.PropertyValues.CURRENCY_VALUE
        )

        shoppingItems.putString(
            FirebaseAnalytics.Param.SHIPPING_TIER,
            shippingTier
        )

        shoppingItems.putDouble(
            FirebaseAnalytics.Param.VALUE,
            value
        )


        val eventItemsArray = arrayListOf<Bundle>()
        for (cartItem in cartItemList) {
            val addShippingInfoItem = Bundle()
            addShippingInfoItem.putString(
                FirebaseAnalytics.Param.ITEM_ID,
                cartItem.commerceItemInfo.productId
            )

            addShippingInfoItem.putString(
                FirebaseAnalytics.Param.ITEM_NAME,
                cartItem.commerceItemInfo.productDisplayName
            )

            addShippingInfoItem.putDouble(
                FirebaseAnalytics.Param.PRICE,
                cartItem.priceInfo.amount
            )

            addShippingInfoItem.putString(
                FirebaseAnalytics.Param.ITEM_BRAND,
                cartItem.commerceItemInfo?.productDisplayName
            )

            addShippingInfoItem.putInt(
                FirebaseAnalytics.Param.QUANTITY,
                cartItem.commerceItemInfo.quantity
            )
            eventItemsArray.add(addShippingInfoItem)

        }
        shoppingItems.putParcelableArray(
            FirebaseAnalytics.Param.ITEMS,
            eventItemsArray.toTypedArray()
        )

        AnalyticsManager.logEvent(
            FirebaseManagerAnalyticsProperties.ADD_SHIPPING_INFO,
            shoppingItems
        )

    }
}