package za.co.woolworths.financial.services.android.repository.shop

import android.location.Location
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidateLocationResponse
import za.co.woolworths.financial.services.android.models.dto.RootCategories
import za.co.woolworths.financial.services.android.models.dto.shop.DashCategories
import za.co.woolworths.financial.services.android.models.network.Resource

interface ShopRepository {

    suspend fun fetchDashLandingDetails(): Resource<DashCategories>
    suspend fun fetchOnDemandCategories(location: Location?): Resource<RootCategories>
    suspend fun validateLocation(placeId: String): Resource<ValidateLocationResponse>
}