package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.credit_card_cancel_delivery_confirmation_dialog.*
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.activities.credit_card_delivery.CreditCardDeliveryActivity.DeliveryStatus
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.Utils

class CancelOrToLateDeliveryDialog : WBottomSheetDialogFragment(), View.OnClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        var layoutToShow: Int = R.layout.credit_card_delivery_too_late_to_edit_dialog
        arguments?.apply {
            if (containsKey("creditCardStatus")) {
                if (arguments?.getInt("creditCardStatus") == (DeliveryStatus.CANCEL_DELIVERY.value)) {
                    layoutToShow = R.layout.credit_card_cancel_delivery_confirmation_dialog
                } else if (arguments?.getInt("creditCardStatus") == (DeliveryStatus.EDIT_ADDRESS.value)) {
                    layoutToShow = R.layout.credit_card_delivery_too_late_to_edit_dialog
                }
            }
        }
        return inflater.inflate(layoutToShow, container, false)
    }

    companion object {
        fun newInstance(deliveryStatus: DeliveryStatus) = CancelOrToLateDeliveryDialog().withArgs {
            putInt("creditCardStatus", deliveryStatus.value)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        cancel.setOnClickListener(this)
        callCallCenter.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.cancel -> {
                dismiss()
            }
            R.id.callCallCenter -> {
                dismiss()
                activity?.apply {
                    Utils.makeCall(WoolworthsApplication.getCreditCardDelivery().callCenterNumber)
                }
            }
        }
    }
}