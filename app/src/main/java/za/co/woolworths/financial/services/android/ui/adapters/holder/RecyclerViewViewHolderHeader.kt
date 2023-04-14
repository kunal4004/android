package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.fragment.app.FragmentActivity
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ItemFoundLayoutBinding
import za.co.woolworths.financial.services.android.contracts.IProductListing
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.util.ImageManager

class RecyclerViewViewHolderHeader(val itemBinding: ItemFoundLayoutBinding) : RecyclerViewViewHolder(itemBinding.root) {
    fun setNumberOfItems(activity: FragmentActivity?, productList: ProductList?) {

        when (productList?.numberOfItems) {
            1 -> productList?.numberOfItems?.toString()?.let { numberOfItems ->
                itemBinding.tvNumberOfItem.text = numberOfItems; itemBinding.tvFoundItem.text =
                activity?.getString(R.string.product_item)
            }
            else -> productList?.numberOfItems?.toString()?.let { numberOfItems ->
                itemBinding.tvNumberOfItem.text = numberOfItems
            }
        }
    }

    fun setChanelBanner(mBannerLabel: String?, mBannerImage: String?, mIsComingFromBLP: Boolean, navigator: IProductListing?) {
        if (mIsComingFromBLP) {
            itemBinding.viewPlpSeperator.root.visibility = View.VISIBLE
            if (mBannerImage?.isNullOrEmpty() == true) {
                itemBinding.chanelLogoHeader?.root?.visibility = View.VISIBLE
                itemBinding.chanelLogoHeader?.tvLogoName?.text = mBannerLabel
                itemBinding.chanelLogoHeader?.root?.setOnClickListener {

                    navigator?.openBrandLandingPage()
                }
            } else {
                itemBinding.chanelImgBanner.visibility = View.VISIBLE
                ImageManager.loadImage(itemBinding.chanelImgBanner, mBannerImage)
                itemBinding.chanelImgBanner?.setOnClickListener {
                    navigator?.openBrandLandingPage()
                }
            }
        }
    }

    fun setPromotionalBanner(promotionalRichText: String?) {
        if(!promotionalRichText.isNullOrEmpty()) {
            itemBinding.promotionalTextBannerLayout.root.visibility = View.VISIBLE
            itemBinding.promotionalTextBannerLayout.apply {
                promotionalTextDesc.text =
                    HtmlCompat.fromHtml(
                        promotionalRichText.toString(),
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                promotionalTextDesc.movementMethod = LinkMovementMethod.getInstance()
            }
        }
    }
}