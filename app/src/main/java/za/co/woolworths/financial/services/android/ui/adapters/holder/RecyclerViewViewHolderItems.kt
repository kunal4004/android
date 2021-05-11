package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.graphics.Color
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.product_details_fragment.*
import kotlinx.android.synthetic.main.product_listing_page_row.view.*
import kotlinx.android.synthetic.main.product_listing_price_layout.view.*
import kotlinx.android.synthetic.main.product_listing_promotional_images.view.*
import za.co.woolworths.financial.services.android.contracts.IProductListing
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.dto.PromotionImages
import za.co.woolworths.financial.services.android.util.ImageManager
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils


class RecyclerViewViewHolderItems(parent: ViewGroup) : RecyclerViewViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.product_listing_page_row, parent, false)) {

    fun setProductItem(productList: ProductList, navigator: IProductListing, nextProduct: ProductList? = null, previousProduct: ProductList? = null) {
        with(productList) {
            setProductImage(this)
            setPromotionalImage(promotionImages)
            setProductName(this)
            setBrandText(this, nextProduct, previousProduct)
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
                        onlinePromotionalTextView1?.visibility = VISIBLE
                        val promotionsListCount = productList?.promotionsList.size
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
                brandNameFakeView?.visibility = if (productList?.brandText.isNullOrEmpty()) VISIBLE else GONE
            }
        }
        nextProduct?.let {
            if (productList?.brandText.isNullOrEmpty() && it.brandText.isNullOrEmpty()) {
                brandName?.visibility = GONE
            } else {
                brandName?.visibility = if (productList?.brandText.isNullOrEmpty()) GONE else VISIBLE
                brandNameFakeView?.visibility = if (productList?.brandText.isNullOrEmpty()) VISIBLE else GONE
            }
        }
    }

    private fun setPromotionalImage(imPromo: PromotionImages?) {
        with(itemView) {

            measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            val itemWidth = itemView.measuredWidth
            imReducedImage?.layoutParams?.width = (itemWidth / 2) - Utils.dp2px(8f)

            ImageManager.setPictureOverrideWidthHeight(imReducedImage, imPromo?.reduced ?: "")
            ImageManager.setPictureWithoutPlaceHolder(imFreeGiftImage, imPromo?.freeGift ?: "")
            ImageManager.setPictureOverrideWidthHeight(imSave, imPromo?.save ?: "")
            ImageManager.setPictureWithoutPlaceHolder(imReward, imPromo?.wRewards ?: "")
            ImageManager.setPictureWithoutPlaceHolder(imVitality, imPromo?.vitality ?: "")
            ImageManager.setPictureWithoutPlaceHolder(imNewImage, imPromo?.newImage ?: "")
        }
    }

    private fun setProductImage(productList: ProductList) {
        val productImageUrl = productList.externalImageRef ?: ""
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
            val quickShopDefaultValues = WoolworthsApplication.getQuickShopDefaultValues()
            val userSelectedDeliveryLocation = Utils.getPreferredDeliveryLocation()
            var defaultStoreId = ""
            if (userSelectedDeliveryLocation == null || (userSelectedDeliveryLocation.suburb?.fulfillmentStores == null && userSelectedDeliveryLocation.store?.fulfillmentStores == null) || !SessionUtilities.getInstance().isUserAuthenticated) {
                quickShopDefaultValues?.suburb?.fulfilmentTypes?.forEach { fulfillmentType ->
                    if (fulfillmentType.fulfilmentTypeId.equals(fulfilmentTypeId, ignoreCase = true)) {
                        defaultStoreId = fulfillmentType.fulfilmentStoreId.toString()
                        return@forEach
                    }
                }
            } else {
                Utils.retrieveStoreId(fulfilmentTypeId)?.let { defaultStoreId = it }
            }

            return defaultStoreId
        }
    }
}
