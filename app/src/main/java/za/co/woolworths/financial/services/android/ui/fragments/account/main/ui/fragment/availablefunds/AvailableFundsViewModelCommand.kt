package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.availablefunds

import androidx.navigation.NavDirections
import za.co.woolworths.financial.services.android.models.dto.PMACardPopupModel

sealed class AvailableFundsCommand {
    object DisplayCardNumberNotFound : AvailableFundsCommand()
    class NavigateToOnlineBankingActivity(val isRegistered: Boolean): AvailableFundsCommand()

    object NavigateToDeepLinkView: AvailableFundsCommand()
    class SessionExpired(val onSessionData: String?) :
        AvailableFundsCommand()

    object HttpError: AvailableFundsCommand()
    object ExceptionError: AvailableFundsCommand()
    class SetViewDetails(
        val availableFund: String,
        val currentBalance: String,
        val creditLimit: String,
        val totalAmountDueAmount: String,
        val paymentDueDate: String,
        val amountOverdue: String,
    ): AvailableFundsCommand()

    object PresentPayMyAccountActivity: AvailableFundsCommand()
    class TriggerFirebaseEvent(val eventName: String?): AvailableFundsCommand()

    object PayMyAccountRetryErrorFragment: AvailableFundsCommand()
    class OpenPayMyAccountOptionOrEnterPaymentAmountDialogFragment(val directions: NavDirections?): AvailableFundsCommand()

    class SetPMAData(card: PMACardPopupModel): AvailableFundsCommand()
}