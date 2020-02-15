package za.co.woolworths.financial.services.android.ui.fragments.shop

import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.cancel_order_confirmation_dialog.*
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

class CancelOrderConfirmationDialogFragment : WBottomSheetDialogFragment() {

    private var listener: ICancelOrderConfirmation? = null

    interface ICancelOrderConfirmation {
        fun onCancelOrderConfirmation()
    }

    companion object {
        fun newInstance() = CancelOrderConfirmationDialogFragment().withArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.cancel_order_confirmation_dialog, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            listener = parentFragment as ICancelOrderConfirmation?
        } catch (e: ClassCastException) {
            throw ClassCastException("Calling fragment must implement Callback interface")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cancel?.apply {
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            setOnClickListener { dismissAllowingStateLoss() }
        }
        confirmCancelOrder.setOnClickListener {
            listener?.onCancelOrderConfirmation()
            dismissAllowingStateLoss()
        }
    }

}