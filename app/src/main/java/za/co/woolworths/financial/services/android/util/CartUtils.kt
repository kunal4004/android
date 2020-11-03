package za.co.woolworths.financial.services.android.util

import za.co.woolworths.financial.services.android.models.dto.CartResponse
import za.co.woolworths.financial.services.android.models.dto.CommerceItem

class CartUtils {
    companion object {

        fun filterCommerceItemFromCartResponse(cartResponse: CartResponse, commerceId: String): CommerceItem? {
            var commerceItem: CommerceItem? = null
            cartResponse.cartItems?.forEach { group ->
                commerceItem = group.commerceItems?.find { it.commerceItemInfo.commerceId.equals(commerceId, true) }
                if (commerceItem != null)
                    return@forEach
            }
            return commerceItem
        }

    }
}