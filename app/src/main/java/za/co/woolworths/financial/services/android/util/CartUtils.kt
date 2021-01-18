package za.co.woolworths.financial.services.android.util

import za.co.woolworths.financial.services.android.models.dto.CartResponse
import za.co.woolworths.financial.services.android.models.dto.CommerceItem
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.Voucher

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

    }
}