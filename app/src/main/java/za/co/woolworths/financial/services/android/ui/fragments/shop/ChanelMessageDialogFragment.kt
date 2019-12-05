package za.co.woolworths.financial.services.android.ui.fragments.shop

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.chanel_message_dialog.*
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

class ChanelMessageDialogFragment : WBottomSheetDialogFragment() {
    private var listener: IChanelMessageDialogDismissListener? = null

    interface IChanelMessageDialogDismissListener {
        fun onDialogDismiss()
    }

    public companion object {
        fun newInstance() = ChanelMessageDialogFragment()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as IChanelMessageDialogDismissListener?
        } catch (e: ClassCastException) {
            throw ClassCastException("Calling Activity must implement Callback interface")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.chanel_message_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buttonContinue.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener?.onDialogDismiss()
    }

}