package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlanResponse
import za.co.woolworths.financial.services.android.models.dto.Response
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.ui.fragments.account.card_not_received.data.CardNotReceivedDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.card_not_received.data.ICardNotReceivedService
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.ApiResult
import javax.inject.Inject

@HiltViewModel
class MyAccountsRemoteApiViewModel @Inject constructor(
    private val iTreatmentPlanDataSource: ITreatmentPlanDataSource,
    private val cardNotReceived: CardNotReceivedDataSource
) : ViewModel(), ICardNotReceivedService by cardNotReceived {

    var account: Account? = null

    private val _notifyCardNotReceived = MutableLiveData<ApiResult<Response>>()
    val notifyCardNotReceived: LiveData<ApiResult<Response>> = _notifyCardNotReceived

    fun getState() = AccountsProductGroupCode.getEnum(account?.productGroupCode)

    fun fetchCheckEligibilityTreatmentPlan(
        productGroupCode: String,
        successHandler: (EligibilityPlanResponse) -> Unit,
        failureHandler: (String) -> Unit
    ) {
        viewModelScope.launch {
            when (val checkEligibilityResponse =
                iTreatmentPlanDataSource.fetchTreatmentPlanEligibility(productGroupCode.uppercase())) {
                is ApiResult.Success -> {
                    successHandler(checkEligibilityResponse.data)
                }
                is ApiResult.Error -> {
                    failureHandler(checkEligibilityResponse.exception.toString())
                }
            }
        }
    }

    fun queryServiceCardNotYetReceived() {
        viewModelScope.launch {
            val query = queryServiceNotifyCardNotYetReceived()
            _notifyCardNotReceived.value = query
        }
    }
    
}