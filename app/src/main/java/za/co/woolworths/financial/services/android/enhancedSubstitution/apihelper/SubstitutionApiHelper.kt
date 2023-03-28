package za.co.woolworths.financial.services.android.enhancedSubstitution.apihelper

import za.co.woolworths.financial.services.android.models.dto.ProductView
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.RetrofitConfig
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils

class SubstitutionApiHelper : RetrofitConfig() {

    suspend fun getProductSubstitution(productId: String?) = mApiInterface.getSubstitution(
            getSessionToken(),
            getDeviceIdentityToken(),
            productId
    )


    suspend fun getSearchedProducts(requestParams: ProductsRequestParams): retrofit2.Response<ProductView> {
        val (suburbId: String?, storeId: String?) = OneAppService.getSuburbOrStoreId()

        val (deliveryType, deliveryDetails) = when {
            !requestParams.sendDeliveryDetailsParams -> {
                Pair(null, null)
            }
            else ->
                Pair(
                        KotlinUtils.browsingDeliveryType?.type,
                        KotlinUtils.getDeliveryDetails(requestParams.isUserBrowsing)
                )
        }

        return if (Utils.isLocationEnabled(appContext())) {
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

}