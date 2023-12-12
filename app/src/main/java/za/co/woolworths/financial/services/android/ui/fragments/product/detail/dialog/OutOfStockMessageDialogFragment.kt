package za.co.woolworths.financial.services.android.ui.fragments.product.detail.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.databinding.OutOfStockMessageDialogBinding
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.analytics.FirebaseAnalyticsEventHelper

class OutOfStockMessageDialogFragment : WBottomSheetDialogFragment() {

    private lateinit var binding: OutOfStockMessageDialogBinding
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
        binding = OutOfStockMessageDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Firebase event to be triggered when displaying the out of stock dialog
        FirebaseAnalyticsEventHelper.outOfStock()
        with(binding) {
            description.text = outOfStockMessage
            btnOk.setOnClickListener {
                dismissAllowingStateLoss()
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener?.onOutOfStockDialogDismiss()
    }
}