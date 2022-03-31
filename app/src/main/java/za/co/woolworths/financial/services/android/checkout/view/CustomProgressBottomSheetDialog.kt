package za.co.woolworths.financial.services.android.checkout.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.adding_tip_progress_dialog.*
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.opt_in.otp.ProgressIndicator
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

/**
 * Created by Kunal Uttarwar on 28/03/22.
 */
class CustomProgressBottomSheetDialog : WBottomSheetDialogFragment() {

    private lateinit var mTipValue: String


    interface ClickListner {
        fun onConfirmClick(tipValue: String)
    }

    companion object {
        private const val TIP_VALUE = "TIP_VALUE"
        private var clickListner: ClickListner? = null

        fun newInstance(tipValue: String, listner: ClickListner) =
            CustomProgressBottomSheetDialog().withArgs {
                putString(TIP_VALUE, tipValue)
                clickListner = listner
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
        return inflater.inflate(R.layout.adding_tip_progress_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showProgressBar()
        progressTitleTextView?.text = bindString(R.string.amt_tip_added, mTipValue)
    }

    private fun showProgressBar() {
        gotItActionButton?.visibility = View.VISIBLE
        val circularProgressIndicator =
            ProgressIndicator(circularProgressIndicator, success_frame, imFailureIcon, success_tick)
        circularProgressIndicator.apply {
            progressIndicatorListener {}
            stopSpinning()
            animationStatus = ProgressIndicator.AnimationStatus.Success
        }
        gotItActionButton?.setOnClickListener {
            // dismiss dialog and pass the value to original fragment
            dismiss()
            clickListner?.onConfirmClick(mTipValue)
        }
    }
}
