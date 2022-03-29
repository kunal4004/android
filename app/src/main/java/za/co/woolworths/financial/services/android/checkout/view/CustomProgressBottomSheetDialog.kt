package za.co.woolworths.financial.services.android.checkout.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.adding_tip_progress_dialog.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.opt_in.otp.ProgressIndicator
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.AppConstant

/**
 * Created by Kunal Uttarwar on 28/03/22.
 */
class CustomProgressBottomSheetDialog : WBottomSheetDialogFragment() {

    private var mTitle: String? = null
    private var mSubTitle: String? = null
    private lateinit var mTipValue: String


    interface ClickListner {
        fun onConfirmClick(tipValue: String)
    }

    companion object {
        private const val TITLE = "TITLE"
        private const val SUB_TITLE = "SUB_TITLE"
        private const val TIP_VALUE = "TIP_VALUE"
        private var clickListner: ClickListner? = null

        fun newInstance(title: String, subTitle: String, tipValue: String, listner: ClickListner) =
            CustomProgressBottomSheetDialog().withArgs {
                putString(TITLE, title)
                putString(SUB_TITLE, subTitle)
                putString(TIP_VALUE, tipValue)
                clickListner = listner
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            mTitle = getString(TITLE, "")
            mSubTitle = getString(SUB_TITLE, "")
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
        progressTitleTextView?.text = mTitle
        progressDescriptionTextView?.text = mSubTitle
    }

    private fun showProgressBar() {
        val circularProgressIndicator =
            ProgressIndicator(circularProgressIndicator, success_frame, imFailureIcon, success_tick)
        circularProgressIndicator?.apply {
            // To show as processing
            animationStatus = ProgressIndicator.AnimationStatus.InProgress
            progressIndicatorListener {}
        }
        viewLifecycleOwner.lifecycleScope.launch {
            delay(AppConstant.DELAY_2000_MS)
            circularProgressIndicator?.apply {
                stopSpinning()
                animationStatus = ProgressIndicator.AnimationStatus.Success
            }
            gotItActionButton?.visibility = View.VISIBLE
            gotItActionButton?.setOnClickListener {
                // dismiss dialog and pass the value to original fragment
                dismiss()
                clickListner?.onConfirmClick(mTipValue)
            }
            progressTitleTextView?.text = bindString(R.string.amt_tip_added, mTipValue)
            progressDescriptionTextView?.visibility = View.GONE
        }
    }
}
