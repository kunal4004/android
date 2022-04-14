package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_credit_limit_increase

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.getViewStateFlowForNetworkCall
import javax.inject.Inject

@HiltViewModel
class CreditLimitIncreaseViewModel @Inject constructor(private val cliApi: CreditLimitIncreaseDataSource) :
    ViewModel(), ICreditLimitIncrease by cliApi {

    suspend fun queryRemoteServiceCLIOfferActive() = getViewStateFlowForNetworkCall { queryCliServiceOfferActive() } }