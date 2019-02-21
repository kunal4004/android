package za.co.woolworths.financial.services.android.ui.views

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.facebook.drawee.gestures.GestureDetector


class ShopViewPager : ViewPager {
    private var swipeable = true
    private val lastX = 0f
    private val lastTime: Long = 0
    private val mGestureDetector: GestureDetector? = null
    private val mScrolling = false


    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)


    fun setSwipeable(swipeable: Boolean) {
        this.swipeable = swipeable
    }

    override fun canScroll(v: View, checkV: Boolean, dx: Int, x: Int, y: Int): Boolean {
        Log.i(TAG, " boolean checkV ->$checkV, int dx-> $dx, int x->$x, int y->$y")
        return x < 300 || x > 1700
    }

    companion object {
        private val TAG = ShopViewPager::class.java.simpleName
    }
}