package za.co.woolworths.financial.services.android.enhancedSubstitution.repository

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import za.co.woolworths.financial.services.android.enhancedSubstitution.apihelper.SubstitutionApiHelper
import za.co.woolworths.financial.services.android.models.dto.PagingResponse
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager

class SubstitutionPagingSource (var apiHelper: SubstitutionApiHelper,
                                var requestParams: ProductsRequestParams,
                                var _pagingResponse: MutableLiveData<PagingResponse>) :
        PagingSource<Int, ProductList>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ProductList> {
        return try {
            val position = params.key ?: 0
            val response = apiHelper.getSearchedProducts(requestParams)
            val products:List<ProductList> = response.products
            val pagingResponse = response?.pagingResponse
            _pagingResponse.value = pagingResponse
            val nextKey = if (products.isEmpty() || products?.size!! <= 60) {
                null
            } else {
                position + 60
            }

            LoadResult.Page(
                    data = products,
                    prevKey =  if (position == 0) null else position,
                    nextKey = nextKey)
        } catch (e: Exception) {
            FirebaseManager.logException(e.message)
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ProductList>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(60)
                    ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(60)
        }
    }
}