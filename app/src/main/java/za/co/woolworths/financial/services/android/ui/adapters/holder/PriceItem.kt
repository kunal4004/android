package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.*
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.content.ContextCompat
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.product_listing_price_layout.view.*
import kotlinx.android.synthetic.main.product_listing_price_layout.view.fromPriceLabelTextView
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.util.WFormatter

class PriceItem {

    fun setPrice(productList: ProductList?, itemView: View, shopFromItem: Boolean = false) {
        val wasPrice: String? = productList?.wasPrice?.toString() ?: ""
        val price: String? = productList?.price?.toString() ?: ""
        val kilogramPrice: String = productList?.kilogramPrice?.toString() ?: ""
        val priceType = productList?.priceType

        var spannableItemPrice: SpannableStringBuilder
        var priceText: String
        var wasPriceText : String
        with(itemView) {
            fromPriceLabelTextView?.visibility =  GONE
            if (TextUtils.isEmpty(wasPrice)) {
                priceText = if (TextUtils.isEmpty(price) && !(shopFromItem && kilogramPrice.isEmpty())) "" else getMassPrice(price ?: "0", priceType, kilogramPrice, shopFromItem)
                spannableItemPrice = SpannableStringBuilder(priceText)
                spannableItemPrice.setSpan(ForegroundColorSpan(Color.BLACK),0, priceText.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                priceTextView?.text = spannableItemPrice
            } else {
                if (wasPrice.equals(price, ignoreCase = true)) {
                    priceText = if (TextUtils.isEmpty(price))  WFormatter.formatAmount(wasPrice) else getMassPrice(price ?: "0", priceType, kilogramPrice, shopFromItem)
                    spannableItemPrice = SpannableStringBuilder(priceText)
                    spannableItemPrice.setSpan(ForegroundColorSpan(Color.BLACK),0, priceText.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                    priceTextView?.text = spannableItemPrice
                } else {
                    // Create a span that will strikeThrough the text
                    val strikeThroughSpan = StrikethroughSpan()
                    val textPaint = TextPaint()
                    textPaint.textSize = 100f
                    strikeThroughSpan.updateDrawState(textPaint)
                    priceText = WFormatter.formatAmount(price)
                    wasPriceText =  getMassPrice(wasPrice ?: "0", priceType, kilogramPrice, shopFromItem)

                    val fromWasPrice = "$wasPriceText $priceText"
                    spannableItemPrice = SpannableStringBuilder(fromWasPrice)
                    with(spannableItemPrice) {
                        setSpan(ForegroundColorSpan(Color.BLACK), 0, wasPriceText.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                        setSpan(strikeThroughSpan, 0, wasPriceText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        setSpan(ForegroundColorSpan(ContextCompat.getColor(WoolworthsApplication.getAppContext(), R.color.was_price_color)), wasPriceText.length, fromWasPrice.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                        priceTextView?.text = this
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

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun View.showFromPriceLabel(priceType: String?) {
        priceType?.let {
            if (it.toLowerCase().contains("from", true)) {
                fromPriceLabelTextView?.visibility = VISIBLE
                fromPriceLabelTextView?.text = "From " // add space on StrikeThrough only
            } else {
                fromPriceLabelTextView?.visibility = GONE
                fromPriceLabelTextView?.text = ""
            }
        }
    }
}