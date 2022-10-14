package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.text.Html
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.product_listing_page_row.view.*
import kotlinx.android.synthetic.main.product_listing_price_layout.view.*
import kotlinx.android.synthetic.main.product_listing_promotional_images.view.*
import za.co.woolworths.financial.services.android.contracts.IProductListing
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.dto.PromotionImages
import za.co.woolworths.financial.services.android.ui.vto.utils.VirtualTryOnUtil
import za.co.woolworths.financial.services.android.util.ImageManager
import za.co.woolworths.financial.services.android.util.Utils


class RecyclerViewViewHolderItems(parent: ViewGroup) : RecyclerViewViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.product_listing_page_row, parent, false)) {

    fun setProductItem(productList: ProductList, navigator: IProductListing, nextProduct: ProductList? = null, previousProduct: ProductList? = null) {
        with(productList) {
            setProductImage(this)
            setPromotionalImage(promotionImages,virtualTryOn)
            setProductName(this)
            setBrandText(this, nextProduct, previousProduct)
            setBrandHeaderDescriptionText(this)
            setPromotionalText(this)
            val priceItem = PriceItem()
            priceItem.setPrice(productList, itemView)
            setProductVariant(this)
            quickShopAddToCartSwitch(this)
            setOnClickListener(navigator, this)
        }
    }

    private fun setOnClickListener(navigator: IProductListing, productList: ProductList) {
        itemView.setOnClickListener { navigator.openProductDetailView(productList) }
    }

    private fun setProductName(productList: ProductList?) = with(itemView) {
        tvProductName.maxLines = 3
        tvProductName.minLines = 1
        tvProductName?.text = productList?.productName ?: ""
    }

    private fun setPromotionalText(productList: ProductList?) = with(itemView) {
        if (productList?.promotions?.isEmpty() == false) {
            productList?.promotions?.forEachIndexed { i, it ->
                var editedPromotionalText: String? = it.promotionalText
                if (it.promotionalText?.contains(":") == true) {
                    val beforeColon: String? = it.promotionalText?.substringBefore(":")
                    val afterColon: String? = it.promotionalText?.substringAfter(":")
                    editedPromotionalText = "<b>" + beforeColon + ":" + "</b>" + afterColon
                }
                when (i) {
                    0 -> {
                        onlinePromotionalTextView1?.visibility = VISIBLE
                        val promotionsListCount = productList?.promotions?.size ?: 0
                        onlinePromotionalTextView1?.text = Html.fromHtml(editedPromotionalText)
                        if (promotionsListCount == 1) {
                            onlinePromotionalTextView1?.maxLines = 2
                            onlinePromotionalTextView2?.text = ""
                            onlinePromotionalTextView2?.visibility = GONE
                        }
                        else
                            onlinePromotionalTextView1?.maxLines = 1
                    }
                    1 -> {
                        onlinePromotionalTextView2?.visibility = VISIBLE
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
            productVariantTextView?.visibility = VISIBLE
            productVariantTextView?.text = productVarientName
        } else {
            productVariantTextView?.visibility = GONE
            productVariantTextView?.text = ""
        }
    }

    private fun setBrandText(productList: ProductList?, nextProduct: ProductList?, previousProduct: ProductList?) = with(itemView) {
        brandName?.text = productList?.brandText ?: ""
        previousProduct?.let {
            if (productList?.brandText.isNullOrEmpty() && it.brandText.isNullOrEmpty()) {
                brandName?.visibility = GONE
            } else {
                brandName?.visibility = if (productList?.brandText.isNullOrEmpty()) GONE else VISIBLE
            }
        }
        nextProduct?.let {
            if (productList?.brandText.isNullOrEmpty() && it.brandText.isNullOrEmpty()) {
                brandName?.visibility = GONE
            } else {
                brandName?.visibility = if (productList?.brandText.isNullOrEmpty()) GONE else VISIBLE
            }
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

    private fun setPromotionalImage(imPromo: PromotionImages?,virtualTryOn : String?) {
        with(itemView) {
            measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

            ImageManager.setPictureOverrideWidthHeight(imReducedImage, imPromo?.reduced ?: "")
            ImageManager.setPictureWithoutPlaceHolder(imFreeGiftImage, imPromo?.freeGift ?: "")
            ImageManager.setPictureOverrideWidthHeight(imSave, imPromo?.save ?: "")
            ImageManager.setPictureWithoutPlaceHolder(imReward, imPromo?.wRewards ?: "")
            ImageManager.setPictureWithoutPlaceHolder(imVitality, imPromo?.vitality ?: "")
            ImageManager.setPictureWithoutPlaceHolder(imNewImage, imPromo?.newImage ?: "")
            if (VirtualTryOnUtil.isVtoConfigAvailable()) {
                ImageManager.setPictureWithoutPlaceHolder(imgTryItOn, virtualTryOn ?: "")
            }
        }
    }

    private fun setProductImage(productList: ProductList) {
        val productImageUrl = productList.externalImageRefV2 ?: ""
        ImageManager.setPicture(itemView.imProductImage, productImageUrl + if (productImageUrl.indexOf("?") > 0) "w=300&q=85" else "?w=300&q=85")
    }

    private fun quickShopAddToCartSwitch(productList: ProductList?) {
        with(itemView) {
            context?.apply {
                productList?.apply {
                    imQuickShopAddToCartIcon?.visibility = if (productType.equals(getString(R.string.food_product_type), ignoreCase = true)) VISIBLE else GONE
                }
            }
        }
    }

    companion object {
        // Extracting the fulfilmentStoreId from user location or default MC config
        fun getFulFillmentStoreId(fulfilmentTypeId: String): String {
            var defaultStoreId = ""
            Utils.retrieveStoreId(fulfilmentTypeId)?.let { defaultStoreId = it }
            return defaultStoreId
        }
    }
}
