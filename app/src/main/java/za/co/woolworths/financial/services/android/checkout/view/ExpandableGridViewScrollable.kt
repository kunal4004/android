package za.co.woolworths.financial.services.android.checkout.view

import android.content.Context
import android.util.AttributeSet
import android.widget.GridView

/**
 * Created by Kunal Uttarwar on 25/07/21.
 */
class ExpandableGridViewScrollable @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : GridView(context, attrs, defStyleAttr) {
    var expanded = false

    fun isExpanded(): Boolean {
        return expanded
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (isExpanded()) {
            // Calculate entire height by providing a very large height hint.
            // View.MEASURED_SIZE_MASK represents the largest height possible.
            val expandSpec = MeasureSpec.makeMeasureSpec(
                MEASURED_SIZE_MASK,
                MeasureSpec.AT_MOST
            )
            super.onMeasure(widthMeasureSpec, expandSpec)
            val params = layoutParams
            params.height = measuredHeight
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    fun setViewExpanded(expanded: Boolean) {
        this.expanded = expanded
    }
}