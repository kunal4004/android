package za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing

sealed class PopUpCommands {
    object MakePayment : PopUpCommands()
    object TreatPlanView : PopUpCommands()
    object TreatPlanSetup : PopUpCommands()
    object CallsUs : PopUpCommands()
}