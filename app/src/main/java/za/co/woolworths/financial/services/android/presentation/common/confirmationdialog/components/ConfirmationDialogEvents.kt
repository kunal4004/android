package za.co.woolworths.financial.services.android.presentation.common.confirmationdialog.components

sealed class ConfirmationDialogEvents {
    data class OnCheckedChange(val isChecked: Boolean) : ConfirmationDialogEvents()
    object OnConfirmClick : ConfirmationDialogEvents()
    object OnDismissClick : ConfirmationDialogEvents()
}
