package za.co.woolworths.financial.services.android.repository.shop

import android.location.Location
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidateLocationResponse
import za.co.woolworths.financial.services.android.models.dto.RootCategories
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
                            Resource.error("An unknown error occurred", it)
                    }
                } ?: Resource.error("An unknown error occurred", null)
            } else {
                Resource.error("An unknown error occurred", null)
            }
        } catch (e: IOException) {
            FirebaseManager.logException(e)
            Resource.error("Couldn't reach the server. Check your internet connection", null)
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
                            Resource.error("An unknown error occurred", it)
                    }
                } ?: Resource.error("An unknown error occurred", null)
            } else {
                Resource.error("An unknown error occurred", null)
            }
        } catch (e: IOException) {
            FirebaseManager.logException(e)
            Resource.error("Couldn't reach the server. Check your internet connection", null)
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
                            Resource.error("An unknown error occurred", it)
                    }
                } ?: Resource.error("An unknown error occurred", null)
            } else {
                Resource.error("An unknown error occurred", null)
            }
        } catch (e: IOException) {
            FirebaseManager.logException(e)
            Resource.error("Couldn't reach the server. Check your internet connection", null)
        }
    }
}