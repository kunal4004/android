package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.graphics.Paint
import android.text.TextUtils
import android.text.style.StrikethroughSpan
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.content.ContextCompat
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.product_listing_price_layout.view.*
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.util.WFormatter

class PriceItem {

    fun setPrice(productList: ProductList?, itemView: View, shopFromItem: Boolean = false) {
        val wasPrice: String? = productList?.wasPrice?.toString() ?: ""
        val price: String? = productList?.price?.toString() ?: "0"
        val kilogramPrice: String = productList?.kilogramPrice?.toString() ?: ""
        val priceType = productList?.priceType
        var priceText: String = ""
        var wasPriceText: String
        with(itemView) {
            fromLabel?.visibility = GONE
            tvWasOrKgPrice.visibility = GONE
            if (TextUtils.isEmpty(wasPrice)) {
                priceText = WFormatter.formatAmount(price)
                with(priceType) {
                    when {
                        isNullOrEmpty() -> priceText = WFormatter.formatAmount(price)
                        this!!.toLowerCase().contains("from", true) -> priceText = "From " + WFormatter.formatAmount(price)
                        this.contains("Kilogram", true) -> {
                            tvWasOrKgPrice.apply {
                                text = "(" + WFormatter.formatAmount(kilogramPrice) + "/kg)"
                                visibility = VISIBLE
                            }
                        }
                        else -> priceText = WFormatter.formatAmount(price)
                    }
                }
                tvPrice?.apply {
                    text = priceText
                    setTextColor(ContextCompat.getColor(WoolworthsApplication.getAppContext(), R.color.black))
                }

            } else {
                if (wasPrice.equals(price, ignoreCase = true)) {
                    with(priceType) {
                        when {
                            isNullOrEmpty() -> priceText = WFormatter.formatAmount(price)
                            this!!.toLowerCase().contains("from", true) -> fromLabel.visibility = GONE
                            this.contains("Kilogram", true) -> {
                                wasPriceText = "(" + WFormatter.formatAmount(kilogramPrice) + "/kg)"
                                tvWasOrKgPrice.apply {
                                    text = wasPriceText
                                    visibility = VISIBLE
                                }

                            }
                            else -> priceText = WFormatter.formatAmount(price)
                        }
                    }
                    tvPrice?.apply {
                        text = priceText
                        setTextColor(ContextCompat.getColor(WoolworthsApplication.getAppContext(), R.color.black))
                    }

                } else {
                    // Create a span that will strikeThrough the text
                    val strikeThroughSpan = StrikethroughSpan()
                    priceText = WFormatter.formatAmount(price)
                    with(priceType) {
                        when {
                            isNullOrEmpty() -> {
                                wasPriceText = WFormatter.formatAmount(wasPrice)
                                tvWasOrKgPrice?.apply {
                                    text = wasPriceText
                                    paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                                    visibility = VISIBLE
                                }
                            }
                            this!!.toLowerCase().contains("from", true) -> {
                                fromLabel.visibility = VISIBLE
                                wasPriceText = WFormatter.formatAmount(wasPrice)
                                tvWasOrKgPrice?.apply {
                                    text = wasPriceText
                                    paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                                    visibility = VISIBLE
                                }
                            }
                            this.contains("Kilogram", true) -> {
                                wasPriceText = "(" + WFormatter.formatAmount(kilogramPrice) + "/kg)"
                                tvWasOrKgPrice?.apply {
                                    text = wasPriceText
                                    visibility = VISIBLE
                                }
                            }
                            else -> priceText = WFormatter.formatAmount(price)
                        }
                    }


                    tvPrice?.apply {
                        text = priceText
                        setTextColor(ContextCompat.getColor(WoolworthsApplication.getAppContext(), R.color.was_price_color))
                    }
                }
            }
        }
    }
}