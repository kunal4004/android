package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlanResponse
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.treatmentplan.ProductOffering
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.ApiResult
import javax.inject.Inject

@HiltViewModel
class MyAccountsRemoteApiViewModel @Inject constructor(private val iTreatmentPlanDataSource: ITreatmentPlanDataSource): ViewModel() {

    var account: Account? = null
    fun getProductOffering() = ProductOffering(account = account)
    fun getState() = AccountsProductGroupCode.getEnum(account?.productGroupCode)
    fun fetchCheckEligibilityTreatmentPlan(productGroupCode: String, successHandler: (EligibilityPlanResponse) -> Unit, failureHandler: (String) -> Unit) {
       viewModelScope.launch {
           when(val checkEligibilityResponse = iTreatmentPlanDataSource.fetchTreatmentPlanEligibility(productGroupCode.uppercase())){
              is ApiResult.Success -> { successHandler(checkEligibilityResponse.data) }
              is ApiResult.Error -> { failureHandler(checkEligibilityResponse.exception.toString()) }
           }
       }
    }


}