package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlan
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlanResponse
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.ViewState
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.getViewStateFlowForNetworkCall
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.repository.storecard.CollectionRepository
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.repository.storecard.ICollectionRepository
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.*
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.AccountOptionsScreenUI
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.IStoreCardNavigator
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.StoreCardNavigator
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ViewTreatmentPlanImpl
import javax.inject.Inject

@HiltViewModel
class AccountProductsHomeViewModel @Inject constructor(
    screen: AccountProductLandingScreenStatus,
    account: AccountProductLandingDao,
    private val collectionRepository: CollectionRepository,
    val navigator: StoreCardNavigator,
    val accountOptions: AccountOptionsImpl
) : ViewModel(),
    IAccountProductLandingDao by account,
    IAccountOptions by accountOptions,
    IAccountProductLandingScreen by screen,
    ICollectionRepository by collectionRepository,
    IStoreCardNavigator by navigator {

    var mViewTreatmentPlanImpl: ViewTreatmentPlanImpl? = null
    private val _viewState = MutableSharedFlow<List<AccountOptionsScreenUI>>()
    val viewState: SharedFlow<List<AccountOptionsScreenUI>> = _viewState

    fun setUpViewTreatmentPlan(eligibilityPlan: EligibilityPlan?) {
         mViewTreatmentPlanImpl = ViewTreatmentPlanImpl(
            account = product,
            applyNowState = ApplyNowState.STORE_CARD,
            eligibilityPlan = eligibilityPlan
        )
    }

    init {
        with(_viewState) {
            viewModelScope.launch {
                emit(balanceProtectionInsurance())
                emit(isDebitOrderActive())
                emit(paymentOptions())
            }
        }
    }

    fun updateBPI(account: Account) {
        viewModelScope.launch {
            product?.bpiInsuranceApplication = account.bpiInsuranceApplication
            _viewState.emit(balanceProtectionInsurance())
        }
    }

    fun emitEligibilityPlan(eligibilityPlan: EligibilityPlan?){
        viewModelScope.launch {
            eligibilityPlan?.let { plan ->
                if (!plan.actionText.isNullOrEmpty() && !plan.displayText.isNullOrEmpty()) {
                    _viewState.emit(collectionTreatmentPlanItem(plan))
                }
            }
        }
    }

    /**
     * _accountsCollectionsCheckEligibility will determine the visibility of collection journey
     * (view treatment plan elite plan etc)
     */
    private val _accountsCollectionsCheckEligibility =
        MutableSharedFlow<ViewState<EligibilityPlanResponse>>(0)
    val accountsCollectionsCheckEligibility: SharedFlow<ViewState<EligibilityPlanResponse>> =
        _accountsCollectionsCheckEligibility

    fun requestAccountsCollectionsCheckEligibility() = viewModelScope.launch {
        getViewStateFlowForNetworkCall { queryServiceCheckCustomerEligibilityPlan() }.collect {
            _accountsCollectionsCheckEligibility.emit(it)
        }
    }
}
