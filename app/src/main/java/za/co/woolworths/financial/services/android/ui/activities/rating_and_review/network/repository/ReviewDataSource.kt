package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.network.repository

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.Reviews
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.network.service.ReviewApiHelper

class ReviewDataSource(private val reviewApiHelper: ReviewApiHelper): PagingSource<Int, Reviews>() {

    override fun getRefreshKey(state: PagingState<Int, Reviews>): Int? {
        return state.anchorPosition
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Reviews> {
        Log.e("Load:", "called")
        return try {
            val currentLoadingPageKey = params.key ?:1
            val response = reviewApiHelper.getMoreReviews(currentLoadingPageKey)
            val responseData = mutableListOf<Reviews>()
            val data = response.body()?.data?.get(0)?.reviews ?: emptyList()
            val prevKey = if (currentLoadingPageKey == 1) null else currentLoadingPageKey-1

         LoadResult.Page(
                    data,
                    prevKey,
                    if (data.size == 0) null else currentLoadingPageKey.plus(1)
            )
        } catch (exception:Exception) {
            return LoadResult.Error(exception)
        }
    }
}