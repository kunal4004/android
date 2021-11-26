package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.datasource

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.Reviews
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.newtwork.apihelper.RatingAndReviewApiHelper

class ReviewsDataSource(val reviewApiHelper: RatingAndReviewApiHelper,
                        val prodId: String, val sort: String?, val refinement: String?) : PagingSource<Int, Reviews>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Reviews> {

            val position  = params.key ?: 0
            val response = reviewApiHelper.getMoreReviews(prodId, position, sort, refinement)
        return try {
            val reviews = response.data[0].reviews
            val nextKey = if (reviews.isEmpty()) {
                null
            } else {
                position + 10
            }

            val nextPageNumber = params.key ?: 1
            val response = reviewApiHelper.getMoreReviews(prodId, nextPageNumber,sort,refinement)
            LoadResult.Page (
                    data = reviews,
                    prevKey =  if (position == 0) null else position - 10,
                    nextKey = nextKey
            )
        } catch (e: Exception) {
           return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Reviews>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(10)
                    ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(10)
        }
    }
}