package za.co.woolworths.financial.services.android.repository.shop

import android.location.Location
import za.co.woolworths.financial.services.android.checkout.service.network.ConfirmDeliveryAddressResponse
import za.co.woolworths.financial.services.android.geolocation.model.request.ConfirmLocationRequest
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidateLocationResponse
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.dash.LastOrderDetailsResponse
import za.co.woolworths.financial.services.android.models.dto.shop.DashCategories
import za.co.woolworths.financial.services.android.models.network.Resource

interface ShopRepository {

    suspend fun fetchDashLandingDetails(): Resource<DashCategories>
    suspend fun fetchOnDemandCategories(location: Location?): Resource<DashRootCategories>
    suspend fun fetchInventorySkuForStore(mStoreId: String, referenceId: String): Resource<SkusInventoryForStoreResponse>
    suspend fun addItemsToCart(mAddItemsToCart: MutableList<AddItemToCart>): Resource<AddItemToCartResponse>
    suspend fun validateLocation(placeId: String): Resource<ValidateLocationResponse>
    suspend fun confirmPlace(confirmLocationRequest: ConfirmLocationRequest): Resource<ConfirmDeliveryAddressResponse>
    suspend fun callStoreFinder(sku: String, startRadius: String?, endRadius: String?): Resource<LocationResponse>
    suspend fun fetchLastDashOrderDetails(): Resource<LastOrderDetailsResponse>
}