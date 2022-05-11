package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_credit_limit_increase

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.getViewStateFlowForNetworkCall
import javax.inject.Inject

@HiltViewModel
class CreditLimitIncreaseViewModel @Inject constructor(
    private val creditLimitIncreaseDataSource: CreditLimitIncreaseDataSource,
    private val handleCreditLimitIncreaseStatus: HandleCreditLimitIncreaseStatus
) : ViewModel(), ICreditLimitIncrease by creditLimitIncreaseDataSource,
    IHandleCreditLimitIncreaseStatus by handleCreditLimitIncreaseStatus {

    suspend fun queryRemoteServiceCLIOfferActive() = getViewStateFlowForNetworkCall { queryCliServiceOfferActive() }
}