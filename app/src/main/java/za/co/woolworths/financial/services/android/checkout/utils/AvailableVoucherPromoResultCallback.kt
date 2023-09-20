package za.co.woolworths.financial.services.android.checkout.utils

import androidx.activity.result.ActivityResult
import za.co.woolworths.financial.services.android.cart.view.CartFragment
import za.co.woolworths.financial.services.android.cart.view.CartFragment.Companion.SHOPPING_CART_RESPONSE
import za.co.woolworths.financial.services.android.models.dto.ShoppingCartResponse
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

/**
 * Created by Kunal Uttarwar on 28/08/23.
 */

interface AvailableVoucherPromoResult {
    fun voucherPromoCallback(result: ActivityResult): ShoppingCartResponse?
}

class AvailableVoucherPromoResultCallback @Inject constructor() : AvailableVoucherPromoResult {
    override fun voucherPromoCallback(result: ActivityResult): ShoppingCartResponse? {
        return when (result.resultCode) {
            CartFragment.APPLY_PROMO_CODE_REQUEST_CODE, CartFragment.REDEEM_VOUCHERS_REQUEST_CODE -> {
                return Utils.strToJson(
                    result.data?.getStringExtra(SHOPPING_CART_RESPONSE),
                    ShoppingCartResponse::class.java
                ) as ShoppingCartResponse
            }

            else -> null
        }
    }
}