package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.availablefunds

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import za.co.absa.openbankingapi.woolworths.integration.AbsaSecureCredentials
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.Card
import za.co.woolworths.financial.services.android.models.dto.CreditCardTokenResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.AccountProductLandingDao
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.SingleLiveEvent
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.FontHyperTextParser
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.WFormatter
import javax.inject.Inject


interface IAvailableFundsImpl {
    val product: Account?
//    val viewState: LiveData<MutableList<AccountOptions>>

    fun getEligibilityPlan()
    fun handleUserCreditCardToken(creditCardTokenResponse: CreditCardTokenResponse)
    val creditCardNumber: LiveData<String>
    val command: SingleLiveEvent<AvailableFundsCommand>
}

class AvailableFundsImpl @Inject constructor(
    val accountProductLandingDao: AccountProductLandingDao
) : IAvailableFundsImpl {
    override val product: Account?
        get() = accountProductLandingDao.getAccountProduct()

    private var _creditCardNumber: MutableLiveData<String> = MutableLiveData()
    override val creditCardNumber: LiveData<String>
        get() = _creditCardNumber
    override val command = SingleLiveEvent<AvailableFundsCommand>()

    init {
        setUpView()
    }
    override fun getEligibilityPlan() {
        accountProductLandingDao.getAccountProduct()
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

    private fun getCreditCardNumber(cards: ArrayList<Card>?): String? {
        return cards?.takeIf { it.isNotEmpty() }?.let { it[0].absaCardToken }
    }
    private fun setUpView() {
        product?.apply {
            val availableFund = Utils.removeNegativeSymbol(
                FontHyperTextParser.getSpannable(
                    CurrencyFormatter.formatAmountToRandAndCentNoSpace(availableFunds), 1
                )
            )
            val currentBalance = Utils.removeNegativeSymbol(
                CurrencyFormatter.formatAmountToRandAndCentWithSpace(currentBalance)
            )
            val creditLimit = Utils.removeNegativeSymbol(
                FontHyperTextParser.getSpannable(
                    CurrencyFormatter.formatAmountToRandAndCentWithSpace(creditLimit), 1
                )
            )
            val paymentDueDate = paymentDueDate?.let { paymentDueDate ->
                WFormatter.addSpaceToDate(
                    WFormatter.newDateFormat(paymentDueDate)
                )
            }
                ?: "N/A"
            val amountOverdue = Utils.removeNegativeSymbol(
                FontHyperTextParser.getSpannable(
                    CurrencyFormatter.formatAmountToRandAndCentWithSpace(amountOverdue), 1
                )
            )

            val totalAmountDueAmount = Utils.removeNegativeSymbol(
                CurrencyFormatter.formatAmountToRandAndCentWithSpace(totalAmountDue)
            )
            command.postValue(
                AvailableFundsCommand.SetViewDetails(
                    availableFund,
                    currentBalance,
                    creditLimit,
                    totalAmountDueAmount,
                    paymentDueDate,
                    amountOverdue
                )
            )

        }
    }
}