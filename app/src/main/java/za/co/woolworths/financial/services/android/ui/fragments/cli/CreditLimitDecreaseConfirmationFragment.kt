package za.co.woolworths.financial.services.android.ui.fragments.cli

import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import com.awfs.coordination.databinding.CreditLimitDecreaseBottomSheetFragmentBinding
import za.co.woolworths.financial.services.android.contracts.ICreditLimitDecrease
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

class CreditLimitDecreaseConfirmationFragment : WBottomSheetDialogFragment() {

    private lateinit var binding: CreditLimitDecreaseBottomSheetFragmentBinding
    private var mCreditLimitDecreaseListener: ICreditLimitDecrease? = null

    companion object {
        private var mTrackedAmount: String? = null
        fun newInstance(trackedAmount: String) = CreditLimitDecreaseConfirmationFragment().withArgs {
            putString("TRACKED_AMOUNT", trackedAmount)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mCreditLimitDecreaseListener = context as? ICreditLimitDecrease
        } catch (e: ClassCastException) {
            throw ClassCastException("$this must implement MyInterface ")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            mTrackedAmount = getString("TRACKED_AMOUNT", "")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = CreditLimitDecreaseBottomSheetFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            tvGotIt?.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            tvDescriptionPart2?.text =
                getString(R.string.credit_limit_decrease_desc_part_2, mTrackedAmount)
            btnProceedWithMaximum?.setOnClickListener {
                mCreditLimitDecreaseListener?.apply {
                    dismissAllowingStateLoss()
                    onCreditDecreaseProceedWithMaximum()
                }
            }
            tvGotIt?.setOnClickListener { dismissAllowingStateLoss() }
        }
    }

    override fun onDetach() {
        super.onDetach()
        // Reset callback
        mCreditLimitDecreaseListener = null
    }
}