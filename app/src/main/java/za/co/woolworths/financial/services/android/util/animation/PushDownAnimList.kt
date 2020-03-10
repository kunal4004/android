package za.co.woolworths.financial.services.android.util.animation

import android.view.View
import android.view.View.OnLongClickListener
import android.view.View.OnTouchListener
import android.view.animation.AccelerateDecelerateInterpolator
import za.co.woolworths.financial.services.android.contracts.PushDown
import java.util.*

class PushDownAnimList internal constructor(vararg views: View?) : PushDown {
    private val pushDownList: MutableList<PushDownAnim> =
            ArrayList()

    override fun setScale(scale: Float): PushDownAnimList {
        for (pushDown in pushDownList) {
            pushDown.setScale(scale)
        }
        return this
    }

    override fun setScale(mode: Int, scale: Float): PushDown {
        for (pushDown in pushDownList) {
            pushDown.setScale(mode, scale)
        }
        return this
    }

    override fun setDurationPush(duration: Long): PushDownAnimList {
        for (pushDown in pushDownList) {
            pushDown.setDurationPush(duration)
        }
        return this
    }

    override fun setDurationRelease(duration: Long): PushDownAnimList {
        for (pushDown in pushDownList) {
            pushDown.setDurationRelease(duration)
        }
        return this
    }

    override fun setInterpolatorPush(interpolatorPush: AccelerateDecelerateInterpolator?): PushDownAnimList {
        for (pushDown in pushDownList) {
            pushDown.setInterpolatorPush(interpolatorPush)
        }
        return this
    }

    override fun setInterpolatorRelease(interpolatorRelease: AccelerateDecelerateInterpolator?): PushDownAnimList {
        for (pushDown in pushDownList) {
            pushDown.setInterpolatorRelease(interpolatorRelease)
        }
        return this
    }

    override fun setOnClickListener(clickListener: View.OnClickListener?): PushDownAnimList {
        for (pushDown in pushDownList) {
            if (clickListener != null) {
                pushDown.setOnClickListener(clickListener)
            }
        }
        return this
    }

    override fun setOnLongClickListener(clickListener: OnLongClickListener?): PushDown {
        for (pushDown in pushDownList) {
            if (clickListener != null) {
                pushDown.setOnLongClickListener(clickListener)
            }
        }
        return this
    }

    override fun setOnTouchEvent(eventListener: OnTouchListener?): PushDownAnimList {
        for (pushDown in pushDownList) {
            if (eventListener != null) {
                pushDown.setOnTouchEvent(eventListener)
            }
        }
        return this
    }

    init {
        for (view in views) {
            val pushDown: PushDownAnim? = view?.let { PushDownAnim.setPushDownAnimTo(it) }
            pushDown?.setOnTouchEvent(null)
            pushDown?.let { pushDownList.add(it) }
        }
    }
}
