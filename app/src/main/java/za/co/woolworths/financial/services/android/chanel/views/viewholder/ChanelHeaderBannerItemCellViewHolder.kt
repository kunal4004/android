package za.co.woolworths.financial.services.android.chanel.views.viewholder

import android.text.Html
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.chanel_products_horizontal_item_cell.view.*
import kotlinx.android.synthetic.main.product_listing_page_row.view.*
import kotlinx.android.synthetic.main.product_listing_price_layout.view.*
import kotlinx.android.synthetic.main.product_listing_promotional_images.view.*
import za.co.woolworths.financial.services.android.chanel.views.NavigationClickListener

import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.dto.PromotionImages
import za.co.woolworths.financial.services.android.ui.adapters.holder.PriceItem
import za.co.woolworths.financial.services.android.ui.vto.utils.VirtualTryOnUtil
import za.co.woolworths.financial.services.android.util.ImageManager


class ChanelHeaderBannerItemCellViewHolder(itemView: View) :
    RecyclerView.ViewHolder(itemView) {

    fun setProductItem(productList: ProductList, NavigationClickListener: NavigationClickListener) {
        with(productList) {
            setProductImage(this)
            setPromotionalImage(promotionImages,virtualTryOn)
            setProductName(this)
            setBrandText(this)
            setPromotionalText(this)
            val priceItem = PriceItem()
            priceItem.setPrice(productList, itemView)
            setProductVariant(this)
            itemView.imQuickShopAddToCartIcon.visibility = View.GONE
            setOnClickListener(NavigationClickListener, this)
        }
    }

    private fun setProductImage(productList: ProductList) {
        val productImageUrl = productList.externalImageRefV2 ?: ""
        ImageManager.setPicture(itemView.row_layout.imProductImage, productImageUrl + if (productImageUrl.indexOf("?") > 0) "w=300&q=85" else "?w=300&q=85")
    }

    private fun setPromotionalImage(imPromo: PromotionImages?, virtualTryOn : String?) {
        with(itemView) {
            measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

            ImageManager.setPictureOverrideWidthHeight(row_layout.imReducedImage, imPromo?.reduced ?: "")
            ImageManager.setPictureWithoutPlaceHolder(row_layout.imFreeGiftImage, imPromo?.freeGift ?: "")
            ImageManager.setPictureOverrideWidthHeight(row_layout.imSave, imPromo?.save ?: "")
            ImageManager.setPictureWithoutPlaceHolder(row_layout.imReward, imPromo?.wRewards ?: "")
            ImageManager.setPictureWithoutPlaceHolder(row_layout.imVitality, imPromo?.vitality ?: "")
            ImageManager.setPictureWithoutPlaceHolder(row_layout.imNewImage, imPromo?.newImage ?: "")
            if (VirtualTryOnUtil.isVtoConfigAvailable()) {
                ImageManager.setPictureWithoutPlaceHolder(row_layout.imgTryItOn, virtualTryOn ?: "")
            }
        }
    }

    private fun setProductName(productList: ProductList?) = with(itemView) {
        tvProductName.maxLines = 3
        tvProductName.minLines = 1
        tvProductName?.text = productList?.productName ?: ""
    }

    private fun setBrandText(productList: ProductList?) = with(itemView) {
        brandName?.text = productList?.brandText ?: ""
    }

    private fun setPromotionalText(productList: ProductList?) = with(itemView) {
        if (productList?.promotionsList?.isEmpty() == false) {
            productList?.promotionsList.forEachIndexed { i, it ->
                var editedPromotionalText: String? = it.promotionalText
                if (it.promotionalText?.contains(":") == true) {
                    val beforeColon: String? = it.promotionalText?.substringBefore(":")
                    val afterColon: String? = it.promotionalText?.substringAfter(":")
                    editedPromotionalText = "<b>" + beforeColon + ":" + "</b>" + afterColon
                }
                when (i) {
                    0 -> {
                        onlinePromotionalTextView1?.visibility = View.VISIBLE
                        val promotionsListCount = productList?.promotionsList.size
                        onlinePromotionalTextView1?.text = Html.fromHtml(editedPromotionalText)
                        if (promotionsListCount == 1) {
                            onlinePromotionalTextView1?.maxLines = 2
                            onlinePromotionalTextView2?.text = ""
                            onlinePromotionalTextView2?.visibility = View.GONE
                        }
                        else
                            onlinePromotionalTextView1?.maxLines = 1
                    }
                    1 -> {
                        onlinePromotionalTextView2?.visibility = View.VISIBLE
                        onlinePromotionalTextView2?.text = Html.fromHtml(editedPromotionalText)
                    }
                }
            }
        } else {
            onlinePromotionalTextView1?.text = ""
            onlinePromotionalTextView2?.text = ""
        }
    }

    private fun setProductVariant(productList: ProductList?) = with(itemView) {
        val productVarientName = productList?.productVariants ?: ""
        if (!TextUtils.isEmpty(productVarientName)) {
            productVariantTextView?.visibility = View.VISIBLE
            productVariantTextView?.text = productVarientName
        } else {
            productVariantTextView?.visibility = View.GONE
            productVariantTextView?.text = ""
        }
    }

    private fun setOnClickListener(navigationClickListener: NavigationClickListener,
                                   productList: ProductList) {
        itemView.setOnClickListener { navigationClickListener.openProductDetailsView(productList) }
    }

}