package za.co.woolworths.financial.services.android.util

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.databinding.AddingTipProgressDialogBinding
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.opt_in.otp.ProgressIndicator
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

/**
 * Created by Kunal Uttarwar on 06/07/23.
 */
class CustomProgressBar : WBottomSheetDialogFragment() {

    private lateinit var binding: AddingTipProgressDialogBinding
    private lateinit var title: String
    private lateinit var subTitle: String

    companion object {
        private const val TITLE = "TITLE"
        private const val SUB_TITLE = "SUB_TITLE"

        fun newInstance(title: String, subTitle: String) =
            CustomProgressBar().withArgs {
                putString(TITLE, title)
                putString(SUB_TITLE, subTitle)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            title = getString(TITLE, "")
            subTitle = getString(SUB_TITLE, "")
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
        binding.progressTitleTextView?.text = title
        binding.progressDescriptionTextView?.visibility = View.VISIBLE
        binding.progressDescriptionTextView?.text = subTitle
    }

    private fun AddingTipProgressDialogBinding.showProgressBar() {
        val circularProgressIndicator =
            ProgressIndicator(circularProgressIndicator, successFrame, imFailureIcon, successTick)
        circularProgressIndicator.apply {
            progressIndicatorListener {}
            animationStatus = ProgressIndicator.AnimationStatus.InProgress
        }
    }
}
