package za.co.woolworths.financial.services.android.checkout.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.credit_card_cancel_delivery_confirmation_dialog.*
import kotlinx.android.synthetic.main.credit_card_cancel_delivery_confirmation_dialog.callCallCenter
import kotlinx.android.synthetic.main.fragment_suburb_not_deliverable_bottomsheet_dialog.*
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.activities.credit_card_delivery.CreditCardDeliveryActivity
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery.CancelOrToLateDeliveryDialog
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.Utils

class SuburbNotDeliverableBottomsheetDialogFragment : WBottomSheetDialogFragment(),
    View.OnClickListener {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(
            R.layout.fragment_suburb_not_deliverable_bottomsheet_dialog,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        tvDismiss.setOnClickListener(this)
        buttonChangeAddress.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        dismiss()
    }
}