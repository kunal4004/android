package za.co.woolworths.financial.services.android.repository.shop

import android.location.Location
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart
import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidateLocationResponse
import za.co.woolworths.financial.services.android.models.dto.RootCategories
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse
import za.co.woolworths.financial.services.android.models.dto.shop.DashCategories
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.FirebaseManager
import java.io.IOException

class MainShopRepository : ShopRepository {

    override suspend fun fetchDashLandingDetails(): Resource<DashCategories> {
        return try {
            val response = OneAppService.getDashLandingDetails()
            if (response.isSuccessful) {
                response.body()?.let {
                    return when (it.httpCode) {
                        AppConstant.HTTP_OK, AppConstant.HTTP_OK_201 ->
                            Resource.success(it)
                        else ->
                            Resource.error(R.string.error_unknown, it)
                    }
                } ?: Resource.error(R.string.error_unknown, null)
            } else {
                Resource.error(R.string.error_unknown, null)
            }
        } catch (e: IOException) {
            FirebaseManager.logException(e)
            Resource.error(R.string.error_internet_connection, null)
        }
    }

    override suspend fun fetchOnDemandCategories(location: Location?): Resource<RootCategories> {
        return try {

            val response = OneAppService.getDashCategoryNavigation(location)
            if (response.isSuccessful) {
                response.body()?.let {
                    return when (it.httpCode) {
                        AppConstant.HTTP_OK, AppConstant.HTTP_OK_201 ->
                            Resource.success(it)
                        else ->
                            Resource.error(R.string.error_unknown, it)
                    }
                } ?: Resource.error(R.string.error_unknown, null)
            } else {
                Resource.error(R.string.error_unknown, null)
            }
        } catch (e: IOException) {
            FirebaseManager.logException(e)
            Resource.error(R.string.error_internet_connection, null)
        }
    }

    override suspend fun fetchInventorySkuForStore(
        mStoreId: String,
        referenceId: String
    ): Resource<SkusInventoryForStoreResponse> {
        return try {

            val response = OneAppService.fetchInventorySkuForStore(mStoreId, referenceId)
            if (response.isSuccessful) {
                response.body()?.let {
                    return when (it.httpCode) {
                        AppConstant.HTTP_OK, AppConstant.HTTP_OK_201 ->
                            Resource.success(it)
                        else ->
                            Resource.error(R.string.error_unknown, it)
                    }
                } ?: Resource.error(R.string.error_unknown, null)
            } else {
                Resource.error(R.string.error_unknown, null)
            }
        } catch (e: IOException) {
            FirebaseManager.logException(e)
            Resource.error(R.string.error_internet_connection, null)
        }
    }

    override suspend fun addItemsToCart(mAddItemsToCart: MutableList<AddItemToCart>): Resource<AddItemToCartResponse> {
       return try {

            val response = OneAppService.addItemsToCart(mAddItemsToCart)
            if (response.isSuccessful) {
                response.body()?.let {
                    return when (it.httpCode) {
                        AppConstant.HTTP_OK, AppConstant.HTTP_OK_201 ->
                            Resource.success(it)
                        else ->
                            Resource.error(R.string.error_unknown, it)
                    }
                } ?: Resource.error(R.string.error_unknown, null)
            } else {
                Resource.error(R.string.error_unknown, null)
            }
        } catch (e: IOException) {
            FirebaseManager.logException(e)
            Resource.error(R.string.error_internet_connection, null)
        }
    }

    override suspend fun validateLocation(placeId: String): Resource<ValidateLocationResponse> {
        return try {

            val response = OneAppService.getValidateLocation(placeId)
            if (response.isSuccessful) {
                response.body()?.let {
                    return when (it.httpCode) {
                        AppConstant.HTTP_OK, AppConstant.HTTP_OK_201 ->
                            Resource.success(it)
                        else ->
                            Resource.error(R.string.error_unknown, it)
                    }
                } ?: Resource.error(R.string.error_unknown, null)
            } else {
                Resource.error(R.string.error_unknown, null)
            }
        } catch (e: IOException) {
            FirebaseManager.logException(e)
            Resource.error(R.string.error_internet_connection, null)
        }
    }
}