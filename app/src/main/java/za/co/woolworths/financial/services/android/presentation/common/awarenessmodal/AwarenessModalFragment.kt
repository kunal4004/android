package za.co.woolworths.financial.services.android.presentation.common.awarenessmodal

import android.app.Activity
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.ui.wfs.common.contentView
import za.co.woolworths.financial.services.android.util.AppConstant

class AwarenessModalFragment : WBottomSheetDialogFragment() {

    companion object {
        const val REQUEST_AWARENESS_MODAL = "REQUEST_AWARENESS_MODAL"
        const val RESULT_AWARENESS_MODAL = "RESULT_AWARENESS_MODAL"
    }

    private val awarenessViewModel: AwarenessViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = contentView(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)) {

        AwarenessModalView(
            awarenessViewModel
        ) {
            when (it) {
                AwarenessScreenEvents.ConfirmButtonClick -> setResultAndDismiss(Activity.RESULT_OK)
                AwarenessScreenEvents.DismissButtonClick -> setResultAndDismiss(Activity.RESULT_CANCELED)
                is AwarenessScreenEvents.DontShowAgainClicked -> {
                    awarenessViewModel.setNoteChecked(it.isChecked)
                }
            }
        }
    }

    private fun setResultAndDismiss(result: Int) {
        setFragmentResult(
            REQUEST_AWARENESS_MODAL,
            bundleOf(
                RESULT_AWARENESS_MODAL to result,
                AppConstant.Keys.BUNDLE_KEY_DONT_ASK_AGAIN_CHECKED to awarenessViewModel.isNoteChecked()
            )
        )
        dismissAllowingStateLoss()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        setResultAndDismiss(Activity.RESULT_CANCELED)
    }

}