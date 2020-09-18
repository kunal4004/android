package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.pay_my_account_retry_on_error_dialog_fragment.*
import za.co.woolworths.financial.services.android.ui.fragments.account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension


class PayMyAccountRetryErrorFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()

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
                payMyAccountViewModel.setNavigationResult(PayMyAccountViewModel.OnBackNavigation.RETRY)
                dismiss()
            }
        }
    }
}