package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.dialog_generic_action_or_cancel.*
import za.co.woolworths.financial.services.android.ui.extension.withArgs

class GenericActionOrCancelDialogFragment : WBottomSheetDialogFragment() {

    interface IActionOrCancel {
        fun onDialogActionClicked(dialogId: Int)
    }

    companion object {
        private const val ARG_DIALOG_ID = "argDialogId"
        private const val ARG_TITLE = "argTitle"
        private const val ARG_DESC = "argDesc"
        private const val ARG_ACTION_TEXT = "argActionText"
        private const val ARG_CANCEL_TEXT = "argCancelText"

        private var listener: IActionOrCancel? = null

        fun newInstance(
                dialogId: Int,
                title: String,
                desc: String,
                actionButtonText: String,
                cancelButtonText: String,
                onActionListener: IActionOrCancel
        ): GenericActionOrCancelDialogFragment {
            listener = onActionListener
            return GenericActionOrCancelDialogFragment().withArgs {
                putInt(ARG_DIALOG_ID, dialogId)
                putString(ARG_TITLE, title)
                putString(ARG_DESC, desc)
                putString(ARG_ACTION_TEXT, actionButtonText)
                putString(ARG_CANCEL_TEXT, cancelButtonText)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_generic_action_or_cancel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var dialogId = -1
        arguments?.let {
            dialogId = it.getInt(ARG_DIALOG_ID)
            tvTitle.text = it.getString(ARG_TITLE)
            tvDescription.text = it.getString(ARG_DESC)
            tvAction.text = it.getString(ARG_ACTION_TEXT)
            tvCancel.text = it.getString(ARG_CANCEL_TEXT)
        }

        tvAction.setOnClickListener {
            listener?.onDialogActionClicked(dialogId)
            dismissAllowingStateLoss()
        }

        tvCancel.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        tvCancel.setOnClickListener { dismissAllowingStateLoss() }
    }

}