package za.co.woolworths.financial.services.android.contracts

import android.view.View
import android.view.View.OnLongClickListener
import android.view.View.OnTouchListener
import android.view.animation.AccelerateDecelerateInterpolator
import za.co.woolworths.financial.services.android.util.animation.PushDownAnim

interface PushDown {
    fun setScale(scale: Float): PushDown?
    fun setScale(@PushDownAnim.Mode mode: Int, scale: Float): PushDown?
    fun setDurationPush(duration: Long): PushDown?
    fun setDurationRelease(duration: Long): PushDown?
    fun setInterpolatorPush(interpolatorPush: AccelerateDecelerateInterpolator?): PushDown?
    fun setInterpolatorRelease(interpolatorRelease: AccelerateDecelerateInterpolator?): PushDown?
    fun setOnClickListener(clickListener: View.OnClickListener?): PushDown?
    fun setOnLongClickListener(clickListener: OnLongClickListener?): PushDown?
    fun setOnTouchEvent(eventListener: OnTouchListener?): PushDown?
}

