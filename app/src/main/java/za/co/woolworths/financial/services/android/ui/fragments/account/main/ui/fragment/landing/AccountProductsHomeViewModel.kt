package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
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
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.local.AccountDataClass
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.local.IAccountDataClass
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.repository.storecard.CollectionRepository
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.repository.storecard.ICollectionRepository
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.*
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.AccountOptionsScreenUI
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.IStoreCardNavigator
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.StoreCardNavigator
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.DialogData
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.overlay.DisplayInArrearsPopup
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ViewTreatmentPlanImpl
import javax.inject.Inject

@HiltViewModel
class AccountProductsHomeViewModel @Inject constructor(
    accountDataClass: AccountDataClass,
    screen: AccountProductLandingScreenStatus,
    account: AccountProductLandingDao,
    private val collectionRepository: CollectionRepository,
    val navigator: StoreCardNavigator,
    val accountOptions: AccountOptionsImpl
) : ViewModel(),
    IAccountDataClass by accountDataClass,
    IAccountProductLandingDao by account,
    IAccountOptions by accountOptions,
    IAccountProductLandingScreen by screen,
    ICollectionRepository by collectionRepository,
    IStoreCardNavigator by navigator {

    @Inject
    lateinit var treatmentPlan: TreatmentPlanImpl

    var bottomSheetBehaviorState: Int? = BottomSheetBehavior.STATE_COLLAPSED
    var viewTreatmentPlan: ViewTreatmentPlanImpl? = null

    private val _isBottomSheetBehaviorExpanded = MutableSharedFlow<Boolean>()
    val isBottomSheetBehaviorExpanded: SharedFlow<Boolean> = _isBottomSheetBehaviorExpanded

    fun setIsBottomSheetBehaviorExpanded(isExpanded : Boolean){
        viewModelScope.launch { _isBottomSheetBehaviorExpanded.emit(isExpanded) }
    }

    var eligibilityPlan : EligibilityPlan? = null

    private val _viewState = MutableSharedFlow<List<AccountOptionsScreenUI>>()
    val viewState: SharedFlow<List<AccountOptionsScreenUI>> = _viewState

    fun setTreatmentPlan(eligibilityPlan: EligibilityPlan?) {
         viewTreatmentPlan = ViewTreatmentPlanImpl(
            account = product,
            applyNowState = ApplyNowState.STORE_CARD,
            eligibilityPlan = eligibilityPlan
        )
    }

    fun init() {
        with(_viewState) {
            viewModelScope.launch {
                emit(balanceProtectionInsurance())
                emit(isDebitOrderActive())
                emit(paymentOptions())
                emit(withdrawCashNow())
            }
        }
    }

    fun updateBPI(account: Account) {
        viewModelScope.launch {
            product?.bpiInsuranceApplication = account.bpiInsuranceApplication
            _viewState.emit(balanceProtectionInsurance())
        }
    }

    fun emitEligibilityPlanWhenNotEmpty(eligibilityPlan: EligibilityPlan?){
        viewModelScope.launch {
            eligibilityPlan?.let { plan ->
                if (!plan.actionText.isNullOrEmpty() && !plan.displayText.isNullOrEmpty()) {
                    _viewState.tryEmit(collectionTreatmentPlanItem(plan))
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

    fun initPopup(viewLifecycleOwner : LifecycleOwner, navigationTo: (DialogData?, EligibilityPlan?) -> Unit): DisplayInArrearsPopup {
        return DisplayInArrearsPopup(viewLifecycleOwner, treatmentPlanImpl = treatmentPlan, homeViewModel = this){ item, eligibilityPlan ->
            navigationTo(item, eligibilityPlan)
        }
    }

    fun emitAccountIsChargedOff() {
        viewTreatmentPlan?.getPopupData(eligibilityPlan)
    }

    fun emitShowViewTreatmentPlanPopupFromConfigForChargedOff() {
        viewTreatmentPlan?.getPopupData(eligibilityPlan)
    }

    fun emitShowViewTreatmentPlanPopupInArrearsFromConfig() {
        viewTreatmentPlan?.getPopupData(eligibilityPlan)
    }
}
