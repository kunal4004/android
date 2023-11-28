package za.co.woolworths.financial.services.android.shoptoggle.data.repository

import za.co.woolworths.financial.services.android.geolocation.network.apihelper.GeoLocationApiHelper
import za.co.woolworths.financial.services.android.shoptoggle.domain.repository.ShopToggleRepository
import javax.inject.Inject

class ShopToggleRepositoryImpl @Inject constructor(
    private val geoLocationApiHelper: GeoLocationApiHelper
) : ShopToggleRepository {

    override suspend fun getValidateLocation(placeId: String) = geoLocationApiHelper.getValidateLocation(placeId)
}