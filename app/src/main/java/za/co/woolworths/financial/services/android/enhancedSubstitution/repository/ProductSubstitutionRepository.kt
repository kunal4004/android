package za.co.woolworths.financial.services.android.enhancedSubstitution.repository

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.awfs.coordination.R
import com.google.gson.JsonSyntaxException
import za.co.woolworths.financial.services.android.enhancedSubstitution.apihelper.SubstitutionApiHelper
import za.co.woolworths.financial.services.android.enhancedSubstitution.model.ProductSubstitution
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


    fun getAllSearchedSubstitutions(requestParams: ProductsRequestParams): LiveData<PagingData<ProductList>> {

        return Pager(
                config = PagingConfig(
                        pageSize = NETWORK_PAGE_SIZE,
                        enablePlaceholders = false,
                        initialLoadSize = 2
                ),
                pagingSourceFactory = {
                    SubstitutionPagingSource(substitutionApiHelper, requestParams)
                }
                , initialKey = 1
        ).liveData
    }


    companion object {
        private val NETWORK_PAGE_SIZE = 60
    }
}