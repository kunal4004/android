package za.co.woolworths.financial.services.android.ui.adapters

import android.content.Context
import za.co.woolworths.financial.services.android.util.ImageManager.Companion.setPicture
import androidx.viewpager.widget.PagerAdapter
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.awfs.coordination.R
import java.util.ArrayList

class ProductViewPagerAdapter(
    private val mContext: Context,
    private var mExternalImageRefList: List<String>?,
    private val multipleImageInterface: MultipleImageInterface
) : PagerAdapter() {
    interface MultipleImageInterface {
        fun SelectedImage(otherSkus: String?)
    }

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(mContext)
        val view = inflater.inflate(R.layout.product_view, collection, false) as ViewGroup
        val image = mExternalImageRefList?.getOrNull(position)
        val mProductImage = view.findViewById<ImageView>(R.id.imProductView)
        setPicture(mProductImage, image)
        collection.addView(view, 0)
        view.setOnClickListener { v1: View? ->
            multipleImageInterface.SelectedImage(
                mExternalImageRefList?.getOrNull(position)
            )
        }
        return view
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as? View)
    }

    override fun getCount(): Int {
        return mExternalImageRefList?.size?:0
    }

    override fun isViewFromObject(view: View, any: Any): Boolean {
        return view === any
    }

    fun updatePagerItems(mAuxiliaryImage: List<String>) {
        mExternalImageRefList = ArrayList()
        notifyDataSetChanged()
        mExternalImageRefList = mAuxiliaryImage
        notifyDataSetChanged()
    }
}