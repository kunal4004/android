package za.co.woolworths.financial.services.android.ui.vto.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.FloatRange
import androidx.annotation.RequiresApi
import com.perfectcorp.perfectlib.MakeupCam


class ComparisonView : View {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
    }

    private var viewMarginPercentage = 0f
    private val dividerPaint = Paint(
        Paint.ANTI_ALIAS_FLAG or
                Paint.FILTER_BITMAP_FLAG or
                Paint.LINEAR_TEXT_FLAG or
                Paint.SUBPIXEL_TEXT_FLAG
    )
    private val dividerDrawingRect = RectF()
    private val dividerHitRect = RectF()
    private var inCompareMode = false
    private var dividerPosition = 0f
    private var makeupCam: MakeupCam? = null

    fun init(makeupCam: MakeupCam?) {
        this.makeupCam = makeupCam
        setupTouchEventListener()
        dividerPaint.color = Color.BLACK
    }

    private fun enableComparisonMode(isEnabled: Boolean) {
        makeupCam?.enableComparison(isEnabled)
    }

    private fun setComparisonPosition(@FloatRange(from = 0.0, to = 1.0) position: Float) {
        makeupCam?.setComparisonPosition(position)
    }

    fun isCompareModeEnable() : Boolean = inCompareMode

    fun enterComparisonMode() {
        enableComparisonMode(true)
        setDividerPosition(DEFAULT_COMPARE_PERCENTAGE)
        inCompareMode = true
        setupViewMarginPercentage()
        setupDividerDrawingRect()
        invalidate()
    }

    private fun setDividerPosition(@FloatRange(from = 0.0, to = 1.0) pos: Float) {
        dividerPosition = pos
        setComparisonPosition(dividerPosition)
        invalidate()
    }

    fun leaveComparisonMode() {
        enableComparisonMode(false)
        inCompareMode = false
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setupViewMarginPercentage()
        setupDividerDrawingRect()
    }

    private fun setupViewMarginPercentage() {
        viewMarginPercentage = VIEW_MARGIN / width.toFloat()
    }

    private fun setupDividerDrawingRect() {
        val viewW = width.toFloat()
        val viewH = height.toFloat()
        val dividerDrawingW = 8f
        val dividerDrawingH = viewH
        val dividerDrawingLeft = viewW * dividerPosition - dividerDrawingW * 0.5f
        dividerDrawingRect[dividerDrawingLeft, 0f, dividerDrawingLeft + dividerDrawingW] =
            dividerDrawingH
        dividerHitRect[dividerDrawingRect.left - DIVIDER_HIT_PADDING, dividerDrawingRect.top, dividerDrawingRect.right + DIVIDER_HIT_PADDING] =
            dividerDrawingRect.bottom
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (inCompareMode) {
            canvas.drawRect(dividerDrawingRect, dividerPaint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchEventListener() {
        setOnTouchListener(object : OnTouchListener {
            private var touchDownX = 0f
            private var touchDownPercentage = 0f
            private var isMovingDivider = false
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (!inCompareMode) {
                    return false
                }
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        if (dividerHitRect.contains(event.x, event.y)) {
                            isMovingDivider = true
                            touchDownX = event.x
                            touchDownPercentage = dividerPosition
                            return true
                        }
                        isMovingDivider = false
                    }
                    MotionEvent.ACTION_MOVE -> if (isMovingDivider) {
                        val diffX = event.x - touchDownX
                        val viewW = width.toFloat()
                        val newPercentage = touchDownPercentage + (diffX / viewW)
                        setDividerPosition(
                            Math.min(
                                1 - viewMarginPercentage,
                                Math.max(viewMarginPercentage, newPercentage)
                            )
                        )
                        setupDividerDrawingRect()
                        return true
                    }
                }
                return false
            }
        })
    }

    companion object {
        private val TAG = "ComparisonView"
        private val DEFAULT_COMPARE_PERCENTAGE = 0.5f
        private val DIVIDER_HIT_PADDING = 21
        private val VIEW_MARGIN = 48
    }
}
