package za.co.woolworths.financial.services.android.enhancedSubstitution.repository

import androidx.lifecycle.MutableLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.awfs.coordination.R
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.Flow
import za.co.woolworths.financial.services.android.enhancedSubstitution.apihelper.SubstitutionApiHelper
import za.co.woolworths.financial.services.android.enhancedSubstitution.model.ProductSubstitution
import za.co.woolworths.financial.services.android.models.dto.PagingResponse
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import java.io.IOException

class ProductSubstitutionRepository(var substitutionApiHelper: SubstitutionApiHelper) {

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
        }
        catch (e: JsonSyntaxException) {
            FirebaseManager.logException(e)
            Resource.error(R.string.error_unknown, null)
        }
    }


    fun getAllSearchedSubstitutions(requestParams: ProductsRequestParams, _pagingResponse: MutableLiveData<PagingResponse>): Flow<PagingData<ProductList>> {
        return Pager(
                config = PagingConfig(
                        pageSize = PAGE_SIZE,
                        enablePlaceholders = false
                ),
                pagingSourceFactory = {
                    SubstitutionPagingSource(substitutionApiHelper, requestParams, _pagingResponse)
                }
        ).flow
    }


    companion object {
         val PAGE_SIZE = 60
    }
}