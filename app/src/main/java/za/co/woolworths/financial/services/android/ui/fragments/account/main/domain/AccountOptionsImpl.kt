package za.co.woolworths.financial.services.android.ui.fragments.account.main.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import za.co.woolworths.financial.services.android.models.dto.account.BpiInsuranceApplicationStatusType
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.AccountOptions
import javax.inject.Inject

interface IAccountOptions {
    fun balanceProtectionInsurance()
    fun isDebitOrderActive()
    fun accountOptionsList(): MutableList<AccountOptions>
    val viewState: LiveData<MutableList<AccountOptions>>
}

class AccountOptionsImpl @Inject constructor(
    private val account: AccountProductLandingDao?,
    private val bpi: BalanceProtectionInsuranceImpl
) : IAccountOptions {

    private var _viewState: MutableLiveData<MutableList<AccountOptions>> = MutableLiveData()
    override val viewState: LiveData<MutableList<AccountOptions>>
        get() = _viewState

    var accountList = accountOptionsList()

    override fun balanceProtectionInsurance() {
        val bpiTag = bpi.balanceProtectionInsuranceTag()
        accountList.add(2,bpiTag)
        _viewState.postValue(accountList)
    }

    override fun isDebitOrderActive() {
        val isActive = account?.getAccountProduct()?.debitOrder?.debitOrderActive ?: false
        accountList.add(5, AccountOptions.DebitOrder(isActive))
        _viewState.postValue(accountList)
    }

    override fun accountOptionsList() = mutableListOf(
        AccountOptions.ViewTreatmentPlan(false),
        AccountOptions.SetUpAPaymentPlan(false),
        AccountOptions.BalanceProtectionInsurance(BpiInsuranceApplicationStatusType.NOT_COVERED),
        AccountOptions.PaymentOptions(false), // mandatory
        AccountOptions.WithdrawCashNow(false),   // Withdraw cash now is not applicable for store card
        AccountOptions.DebitOrder(false)
    )
}