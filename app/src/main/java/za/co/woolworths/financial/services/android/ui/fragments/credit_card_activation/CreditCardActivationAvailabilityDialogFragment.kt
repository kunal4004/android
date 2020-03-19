package za.co.woolworths.financial.services.android.ui.fragments.credit_card_activation

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.credit_card_activation_availability_dialog_fragment.*
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.Utils

class CreditCardActivationAvailabilityDialogFragment : WBottomSheetDialogFragment() {

    private var accountNumberBin: String? = null

    companion object {
        fun newInstance(accountNumberBin: String?) = CreditCardActivationAvailabilityDialogFragment().withArgs {
            putString("ACCOUNT_NUMBER_BIN", accountNumberBin)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.credit_card_activation_availability_dialog_fragment, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            accountNumberBin = it.getString("ACCOUNT_NUMBER_BIN", "")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (accountNumberBin) {
            Utils.BLACK_CARD -> cardImage.setBackgroundResource(R.drawable.black_cc_envelope)
            Utils.GOLD_CARD -> cardImage.setBackgroundResource(R.drawable.gold_cc_envelope)
            else -> cardImage.setBackgroundResource(R.drawable.silver_cc_envelope)
        }
        callUsOnButton?.apply {
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            setOnClickListener { activity?.apply { Utils.makeCall("0861 50 20 20") } }
        }
        gotItButton?.setOnClickListener { dismissAllowingStateLoss() }
    }

}