package za.co.woolworths.financial.services.android.util.animation

import android.view.View
import android.view.animation.RotateAnimation
import android.view.animation.TranslateAnimation
import androidx.core.content.ContextCompat
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.account_sales_activity.*
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.activities.account.apply_now.AccountSalesActivity
import za.co.woolworths.financial.services.android.util.KotlinUtils

class AnimationUtilExtension {
    companion object {
        const val INITIAL_POSITION = 0.0f
        const val ROTATED_POSITION = 180f
        private const val PIVOT_VALUE = 0.5f
        private const val APPLY_NOW_BUTTON_ANIMATE_DURATION: Long = 300

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

        fun animateViewPushDown(view: View?) {
            view?.let { v ->
                PushDownAnim.setPushDownAnimTo(v)
                        .setScale(PushDownAnim.MODE_STATIC_DP, 4.5f)
                        .setDurationPush(PushDownAnim.DEFAULT_PUSH_DURATION)
                        ?.setDurationRelease(PushDownAnim.DEFAULT_RELEASE_DURATION)
                        ?.setInterpolatorPush(PushDownAnim.DEFAULT_INTERPOLATOR)
                        ?.setInterpolatorRelease(PushDownAnim.DEFAULT_INTERPOLATOR)
            }
        }

        fun animateButtonIn(view: View?) {
            if (view?.visibility == View.VISIBLE) return
            view?.visibility = View.VISIBLE
            val animate = TranslateAnimation(0f, 0F, view?.height?.toFloat() ?: 0f, 0f)
            animate.duration = APPLY_NOW_BUTTON_ANIMATE_DURATION
            animate.fillAfter = true
            view?.startAnimation(animate)
            view?.isEnabled = true
        }

        fun animateButtonOut(view: View?) {
            if (view?.visibility == View.INVISIBLE) return
            val animate = TranslateAnimation(0f, 0f, 0f, view?.height?.toFloat()?: 0f)
            animate.duration = APPLY_NOW_BUTTON_ANIMATE_DURATION
            animate.fillAfter = true
            view?.startAnimation(animate)
            view?.visibility = View.INVISIBLE
            view?.isEnabled = false
        }


        fun transitionBottomSheetBackgroundColor(dimView: View?, slideOffset: Float) {
            val context = WoolworthsApplication.getAppContext()
            val colorFrom = ContextCompat.getColor(context, android.R.color.transparent)
            val colorTo = ContextCompat.getColor(context, R.color.black_99)
            dimView?.setBackgroundColor(KotlinUtils.interpolateColor(slideOffset, colorFrom, colorTo))
        }
    }

}