package za.co.woolworths.financial.services.android.enhancedSubstitution.service.network

import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.AddSubstitutionRequest
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.KiboProductRequest
import za.co.woolworths.financial.services.android.models.dto.ProductView
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.models.network.AppContextProviderImpl
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.RetrofitApiProviderImpl
import za.co.woolworths.financial.services.android.models.network.RetrofitConfig
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

class SubstitutionApiHelper @Inject constructor() : RetrofitConfig(AppContextProviderImpl(), RetrofitApiProviderImpl()) {

    suspend fun getProductSubstitution(productId: String?) =
        mApiInterface.getSubstitution(
            getSessionToken(),
            getDeviceIdentityToken(),
            productId
        )

    suspend fun getSearchedProducts(requestParams: ProductsRequestParams): ProductView {
        val (suburbId: String?, storeId: String?) = OneAppService().getSuburbOrStoreId()

        val (deliveryType, deliveryDetails) = when {
            !requestParams.sendDeliveryDetailsParams -> {
                Pair(null, null)
            }

            else ->
                Pair(
                    KotlinUtils.browsingDeliveryType?.type,
                    KotlinUtils.getDeliveryDetails(false)
                )
        }

        return if (Utils.isLocationEnabled(AppContextProviderImpl().appContext())) {
            mApiInterface.getSearchedProducts(
                userAgent = "",
                "",
                "",
                "",
                getSessionToken(),
                getDeviceIdentityToken(),
                requestParams.searchTerm,
                requestParams.searchType.value,
                requestParams.responseType.value,
                requestParams.pageOffset,
                Utils.PAGE_SIZE,
                requestParams.sortOption,
                requestParams.refinement,
                suburbId = suburbId,
                storeId = storeId,
                filterContent = requestParams.filterContent,
                deliveryType = deliveryType,
                deliveryDetails = deliveryDetails
            )
        } else {
            mApiInterface.getSearchedProductsWithoutLocation(
                "",
                "",
                getSessionToken(),
                getDeviceIdentityToken(),
                requestParams.searchTerm,
                requestParams.searchType.value,
                requestParams.responseType.value,
                requestParams.pageOffset,
                Utils.PAGE_SIZE,
                requestParams.sortOption,
                requestParams.refinement,
                suburbId = suburbId,
                storeId = storeId,
                filterContent = requestParams.filterContent,
                deliveryType = deliveryType,
                deliveryDetails = deliveryDetails
            )
        }
    }

    suspend fun addSubstitution(addSubstitutionRequest: AddSubstitutionRequest) =
        mApiInterface.addSubstitution(
            getSessionToken(),
            getDeviceIdentityToken(),
            addSubstitutionRequest
        )

    suspend fun fetchKiboProducts(kiboProductRequest: KiboProductRequest) =
        mApiInterface.getKiboProductsFromResponse(
            getSessionToken(),
            getDeviceIdentityToken(),
            kiboProductRequest
        )

    suspend fun fetchInventoryForKiboProducts(storeId: String, multipleSku: String) =
        mApiInterface.fetchKiboInventorySKUForStore(
            getSessionToken(),
            getDeviceIdentityToken(),
            store_id = storeId,
            multipleSku = multipleSku,
            substitution = true
        )
}