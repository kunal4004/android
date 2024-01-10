package za.co.woolworths.financial.services.android.presentation.common.confirmationdialog.components

sealed interface ConfirmationDialogUiState {

    object None: ConfirmationDialogUiState

    object StateDeleteListConfirmation: ConfirmationDialogUiState

    object StateDeleteProgress: ConfirmationDialogUiState
}
