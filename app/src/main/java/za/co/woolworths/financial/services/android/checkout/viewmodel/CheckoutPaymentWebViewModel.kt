package za.co.woolworths.financial.services.android.checkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.domain.repository.CheckoutRepository
import za.co.woolworths.financial.services.android.models.dto.cart.FulfillmentDetails
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import javax.inject.Inject

@HiltViewModel
class CheckoutPaymentWebViewModel @Inject constructor(
    private val checkoutRepository: CheckoutRepository
) : ViewModel() {

    fun postCheckoutComplete() {
        viewModelScope.launch {
           val suburbId = KotlinUtils.getPreferredSuburbId()
            suburbId.ifEmpty {
                return@launch
            }
            checkoutRepository.checkoutComplete(suburbId)
        }
    }

}