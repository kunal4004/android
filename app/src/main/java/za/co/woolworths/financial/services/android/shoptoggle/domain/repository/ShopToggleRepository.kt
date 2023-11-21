package za.co.woolworths.financial.services.android.shoptoggle.domain.repository

import za.co.woolworths.financial.services.android.geolocation.network.model.ValidateLocationResponse

interface ShopToggleRepository {

    suspend fun getValidateLocation(placeId: String): ValidateLocationResponse
}