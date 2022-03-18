package za.co.woolworths.financial.services.android.ui.fragments.account.main.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import za.co.woolworths.financial.services.android.models.dto.account.BpiInsuranceApplicationStatusType
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.AccountOptionsScreenUI
import javax.inject.Inject

interface IAccountOptions {
    fun balanceProtectionInsurance()
    fun isDebitOrderActive()
    fun accountOptionsList(): MutableList<AccountOptionsScreenUI>
    val viewState: LiveData<MutableList<AccountOptionsScreenUI>>
}

class AccountOptionsImpl @Inject constructor(
    private val account: AccountProductLandingDao?,
    private val bpi: BalanceProtectionInsuranceImpl
) : IAccountOptions {

    private var _viewState: MutableLiveData<MutableList<AccountOptionsScreenUI>> = MutableLiveData()
    override val viewState: LiveData<MutableList<AccountOptionsScreenUI>>
        get() = _viewState

    var accountList = accountOptionsList()

    override fun balanceProtectionInsurance() {
        val bpiTag = bpi.balanceProtectionInsuranceTag()
        accountList.add(2,bpiTag)
        _viewState.postValue(accountList)
    }

    override fun isDebitOrderActive() {
        val isActive = account?.getAccountProduct()?.debitOrder?.debitOrderActive ?: false
        accountList.add(5, AccountOptionsScreenUI.DebitOrder(isActive))
        _viewState.postValue(accountList)
    }

    override fun accountOptionsList() = mutableListOf(
        AccountOptionsScreenUI.ViewTreatmentPlan(false),
        AccountOptionsScreenUI.SetUpAPaymentPlan(false),
        AccountOptionsScreenUI.BalanceProtectionInsurance(BpiInsuranceApplicationStatusType.NOT_COVERED),
        AccountOptionsScreenUI.PaymentOptionsScreenUI(false), // mandatory
        AccountOptionsScreenUI.WithdrawCashNow(false),   // Withdraw cash now is not applicable for store card
        AccountOptionsScreenUI.DebitOrder(false)
    )
}