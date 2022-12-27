package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.os.Bundle
import android.view.View
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ProcessingRequestFailureFragmentBinding
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class ProcessRequestFailureFragment : BaseFragmentBinding<ProcessingRequestFailureFragmentBinding>(ProcessingRequestFailureFragmentBinding::inflate), View.OnClickListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupOnClickListener()
    }

    private fun setupOnClickListener() {
        binding.callCenterNumberTextView?.apply {
            setOnClickListener(this@ProcessRequestFailureFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnRetryOnFailure -> {
            }
            R.id.callCenterNumberTextView -> {
            }
        }
    }

}