package za.co.woolworths.financial.services.android.ui.views

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.NonNull
import androidx.annotation.Nullable

import androidx.core.widget.NestedScrollView

class LockableNestedScrollViewV2 : NestedScrollView {
    // by default is scrollable
    private var scrollable = true
    private var scrollerTask: Runnable? = null
    private var initialPosition = 0

    private val newCheck = 100L

    interface OnScrollStoppedListener {
        fun onScrollStopped()
    }

    private var onScrollStoppedListener: OnScrollStoppedListener? = null

    constructor(@NonNull context: Context?) : super(context!!) {
        init()
    }

    constructor(@NonNull context: Context?, @Nullable attrs: AttributeSet?) : super(
        context!!,
        attrs
    ) {
        init()
    }

    constructor(
        @NonNull context: Context?,
        @Nullable attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context!!, attrs, defStyleAttr) {
        init()
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return scrollable && super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return scrollable && super.onInterceptTouchEvent(ev)
    }

    private fun init() {
        scrollerTask = Runnable {
            val newPosition = scrollY
            if (initialPosition - newPosition == 0) { //has stopped
                if (onScrollStoppedListener != null) {
                    onScrollStoppedListener?.onScrollStopped()
                }
            } else {
                initialPosition = scrollY
                this@LockableNestedScrollViewV2.postDelayed(scrollerTask, newCheck)
            }
        }
    }

    fun setScrollingEnabled(enabled: Boolean) {
        scrollable = enabled
    }

    fun setOnScrollStoppedListener(listener: OnScrollStoppedListener) {
        onScrollStoppedListener = listener
    }

    fun startScrollerTask() {
        initialPosition = scrollY
        this@LockableNestedScrollViewV2.postDelayed(scrollerTask, newCheck)
    }

    fun isViewVisible(view: View): Boolean {
        val scrollBounds = Rect()
        getHitRect(scrollBounds)
        return view.getLocalVisibleRect(scrollBounds)
    }
}