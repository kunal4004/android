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
    override fun getRefreshKey(state: PagingState<Int, ProductList>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(ProductSubstitutionRepository.PAGE_SIZE)

                    ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(ProductSubstitutionRepository.PAGE_SIZE)
        }
    }
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ProductList> {
        return try {
            val position = params.key ?: 0
            val response = apiHelper.getSearchedProducts(requestParams)
            val pagingResponse = response.body()?.pagingResponse
            _pagingResponse.value = pagingResponse
            LoadResult.Page(
                    data = response.body()!!.products,
                    prevKey = if (position == 0) null else position - ProductSubstitutionRepository.PAGE_SIZE,
                    nextKey = position + ProductSubstitutionRepository.PAGE_SIZE)
        } catch (e: Exception) {
            FirebaseManager.logException(e.message)
            LoadResult.Error(e)
        }
    }
}