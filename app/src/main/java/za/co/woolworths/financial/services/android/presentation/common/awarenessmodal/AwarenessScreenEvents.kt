package za.co.woolworths.financial.services.android.presentation.common.awarenessmodal

sealed class AwarenessScreenEvents {
    object ConfirmButtonClick : AwarenessScreenEvents()
    object DismissButtonClick : AwarenessScreenEvents()

    data class DontShowAgainClicked(val isChecked: Boolean = false): AwarenessScreenEvents()
}