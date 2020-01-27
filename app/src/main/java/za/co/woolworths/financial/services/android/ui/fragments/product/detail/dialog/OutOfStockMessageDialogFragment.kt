package za.co.woolworths.financial.services.android.ui.fragments.product.detail.dialog

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.out_of_stock_message_dialog.*
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

class OutOfStockMessageDialogFragment : WBottomSheetDialogFragment() {
    private var listener: IOutOfStockMessageDialogDismissListener? = null

    interface IOutOfStockMessageDialogDismissListener {
        fun onOutOfStockDialogDismiss()
    }

    private var outOfStockMessage: String? = ""

    companion object {
        private const val MESSAGE = "MESSAGE"
        fun newInstance(message: String) = OutOfStockMessageDialogFragment().withArgs {
            putString(MESSAGE, message)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        outOfStockMessage = arguments?.getString(MESSAGE, "")
        try {
            listener = parentFragment as IOutOfStockMessageDialogDismissListener?
        } catch (e: ClassCastException) {
            throw ClassCastException("Calling fragment must implement Callback interface")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.out_of_stock_message_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        description.text = outOfStockMessage
        btnOk.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener?.onOutOfStockDialogDismiss()
    }

}