package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.view.View
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ProcessRequestFragmentBinding
import za.co.woolworths.financial.services.android.contracts.IProgressAnimationState
import za.co.woolworths.financial.services.android.ui.views.tick_animation.AnimationState
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

open class ProcessYourRequestFragment : BaseFragmentBinding<ProcessRequestFragmentBinding>(ProcessRequestFragmentBinding::inflate) {

    var isAPICallSuccessFul: Boolean = false
    private var stateAnimation: IProgressAnimationState? = null

    fun circularProgressListener(codeSuccess: () -> Unit, codeFailure: () -> Unit) {
        binding.includeCircleProgressLayout.circularProgressIndicator?.setOnAnimationStateChangedListener { _animationState ->
            _animationState?.apply {
                if (this == AnimationState.ANIMATING) {
                    if (isAPICallSuccessFul) {
                        binding.includeCircleProgressLayout.successTick?.apply {
                            visibility = View.VISIBLE
                            colorCode = R.color.success_tick_color
                            startTickAnim()
                            codeSuccess()
                        }
                    } else {
                        binding.includeCircleProgressLayout.imFailureIcon?.visibility = View.VISIBLE
                        codeFailure()
                    }
                    stateAnimation?.onAnimationEnd(isAPICallSuccessFul)
                }
            }
        }
    }

    fun stopSpinning(isSuccess: Boolean) {
        this.isAPICallSuccessFul = isSuccess
        binding.includeCircleProgressLayout.circularProgressIndicator?.apply {
            stopSpinning()
            setValueAnimated(100f)
        }
    }

    fun startSpinning() {
        binding.includeCircleProgressLayout.imFailureIcon?.visibility = View.GONE
        binding.includeCircleProgressLayout.circularProgressIndicator?.spin()
    }
}