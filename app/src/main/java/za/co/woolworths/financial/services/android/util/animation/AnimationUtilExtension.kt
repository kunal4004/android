package za.co.woolworths.financial.services.android.util.animation

import android.view.View
import android.view.animation.RotateAnimation

class AnimationUtilExtension {
    companion object {
        const val INITIAL_POSITION = 0.0f
        const val ROTATED_POSITION = 180f
        private const val PIVOT_VALUE = 0.5f

        fun rotateView(expanded: Boolean, view: View?) {
            val rotateAnimation: RotateAnimation? = if (expanded) { // rotate clockwise
                RotateAnimation(ROTATED_POSITION,
                        INITIAL_POSITION,
                        RotateAnimation.RELATIVE_TO_SELF, PIVOT_VALUE,
                        RotateAnimation.RELATIVE_TO_SELF, PIVOT_VALUE)
            } else { // rotate counterclockwise
                RotateAnimation(-1 * ROTATED_POSITION,
                        INITIAL_POSITION,
                        RotateAnimation.RELATIVE_TO_SELF, PIVOT_VALUE,
                        RotateAnimation.RELATIVE_TO_SELF, PIVOT_VALUE)
            }
            rotateAnimation?.duration = 200
            rotateAnimation?.fillAfter = true
            view?.startAnimation(rotateAnimation)
        }
    }
}