package za.co.woolworths.financial.services.android.ui.views.switcher

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.awfs.coordination.R

class SwitchCompatWidget : SwitchCompat {

    companion object {

        val TRACK_STROKE_WIDTH = 2f.dp2Px.toInt()
        const val TRACK_LABEL_COLOR = 0xFFFFFFFF.toInt()
        val TRACK_LABEL_SIZE = 0f.sp2Px

        const val THUMB_LABEL_COLOR = 0xFFFFFFFF.toInt()
        val THUMB_LABEL_SIZE = 0f.sp2Px

        fun drawLabel(
            canvas: Canvas,
            bounds: Rect,
            paint: Paint,
            text: CharSequence?
        ) {
            text ?: return

            val tb = RectF()
            tb.right = paint.measureText(text, 0, text.length)
            tb.bottom = paint.descent() - paint.ascent()
            tb.left += bounds.centerX() - tb.centerX()
            tb.top += bounds.centerY() - tb.centerY() - paint.ascent()

            canvas.drawText(text.toString(), tb.left, tb.top, paint)
        }

        private inline val Float.sp2Px
            get() = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                this,
                Resources.getSystem().displayMetrics
            )

        private inline val Float.dp2Px
            get() = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                this,
                Resources.getSystem().displayMetrics
            )
    }

    private val trackLabelPaint = Paint().apply {
        isAntiAlias = true
        textSize = TRACK_LABEL_SIZE
        color = TRACK_LABEL_COLOR
    }

    private val thumbLabelPaint = Paint().apply {
        isAntiAlias = true
        textSize = THUMB_LABEL_SIZE
        color = THUMB_LABEL_COLOR
    }

    private val thumbLabel
        get() = if (isChecked) textOn else textOff

    private val textOffBounds = Rect()
    private val textOnBounds = Rect()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        background = null
        trackDrawable = TrackDrawable()
        thumbDrawable = ThumbDrawable()
    }

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int
    ) {
        super.onSizeChanged(w, h, oldw, oldh)

        (trackDrawable as GradientDrawable).setSize(w, h)
        (thumbDrawable as GradientDrawable).setSize(w / 2, h)
    }

    inner class TrackDrawable : GradientDrawable() {

        private val trackCheckStateColorList = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()), intArrayOf(
                ContextCompat.getColor(context, R.color.black),
                ContextCompat.getColor(context, R.color.gray)
            )
        )

        private val trackStrokeColorList = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()), intArrayOf(
                ContextCompat.getColor(context, R.color.black),
                ContextCompat.getColor(context, R.color.gray)
            )
        )


        init {
            color = trackCheckStateColorList
            setStroke(TRACK_STROKE_WIDTH, trackStrokeColorList)
        }

        override fun onBoundsChange(r: Rect) {
            super.onBoundsChange(r)

            cornerRadius = r.height() / 2f

            textOffBounds.set(r)
            textOffBounds.right /= 2

            textOnBounds.set(textOffBounds)
            textOnBounds.offset(textOffBounds.right, 0)
        }

        override fun draw(canvas: Canvas) {
            super.draw(canvas)
            drawLabel(canvas, textOffBounds, trackLabelPaint, textOff)
            drawLabel(canvas, textOnBounds, trackLabelPaint, textOn)
        }
    }

    inner class ThumbDrawable : GradientDrawable() {

        private val thumbCheckStateColorList = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()), intArrayOf(
                ContextCompat.getColor(context, R.color.white),
                ContextCompat.getColor(context, R.color.white)
            )
        )

        private val thumbLabelBounds = Rect()

        init {
            //Set color for selected items background
            color = thumbCheckStateColorList
            setStroke(TRACK_STROKE_WIDTH, thumbCheckStateColorList)
        }

        override fun onBoundsChange(r: Rect) {
            super.onBoundsChange(r)

            setupPadding(r)
            cornerRadius = (r.height() / 2f) + 10f.dp2Px.toInt()
            thumbLabelBounds.set(r)
        }

        private fun setupPadding(r: Rect) {
            val padding = 3f.dp2Px.toInt()
            r.top += padding
            r.right -= padding
            r.bottom -= padding
            r.left += padding
        }

        override fun draw(canvas: Canvas) {
            super.draw(canvas)

            drawLabel(canvas, thumbLabelBounds, thumbLabelPaint, thumbLabel)
            invalidate()
            requestLayout()
        }
    }
}