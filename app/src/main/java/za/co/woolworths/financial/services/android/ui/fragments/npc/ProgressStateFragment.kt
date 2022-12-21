package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import com.awfs.coordination.R
import com.awfs.coordination.databinding.CircleProgressLayoutBinding
import za.co.woolworths.financial.services.android.contracts.IProgressAnimationState
import za.co.woolworths.financial.services.android.ui.views.tick_animation.AnimationState
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class ProgressStateFragment : BaseFragmentBinding<CircleProgressLayoutBinding>(CircleProgressLayoutBinding::inflate) {

    private var isSuccess: Boolean = false
    private var stateAnimation: IProgressAnimationState? = null

    companion object {
        fun newInstance(animationState: IProgressAnimationState?) = ProgressStateFragment().apply {
            stateAnimation = animationState
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.circularProgressIndicator?.setOnAnimationStateChangedListener { _animationState ->
            _animationState?.apply {
                if (this == AnimationState.ANIMATING) {
                    if (isSuccess) {
                        binding.successTick?.apply {
                            visibility = VISIBLE
                            startTickAnim()
                        }
                    } else {
                        binding.imFailureIcon?.visibility = VISIBLE
                    }
                    stateAnimation?.onAnimationEnd(isSuccess)

                }
            }
        }
        uniqueIdsForProgressIndicator()
    }

    private fun uniqueIdsForProgressIndicator() {
        activity?.resources?.apply {
            binding.successFrame?.contentDescription = getString(R.string.progress_indicator_state)
        }
    }

    fun animateSuccessEnd(isSuccess: Boolean) {
        this.isSuccess = isSuccess
        binding.circularProgressIndicator?.apply {
            stopSpinning()
            setValueAnimated(100f)
        }
    }

    fun restartSpinning() {
        binding.apply {
            imFailureIcon?.visibility = GONE
            successTick?.visibility = GONE
            circularProgressIndicator?.apply {
                spin()
            }
        }
    }
}