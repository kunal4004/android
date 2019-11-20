package za.co.woolworths.financial.services.android.ui.fragments.product.detail.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.confirm_deliverylocation_bottom_sheet_dialog.*
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.IOnConfirmDeliveryLocationActionListener
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.Utils

class ConfirmDeliveryLocationFragment : WBottomSheetDialogFragment() {
    private var listener: IOnConfirmDeliveryLocationActionListener? = null

    companion object {
        fun newInstance() = ConfirmDeliveryLocationFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        try {
            listener = parentFragment as IOnConfirmDeliveryLocationActionListener?
        } catch (e: ClassCastException) {
            throw ClassCastException("Calling fragment must implement Callback interface")
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.confirm_deliverylocation_bottom_sheet_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imCloseIcon.setOnClickListener {
            listener?.onConfirmLocationDialogDismiss()
            dismissAllowingStateLoss()
        }
        btnDefaultLocation.setOnClickListener {
            listener?.onConfirmLocation()
            dismissAllowingStateLoss()
        }
        btnSetNewLocation.setOnClickListener {
            listener?.onSetNewLocation()
            dismissAllowingStateLoss()
        }
        configureUI()
    }

    private fun configureUI() {
        Utils.getPreferredDeliveryLocation()?.apply {
            suburb?.let {
                tvLocation.setText(it.name + ", " + this.province.name)
            }
        }
    }

}