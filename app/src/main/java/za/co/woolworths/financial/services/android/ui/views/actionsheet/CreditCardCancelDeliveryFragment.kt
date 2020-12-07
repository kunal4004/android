package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.credit_card_cancel_delivery_confirmation_dialog.*
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.util.Utils

class CreditCardCancelDeliveryFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.credit_card_cancel_delivery_confirmation_dialog, container, false)

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