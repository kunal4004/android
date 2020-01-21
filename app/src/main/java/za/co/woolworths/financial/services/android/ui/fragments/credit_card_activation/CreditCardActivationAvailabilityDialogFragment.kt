package za.co.woolworths.financial.services.android.ui.fragments.credit_card_activation

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.credit_card_activation_availability_dialog_fragment.*
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.Utils

class CreditCardActivationAvailabilityDialogFragment : WBottomSheetDialogFragment() {

    companion object {
        fun newInstance() = CreditCardActivationAvailabilityDialogFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.credit_card_activation_availability_dialog_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        callUsOnButton?.apply {
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            setOnClickListener { activity?.apply { Utils.makeCall(this, "0861 50 20 20") } }
        }
    }

}