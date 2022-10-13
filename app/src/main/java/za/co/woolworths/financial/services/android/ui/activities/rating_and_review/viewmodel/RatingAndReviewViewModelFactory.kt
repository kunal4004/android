package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.network.apihelper.RatingAndReviewApiHelper

class RatingAndReviewViewModelFactory(
        private val ratingAndReviewApiHelper: RatingAndReviewApiHelper)
    : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RatingAndReviewViewModel::class.java)) {
            return RatingAndReviewViewModel(ratingAndReviewApiHelper) as T
        }
        throw IllegalArgumentException("Unknown class name")
    }
}