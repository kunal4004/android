package za.co.woolworths.financial.services.android.ui.fragments.card

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.circle_progress_layout.*
import za.co.woolworths.financial.services.android.contracts.IProgressAnimationState
import za.co.woolworths.financial.services.android.ui.views.tick_animation.AnimationState

class ProgressStateFragment : Fragment() {

    private var stateAnimation: IProgressAnimationState? = null

    companion object {
        fun newInstance(animationState: IProgressAnimationState?) = ProgressStateFragment().apply {
            stateAnimation = animationState
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.circle_progress_layout, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        circularProgressIndicator?.setOnAnimationStateChangedListener { _animationState ->
            _animationState?.apply {
                if (this == AnimationState.ANIMATING) {
                    success_tick?.apply {
                        visibility = View.VISIBLE
                        startTickAnim()
                        stateAnimation?.onAnimationEnd()
                    }
                }
            }
        }
    }

    fun animateSuccessEnd() {
        circularProgressIndicator?.apply {
            stopSpinning()
            setValueAnimated(100f)
        }
    }
}