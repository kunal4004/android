package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.product_listing_page_row.view.*
import kotlinx.android.synthetic.main.product_listing_price_layout.view.*
import kotlinx.android.synthetic.main.product_listing_promotional_images.view.*
import za.co.woolworths.financial.services.android.contracts.IProductListing
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.dto.PromotionImages
import za.co.woolworths.financial.services.android.ui.extension.getFuturaMediumFont
import za.co.woolworths.financial.services.android.ui.extension.getFuturaSemiBoldFont
import za.co.woolworths.financial.services.android.ui.extension.measuredWidth
import za.co.woolworths.financial.services.android.ui.extension.width
import za.co.woolworths.financial.services.android.util.ImageManager
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils

class RecyclerViewViewHolderItems(parent: ViewGroup) : RecyclerViewViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.product_listing_page_row, parent, false)) {

    fun setProductItem(productList: ProductList, navigator: IProductListing, nextProduct: ProductList? = null, previousProduct: ProductList? = null) {
        with(productList) {
            setProductImage(this)
            setPromotionalImage(promotionImages)
            setSaveTextFontFamily(promotionImages)
            setProductName(this)
            setSaveText(this, nextProduct, previousProduct)
            setBrandText(this, nextProduct, previousProduct)
            val priceItem = PriceItem()
            priceItem.setPrice(productList, itemView)
            quickShopAddToCartSwitch(this)
            setOnClickListener(navigator, this)
        }
    }

    private fun setSaveTextFontFamily(promotionImages: PromotionImages?) {
        itemView.tvSaveText?.typeface = if (TextUtils.isEmpty(promotionImages?.reduced)) getFuturaMediumFont() else getFuturaSemiBoldFont()
    }

    private fun setOnClickListener(navigator: IProductListing, productList: ProductList) {
        itemView.setOnClickListener { navigator.openProductDetailView(productList) }
    }

    private fun setProductName(productList: ProductList?) = with(itemView) {
        tvProductName?.text = productList?.productName ?: ""
    }

    private fun setSaveText(productList: ProductList?, nextProduct: ProductList?, previousProduct: ProductList?) = with(itemView) {
        tvSaveText?.text = productList?.saveText ?: ""
        previousProduct?.let {
            tvSaveText.visibility = if (productList?.saveText.isNullOrEmpty() && it.saveText.isNullOrEmpty()) GONE else VISIBLE
        }
        nextProduct?.let {
            tvSaveText.visibility = if (productList?.saveText.isNullOrEmpty() && it.saveText.isNullOrEmpty()) GONE else VISIBLE
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
            imProductImage.width { width ->
                val reducedPlaceHolderWidth: Int = width / 2
                imReducedImage?.layoutParams?.width = reducedPlaceHolderWidth

            }

            imProductImage.measuredWidth { measuredWidth ->
                val reducedPlaceHolderWidth: Int = measuredWidth / 2
                imReducedImage?.layoutParams?.width = reducedPlaceHolderWidth
            }

            ImageManager.setPictureOverrideWidthHeight(imReducedImage, imPromo?.reduced ?: "")
            ImageManager.setPictureWithoutPlaceHolder(imFreeGiftImage, imPromo?.freeGift ?: "")
            ImageManager.setPictureWithoutPlaceHolder(imSave, imPromo?.save ?: "")
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
        fun getFulFillmentStoreId(fulfilmentTypeId: String): String? {
            val quickShopDefaultValues = WoolworthsApplication.getQuickShopDefaultValues()
            val userSelectedDeliveryLocation = Utils.getPreferredDeliveryLocation()
            var defaultStoreId = ""
            if (userSelectedDeliveryLocation == null || userSelectedDeliveryLocation.suburb?.fulfillmentStores == null || !SessionUtilities.getInstance().isUserAuthenticated) {
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
