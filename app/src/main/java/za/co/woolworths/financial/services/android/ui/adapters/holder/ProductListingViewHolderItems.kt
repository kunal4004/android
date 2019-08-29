package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
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
import za.co.woolworths.financial.services.android.ui.views.WrapContentDraweeView
import za.co.woolworths.financial.services.android.util.DrawImage
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
        setPromotionalImage(itemView.imSave, imPromo?.save ?: "")
        setPromotionalImage(itemView.imReward, imPromo?.wRewards ?: "")
        setPromotionalImage(itemView.imVitality, imPromo?.vitality ?: "")
        setPromotionalImage(itemView.imNewImage, imPromo?.newImage ?: "")
    }

    private fun setPromotionalImage(image: WrapContentDraweeView, url: String) {
        image.visibility = if (url.isEmpty()) GONE else VISIBLE
        val drawImage = DrawImage(WoolworthsApplication.getAppContext())
        drawImage.displayImage(image, if (url.isEmpty()) Utils.getExternalImageRef() else url)
    }

    private fun setProductImage(productList: ProductList) {
        val productImageUrl = productList.externalImageRef ?: ""
        if (productImageUrl.isNotEmpty()) {
            itemView.imProductImage?.apply {
                setResizeImage(true)
                setImageURI(productImageUrl + if (productImageUrl.indexOf("?") > 0) "w=300&q=85" else "?w=300&q=85")
            }
        }
    }

    private fun setPrice(productList: ProductList?) {
        val wasPriceList: MutableList<Double> = mutableListOf()

        productList?.otherSkus?.forEach { sku -> sku?.wasPrice?.toDouble()?.apply { wasPriceList.add(this) } }

        val wasPrice: String? = if (wasPriceList.isNullOrEmpty()) "" else Collections.max(wasPriceList)?.toString()
                ?: ""
        val fromPrice: String? = productList?.fromPrice?.toString() ?: ""

        with(itemView) {
            if (wasPrice.isNullOrEmpty()) {
                tvFromPrice.text = if (fromPrice!!.isEmpty()) "" else WFormatter.formatAmount(fromPrice)
                tvFromPrice.setTextColor(Color.BLACK)
                tvWasPrice.text = ""
            } else {
                if (wasPrice.equals(fromPrice, ignoreCase = true)) {
                    tvFromPrice.text = if (fromPrice!!.isEmpty()) WFormatter.formatAmount(wasPrice) else WFormatter.formatAmount(fromPrice)
                    tvFromPrice.setTextColor(Color.BLACK)
                    tvWasPrice.text = ""
                } else {
                    tvFromPrice.text = WFormatter.formatAmount(fromPrice)
                    tvFromPrice.setTextColor(ContextCompat.getColor(WoolworthsApplication.getAppContext(), R.color.was_price_color))
                    tvWasPrice.text = WFormatter.formatAmount(wasPrice)
                    tvWasPrice.paintFlags = tvWasPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    tvWasPrice.setTextColor(Color.BLACK)
                }
            }
        }
    }

    private fun quickShopAddToCartSwitch(productList: ProductList?) {
        itemView.pbQueryInventory?.indeterminateDrawable?.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY)
        with(itemView) {
            context?.apply {
                productList?.apply {
                    with(vsQuickShoAddToCart) {
                        visibility = if (productType.equals(getString(R.string.food_product_type), ignoreCase = true)) VISIBLE else GONE
                    }
                }
            }
        }
    }

    // Extracting the fulfilmentStoreId from user location or default MC config
    fun getFulfillmentTypeId(productList: ProductList?): String? {
        val quickShopDefaultValues = WoolworthsApplication.getQuickShopDefaultValues()
        var defaultStoreId: String? = quickShopDefaultValues?.suburb?.id?.toString() ?: ""
        productList?.apply {
            val foodFulfilmentTypeId = quickShopDefaultValues.foodFulfilmentTypeId
            val fulfillmentTypeId: String? = fulfillmentType ?: foodFulfilmentTypeId.toString()
            quickShopDefaultValues.suburb.fulfilmentTypes.forEach { fulfillmentType ->
                if (fulfillmentType.fulfilmentTypeId.toString().equals(fulfillmentTypeId, ignoreCase = true)) {
                    defaultStoreId = fulfillmentType.fulfilmentStoreId.toString()
                    return@forEach
                }
            }
            val userStoreId = Utils.retrieveStoreId(fulfillmentType)
            return if (userStoreId?.isEmpty() == true) defaultStoreId else userStoreId
        }
        return defaultStoreId
    }

}