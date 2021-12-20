package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.Normal
import za.co.woolworths.financial.services.android.util.ImageManager.Companion.setPicture

class ProductReviewViewPagerAdapter(var context: Context?, var imagesList: List<Normal>)
    : PagerAdapter() {

    override fun getCount(): Int {
        return imagesList.size
    }

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.product_review, collection, false) as ViewGroup
        val image: String = imagesList.get(position).url
        val mProductImage = view.findViewById<ImageView>(R.id.imReviewProductView)
        setPicture(mProductImage, image)
        collection.addView(view, 0)
        return view
    }

    override fun isViewFromObject(view: View, pageView: Any): Boolean {
        return  view == pageView
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View?)
    }
}