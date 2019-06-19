package za.co.woolworths.financial.services.android.util.numberkeyboard

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout

class SquareFrameLayout : FrameLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)

        if (widthSize == 0 && heightSize == 0) {
            // If there are no constraints on size, let FrameLayout measure
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)

            // Now use the smallest of the measured dimensions for both dimensions
            val minSize = Math.min(measuredWidth, measuredHeight)
            setMeasuredDimension(minSize, minSize)
            return
        }

        val size = if (widthSize == 0 || heightSize == 0) {
            // If one of the dimensions has no restriction on size, set both dimensions to be the
            // on that does
            Math.max(widthSize, heightSize)
        } else {
            // Both dimensions have restrictions on size, set both dimensions to be the
            // smallest of the two
            Math.min(widthSize, heightSize)
        }

        val newMeasureSpec = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY)
        super.onMeasure(newMeasureSpec, newMeasureSpec)
    }
}