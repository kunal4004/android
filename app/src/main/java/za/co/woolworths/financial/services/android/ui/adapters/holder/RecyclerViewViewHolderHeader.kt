package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.chanel_logo_view.view.*
import kotlinx.android.synthetic.main.item_found_layout.view.*
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.util.ImageManager

class RecyclerViewViewHolderHeader(parent: ViewGroup) : RecyclerViewViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.item_found_layout, parent, false)
) {
    fun setNumberOfItems(activity: FragmentActivity?, productList: ProductList?) {

        when (productList?.numberOfItems) {
            1 -> productList?.numberOfItems?.toString()?.let { numberOfItems ->
                itemView.tvNumberOfItem.text = numberOfItems; itemView.tvFoundItem.text =
                activity?.getString(R.string.product_item)
            }
            else -> productList?.numberOfItems?.toString()?.let { numberOfItems ->
                itemView.tvNumberOfItem.text = numberOfItems
            }
        }
    }

    fun setChanelBanner(mBannerLabel: String?, mBannerImage: String?, mIsComingFromBLP: Boolean) {
        if (mIsComingFromBLP) {
            itemView.view_plp_seperator.visibility = View.VISIBLE
            if (mBannerImage?.isNullOrEmpty() == true) {
                itemView.chanel_logo_header?.visibility = View.VISIBLE
                itemView.chanel_logo_header?.tv_logo_name?.text = mBannerLabel
            } else {
                itemView.chanel_img_banner.visibility = View.VISIBLE
                ImageManager.setPicture(itemView.chanel_img_banner, mBannerImage)
            }
        }
    }
}