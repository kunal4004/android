package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_credit_limit_increase

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel

import za.co.woolworths.financial.services.android.models.dto.OfferActive
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.getViewStateFlowForNetworkCall
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.AccountProductLandingDao
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.IAccountProductLandingDao
import javax.inject.Inject

@HiltViewModel
class CreditLimitIncreaseViewModel @Inject constructor(
    private val accountDao : AccountProductLandingDao,
    private val firebaseEvent: CreditLimitIncreaseFirebaseEvent,
    private val dataSource: CreditLimitIncreaseDataSource,
    private val handleResult: HandleCreditLimitIncreaseStatus
) : ViewModel(),
    IAccountProductLandingDao by accountDao,
    ICreditLimitIncrease by dataSource,
    IHandleCreditLimitIncreaseStatus by handleResult,
    ICreditLimitIncreaseFirebaseEvent by firebaseEvent {

    var mOfferActive: OfferActive? = null
    suspend fun queryRemoteServiceCLIOfferActive() =
        getViewStateFlowForNetworkCall { queryCliServiceOfferActive() }

    fun getCreditLimitIncreaseLanding(): CreditLimitIncreaseLanding {
        return CreditLimitIncreaseLanding(
            productOfferingId = getProductOfferingId(),
            offerActive = mOfferActive,
            applyNowState = getApplyNowState()
        )
    }

}