package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.datasource

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.Reviews
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.newtwork.apihelper.RatingAndReviewApiHelper

class ReviewsDataSource(val reviewApiHelper: RatingAndReviewApiHelper,
                        val prodId: String) : PagingSource<Int, Reviews>() {

    override fun getRefreshKey(state: PagingState<Int, Reviews>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(10) ?: anchorPage?.nextKey?.minus(10)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Reviews> {
        return try {
            Log.e("params_key :", params.key.toString())
            val nextPageNumber = params.key ?: 1
            Log.e("nextPageNumber :", nextPageNumber.toString())
            val response = reviewApiHelper.getMoreReviews(prodId, nextPageNumber)
            LoadResult.Page (
                    data = response.data.get(0).reviews,
                    prevKey = if (nextPageNumber > 1) nextPageNumber - 10 else null,
                    nextKey = if (nextPageNumber < response.data.get(0).reviews.size) nextPageNumber + 10 else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}