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
        fun newInstance(title: String, desc: String, onDialogDismissListener: IDialogListener): GotITDialogFragment {
            mOnDialogDismiss = onDialogDismissListener
            val gotITDialogFragment = GotITDialogFragment()
            val bundle = Bundle()
            bundle.putString("title", title)
            bundle.putString("desc", desc)
            gotITDialogFragment.arguments = bundle
            return gotITDialogFragment
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addContentView(R.layout.got_it_dialog_fragment)

        val bundleArguments = arguments
        val mResponseTitle = bundleArguments.getString("title")
        val mResponseDesc = bundleArguments.getString("desc")

        if (!TextUtils.isEmpty(mResponseTitle))
            tvTitle.setText(mResponseTitle)

        if (!TextUtils.isEmpty(mResponseDesc))
            tvDescription.setText(mResponseDesc)

        btnGotIt.setOnClickListener(this)

        mRootActionSheetConstraint.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnGotIt, R.id.rootActionSheetConstraint -> {
                onDialogBackPressed(false)
                mOnDialogDismiss?.onDialogDismissed()
            }
        }
    }
}
