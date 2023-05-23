package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.availablefunds

import androidx.annotation.IdRes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.awfs.coordination.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.CreditCardTokenResponse
import za.co.woolworths.financial.services.android.models.dto.PMACardPopupModel
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.ViewState
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.mapNetworkCallToViewStateFlow
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.repository.storecard.StoreCardRepository
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.creditcard.CreditCardDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.creditcard.ICreditCardDataSource
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.Utils
import java.net.ConnectException
import javax.inject.Inject

@HiltViewModel
class AvailableFundsViewModel @Inject constructor(
    repository: StoreCardRepository,
    removeBlockOnCollectionImpl: RemoveBlockOnCollectionImpl,
    val availableFunds: AvailableFundsImpl,
    private val creditCardDataSource: CreditCardDataSource
    ) : PayMyAccountViewModel(), IAvailableFundsImpl by availableFunds,
    ICreditCardDataSource by creditCardDataSource,
    IRemoveBlockOnCollection by removeBlockOnCollectionImpl{

    /**
     * [MutableLiveData] to notify the Popular photos list view with the list of photos
     */
    val creditCardTokenLiveData: MutableLiveData<ViewState<CreditCardTokenResponse>> by lazy {
        MutableLiveData<ViewState<CreditCardTokenResponse>>()
    }

    fun collectCreditCardToken() {
        viewModelScope.launch {
            mapNetworkCallToViewStateFlow {
                queryServiceCreditCardToken()
            }.collect {  }
        }
    }

    //val paymentPAYUService = repository.getPaymentPAYUMethod()
    val mAccountPair: MutableLiveData<Pair<ApplyNowState, Account>> = MutableLiveData()

    init {

        // must fixed
        //  mAccountPair.value = Pair(ApplyNowState.STORE_CARD, product)
    }

    fun queryServicePayUPaymentMethod() {
        when (!isQueryPayUPaymentMethodComplete) {
            true -> {
                val cardInfo = getCardDetail()
                val account = product
                val amountEntered = account?.amountOverdue?.let { amountDue ->
                    Utils.removeNegativeSymbol(
                        CurrencyFormatter.formatAmountToRandAndCent(amountDue)
                    )
                }
                val payUMethodType = PAYUMethodType.CREATE_USER
                val paymentMethodList = cardInfo?.paymentMethodList

                val card =
                    PMACardPopupModel(
                        amountEntered,
                        paymentMethodList, mAccountPair.value,
                        payUMethodType
                    )
                setPMACardInfo(card)
                command.postValue(AvailableFundsCommand.SetPMAData(card))

                queryServicePayUPaymentMethod(
                    { // onSuccessResult
                        isQueryPayUPaymentMethodComplete = true
                        command.postValue(AvailableFundsCommand.NavigateToDeepLinkView)
                    }, { onSessionData ->
                        command.postValue(AvailableFundsCommand.SessionExpired(onSessionData))
                        isQueryPayUPaymentMethodComplete = true

                    }, { // on unknown http error / general error
                        command.postValue(AvailableFundsCommand.HttpError)
                        isQueryPayUPaymentMethodComplete = true

                    }, { throwable ->
                        command.postValue(AvailableFundsCommand.ExceptionError)
                        isQueryPayUPaymentMethodComplete = throwable !is ConnectException

                    })
            }
            false -> return
        }
    }

    fun onPayMyAccountButtonTap(
        eventName: String?,
        @IdRes currentDestination: Int?,
        directions: NavDirections?
    ) {
        this.apply {
            //Redirect to payment options when  ABSA cards array is empty for credit card products
            if (getProductGroupCode().equals(
                    AccountsProductGroupCode.CREDIT_CARD.groupCode,
                    ignoreCase = true
                )
            ) {
                if (getAccount()?.cards?.isEmpty() == true) {
                    command.postValue(AvailableFundsCommand.PresentPayMyAccountActivity)
                    return
                }
            }

            payMyAccountPresenter.apply {
                command.postValue(AvailableFundsCommand.TriggerFirebaseEvent(eventName))
                resetAmountEnteredToDefault()
                when (isPaymentMethodOfTypeError()) {
                    true -> {
                        when (currentDestination) {
                            R.id.storeCardFragment,
                            R.id.blackCreditCardFragment,
                            R.id.goldCreditCardFragment,
                            R.id.silverCreditCardFragment,
                            R.id.personalLoanFragment -> {
                                command.postValue(AvailableFundsCommand.PayMyAccountRetryErrorFragment)
                            }
                        }
                    }
                    false -> {
                        command.postValue(
                            AvailableFundsCommand.OpenPayMyAccountOptionOrEnterPaymentAmountDialogFragment(
                                directions
                            )
                        )
                    }
                }
            }
        }
    }

}

