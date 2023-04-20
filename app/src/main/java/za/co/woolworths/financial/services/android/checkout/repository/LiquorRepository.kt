package za.co.woolworths.financial.services.android.checkout.repository

import za.co.woolworths.financial.services.android.models.dto.ShoppingCartResponse
import za.co.woolworths.financial.services.android.models.network.Resource

interface LiquorRepository {
    suspend fun getShoppingCartData() : Resource<ShoppingCartResponse>
}