package za.co.woolworths.financial.services.android.repository.shop

import za.co.woolworths.financial.services.android.models.dto.RootCategories
import za.co.woolworths.financial.services.android.models.network.Resource

interface ShopRepository {

    suspend fun fetchDashCategories(): Resource<RootCategories>
}