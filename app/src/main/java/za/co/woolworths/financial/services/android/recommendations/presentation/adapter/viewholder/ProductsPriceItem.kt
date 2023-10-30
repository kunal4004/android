package za.co.woolworths.financial.services.android.recommendations.presentation.adapter.viewholder

import android.graphics.Paint
import android.text.TextUtils
import android.text.style.StrikethroughSpan
import android.view.View
import androidx.core.content.ContextCompat
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ProductListingPriceLayoutBinding
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import java.util.*

class ProductsPriceItem {

    fun setPrice(productList: ProductList?, binding: ProductListingPriceLayoutBinding, shopFromItem: Boolean = false) {
        val wasPrice: String = productList?.wasPrice?.toString() ?: ""
        val price: String = productList?.price?.toString() ?: "0"
        val kilogramPrice: String = productList?.kilogramPrice?.toString() ?: ""
        val priceType = productList?.priceType
        var priceText: String = ""
        var wasPriceText: String
        binding.apply {
            fromLabel?.visibility = View.GONE
            tvWasOrKgPrice.visibility = View.GONE
            if (TextUtils.isEmpty(wasPrice)) {
                priceText = CurrencyFormatter.formatAmountToRandAndCentWithSpace(price)
                with(priceType) {
                    when {
                        isNullOrEmpty() -> priceText = CurrencyFormatter.formatAmountToRandAndCentWithSpace(price)
                        (this?.lowercase(Locale.getDefault()) ?: "").contains("from", true) -> priceText = "From " + CurrencyFormatter.formatAmountToRandAndCentWithSpace(price)
                        this.contains("Kilogram", true) -> {
                            tvWasOrKgPrice.apply {
                                text = "( ${CurrencyFormatter.formatAmountToRandAndCentWithSpace(kilogramPrice)} /kg)"
                                visibility = View.VISIBLE
                            }
                        }
                        else -> priceText = CurrencyFormatter.formatAmountToRandAndCentWithSpace(price)
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
                            isNullOrEmpty() -> priceText = CurrencyFormatter.formatAmountToRandAndCentWithSpace(price)
                            this!!.toLowerCase().contains("from", true) -> fromLabel.visibility =
                                View.GONE
                            this.contains("Kilogram", true) -> {
                                wasPriceText = "(" + CurrencyFormatter.formatAmountToRandAndCentWithSpace(kilogramPrice) + "/kg)"
                                tvWasOrKgPrice.apply {
                                    text = wasPriceText
                                    visibility = View.VISIBLE
                                }

                            }
                            else -> priceText = CurrencyFormatter.formatAmountToRandAndCentWithSpace(price)
                        }
                    }
                    tvPrice?.apply {
                        text = priceText
                        setTextColor(ContextCompat.getColor(WoolworthsApplication.getAppContext(), R.color.black))
                    }

                } else {
                    // Create a span that will strikeThrough the text
                    val strikeThroughSpan = StrikethroughSpan()
                    priceText = CurrencyFormatter.formatAmountToRandAndCentWithSpace(price)
                    with(priceType) {
                        when {
                            isNullOrEmpty() -> {
                                wasPriceText = CurrencyFormatter.formatAmountToRandAndCentWithSpace(wasPrice)
                                tvWasOrKgPrice?.apply {
                                    text = wasPriceText
                                    paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                                    visibility = View.VISIBLE
                                }
                            }
                            this.toLowerCase(Locale.ROOT).contains("from", true) -> {
                                fromLabel.visibility = View.VISIBLE
                                wasPriceText = CurrencyFormatter.formatAmountToRandAndCentWithSpace(wasPrice)
                                tvWasOrKgPrice?.apply {
                                    text = wasPriceText
                                    paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                                    visibility = View.VISIBLE
                                }
                            }
                            this.contains("Kilogram", true) -> {
                                wasPriceText = "(" + CurrencyFormatter.formatAmountToRandAndCentWithSpace(kilogramPrice) + "/kg)"
                                tvWasOrKgPrice?.apply {
                                    text = wasPriceText
                                    visibility = View.VISIBLE
                                }
                            }
                            else -> priceText = CurrencyFormatter.formatAmountToRandAndCentWithSpace(price)
                        }
                    }
                    tvPrice?.apply {
                        text = priceText
                        setTextColor(ContextCompat.getColor(WoolworthsApplication.getAppContext(), R.color.promo_text_color))
                    }
                }
            }
        }
    }
}
