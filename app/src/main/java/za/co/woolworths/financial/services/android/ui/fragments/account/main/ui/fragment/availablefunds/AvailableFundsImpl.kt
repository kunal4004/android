package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.availablefunds

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import za.co.absa.openbankingapi.woolworths.integration.AbsaSecureCredentials
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.CreditCardTokenResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.creditcard.CreditCardImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.creditcard.ICreditCard
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.SingleLiveEvent
import javax.inject.Inject

interface IAvailableFundsImpl {
     val product : Account?
//    val viewState: LiveData<MutableList<AccountOptionsScreenUI>>

    fun getEligibilityPlan()
    fun handleUserCreditCardToken(creditCardTokenResponse: CreditCardTokenResponse)
    val creditCardNumber: LiveData<String>
    val command: SingleLiveEvent<AvailableFundsCommand>
}

class AvailableFundsImpl @Inject constructor(
    private val balanceFormat: UserAccountBalance,
    private val creditCard: CreditCardImpl) : IAvailableFundsImpl, IUserAccountBalance by balanceFormat,
    ICreditCard by creditCard  {

    private var _creditCardNumber: MutableLiveData<String> = MutableLiveData()
    override val creditCardNumber: LiveData<String>
        get() = _creditCardNumber
    override val command = SingleLiveEvent<AvailableFundsCommand>()

   // val balanceflow : StateFlow<AvailableFundsCommand> = MutableStateFlow(AvailableFundsCommand())


    override val product: Account?
        get() = balanceFormat.product

    override fun getEligibilityPlan() {
    }

    override fun handleUserCreditCardToken(creditCardTokenResponse: CreditCardTokenResponse) {
        val cards = creditCardTokenResponse.cards
        when (cards.isNullOrEmpty()) {
            true -> command.postValue(AvailableFundsCommand.DisplayCardNumberNotFound)
            false -> {
                _creditCardNumber.postValue(getCreditCardNumber(cards))
                when (creditCardNumber.value.isNullOrEmpty()) {
                    true ->
                        command.postValue(AvailableFundsCommand.DisplayCardNumberNotFound)
                    false -> {
                        val absaSecureCredentials = AbsaSecureCredentials()
                        val aliasID = absaSecureCredentials.aliasId
                        val deviceID = absaSecureCredentials.deviceId
                        command.postValue(
                            AvailableFundsCommand.NavigateToOnlineBankingActivity(
                                !(TextUtils.isEmpty(aliasID) || TextUtils.isEmpty(deviceID))
                            )
                        )
                    }
                }
            }
        }
    }

    fun setUpView() {
        command.postValue(
            AvailableFundsCommand.SetViewDetails(
                getAvailableFunds(),
                getCurrentBalance(),
                getCreditLimit(),
                getTotalAmountDue(),
                getPaymentDueDate(),
                getAmountOverdue()
            )
        )
    }
}