package za.co.woolworths.financial.services.android.ui.fragments.product.detail.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart
import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse
import za.co.woolworths.financial.services.android.models.dto.ProductDetailResponse
import za.co.woolworths.financial.services.android.models.dto.ProductRequest
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse
import za.co.woolworths.financial.services.android.models.network.ApiInterface
import za.co.woolworths.financial.services.android.models.network.AppContextProviderImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

/**
 * Created by Kunal Uttarwar on 08/02/24.
 */
class MatchingSetRepositoryImpl @Inject constructor(private val apiInterface: ApiInterface) :
    MatchingSetRepository, CoreDataSource(), ApiInterface by apiInterface {

    override suspend fun getMatchingItemDetail(
        productsDetailsRequest: ProductRequest
    ): Flow<IOTaskResult<ProductDetailResponse>> {

        val loc = getMyLocation()
        val (suburbId: String?, storeId: String?) = getSuburbOrStoreId()
        val deliveryType = KotlinUtils.getPreferredDeliveryType()?.type ?:""

         if (Utils.isLocationEnabled(AppContextProviderImpl().appContext())) {
            return executeSafeNetworkApiCall {
                matchingSetProductDetail(
                    "",
                    "",
                    loc.longitude,
                    loc.latitude,
                    getSessionToken(),
                    getDeviceIdentityToken(),
                    productsDetailsRequest.productId,
                    productsDetailsRequest.skuId,
                    suburbId,
                    storeId,
                    deliveryType = deliveryType,
                    deliveryDetails = KotlinUtils.getDeliveryDetails(productsDetailsRequest.isUserBrowsing)
                )
            }
        } else {
            return executeSafeNetworkApiCall {
                matchingSetProductDetail(
                    "",
                    "",
                    getSessionToken(),
                    getDeviceIdentityToken(),
                    productsDetailsRequest.productId,
                    productsDetailsRequest.skuId,
                    suburbId,
                    storeId,
                    deliveryType = deliveryType,
                    deliveryDetails = KotlinUtils.getDeliveryDetails(productsDetailsRequest.isUserBrowsing)
                )
            }
        }
    }

    override suspend fun getInventoryForMatchingItems(storeId:String, multipleSku:String): Flow<IOTaskResult<SkusInventoryForStoreResponse>> {
        return executeSafeNetworkApiCall {
            inventoryForMatchingItemDetails(
                getSessionToken(),
                getDeviceIdentityToken(),
                storeId,
                multipleSku

            )
        }
    }

    override suspend fun addToCartForMatchingItems(addToCart: MutableList<AddItemToCart>): Flow<IOTaskResult<AddItemToCartResponse>> {
        val deliveryType = KotlinUtils.getPreferredDeliveryType()?.type ?: ""
        return executeSafeNetworkApiCall {
            addItemToCartForMatchingSet(
                super.getDeviceIdentityToken(),
                deliveryType,
                addToCart
            )
        }
    }

    fun getSuburbOrStoreId(): Pair<String?, String?> {
        val suburbId: String? = null
        val storeId: String? = null
        return Pair(suburbId, storeId)
    }
}