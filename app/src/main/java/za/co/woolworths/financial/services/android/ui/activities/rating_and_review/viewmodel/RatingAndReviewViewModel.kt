package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.RatingReviewResponse
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.datasource.ReviewsDataSource
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.network.apihelper.RatingAndReviewApiHelper

class RatingAndReviewViewModel(
        private val reviewApiHelper: RatingAndReviewApiHelper,
) : ViewModel() {

    private var ratingReviewResponseLiveData = MutableLiveData<RatingReviewResponse>()

    companion object {
        const val PAGE_SIZE =  10
    }

    fun getReviewDataSource(prodId: String, sort: String?, refinement: String?, ratingAndResponse: RatingReviewResponse) =
            Pager(PagingConfig(pageSize = PAGE_SIZE,
            enablePlaceholders = false)) {
        ReviewsDataSource(reviewApiHelper, prodId, sort, refinement, ratingReviewResponseLiveData)
    }.flow.cachedIn(viewModelScope)

    fun getRatingReviewResponseLiveData() = ratingReviewResponseLiveData

}


