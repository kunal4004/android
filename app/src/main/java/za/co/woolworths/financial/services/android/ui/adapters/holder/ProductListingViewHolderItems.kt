package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.product_listing_page_row.view.*
import za.co.woolworths.financial.services.android.contracts.IProductListing
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.dto.PromotionImages
import za.co.woolworths.financial.services.android.util.ImageManager
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.WFormatter
import java.util.*

class ProductListingViewHolderItems(parent: ViewGroup) : ProductListingViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.product_listing_page_row, parent, false)) {

    fun setProductItem(productList: ProductList, navigator: IProductListing) {
        with(productList) {
            setProductImage(this)
            setPromotionalImage(promotionImages)
            setProductName(this)
            setSaveText(this)
            setPrice(this)
            quickShopAddToCartSwitch(this)
            setOnClickListener(navigator, this)
        }
    }

    private fun setOnClickListener(navigator: IProductListing, productList: ProductList) {
        itemView.setOnClickListener { navigator.openProductDetailView(productList) }
    }

    private fun setProductName(productList: ProductList?) = with(itemView) {
        tvProductName?.text = productList?.productName ?: ""
    }

    private fun setSaveText(productList: ProductList?) = with(itemView) {
        tvSaveText?.text = productList?.saveText ?: ""
    }

    private fun setPromotionalImage(imPromo: PromotionImages?) {
        ImageManager.setPictureWithoutPlaceHolder(itemView.imSave, imPromo?.save ?: "")
        ImageManager.setPictureWithoutPlaceHolder(itemView.imReward, imPromo?.wRewards ?: "")
        ImageManager.setPictureWithoutPlaceHolder(itemView.imVitality, imPromo?.vitality ?: "")
        ImageManager.setPictureWithoutPlaceHolder(itemView.imNewImage, imPromo?.newImage ?: "")
    }

    private fun setProductImage(productList: ProductList) {
        val productImageUrl = productList.externalImageRef ?: ""
        ImageManager.setPicture(itemView.imProductImage, productImageUrl + if (productImageUrl.indexOf("?") > 0) "w=300&q=85" else "?w=300&q=85")
    }

    private fun setPrice(productList: ProductList?) {
        val wasPrice: String? = productList?.wasPrice?.toString() ?: ""
        val price: String? = productList?.price?.toString() ?: ""
        val kilogramPrice: String = productList?.kilogramPrice?.toString() ?: ""


        with(itemView) {
            if (wasPrice.isNullOrEmpty()) {
                tvPrice.text = if (price!!.isEmpty()) "" else getMassPrice(price, productList?.priceType, kilogramPrice)
                tvPrice.setTextColor(Color.BLACK)
                tvWasPrice.text = ""
                fromPriceStrikeThrough.visibility = GONE
            } else {
                if (wasPrice.equals(price, ignoreCase = true)) {
                    tvPrice.text = if (price!!.isEmpty()) WFormatter.formatAmount(wasPrice) else getMassPrice(price, productList?.priceType, kilogramPrice)
                    tvPrice.setTextColor(Color.BLACK)
                    fromPriceStrikeThrough.visibility = GONE
                    tvWasPrice.text = ""
                } else {
                    tvPrice.text = WFormatter.formatAmount(price)
                    tvPrice.setTextColor(ContextCompat.getColor(WoolworthsApplication.getAppContext(), R.color.was_price_color))
                    tvWasPrice.text = wasPrice?.let { getMassPrice(it, productList?.priceType, kilogramPrice) }
                    fromPriceStrikeThrough.visibility = VISIBLE
                    tvWasPrice.setTextColor(Color.BLACK)
                }
            }
        }
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



    private fun getMassPrice(price: String, priceType: String?, kilogramPrice: String): String {
        return with(priceType) {
            when {
                isNullOrEmpty() -> {
                    WFormatter.formatAmount(price)
                }
                this!!.contains("from", true) -> {
                    "From " + WFormatter.formatAmount(price)
                }
                this.contains("Kilogram", true) -> {
                    WFormatter.formatAmount(price)+" ("+WFormatter.formatAmount(kilogramPrice)+"/kg)"
                }
                else -> WFormatter.formatAmount(price)
            }
        }
    }

    companion object {
        // Extracting the fulfilmentStoreId from user location or default MC config
        fun getFulFillmentStoreId(): String? {
            val quickShopDefaultValues = WoolworthsApplication.getQuickShopDefaultValues()
            val userSelectedDeliveryLocation = Utils.getPreferredDeliveryLocation()
            val foodFulfilmentTypeId = quickShopDefaultValues?.foodFulfilmentTypeId?.toString()
            var defaultStoreId = ""
            if (userSelectedDeliveryLocation == null || userSelectedDeliveryLocation.suburb?.fulfillmentStores == null) {
                quickShopDefaultValues?.suburb?.fulfilmentTypes?.forEach { fulfillmentType ->
                    if (fulfillmentType.fulfilmentTypeId.toString().equals(foodFulfilmentTypeId, ignoreCase = true)) {
                        defaultStoreId = fulfillmentType.fulfilmentStoreId.toString()
                        return@forEach
                    }
                }
            } else {
                Utils.retrieveStoreId(foodFulfilmentTypeId)?.let { defaultStoreId = it }
            }

            return defaultStoreId
        }
    }

}