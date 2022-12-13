package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.graphics.Paint
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import com.awfs.coordination.databinding.GotItDialogFragmentBinding
import za.co.woolworths.financial.services.android.contracts.IDialogListener

class GotITDialogFragment : WBottomSheetDialogFragment(), View.OnClickListener {

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

    private lateinit var binding: GotItDialogFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = GotItDialogFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bundleArguments = arguments
        val mResponseTitle = bundleArguments?.getString("title")
        val mResponseDesc = bundleArguments?.getString("desc")
        val dismissDialogText = bundleArguments?.getString("dismissDialogText")
        val actionButtonText = bundleArguments?.getString("actionButtonText")
        val isIconAvailable = bundleArguments?.getBoolean("isIconAvailable")
        val icon = bundleArguments?.getInt("icon")

        with(binding) {
            if (!TextUtils.isEmpty(mResponseTitle))
                tvTitle.setText(mResponseTitle)

            if (!TextUtils.isEmpty(mResponseDesc))
                tvDescription.setText(mResponseDesc)

            if (!TextUtils.isEmpty(dismissDialogText))
                btnGotIt.text = dismissDialogText

            if (!TextUtils.isEmpty(actionButtonText))
                actionButton.text = actionButtonText

            actionButton.visibility =
                if (TextUtils.isEmpty(actionButtonText)) View.INVISIBLE else View.VISIBLE
            vHorizontalDivider.visibility =
                if (TextUtils.isEmpty(actionButtonText)) View.VISIBLE else View.INVISIBLE
            if (isIconAvailable!!) {
                icon?.let { imageIcon.setBackgroundResource(it) }
                imageIcon.visibility = View.VISIBLE
            }
            btnGotIt.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            btnGotIt.setOnClickListener(this@GotITDialogFragment)

            actionButton.setOnClickListener(this@GotITDialogFragment)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnGotIt -> {
                dismissAllowingStateLoss()
                mOnDialogDismiss?.onDialogDismissed()
            }
            R.id.actionButton -> {
                dismissAllowingStateLoss()
                mOnDialogDismiss?.onDialogButtonAction()
            }

        }
    }
}
