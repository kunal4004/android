package za.co.woolworths.financial.services.android.cart.viewmodel

import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import za.co.woolworths.financial.services.android.cart.service.network.CartResponse
import za.co.woolworths.financial.services.android.models.dto.CommerceItem
import za.co.woolworths.financial.services.android.models.dto.item_limits.ProductCountMap
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.Voucher
import za.co.woolworths.financial.services.android.util.Utils

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

        fun getAppliedVouchersCount(vouchers: ArrayList<Voucher>): Int {
            return vouchers.filter { it.voucherApplied }.size
        }

        fun updateItemLimitsBanner(productCountMap: ProductCountMap?, banner: ConstraintLayout?, message: TextView?, counter: TextView?, showBanner: Boolean) {
            banner?.visibility = View.GONE
            productCountMap?.let {
                if (it.quantityLimit?.foodLayoutColour != null && showBanner && it.totalProductCount ?: 0 > Utils.THRESHOLD_FOR_CART_LIMIT_BANNER) {
                    message?.text = it.quantityLimit.foodLayoutMessage ?: ""
                    if(it.quantityLimit.other!=null && it.totalProductCount !=null && it.totalProductCount>it.quantityLimit.other) {
                        counter?.text = ((it.totalProductCount - it.quantityLimit.other).toString() + "/" + it.quantityLimit.foodMaximumQuantity) ?: ""
                    }
                    if (it.quantityLimit.foodLayoutColour.isNotEmpty() && !it.quantityLimit.foodLayoutMessage.isNullOrEmpty()) {
                        banner?.visibility = View.VISIBLE
                        banner?.setBackgroundColor(Color.parseColor(it.quantityLimit.foodLayoutColour))
                    }
                }
            }
        }
    }
}