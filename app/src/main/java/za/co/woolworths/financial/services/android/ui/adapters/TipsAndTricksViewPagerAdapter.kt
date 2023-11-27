package za.co.woolworths.financial.services.android.ui.adapters

import android.app.Activity
import android.content.res.TypedArray
import androidx.viewpager.widget.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.awfs.coordination.R

class TipsAndTricksViewPagerAdapter(context: Activity) : PagerAdapter() {

    var mContext: Activity = context
    var images: TypedArray = context.resources.obtainTypedArray(R.array.tips_tricks_images)

    override fun getCount(): Int {
        return images.length()
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        // Return the current view
        return view === `object` as View
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(mContext).inflate(R.layout.tips_and_trics_viewpager_item, container, false)
        val imageView: ImageView = view.findViewById(R.id.tipsAndTrickImage)
        imageView.contentDescription = mContext.getString(R.string.tips_and_tricks_image)
        imageView.setBackgroundResource(images.getResourceId(position, -1))
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

}