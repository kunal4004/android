package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.network.service.ReviewApiHelper

class ReviewViewModelFactory (private val reviewApiHelper: ReviewApiHelper) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReviewViewModel::class.java)) {
            return ReviewViewModel(reviewApiHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}