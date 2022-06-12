package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlanResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.*
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.IStoreCardDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.StoreCardDataSource
import za.co.woolworths.financial.services.android.models.dto.Response
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.ui.fragments.account.card_not_received.data.CardNotReceivedDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.card_not_received.data.ICardNotReceivedService
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.ApiResult
import javax.inject.Inject

@HiltViewModel
class MyAccountsRemoteApiViewModel @Inject constructor(
    private val collection: TreatmentPlanDataSource,
    val storeCardDataSource: StoreCardDataSource,
    private val cardNotReceived: CardNotReceivedDataSource

) : ViewModel(), IStoreCardDataSource by storeCardDataSource,ICardNotReceivedService by cardNotReceived {

    private val _viewState = MutableStateFlow(CoreDataSource.IOTaskResult.Empty)
    val viewState = _viewState.asStateFlow()

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
                collection.fetchTreatmentPlanEligibility(productGroupCode.uppercase())) {
                is ApiResult.Success -> {
                    successHandler(checkEligibilityResponse.data)
                }
                is ApiResult.Error -> {
                    failureHandler(checkEligibilityResponse.exception.toString())
                }
            }
        }
    }

    suspend fun queryServiceGetStoreCardCards() = getViewStateFlowForNetworkCall { queryServiceGetStoreCards() }

    fun queryServiceCardNotYetReceived() {
        viewModelScope.launch {
            val query = queryServiceNotifyCardNotYetReceived()
            _notifyCardNotReceived.value = query
        }
    }
}