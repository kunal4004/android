package za.co.woolworths.financial.services.android.chanel.views.viewholder

import android.text.Html
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.chanel_products_horizontal_item_cell.view.*
import kotlinx.android.synthetic.main.product_listing_page_row.view.*
import kotlinx.android.synthetic.main.product_listing_price_layout.view.*
import kotlinx.android.synthetic.main.product_listing_promotional_images.view.*

import za.co.woolworths.financial.services.android.contracts.IProductListing
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.dto.PromotionImages
import za.co.woolworths.financial.services.android.ui.adapters.holder.PriceItem
import za.co.woolworths.financial.services.android.ui.vto.utils.VirtualTryOnUtil
import za.co.woolworths.financial.services.android.util.ImageManager


class ChanelHeaderBannerItemCellViewHolder(itemView: View) :
    RecyclerView.ViewHolder(itemView) {

    fun setProductItem(productList: ProductList, navigator: IProductListing) {
        with(productList) {
            setProductImage(this)
            setPromotionalImage(promotionImages,virtualTryOn)
            setProductName(this)
            setBrandText(this, null, null)
            setPromotionalText(this)
            val priceItem = PriceItem()
            priceItem.setPrice(productList, itemView)
            setProductVariant(this)
            quickShopAddToCartSwitch(this)
            setOnClickListener(navigator, this)
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

    private fun setBrandText(productList: ProductList?, nextProduct: ProductList?, previousProduct: ProductList?) = with(itemView) {
        brandName?.text = productList?.brandText ?: ""
        previousProduct?.let {
            if (productList?.brandText.isNullOrEmpty() && it.brandText.isNullOrEmpty()) {
                brandName?.visibility = View.GONE
            } else {
                brandName?.visibility = if (productList?.brandText.isNullOrEmpty()) View.GONE else View.VISIBLE
                brandNameFakeView?.visibility = if (productList?.brandText.isNullOrEmpty()) View.VISIBLE else View.GONE
            }
        }
        nextProduct?.let {
            if (productList?.brandText.isNullOrEmpty() && it.brandText.isNullOrEmpty()) {
                brandName?.visibility = View.GONE
            } else {
                brandName?.visibility = if (productList?.brandText.isNullOrEmpty()) View.GONE else View.VISIBLE
                brandNameFakeView?.visibility = if (productList?.brandText.isNullOrEmpty()) View.VISIBLE else View.GONE
            }
        }
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

    private fun quickShopAddToCartSwitch(productList: ProductList?) {
        with(itemView) {
            context?.apply {
                productList?.apply {
                    row_layout.imQuickShopAddToCartIcon?.visibility = if (productType.equals(getString(
                            R.string.food_product_type), ignoreCase = true)) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun setOnClickListener(navigator: IProductListing, productList: ProductList) {
        itemView.setOnClickListener { navigator.openProductDetailView(productList) }
    }

}