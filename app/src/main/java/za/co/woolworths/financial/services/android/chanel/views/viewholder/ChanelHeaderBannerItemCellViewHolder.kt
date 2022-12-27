package za.co.woolworths.financial.services.android.chanel.views.viewholder

import android.text.Html
import android.text.TextUtils
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.ChanelProductsHorizontalItemCellBinding
import za.co.woolworths.financial.services.android.chanel.views.ChanelNavigationClickListener
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.dto.PromotionImages
import za.co.woolworths.financial.services.android.ui.adapters.holder.PriceItem
import za.co.woolworths.financial.services.android.ui.vto.utils.VirtualTryOnUtil
import za.co.woolworths.financial.services.android.util.ImageManager

class ChanelHeaderBannerItemCellViewHolder(val binding: ChanelProductsHorizontalItemCellBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun setProductItem(
        productList: ProductList,
        chanelNavigationClickListener: ChanelNavigationClickListener,
        bannerLabel: String?,
        bannerImage: String?
    ) {
        with(productList) {
            setProductImage(this)
            setPromotionalImage(promotionImages,virtualTryOn)
            setProductName(this)
            setBrandText(this)
            setBrandHeaderDescriptionText(this)
            setPromotionalText(this)
            val priceItem = PriceItem()
            priceItem.setPrice(productList, binding.rowLayout.includeProductListingPriceLayout)
            setProductVariant(this)
            binding.rowLayout.includeProductListingPriceLayout.imQuickShopAddToCartIcon.visibility = GONE
            setOnClickListener(chanelNavigationClickListener, this, bannerLabel, bannerImage)
        }
    }

    private fun setBrandHeaderDescriptionText(productList: ProductList?) =
        with(binding.rowLayout.tvRangeName) {
            if (TextUtils.isEmpty(productList?.brandHeaderDescription)) {
                visibility = GONE
            } else {
                visibility = VISIBLE
                text = productList?.brandHeaderDescription
            }
        }

    private fun setProductImage(productList: ProductList) {
        val productImageUrl = productList.externalImageRefV2 ?: ""
        ImageManager.setPicture(binding.rowLayout.imProductImage, productImageUrl + if (productImageUrl.indexOf("?") > 0) "w=300&q=85" else "?w=300&q=85")
    }

    private fun setPromotionalImage(imPromo: PromotionImages?, virtualTryOn : String?) {
        with(itemView) {
            with(binding.rowLayout.productListingPromotionalImage) {
                measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

                ImageManager.apply {
                    setPictureOverrideWidthHeight(imReducedImage, imPromo?.reduced ?: "")
                    setPictureWithoutPlaceHolder(
                        imFreeGiftImage,
                        imPromo?.freeGift ?: ""
                    )
                    setPictureOverrideWidthHeight(imSave, imPromo?.save ?: "")
                    setPictureWithoutPlaceHolder(imReward, imPromo?.wRewards ?: "")
                    setPictureWithoutPlaceHolder(imVitality, imPromo?.vitality ?: "")
                    setPictureWithoutPlaceHolder(imNewImage, imPromo?.newImage ?: "")
                    if (VirtualTryOnUtil.isVtoConfigAvailable()) {
                        setPictureWithoutPlaceHolder(imgTryItOn, virtualTryOn ?: "")
                    }
                }
            }
        }
    }

    private fun setProductName(productList: ProductList?) = with(binding.rowLayout.tvProductName) {
            maxLines = 3
            minLines = 1
            text = productList?.productName ?: ""
    }

    private fun setBrandText(productList: ProductList?) = with(binding.rowLayout) {
        brandName.text = productList?.brandText ?: ""
    }

    private fun setPromotionalText(productList: ProductList?) = with(binding.rowLayout) {
        if (productList?.promotions?.isEmpty() == false) {
            productList?.promotions?.forEachIndexed { i, it ->
                var editedPromotionalText: String? = it.promotionalText
                if (it.promotionalText?.contains(":") == true) {
                    val beforeColon: String? = it.promotionalText?.substringBefore(":")
                    val afterColon: String? = it.promotionalText?.substringAfter(":")
                    editedPromotionalText = "<b>$beforeColon:</b>$afterColon"
                }
                when (i) {
                    0 -> {
                        onlinePromotionalTextView1.visibility = VISIBLE
                        val promotionsListCount = productList?.promotions?.size
                        onlinePromotionalTextView1.text = Html.fromHtml(editedPromotionalText)
                        if (promotionsListCount == 1) {
                            onlinePromotionalTextView1.maxLines = 2
                            onlinePromotionalTextView2.text = ""
                            onlinePromotionalTextView2.visibility = GONE
                        }
                        else
                            onlinePromotionalTextView1.maxLines = 1
                    }
                    1 -> {
                        onlinePromotionalTextView2.visibility = VISIBLE
                        onlinePromotionalTextView2.text = Html.fromHtml(editedPromotionalText)
                    }
                }
            }
        } else {
            onlinePromotionalTextView1.text = ""
            onlinePromotionalTextView2.text = ""
        }
    }

    private fun setProductVariant(productList: ProductList?) = with(binding.rowLayout) {
        val productVariantName = productList?.productVariants ?: ""
        if (!TextUtils.isEmpty(productVariantName)) {
            productVariantTextView.visibility = VISIBLE
            productVariantTextView.text = productVariantName
        } else {
            productVariantTextView.visibility = GONE
            productVariantTextView.text = ""
        }
    }

    private fun setOnClickListener(
        chanelNavigationClickListener: ChanelNavigationClickListener,
        productList: ProductList,
        bannerLabel: String?,
        bannerImage: String?
    ) {
        itemView.setOnClickListener { chanelNavigationClickListener.openProductDetailsView(productList, bannerLabel, bannerImage) }
    }

}