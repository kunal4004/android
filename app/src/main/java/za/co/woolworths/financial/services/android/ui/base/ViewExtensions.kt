package za.co.woolworths.financial.services.android.ui.base

import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

fun View.onClick(clickListener: (View) -> Unit) {
    setOnClickListener(clickListener)
    AnimationUtilExtension.animateViewPushDown(this)
}

fun View.onClick(view : View.OnClickListener) {
    AnimationUtilExtension.animateViewPushDown(this)
    setOnClickListener(view)
}

fun View.isOverlappingWith(otherView: View): Boolean {
    val thisRect = Rect()
    val otherRect = Rect()

    this.getGlobalVisibleRect(thisRect)
    otherView.getGlobalVisibleRect(otherRect)

    return thisRect.intersect(otherRect)
}

fun View.doOnLayoutReady(action: () -> Unit) {
    if (width > 0 && height > 0) {
        // View has already been laid out
        action.invoke()
    } else {
        // View has not been laid out yet, register a listener
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // View has been laid out, invoke the action and remove the listener
                action.invoke()
                viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }
}