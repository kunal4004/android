package za.co.woolworths.financial.services.android.enhancedSubstitution.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import za.co.woolworths.financial.services.android.enhancedSubstitution.apihelper.SubstitutionApiHelper
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams

class SubstitutionPagingSource(var apiHelper: SubstitutionApiHelper,
                               var requestParams: ProductsRequestParams) :
        PagingSource<Int, ProductList>() {

    override fun getRefreshKey(state: PagingState<Int, ProductList>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                    ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ProductList> {
        return try {
            val position = params.key ?: 1
            val response = apiHelper.getSearchedProducts(requestParams)
            LoadResult.Page(
                    data = response.body()!!.products,
                    prevKey = if (position == 1) null else position - 1,
                    nextKey = position + 1)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

  }



