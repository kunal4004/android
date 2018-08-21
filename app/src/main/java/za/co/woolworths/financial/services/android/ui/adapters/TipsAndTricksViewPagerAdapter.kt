package za.co.woolworths.financial.services.android.ui.adapters

import android.app.Activity
import android.content.res.TypedArray
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.awfs.coordination.R
import kotlin.properties.Delegates

class TipsAndTricksViewPagerAdapter(context: Activity) : PagerAdapter() {

    var mContext: Activity by Delegates.notNull();
    var images: TypedArray by Delegates.notNull()

    init {
        this.mContext = context
        this.images = mContext.resources.obtainTypedArray(R.array.tips_tricks_images)
    }

    override fun getCount(): Int {
        return images.length()
    }

    override fun isViewFromObject(view: View?, `object`: Any?): Boolean {
        // Return the current view
        return view === `object` as View
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(mContext).inflate(R.layout.tips_and_trics_viewpager_item, container, false)
        val imageView: ImageView = view.findViewById(R.id.tipsAndTrickImage)
        imageView.setBackgroundResource(images.getResourceId(position, -1))
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any?) {
        container.removeView(`object` as View)
    }

}