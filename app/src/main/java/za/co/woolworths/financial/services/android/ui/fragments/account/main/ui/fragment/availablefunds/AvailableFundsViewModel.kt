package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.availablefunds

import android.os.Bundle
import android.text.TextUtils
import androidx.annotation.IdRes
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavDirections
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import za.co.absa.openbankingapi.woolworths.integration.AbsaSecureCredentials
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.Card
import za.co.woolworths.financial.services.android.models.dto.CreditCardTokenResponse
import za.co.woolworths.financial.services.android.models.dto.PMACardPopupModel
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.repository.storecard.StoreCardRepository
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.SingleLiveEvent
import za.co.woolworths.financial.services.android.util.*
import java.net.ConnectException
import javax.inject.Inject

@HiltViewModel
class AvailableFundsViewModel @Inject constructor(private val repository: StoreCardRepository) :
    PayMyAccountViewModel() {
    val creditCardService by lazy {
        repository.getCreditCardToken()
    }
    val mAccount: MutableLiveData<Account> = MutableLiveData()
    val mAccountPair: MutableLiveData<Pair<ApplyNowState, Account>> = MutableLiveData()
    val creditCardNumber: MutableLiveData<String> = MutableLiveData()
    val command = SingleLiveEvent<Command>()

    @Throws(RuntimeException::class)
    fun start(bundle: Bundle?) {
        val account = bundle?.getString(AccountSignedInPresenterImpl.MY_ACCOUNT_RESPONSE)
        mAccountPair.value =
            Gson().fromJson(account, object : TypeToken<Pair<ApplyNowState, Account>>() {}.type)
        mAccount.value = mAccountPair.value?.second
        setUpView()
    }


    fun handleUserCreditCardToken(creditCardTokenResponse: CreditCardTokenResponse) {
        val cards = creditCardTokenResponse.cards
        when (cards.isNullOrEmpty()) {
            true -> command.value = Command.DisplayCardNumberNotFound
            false -> {
                creditCardNumber.value = getCreditCardNumber(cards)
                when (creditCardNumber.value.isNullOrEmpty()) {
                    true -> command.value =
                        Command.DisplayCardNumberNotFound
                    false -> {
                        val absaSecureCredentials = AbsaSecureCredentials()
                        val aliasID = absaSecureCredentials.aliasId
                        val deviceID = absaSecureCredentials.deviceId


                        command.value = Command.NavigateToOnlineBankingActivity(
                            !(TextUtils.isEmpty(aliasID) || TextUtils.isEmpty(deviceID))
                        )
                    }
                }
            }
        }
    }

    fun queryServicePayUPaymentMethod() {
        when (!isQueryPayUPaymentMethodComplete) {
            true -> {
                val cardInfo = getCardDetail()
                val account = mAccountPair.value
                val amountEntered = account?.second?.amountOverdue?.let { amountDue ->
                    Utils.removeNegativeSymbol(
                        CurrencyFormatter.formatAmountToRandAndCent(amountDue)
                    )
                }
                val payUMethodType = PAYUMethodType.CREATE_USER
                val paymentMethodList = cardInfo?.paymentMethodList

                val card =
                    PMACardPopupModel(amountEntered, paymentMethodList, account, payUMethodType)
                setPMACardInfo(card)
                command.value = Command.setPMAData(card)

                queryServicePayUPaymentMethod(
                    { // onSuccessResult
                        isQueryPayUPaymentMethodComplete = true
                        command.value = Command.NavigateToDeepLinkView
                    }, { onSessionData ->
                        command.value = Command.SessionExpired(onSessionData)
                        isQueryPayUPaymentMethodComplete = true

                    }, { // on unknown http error / general error
                        command.value = Command.HttpError
                        isQueryPayUPaymentMethodComplete = true

                    }, { throwable ->
                        command.value = Command.ExceptionError
                        isQueryPayUPaymentMethodComplete = throwable !is ConnectException

                    })
            }
            false -> return
        }
    }

    fun onPayMyAccountButtonTap(eventName: String?, @IdRes currentDestination: Int?, directions: NavDirections?) {
        this.apply {
            //Redirect to payment options when  ABSA cards array is empty for credit card products
            if (getProductGroupCode().equals(
                    AccountsProductGroupCode.CREDIT_CARD.groupCode,
                    ignoreCase = true
                )
            ) {
                if (getAccount()?.cards?.isEmpty() == true) {
                     command.value = Command.PresentPayMyAccountActivity
                    return
                }
            }

            payMyAccountPresenter.apply {
                command.value = Command.TriggerFirebaseEvent(eventName)
                resetAmountEnteredToDefault()
                when (isPaymentMethodOfTypeError()) {
                    true -> {
                        when (currentDestination) {
                            R.id.storeCardFragment,
                            R.id.blackCreditCardFragment,
                            R.id.goldCreditCardFragment,
                            R.id.silverCreditCardFragment,
                            R.id.personalLoanFragment -> {
                                command.value = Command.PayMyAccountRetryErrorFragment
                            }
                        }
                    }
                    false -> {
                        command.value = Command.OpenPayMyAccountOptionOrEnterPaymentAmountDialogFragment(directions)
                    }
                }
            }
        }
    }

    private fun setUpView() {
        mAccount.value?.apply {
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
            command.value = Command.SetViewDetails(
                availableFund,
                currentBalance,
                creditLimit,
                totalAmountDueAmount,
                paymentDueDate,
                amountOverdue
            )

        }
    }

    fun getCreditCardNumber(cards: ArrayList<Card>?): String? {
        return cards?.takeIf { it.isNotEmpty() }?.let { it[0].absaCardToken }
    }

    sealed class Command {
        object DisplayCardNumberNotFound : Command()
        class NavigateToOnlineBankingActivity(val isRegistered: Boolean) : Command()
        object NavigateToDeepLinkView : Command()
        class SessionExpired(val onSessionData: String?) : Command()
        object HttpError : Command()
        object ExceptionError : Command()
        class SetViewDetails(
            val availableFund: String,
            val currentBalance: String,
            val creditLimit: String,
            val totalAmountDueAmount: String,
            val paymentDueDate: String,
            val amountOverdue: String,
        ) : Command()

        object PresentPayMyAccountActivity : Command()
        class TriggerFirebaseEvent(val eventName: String?) : Command()
        object PayMyAccountRetryErrorFragment : Command()
        class OpenPayMyAccountOptionOrEnterPaymentAmountDialogFragment(val  directions: NavDirections?) : Command()
        class setPMAData(card: PMACardPopupModel) : Command()
    }
}

