package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlan
import za.co.woolworths.financial.services.android.models.dto.ProductGroupCode
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.*
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.AccountOptionsScreenUI
import javax.inject.Inject

@HiltViewModel
class StoreCardAccountOptionsViewModel @Inject constructor(
    private val accountProduct: AccountProductLandingDao,
    private val accountOptions: AccountOptionsImpl,
) : ViewModel(), IAccountProductLandingDao by accountProduct,
    IAccountOptions by accountOptions {

    private val _viewState = MutableStateFlow(mutableListOf<AccountOptionsScreenUI>())
    val viewState: StateFlow<MutableList<AccountOptionsScreenUI>> = _viewState

    var eligibilityPlanState: MutableStateFlow<EligibilityPlan> = MutableStateFlow(EligibilityPlan("", "", "", ProductGroupCode.CC, "", ""))

    init {

        with(_viewState) {
            viewModelScope.launch {
                emit(balanceProtectionInsurance())
                emit(isDebitOrderActive())
                emit(paymentOptions())
            }
        }

            viewModelScope.launch {
                eligibilityPlanState.asStateFlow().collect { plan ->
                    if (!plan.actionText.isNullOrEmpty() && !plan.displayText.isNullOrEmpty()) {
                        _viewState.emit(collectionTreatmentPlanItem(plan))
                    }
                }
        }
    }
}