package za.co.woolworths.financial.services.android.ui.fragments.cli

import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.credit_limit_increase_fragment.*
import za.co.woolworths.financial.services.android.contracts.ICreditLimitDecrease
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

class CreditLimitDecreaseConfirmationFragment : WBottomSheetDialogFragment() {

    private var mCreditLimitDecreaseListener: ICreditLimitDecrease? = null

    companion object {
        fun newInstance() = CreditLimitDecreaseConfirmationFragment()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.apply {
            try {
                mCreditLimitDecreaseListener = this as? ICreditLimitDecrease
            } catch (e: ClassCastException) {
                throw ClassCastException("$this must implement MyInterface ")
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.credit_limit_increase_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvGotIt?.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        mCreditLimitDecreaseListener?.apply {
            btnProceedWithMaximum?.setOnClickListener { onCreditDecreaseProceedWithMaximum() }
            tvGotIt?.setOnClickListener { dismissAllowingStateLoss() }
        }
    }

}