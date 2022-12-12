package za.co.woolworths.financial.services.android.checkout.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AddingTipProgressDialogBinding
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.opt_in.otp.ProgressIndicator
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

/**
 * Created by Kunal Uttarwar on 28/03/22.
 */
class CustomProgressBottomSheetDialog : WBottomSheetDialogFragment() {

    private lateinit var binding: AddingTipProgressDialogBinding
    private lateinit var mTipValue: String

    companion object {
        private const val TIP_VALUE = "TIP_VALUE"

        fun newInstance(tipValue: String) =
            CustomProgressBottomSheetDialog().withArgs {
                putString(TIP_VALUE, tipValue)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            mTipValue = getString(TIP_VALUE, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = AddingTipProgressDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.showProgressBar()
        binding.progressTitleTextView?.text = requireContext().getString(R.string.amt_tip_added, mTipValue.toDoubleOrNull())
    }

    private fun AddingTipProgressDialogBinding.showProgressBar() {
        gotItActionButton?.visibility = View.VISIBLE
        successTick?.colorCode = R.color.color_4ABB77
        val circularProgressIndicator =
            ProgressIndicator(circularProgressIndicator, successFrame, imFailureIcon, successTick)
        circularProgressIndicator.apply {
            progressIndicatorListener {}
            stopSpinning()
            animationStatus = ProgressIndicator.AnimationStatus.Success
        }
        gotItActionButton?.setOnClickListener {
            dismiss()
        }
    }
}
