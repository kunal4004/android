package za.co.woolworths.financial.services.android.util

import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import za.co.woolworths.financial.services.android.models.dto.CartResponse
import za.co.woolworths.financial.services.android.models.dto.CommerceItem
import za.co.woolworths.financial.services.android.models.dto.item_limits.ProductCountMap

class CartUtils {
    companion object {

        fun filterCommerceItemFromCartResponse(cartResponse: CartResponse, commerceId: String): CommerceItem? {
            var commerceItem: CommerceItem? = null
            cartResponse.cartItems?.forEach { group ->
                commerceItem = group.commerceItems?.find { it.commerceItemInfo.commerceId.equals(commerceId, true) }
                if (commerceItem != null)
                    return commerceItem
            }
            return commerceItem
        }

        fun updateItemLimitsBanner(productCountMap: ProductCountMap?, banner: ConstraintLayout, message: TextView, counter: TextView, isClickAndCollect: Boolean) {
            productCountMap.let {
                if (it != null && it.quantityLimit?.foodLayoutColour != null && isClickAndCollect) {
                    message.text = it.quantityLimit.foodLayoutMessage ?: ""
                    counter.text = it.totalProductCount.toString() + "/" + it.quantityLimit.foodMaximumQuantity?:""
                    banner.setBackgroundColor(Color.parseColor(it.quantityLimit.foodLayoutColour))
                    banner.visibility = View.VISIBLE
                } else {
                    banner.visibility = View.GONE
                }
            }
        }

    }
}