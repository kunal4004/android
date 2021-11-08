package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.network.service.ReviewApiHelper
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.network.repository.ReviewDataSource

class ReviewViewModel (private val reviewApiHelper: ReviewApiHelper): ViewModel() {

    val moreReviewData = Pager(PagingConfig(pageSize = 1)) {
        ReviewDataSource(reviewApiHelper)
    }.flow.cachedIn(viewModelScope)
}


