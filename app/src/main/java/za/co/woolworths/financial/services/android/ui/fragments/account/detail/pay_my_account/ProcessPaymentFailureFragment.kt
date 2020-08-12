package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.circle_progress_layout.*
import kotlinx.android.synthetic.main.process_payment_failure_fragment.*
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class ProcessPaymentFailureFragment : ProcessYourRequestFragment(), View.OnClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.process_payment_failure_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        circularProgressIndicator?.stopSpinning()
        imFailureIcon?.visibility = VISIBLE

        btnRetryOnFailure?.apply {
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@ProcessPaymentFailureFragment)
        }

        tvCallCenterNumber?.apply {
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@ProcessPaymentFailureFragment)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnRetryOnFailure -> {

            }

            R.id.tvCallCenterNumber -> {

            }
        }
    }

}