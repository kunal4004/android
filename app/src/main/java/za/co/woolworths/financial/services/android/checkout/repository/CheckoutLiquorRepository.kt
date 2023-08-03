package za.co.woolworths.financial.services.android.checkout.repository

import za.co.woolworths.financial.services.android.models.dto.ShoppingCartResponse
import za.co.woolworths.financial.services.android.models.network.Resource

interface CheckoutLiquorRepository {
    suspend fun getShoppingCartData() : Resource<ShoppingCartResponse>
}