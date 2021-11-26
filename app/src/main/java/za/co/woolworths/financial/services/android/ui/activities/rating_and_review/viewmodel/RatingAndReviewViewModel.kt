package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.datasource.ReviewsDataSource
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.newtwork.apihelper.RatingAndReviewApiHelper

class RatingAndReviewViewModel(
        private val reviewApiHelper: RatingAndReviewApiHelper,
) : ViewModel() {

    companion object {
        const val PAGE_SIZE =  10
    }

    fun getReviewDataSource(prodId: String, sort: String?, refinement: String?) = Pager(PagingConfig(pageSize = PAGE_SIZE,
            enablePlaceholders = false)) {
        ReviewsDataSource(reviewApiHelper, prodId, sort, refinement)
    }.flow.cachedIn(viewModelScope)

}


