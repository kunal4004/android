package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.pay_my_account_retry_on_error_dialog_fragment.*
import za.co.woolworths.financial.services.android.ui.extension.setNavigationResult
import za.co.woolworths.financial.services.android.util.Constant
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension


class PayMyAccountRetryErrorFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.pay_my_account_retry_on_error_dialog_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pmaErrorRetryButton?.apply {
            setOnClickListener(this@PayMyAccountRetryErrorFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.pmaErrorRetryButton -> {
                setNavigationResult(Constant.GET_PAYMENT_METHOD_ERROR, Constant.queryServiceGetPaymentMethod)
                dismiss()
            }
        }
    }
}