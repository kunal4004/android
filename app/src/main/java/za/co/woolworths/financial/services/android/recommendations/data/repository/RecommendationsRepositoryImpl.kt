package za.co.woolworths.financial.services.android.recommendations.data.repository

import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.JsonParseException
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart
import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse
import za.co.woolworths.financial.services.android.models.dto.LocationResponse
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.recommendations.data.response.getresponse.RecommendationResponse
import za.co.woolworths.financial.services.android.recommendations.data.response.request.RecommendationRequest
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import java.io.IOException
import javax.inject.Inject

class RecommendationsRepositoryImpl @Inject constructor(

) : RecommendationsRepository {

    override suspend fun getRecommendationResponse(recommendationRequest: RecommendationRequest?): Resource<RecommendationResponse> {

        return try {
            val response = recommendationRequest?.let { OneAppService.recommendation(it) }
            if (response?.isSuccessful == true) {
                response.body()?.let {
                    return when (it.httpCode) {
                        AppConstant.HTTP_OK ->
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
                var errorResponse: AddItemToCartResponse? = null
                try {
                    errorResponse = Gson().fromJson(
                        response?.errorBody()?.charStream(),
                        AddItemToCartResponse::class.java
                    )
                } catch (jsonException: JsonParseException) {
                    FirebaseManager.logException(jsonException)
                }
                Resource.error(R.string.error_unknown, errorResponse)
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

            val response = OneAppService.fetchInventorySkuForStore(mStoreId, referenceId, false)
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


    override suspend fun callStoreFinder(
        sku: String,
        startRadius: String?,
        endRadius: String?
    ): Resource<LocationResponse> {
        return try {
            val response = OneAppService.productStoreFinder(sku, startRadius, endRadius)
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

