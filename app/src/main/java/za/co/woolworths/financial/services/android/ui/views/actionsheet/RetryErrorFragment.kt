package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.retry_on_error_dialog_fragment.*
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension


class RetryErrorFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.retry_on_error_dialog_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        retryErrorButton?.apply {
            setOnClickListener(this@RetryErrorFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.retryErrorButton -> {
                dismiss()
            }
        }
    }
}