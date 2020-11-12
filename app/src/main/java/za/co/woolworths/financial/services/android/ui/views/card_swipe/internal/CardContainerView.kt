package za.co.woolworths.financial.services.android.ui.views.card_swipe.internal

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import androidx.core.view.MotionEventCompat
import androidx.core.view.ViewCompat
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.views.card_swipe.SwipeDirection
import za.co.woolworths.financial.services.android.ui.views.card_swipe.internal.SwipeUtils.Companion.getQuadrant
import za.co.woolworths.financial.services.android.ui.views.card_swipe.internal.SwipeUtils.Companion.getRadian
import za.co.woolworths.financial.services.android.ui.views.card_swipe.internal.SwipeUtils.Companion.getTargetPoint

import kotlin.math.abs

class CardContainerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    private var option: CardStackOption? = null
    var viewOriginX = 0f
        private set
    var viewOriginY = 0f
        private set
    private var motionOriginX = 0f
    private var motionOriginY = 0f
    private var isDragging = false
    private var isDraggable = true
    var contentContainer: ViewGroup? = null
        private set
    var overlayContainer: ViewGroup? = null
        private set
    private var leftOverlayView: View? = null
    private var rightOverlayView: View? = null
    private var bottomOverlayView: View? = null
    private var topOverlayView: View? = null
    private var containerEventListener: ContainerEventListener? = null
    private val gestureListener: SimpleOnGestureListener = object : SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            if (containerEventListener != null) {
                containerEventListener!!.onContainerClicked()
            }
            return true
        }
    }
    private val gestureDetector =
            GestureDetector(getContext(), gestureListener)

    interface ContainerEventListener {
        fun onContainerDragging(percentX: Float, percentY: Float)
        fun onContainerSwiped(point: Point?, direction: SwipeDirection?)
        fun onContainerMovedToOrigin()
        fun onContainerClicked()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        View.inflate(context, R.layout.card_frame, this)
        contentContainer =
                findViewById<View>(R.id.card_frame_content_container) as ViewGroup
        overlayContainer =
                findViewById<View>(R.id.card_frame_overlay_container) as ViewGroup
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        if (!option!!.isSwipeEnabled || !isDraggable) {
            return true
        }
        when (MotionEventCompat.getActionMasked(event)) {

            MotionEvent.ACTION_DOWN -> {
                handleActionDown(event)
                parent.parent.requestDisallowInterceptTouchEvent(false)
            }
            MotionEvent.ACTION_UP -> {
                handleActionUp(event)
                parent.parent.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_CANCEL -> parent.parent.requestDisallowInterceptTouchEvent(false)
            MotionEvent.ACTION_MOVE -> handleActionMove(event)
        }
        return true
    }

    private fun handleActionDown(event: MotionEvent) {
        motionOriginX = event.rawX
        motionOriginY = event.rawY
    }

    private fun handleActionUp(event: MotionEvent) {
        if (isDragging) {
            isDragging = false
            val motionCurrentX = event.rawX
            val motionCurrentY = event.rawY
            val point =
                    getTargetPoint(motionOriginX, motionOriginY, motionCurrentX, motionCurrentY)
            val quadrant =
                    getQuadrant(motionOriginX, motionOriginY, motionCurrentX, motionCurrentY)
            var radian =
                    getRadian(motionOriginX, motionOriginY, motionCurrentX, motionCurrentY)
            var degree: Double
            var direction: SwipeDirection? = null
            when (quadrant) {
                Quadrant.TopLeft -> {
                    degree = Math.toDegrees(radian)
                    degree = 180 - degree
                    radian = Math.toRadians(degree)
                    direction = if (Math.cos(radian) < -0.5) {
                        SwipeDirection.Left
                    } else {
                        SwipeDirection.Top
                    }
                }
                Quadrant.TopRight -> {
                    degree = Math.toDegrees(radian)
                    radian = Math.toRadians(degree)
                    direction = if (Math.cos(radian) < 0.5) {
                        SwipeDirection.Top
                    } else {
                        SwipeDirection.Right
                    }
                }
                Quadrant.BottomLeft -> {
                    degree = Math.toDegrees(radian)
                    degree += 180
                    radian = Math.toRadians(degree)
                    direction = if (Math.cos(radian) < -0.5) {
                        SwipeDirection.Left
                    } else {
                        SwipeDirection.Bottom
                    }
                }
                Quadrant.BottomRight -> {
                    degree = Math.toDegrees(radian)
                    degree = 360 - degree
                    radian = Math.toRadians(degree)
                    direction = if (Math.cos(radian) < 0.5) {
                        SwipeDirection.Bottom
                    } else {
                        SwipeDirection.Right
                    }
                }
            }
            var percent = 0f
            percent = if (direction === SwipeDirection.Left || direction === SwipeDirection.Right) {
                percentX
            } else {
                percentY
            }
            if (abs(percent) > option!!.swipeThreshold) {
                if (option!!.swipeDirection.contains(direction)) {
                    if (containerEventListener != null) {
                        containerEventListener!!.onContainerSwiped(point, direction)
                    }
                } else {
                    moveToOrigin()
                    if (containerEventListener != null) {
                        containerEventListener!!.onContainerMovedToOrigin()
                    }
                }
            } else {
                moveToOrigin()
                if (containerEventListener != null) {
                    containerEventListener!!.onContainerMovedToOrigin()
                }
            }
        }
        motionOriginX = event.rawX
        motionOriginY = event.rawY
    }

    private fun handleActionMove(event: MotionEvent) {
        isDragging = true
        updateTranslation(event)
        updateRotation()
        updateAlpha()
        if (containerEventListener != null) {
            containerEventListener!!.onContainerDragging(percentX, percentY)
        }
    }

    private fun updateTranslation(event: MotionEvent) {
        ViewCompat.setTranslationX(this, viewOriginX + event.rawX - motionOriginX)
        ViewCompat.setTranslationY(this, viewOriginY + event.rawY - motionOriginY)
    }

    private fun updateRotation() {
        ViewCompat.setRotation(this, percentX * 20)
    }

    private fun updateAlpha() {
        val percentX = percentX
        val percentY = percentY
        if (abs(percentX) > Math.abs(percentY)) {
            if (percentX < 0) {
                showLeftOverlay()
            } else {
                showRightOverlay()
            }
            setOverlayAlpha(Math.abs(percentX))
        } else {
            if (percentY < 0) {
                showTopOverlay()
            } else {
                showBottomOverlay()
            }
            setOverlayAlpha(Math.abs(percentY))
        }
    }

    private fun moveToOrigin() {
        animate().translationX(viewOriginX)
                .translationY(viewOriginY)
                .setDuration(300L)
                .setInterpolator(OvershootInterpolator(1.0f))
                .setListener(null)
                .start()
    }

    fun setContainerEventListener(listener: ContainerEventListener?) {
        containerEventListener = listener
        viewOriginX = ViewCompat.getTranslationX(this)
        viewOriginY = ViewCompat.getTranslationY(this)
    }

    fun setCardStackOption(option: CardStackOption?) {
        this.option = option
    }

    fun setDraggable(isDraggable: Boolean) {
        this.isDraggable = isDraggable
    }

    fun reset() {
        ViewCompat.setAlpha(contentContainer, 1f)
        ViewCompat.setAlpha(overlayContainer, 0f)
    }

    fun setOverlay(left: Int, right: Int, bottom: Int, top: Int) {
        if (leftOverlayView != null) {
            overlayContainer!!.removeView(leftOverlayView)
        }
        if (left != 0) {
            leftOverlayView =
                    LayoutInflater.from(context).inflate(left, overlayContainer, false)
            overlayContainer!!.addView(leftOverlayView)
            ViewCompat.setAlpha(leftOverlayView, 0f)
        }
        if (rightOverlayView != null) {
            overlayContainer!!.removeView(rightOverlayView)
        }
        if (right != 0) {
            rightOverlayView =
                    LayoutInflater.from(context).inflate(right, overlayContainer, false)
            overlayContainer!!.addView(rightOverlayView)
            ViewCompat.setAlpha(rightOverlayView, 0f)
        }
        if (bottomOverlayView != null) {
            overlayContainer!!.removeView(bottomOverlayView)
        }
        if (bottom != 0) {
            bottomOverlayView =
                    LayoutInflater.from(context).inflate(bottom, overlayContainer, false)
            overlayContainer!!.addView(bottomOverlayView)
            ViewCompat.setAlpha(bottomOverlayView, 0f)
        }
        if (topOverlayView != null) {
            overlayContainer!!.removeView(topOverlayView)
        }
        if (top != 0) {
            topOverlayView = LayoutInflater.from(context).inflate(top, overlayContainer, false)
            overlayContainer!!.addView(topOverlayView)
            ViewCompat.setAlpha(topOverlayView, 0f)
        }
    }

    fun setOverlayAlpha(alpha: Float) {
        ViewCompat.setAlpha(overlayContainer, alpha)
    }

    fun showLeftOverlay() {
        if (leftOverlayView != null) {
            ViewCompat.setAlpha(leftOverlayView, 1f)
        }
        if (rightOverlayView != null) {
            ViewCompat.setAlpha(rightOverlayView, 0f)
        }
        if (bottomOverlayView != null) {
            ViewCompat.setAlpha(bottomOverlayView, 0f)
        }
        if (topOverlayView != null) {
            ViewCompat.setAlpha(topOverlayView, 0f)
        }
    }

    fun showRightOverlay() {
        if (leftOverlayView != null) {
            ViewCompat.setAlpha(leftOverlayView, 0f)
        }
        if (bottomOverlayView != null) {
            ViewCompat.setAlpha(bottomOverlayView, 0f)
        }
        if (topOverlayView != null) {
            ViewCompat.setAlpha(topOverlayView, 0f)
        }
        if (rightOverlayView != null) {
            ViewCompat.setAlpha(rightOverlayView, 1f)
        }
    }

    fun showBottomOverlay() {
        if (leftOverlayView != null) {
            ViewCompat.setAlpha(leftOverlayView, 0f)
        }
        if (bottomOverlayView != null) {
            ViewCompat.setAlpha(bottomOverlayView, 1f)
        }
        if (topOverlayView != null) {
            ViewCompat.setAlpha(topOverlayView, 0f)
        }
        if (rightOverlayView != null) {
            ViewCompat.setAlpha(rightOverlayView, 0f)
        }
    }

    fun showTopOverlay() {
        if (leftOverlayView != null) {
            ViewCompat.setAlpha(leftOverlayView, 0f)
        }
        if (bottomOverlayView != null) {
            ViewCompat.setAlpha(bottomOverlayView, 0f)
        }
        if (topOverlayView != null) {
            ViewCompat.setAlpha(topOverlayView, 1f)
        }
        if (rightOverlayView != null) {
            ViewCompat.setAlpha(rightOverlayView, 0f)
        }
    }

    val percentX: Float
        get() {
            var percent =
                    2f * (ViewCompat.getTranslationX(this) - viewOriginX) / width
            if (percent > 1) {
                percent = 1f
            }
            if (percent < -1) {
                percent = -1f
            }
            return percent
        }

    val percentY: Float
        get() {
            var percent =
                    2f * (ViewCompat.getTranslationY(this) - viewOriginY) / height
            if (percent > 1) {
                percent = 1f
            }
            if (percent < -1) {
                percent = -1f
            }
            return percent
        }
}