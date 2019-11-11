package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.product_listing_page_row.view.*
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.util.WFormatter

class PriceItem {

    fun setPrice(productList: ProductList?, itemView: View, shopFromItem: Boolean = false) {
        val wasPrice: String? = productList?.wasPrice?.toString() ?: ""
        val price: String? = productList?.price?.toString() ?: ""
        val kilogramPrice: String = productList?.kilogramPrice?.toString() ?: ""
        val priceType = productList?.priceType
        if (productList?.productName?.toLowerCase()?.contains("skinless chicken breast avg 400g") == true) {
            Log.e("productListValue", "productListValue")
        }
        with(itemView) {
            fromPriceLabelTextView?.text = ""
            if (wasPrice.isNullOrEmpty()) {
                if (price!!.isEmpty()) {
                    tvPrice?.text = ""
                } else {
                    tvPrice?.text = getMassPrice(price, priceType, kilogramPrice, shopFromItem)
                }
                tvPrice?.setTextColor(android.graphics.Color.BLACK)
                tvWasPrice?.text = ""
                fromPriceLabelTextView?.visibility = View.GONE
                fromPriceStrikeThrough?.visibility = View.GONE
                if (shopFromItem && kilogramPrice.isEmpty()) {
                    tvPrice?.text = ""
                    tvWasPrice?.text = getMassPrice(price, priceType, kilogramPrice, shopFromItem)
                    tvWasPrice?.setTextColor(android.graphics.Color.BLACK)
                }
            } else {
                if (wasPrice.equals(price, ignoreCase = true)) {
                    if (price!!.isEmpty()) {
                        tvPrice?.text = WFormatter.formatAmount(wasPrice)
                    } else {
                        tvPrice?.text = getMassPrice(price, priceType, kilogramPrice, shopFromItem)
                    }
                    tvPrice?.setTextColor(android.graphics.Color.BLACK)
                    fromPriceLabelTextView?.visibility = View.GONE
                    fromPriceStrikeThrough?.visibility = View.GONE
                    tvWasPrice?.text = ""
                } else {
                    tvPrice?.text = WFormatter.formatAmount(price)
                    tvPrice?.setTextColor(androidx.core.content.ContextCompat.getColor(za.co.woolworths.financial.services.android.models.WoolworthsApplication.getAppContext(), com.awfs.coordination.R.color.was_price_color))
                    wasPrice.let {
                        tvWasPrice?.text = getMassPrice(it, priceType, kilogramPrice, shopFromItem)
                        fromPriceLabelTextView?.visibility = View.GONE
                        fromPriceStrikeThrough?.visibility = View.VISIBLE
                        tvWasPrice?.setTextColor(android.graphics.Color.BLACK)
                    }
                }
            }
            showFromPriceLabel(priceType)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun getMassPrice(price: String, priceType: String?, kilogramPrice: String, shopFromItem: Boolean = false): String {
        return with(priceType) {
            when {
                isNullOrEmpty() -> WFormatter.formatAmount(price)
                this!!.toLowerCase().contains("from", true) -> WFormatter.formatAmount(price)
                this.contains("Kilogram", true) -> {
                    if (shopFromItem) {
                        WFormatter.formatAmount(price) + "\n(" + WFormatter.formatAmount(kilogramPrice) + "/kg)"
                    } else {
                        WFormatter.formatAmount(price) + " (" + WFormatter.formatAmount(kilogramPrice) + "/kg)"
                    }
                }
                else -> WFormatter.formatAmount(price)
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun View.showFromPriceLabel(priceType: String?) {
        priceType?.let {
            if (it.toLowerCase().contains("from", true)) {
                fromPriceLabelTextView?.visibility = View.VISIBLE
                fromPriceLabelTextView?.text = "From " // add space on StrikeThrough only
            } else {
                fromPriceLabelTextView?.visibility = View.GONE
                fromPriceLabelTextView?.text = ""
            }
        }
    }
}