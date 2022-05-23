package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlanResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.*
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.StoreCardDataSource
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.ApiResult
import javax.inject.Inject

@HiltViewModel
class MyAccountsRemoteApiViewModel @Inject constructor(
    private val collection: TreatmentPlanDataSource,
    val storeCardDataSource: StoreCardDataSource
) : ViewModel() {

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

    suspend fun queryServiceGetStoreCardCards() = getViewStateFlowForNetworkCall { storeCardDataSource.queryServiceGetStoreCards() }

    suspend fun queryServiceBlockUnblockStoreCard(position : Int) = getViewStateFlowForNetworkCall { storeCardDataSource.queryServiceBlockUnBlockStoreCard(position = position) }

}