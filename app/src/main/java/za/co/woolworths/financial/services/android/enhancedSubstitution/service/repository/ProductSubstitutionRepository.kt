package za.co.woolworths.financial.services.android.enhancedSubstitution.service.repository

import androidx.lifecycle.MutableLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.filter
import com.awfs.coordination.R
import kotlinx.coroutines.flow.map
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.AddSubstitutionRequest
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.AddSubstitutionResponse
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.KiboProductRequest
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.KiboProductResponse
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.ProductSubstitution
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.network.SubstitutionApiHelper
import za.co.woolworths.financial.services.android.models.dto.PagingResponse
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import java.io.IOException
import javax.inject.Inject

class ProductSubstitutionRepository @Inject constructor(private var substitutionApiHelper: SubstitutionApiHelper) {

    suspend fun getProductSubstitution(productId: String?): Resource<ProductSubstitution> {
        return try {
            val response = substitutionApiHelper.getProductSubstitution(productId)
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
        } catch (e: Exception) {
            FirebaseManager.logException(e)
            Resource.error(R.string.error_unknown, null)
        }
    }


    fun getAllSearchedSubstitutions(
        requestParams: ProductsRequestParams,
        _pagingResponse: MutableLiveData<PagingResponse?>,
        productId: String?
    ) = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {
            SubstitutionPagingSource(substitutionApiHelper, requestParams, _pagingResponse)
        }
    ).flow.map { pagingData ->
        pagingData.filter { productList->
             productList.productId != productId
        }
    }

    suspend fun addSubstitution(addSubstitutionRequest: AddSubstitutionRequest): Resource<AddSubstitutionResponse> {

        return try {
            val response = substitutionApiHelper.addSubstitution(addSubstitutionRequest)
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
        } catch (e: Exception) {
            FirebaseManager.logException(e)
            Resource.error(R.string.error_unknown, null)
        }
    }

    suspend fun fetchKiboProducts(kiboProductRequest: KiboProductRequest): Resource<KiboProductResponse> {
        return try {
            val response = substitutionApiHelper.fetchKiboProducts(kiboProductRequest)
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
        } catch (e: Exception) {
            FirebaseManager.logException(e)
            Resource.error(R.string.error_unknown, null)
        }
    }

    suspend fun fetchInventoryForKiboProducts(
        storeId: String,
        multiSku: String,
    ): Resource<SkusInventoryForStoreResponse> {
        return try {
            val response = substitutionApiHelper.fetchInventoryForKiboProducts(storeId, multiSku)
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
        } catch (e: Exception) {
            FirebaseManager.logException(e)
            Resource.error(R.string.error_unknown, null)
        }
    }

    companion object {
        const val PAGE_SIZE = 60
    }
}