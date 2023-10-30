package za.co.woolworths.financial.services.android.shoptoggle.domain.repository

import za.co.woolworths.financial.services.android.geolocation.network.model.ValidateLocationResponse
import za.co.woolworths.financial.services.android.shoptoggle.data.dto.ShopToggleData

interface ShopToggleRepository {

    fun getShopToggleList(): List<ShopToggleData>

    suspend fun getValidateLocation(placeId: String): ValidateLocationResponse
}