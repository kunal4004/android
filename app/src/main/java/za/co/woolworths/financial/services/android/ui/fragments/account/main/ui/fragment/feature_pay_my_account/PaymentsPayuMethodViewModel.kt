package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.feature_pay_my_account

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.pma.PaymentMethodsResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.ViewState
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.getViewStateFlowForNetworkCall
import javax.inject.Inject

@HiltViewModel
class PaymentsPayuMethodViewModel @Inject constructor(val request: PaymentsPayuMethodsDataSource) :
    PayMyAccountViewModel(), IPaymentsPayuMethodsDataSource by request {

    private val _paymentMethodsResponseResult = MutableSharedFlow<ViewState<PaymentMethodsResponse>>(replay = 0)
    val paymentMethodsResponseResult: SharedFlow<ViewState<PaymentMethodsResponse>> get() = _paymentMethodsResponseResult

    fun requestPaymentPayuMethod() = viewModelScope.launch {
        getViewStateFlowForNetworkCall { requestPaymentsPayuMethods() }.collect {
            _paymentMethodsResponseResult.emit(it)
        }
    }

    fun getLandingScreen(payMyAccountResponse: PaymentMethodsResponse?): PAYUMethodType {
        val paymentMethods = payMyAccountResponse?.paymentMethods
        val response = payMyAccountResponse?.response

        return when  {
            (paymentMethods?.size ?: 0) == 0 || response?.code?.startsWith("P0453") == true -> PAYUMethodType.CREATE_USER
            (paymentMethods?.size ?: 0) > 0 || paymentMethods?.isEmpty() == false -> PAYUMethodType.CARD_UPDATE
            else ->  PAYUMethodType.ERROR
        }
    }

}