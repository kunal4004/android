package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.network.datasource

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.RatingReviewResponse
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.Reviews
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.network.apihelper.RatingAndReviewApiHelper

class ReviewsDataSource(
    val reviewApiHelper: RatingAndReviewApiHelper,
    val prodId: String, val sort: String?, val refinement: String?,
    var ratingAndResponseLiveData: MutableLiveData<RatingReviewResponse>,
    var ratingAndResponseLiveDataOne: MutableLiveData<RatingReviewResponse>
) :
    PagingSource<Int, Reviews>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Reviews> {
        return try {
            val position = params.key ?: 0
            val response = reviewApiHelper.getMoreReviews(prodId, position, sort, refinement)
            val responseData = response.data[0]
            ratingAndResponseLiveData.postValue(responseData)
            ratingAndResponseLiveDataOne.value = responseData
            val reviews = response.data[0].reviews
            val nextKey = if (reviews.isEmpty()) {
                null
            } else {
                position + 10
            }

            LoadResult.Page(
                data = reviews,
                prevKey = if (position == 0) null else position - 10,
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