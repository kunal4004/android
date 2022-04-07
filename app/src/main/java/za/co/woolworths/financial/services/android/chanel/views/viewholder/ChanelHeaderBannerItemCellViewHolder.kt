package za.co.woolworths.financial.services.android.chanel.views.viewholder

import android.text.Html
import android.text.TextUtils
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.chanel_products_horizontal_item_cell.view.*
import kotlinx.android.synthetic.main.product_listing_page_row.view.*
import kotlinx.android.synthetic.main.product_listing_price_layout.view.*
import kotlinx.android.synthetic.main.product_listing_promotional_images.view.*
import za.co.woolworths.financial.services.android.chanel.views.ChanelNavigationClickListener

import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.dto.PromotionImages
import za.co.woolworths.financial.services.android.ui.adapters.holder.PriceItem
import za.co.woolworths.financial.services.android.ui.vto.utils.VirtualTryOnUtil
import za.co.woolworths.financial.services.android.util.ImageManager


class ChanelHeaderBannerItemCellViewHolder(itemView: View) :
    RecyclerView.ViewHolder(itemView) {

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
            priceItem.setPrice(productList, itemView)
            setProductVariant(this)
            itemView.imQuickShopAddToCartIcon.visibility = View.GONE
            setOnClickListener(chanelNavigationClickListener, this, bannerLabel, bannerImage)
        }
    }

    private fun setBrandHeaderDescriptionText(productList: ProductList?) = with(itemView) {
        if(TextUtils.isEmpty(productList?.brandHeaderDescription)){
            tvRangeName?.visibility = GONE
        } else {
            tvRangeName?.visibility = VISIBLE
            tvRangeName?.text = productList?.brandHeaderDescription
        }
    }

    private fun setProductImage(productList: ProductList) {
        val productImageUrl = productList.externalImageRefV2 ?: ""
        ImageManager.setPicture(itemView.row_layout.imProductImage, productImageUrl + if (productImageUrl.indexOf("?") > 0) "w=300&q=85" else "?w=300&q=85")
    }

    private fun setPromotionalImage(imPromo: PromotionImages?, virtualTryOn : String?) {
        with(itemView) {
            measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

            ImageManager.apply {
                setPictureOverrideWidthHeight(row_layout.imReducedImage, imPromo?.reduced ?: "")
                setPictureWithoutPlaceHolder(row_layout.imFreeGiftImage, imPromo?.freeGift ?: "")
                setPictureOverrideWidthHeight(row_layout.imSave, imPromo?.save ?: "")
                setPictureWithoutPlaceHolder(row_layout.imReward, imPromo?.wRewards ?: "")
                setPictureWithoutPlaceHolder(row_layout.imVitality, imPromo?.vitality ?: "")
                setPictureWithoutPlaceHolder(row_layout.imNewImage, imPromo?.newImage ?: "")
                if (VirtualTryOnUtil.isVtoConfigAvailable()) {
                    setPictureWithoutPlaceHolder(row_layout.imgTryItOn, virtualTryOn ?: "")
                }
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
            productList?.promotionsList!!.forEachIndexed { i, it ->
                var editedPromotionalText: String? = it.promotionalText
                if (it.promotionalText?.contains(":") == true) {
                    val beforeColon: String? = it.promotionalText?.substringBefore(":")
                    val afterColon: String? = it.promotionalText?.substringAfter(":")
                    editedPromotionalText = "<b>" + beforeColon + ":" + "</b>" + afterColon
                }
                when (i) {
                    0 -> {
                        onlinePromotionalTextView1?.visibility = View.VISIBLE
                        val promotionsListCount = productList?.promotionsList!!.size
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

    private fun setOnClickListener(
        chanelNavigationClickListener: ChanelNavigationClickListener,
        productList: ProductList,
        bannerLabel: String?,
        bannerImage: String?
    ) {
        itemView.setOnClickListener { chanelNavigationClickListener.openProductDetailsView(productList, bannerLabel, bannerImage) }
    }

}