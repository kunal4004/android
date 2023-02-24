package za.co.woolworths.financial.services.android.ui.fragments.product.shop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.cart.SubmittedOrderResponse
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.usecase.SubmitRecommendationsUseCase
import javax.inject.Inject

@HiltViewModel
class OrderConfirmationViewModel @Inject constructor(
    private val submitRecommendationsUseCase: SubmitRecommendationsUseCase
) : ViewModel() {

    /**
     * Here we are just submitting the events for recommendations and we do not expect any data in response
     */
    fun submitRecommendationsOnOrderResponse(response: SubmittedOrderResponse) {
        viewModelScope.launch {
            val resp = submitRecommendationsUseCase(response)
            val success = resp.status == Status.SUCCESS
            println("OrderConfirmationViewModel, submit recommendations success $success")
        }
    }
}