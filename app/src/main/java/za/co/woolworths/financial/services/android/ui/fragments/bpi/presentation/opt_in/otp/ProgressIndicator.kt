package za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.opt_in.otp

import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.views.tick_animation.AnimationState
import za.co.woolworths.financial.services.android.ui.views.tick_animation.CircleProgressView
import za.co.woolworths.financial.services.android.ui.views.tick_animation.SuccessTickView

class ProgressIndicator(private val circularProgressIndicator: CircleProgressView?,
                        private val successFrame: FrameLayout?,
                        private val imFailureIcon : ImageView?,
                        private val success_tick: SuccessTickView?)  {

    var animationStatus : AnimationStatus = AnimationStatus.Success

    enum class AnimationStatus {
        InProgress,Success, Failure
    }

    fun progressIndicatorListener(animationState: (AnimationStatus) -> Unit) {
        circularProgressIndicator?.setOnAnimationStateChangedListener { _animationState ->
            _animationState?.apply {
                if (this == AnimationState.ANIMATING) {
                    when (animationStatus) {
                         AnimationStatus.InProgress -> spin()
                        AnimationStatus.Success -> {
                            success_tick?.apply {
                                visibility = View.VISIBLE
                                startTickAnim()
                            }
                        }
                         AnimationStatus.Failure -> {
                            imFailureIcon?.visibility = View.VISIBLE
                            stopSpinning()
                        }
                    }
                    animationState(animationStatus)
                }
            }
        }

        successFrame?.contentDescription = bindString(R.string.progress_indicator_state)
    }

    fun stopSpinning() {
        circularProgressIndicator?.apply {
            stopSpinning()
            setValueAnimated(100f)
        }
    }

    fun spin(){
        imFailureIcon?.visibility = View.GONE
        success_tick?.visibility = View.GONE
        circularProgressIndicator?.spin()
    }
}