package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.annotation.SuppressLint
import com.awfs.coordination.databinding.LayoutCartListProductItemBinding
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.util.CurrencyFormatter

class SearchResultPriceItem {

    fun setPrice(productList: ProductList?, itemBinding: LayoutCartListProductItemBinding, shopFromItem: Boolean = false) {
        val wasPrice: String = productList?.wasPrice?.toString() ?: ""
        val price: String = productList?.price?.toString() ?: ""
        val kilogramPrice: String = productList?.kilogramPrice?.toString() ?: ""
        val priceType = productList?.priceType
        itemBinding.apply {
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
                if (shopFromItem && kilogramPrice.isEmpty()) {
                    tvPrice?.text = ""
                    tvWasPrice?.text = getMassPrice(price, priceType, kilogramPrice, shopFromItem)
                    tvWasPrice?.setTextColor(android.graphics.Color.BLACK)
                    // Remove strike through flag
                    tvWasPrice?.paintFlags =
                        tvWasPrice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
            } else {
                if (wasPrice.equals(price, ignoreCase = true)) {
                    if (price!!.isEmpty()) {
                        tvPrice?.text = CurrencyFormatter.formatAmountToRandAndCentWithSpace(wasPrice)
                    } else {
                        tvPrice?.text = getMassPrice(price, priceType, kilogramPrice, shopFromItem)
                    }
                    tvPrice?.setTextColor(android.graphics.Color.BLACK)
                    fromPriceLabelTextView?.visibility = View.GONE
                    tvWasPrice?.text = ""
                } else {
                    tvPrice?.text = CurrencyFormatter.formatAmountToRandAndCentWithSpace(price)
                    tvPrice?.setTextColor(androidx.core.content.ContextCompat.getColor(za.co.woolworths.financial.services.android.models.WoolworthsApplication.getAppContext(), com.awfs.coordination.R.color.was_price_color))
                    wasPrice.let {
                        tvWasPrice?.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                        tvWasPrice?.text = getMassPrice(it, priceType, kilogramPrice, shopFromItem)
                        fromPriceLabelTextView?.visibility = View.GONE
                        tvWasPrice?.setTextColor(android.graphics.Color.BLACK)
                    }
                }
            }
            showFromPriceLabel(itemBinding, priceType)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun getMassPrice(price: String, priceType: String?, kilogramPrice: String, shopFromItem: Boolean = false): String {
        return with(priceType) {
            when {
                isNullOrEmpty() -> CurrencyFormatter.formatAmountToRandAndCentWithSpace(price)
                this!!.toLowerCase().contains("from", true) -> CurrencyFormatter.formatAmountToRandAndCentWithSpace(price)
                this.contains("Kilogram", true) -> {
                    if (shopFromItem) {
                        CurrencyFormatter.formatAmountToRandAndCentWithSpace(price) + "\n(" + CurrencyFormatter.formatAmountToRandAndCentWithSpace(kilogramPrice) + "/kg)"
                    } else {
                        CurrencyFormatter.formatAmountToRandAndCentWithSpace(price) + " (" + CurrencyFormatter.formatAmountToRandAndCentWithSpace(kilogramPrice) + "/kg)"
                    }
                }
                else -> CurrencyFormatter.formatAmountToRandAndCentWithSpace(price)
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun showFromPriceLabel(itemBinding: LayoutCartListProductItemBinding, priceType: String?) {
        itemBinding.apply {
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
}