package za.co.woolworths.financial.services.android.util

import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import za.co.woolworths.financial.services.android.models.dto.CartResponse
import za.co.woolworths.financial.services.android.models.dto.CommerceItem
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.Voucher
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

        fun getAppliedVouchersCount(vouchers: ArrayList<Voucher>): Int {
            return vouchers.filter { it.voucherApplied }.size
        }

        fun updateItemLimitsBanner(productCountMap: ProductCountMap?, banner: ConstraintLayout?, message: TextView?, counter: TextView?, showBanner: Boolean) {
            banner?.visibility = View.GONE
            productCountMap?.let {
                if (it.quantityLimit?.foodLayoutColour != null && showBanner && it.totalProductCount ?: 0 > 0) {
                    message?.text = it.quantityLimit.foodLayoutMessage ?: ""
                    counter?.text = it.totalProductCount.toString() + "/" + it.quantityLimit.foodMaximumQuantity?:""
                    if (it.quantityLimit.foodLayoutColour.isNotEmpty() && !it.quantityLimit.foodLayoutMessage.isNullOrEmpty()) {
                        banner?.visibility = View.VISIBLE
                        banner?.setBackgroundColor(Color.parseColor(it.quantityLimit.foodLayoutColour))
                    }
                }
            }
        }
    }
}