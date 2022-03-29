package za.co.woolworths.financial.services.android.checkout.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.adding_tip_progress_dialog.*
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

/**
 * Created by Kunal Uttarwar on 28/03/22.
 */
class CustomProgressBottomSheetDialog : WBottomSheetDialogFragment() {

    private var mTitle: String? = null
    private var mSubTitle: String? = null
    private var mTipValue: String? = null


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
        progressTitleTextView?.text = mTitle
        progressDescriptionTextView?.text = mSubTitle
        gotItActionButton.visibility = View.VISIBLE
        gotItActionButton?.setOnClickListener {
            dismiss()
            mTipValue?.let { it1 -> clickListner?.onConfirmClick(it1) }
        }
    }
}
