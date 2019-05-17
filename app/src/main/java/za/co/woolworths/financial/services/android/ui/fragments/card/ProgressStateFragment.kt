package za.co.woolworths.financial.services.android.ui.fragments.card

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.circle_progress_layout.*
import za.co.woolworths.financial.services.android.contracts.IProgressAnimationState
import za.co.woolworths.financial.services.android.ui.views.tick_animation.AnimationState

class ProgressStateFragment : Fragment() {

    private var mCardIsBlocked: Boolean = false
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
                    if (mCardIsBlocked) {
                        success_tick?.apply {
                            visibility = VISIBLE
                            startTickAnim()
                        }
                    } else {
                        imFailureIcon?.visibility = VISIBLE
                    }
                    stateAnimation?.onAnimationEnd(mCardIsBlocked)

                }
            }
        }
    }

    fun animateSuccessEnd(cardIsBlocked: Boolean) {
        this.mCardIsBlocked = cardIsBlocked
        circularProgressIndicator?.apply {
            stopSpinning()
            setValueAnimated(100f)
        }
    }

    fun restartSpinning() {
        imFailureIcon?.visibility = GONE
        circularProgressIndicator?.apply {
            spin()
        }
    }
}