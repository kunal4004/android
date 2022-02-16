package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentTransaction
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.error_message_fragment.*
import za.co.woolworths.financial.services.android.ui.extension.withArgs

class ErrorMessageDialogWithTitleFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    private var title: String? = null
    private var message: String? = null
    private var actionButtonTitle: String? = null
    private var shouldDismissActivity = false

    companion object {
        private const val ERR_TITLE = "ERR_TITLE"
        private const val ERR_DESCRIPTION = "ERR_DESCRIPTION"
        private const val ACTION_BUTTON_TITLE = "ACTION_BUTTON_TITLE"
        private const val SHOULD_CLOSE_ACTIVITY = "SHOULD_CLOSE_ACTIVITY"

        fun newInstance() = ErrorMessageDialogWithTitleFragment()

        fun newInstance(title: String, description: String, actionButtonTitle: String, shouldFinishActivity: Boolean) = ErrorMessageDialogWithTitleFragment().withArgs {
            putString(ERR_TITLE, title)
            putString(ERR_DESCRIPTION, description)
            putString(ACTION_BUTTON_TITLE, actionButtonTitle)
            putBoolean(SHOULD_CLOSE_ACTIVITY, shouldFinishActivity)
        }

        fun newInstance(description: String, shouldFinishActivity: Boolean) = ErrorMessageDialogWithTitleFragment().withArgs {
            putString(ERR_DESCRIPTION, description)
            putBoolean(SHOULD_CLOSE_ACTIVITY, shouldFinishActivity)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            title = getString(ERR_TITLE)
            message = getString(ERR_DESCRIPTION)
            actionButtonTitle = getString(ACTION_BUTTON_TITLE)
            shouldDismissActivity = getBoolean(SHOULD_CLOSE_ACTIVITY, false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.error_message_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!title.isNullOrEmpty())
            tvTitle?.text = title
        else
            tvTitle?.visibility = View.GONE
        if (!message.isNullOrEmpty())
            tvErrorDescription?.text = message
        if (!actionButtonTitle.isNullOrEmpty())
            btnDismissDialog?.text = actionButtonTitle
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
