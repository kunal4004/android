package za.co.woolworths.financial.services.android.repository.shop

import za.co.woolworths.financial.services.android.models.dto.RootCategories
import za.co.woolworths.financial.services.android.models.dto.shop.DashCategories
import za.co.woolworths.financial.services.android.models.network.Resource

interface ShopRepository {

    suspend fun fetchDashCategories(): Resource<DashCategories>
    suspend fun fetchOnDemandCategories(): Resource<RootCategories>
}