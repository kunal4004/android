package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.error_message_fragment.*
import za.co.woolworths.financial.services.android.ui.extension.withArgs

class ErrorMessageDialogWithTitleFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    private var message: String? = null
    private var shouldDismissActivity = false

    companion object {
        private const val ERR_DESCRIPTION = "ERR_DESCRIPTION"
        private const val SHOULD_CLOSE_ACTIVITY = "SHOULD_CLOSE_ACTIVITY"

        fun newInstance() = ErrorMessageDialogWithTitleFragment()
        fun newInstance(description: String) = ErrorMessageDialogWithTitleFragment().withArgs {
            putString(ERR_DESCRIPTION, description)
        }

        fun newInstance(description: String, shouldFinishActivity: Boolean) = ErrorMessageDialogWithTitleFragment().withArgs {
            putString(ERR_DESCRIPTION, description)
            putBoolean(SHOULD_CLOSE_ACTIVITY, shouldFinishActivity)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            message = getString(ERR_DESCRIPTION)
            shouldDismissActivity = getBoolean(SHOULD_CLOSE_ACTIVITY, false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.error_message_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (message?.isNotEmpty() == true)
            tvErrorDescription?.text = message
        btnDismissDialog?.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        dismissAllowingStateLoss()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (shouldDismissActivity) {
            activity?.apply {
                finish()
                overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
            }
        }
    }
}
