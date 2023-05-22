package za.co.woolworths.financial.services.android.ui.fragments.product.detail.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.databinding.PayFlexBottomSheetDialogBinding
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

class PayFlexInfoBottomSheetDialog : WBottomSheetDialogFragment() {
    private lateinit var binding: PayFlexBottomSheetDialogBinding
    private var listener: IPayFlexInfoBottomSheetDialogDismissListener? = null

    interface IPayFlexInfoBottomSheetDialogDismissListener {
        fun onOutOfStockDialogDismiss()
    }

    companion object {

        fun newInstance() = PayFlexInfoBottomSheetDialog()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            listener = parentFragment as IPayFlexInfoBottomSheetDialogDismissListener?
        } catch (e: ClassCastException) {
            throw ClassCastException("Calling fragment must implement Callback interface")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = PayFlexBottomSheetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            payflexInfo.loadUrl("https://widgets.payflex.co.za/how_to.html?")
            payflexInfo.settings.javaScriptEnabled = true
            }

    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener?.onOutOfStockDialogDismiss()
    }

}