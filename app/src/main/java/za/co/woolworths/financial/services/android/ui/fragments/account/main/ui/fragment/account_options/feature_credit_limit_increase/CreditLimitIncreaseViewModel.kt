package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_credit_limit_increase

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect

import za.co.woolworths.financial.services.android.models.dto.OfferActive
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.ViewState
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.getViewStateFlowForNetworkCall
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.AccountProductLandingDao
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.IAccountProductLandingDao
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils.RetryNetworkRequest
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

    @Inject lateinit var retryNetworkRequest: RetryNetworkRequest

    private val _offerActive = MutableStateFlow<ViewState<OfferActive>>(ViewState.Loading(true))
    val offerActive: StateFlow<ViewState<OfferActive>> get() = _offerActive

    var mOfferActive: OfferActive? = null
    suspend fun queryRemoteServiceCLIOfferActive() =
        getViewStateFlowForNetworkCall { queryCliServiceOfferActive() }.collect {
            _offerActive.emit(it)
        }

    fun getCreditLimitIncreaseLanding(): CreditLimitIncreaseLanding {
        return CreditLimitIncreaseLanding(
            productOfferingId = getProductOfferingId(),
            offerActive = mOfferActive,
            applyNowState = getApplyNowState()
        )
    }

}