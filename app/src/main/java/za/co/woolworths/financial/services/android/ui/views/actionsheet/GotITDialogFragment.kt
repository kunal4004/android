package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.got_it_dialog_fragment.*
import za.co.woolworths.financial.services.android.contracts.IDialogListener

class GotITDialogFragment : ActionSheetDialogFragment(), View.OnClickListener {


    companion object {
        private var mOnDialogDismiss: IDialogListener? = null
        fun newInstance(title: String, desc: String, dismissDialogText: String, onDialogDismissListener: IDialogListener, actionButtonText: String = "", isIconAvailable:Boolean=false, icon:Int=R.drawable.appicon): GotITDialogFragment {
            mOnDialogDismiss = onDialogDismissListener
            val gotITDialogFragment = GotITDialogFragment()
            val bundle = Bundle()
            bundle.putString("title", title)
            bundle.putString("desc", desc)
            bundle.putString("dismissDialogText", dismissDialogText)
            bundle.putString("actionButtonText", actionButtonText)
            bundle.putBoolean("isIconAvailable",isIconAvailable)
            bundle.putInt("icon",icon)
            gotITDialogFragment.arguments = bundle
            return gotITDialogFragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addContentView(R.layout.got_it_dialog_fragment)

        val bundleArguments = arguments
        val mResponseTitle = bundleArguments?.getString("title")
        val mResponseDesc = bundleArguments?.getString("desc")
        val dismissDialogText = bundleArguments?.getString("dismissDialogText")
        val actionButtonText = bundleArguments?.getString("actionButtonText")
        val isIconAvailable = bundleArguments?.getBoolean("isIconAvailable")
        val icon = bundleArguments?.getInt("icon")

        if (!TextUtils.isEmpty(mResponseTitle))
            tvTitle.setText(mResponseTitle)

        if (!TextUtils.isEmpty(mResponseDesc))
            tvDescription.setText(mResponseDesc)

        if (!TextUtils.isEmpty(dismissDialogText))
            btnGotIt.text = dismissDialogText

        if (!TextUtils.isEmpty(actionButtonText))
            actionButton.text = actionButtonText

        actionButton.visibility = if (TextUtils.isEmpty(actionButtonText)) View.INVISIBLE else View.VISIBLE
        vHorizontalDivider.visibility = if (TextUtils.isEmpty(actionButtonText)) View.VISIBLE else View.INVISIBLE
        if (isIconAvailable!!) {
            icon?.let { imageIcon.setBackgroundResource(it) }
            imageIcon.visibility = View.VISIBLE
        }

        btnGotIt.setOnClickListener(this)

        mRootActionSheetConstraint.setOnClickListener(this)

        actionButton.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnGotIt, R.id.rootActionSheetConstraint -> {
                shouldAnimateViewOnCancel(true)
                mOnDialogDismiss?.onDialogDismissed()
            }
            R.id.actionButton -> {
                shouldAnimateViewOnCancel(true)
                mOnDialogDismiss?.onDialogButtonAction()
            }

        }
    }
}
