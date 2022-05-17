package za.co.woolworths.financial.services.android.ui.base

import android.view.View
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

fun View.onClick(clickListener: (View) -> Unit) {
    setOnClickListener(clickListener)
    AnimationUtilExtension.animateViewPushDown(this)
}