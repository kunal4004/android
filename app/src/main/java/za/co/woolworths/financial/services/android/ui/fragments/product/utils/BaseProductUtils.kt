package za.co.woolworths.financial.services.android.ui.fragments.product.utils

import android.annotation.SuppressLint
import android.graphics.Paint
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.util.WFormatter

open class BaseProductUtils {
    companion object {

        fun displayPrice(fromPricePlaceHolder:TextView, tvPrice: TextView, tvWasPrice: TextView, price: String?, wasPrice: String?, priceType: String? = "", kilogramPrice: String? = "") {
            val wasPrice: String? = wasPrice ?: ""
            val price: String? = price ?: ""
            val kilogramPrice: String = kilogramPrice ?: ""



            if (wasPrice.isNullOrEmpty()) {
                tvPrice.text = if (price!!.isEmpty()) "" else getMassPrice(price, priceType, kilogramPrice)
                tvPrice.setTextColor(android.graphics.Color.BLACK)
                tvWasPrice.text = ""
                tvWasPrice.visibility = GONE

            } else {
                if (wasPrice.equals(price, ignoreCase = true)) {
                    tvPrice.text = if (price!!.isEmpty()) WFormatter.formatAmount(wasPrice) else getMassPrice(price, priceType, kilogramPrice)
                    tvPrice.setTextColor(android.graphics.Color.BLACK)
                    tvWasPrice.text = ""
                    tvWasPrice.visibility = GONE
                } else {
                    tvPrice.text = WFormatter.formatAmount(price)
                    tvPrice.setTextColor(ContextCompat.getColor(WoolworthsApplication.getAppContext(), R.color.was_price_color))
                    tvWasPrice.text = wasPrice?.let { getMassPrice(it, priceType, kilogramPrice) }
                    tvWasPrice.paintFlags = tvWasPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    tvWasPrice.setTextColor(android.graphics.Color.BLACK)
                    tvWasPrice.visibility = VISIBLE

                }
            }

            showFromPriceLabel(priceType, fromPricePlaceHolder)
        }

        private fun getMassPrice(price: String, priceType: String?, kilogramPrice: String): String {
            return with(priceType) {
                when {
                    isNullOrEmpty() -> {
                        WFormatter.formatAmount(price)
                    }
                    this!!.contains("from", true) -> {
                        WFormatter.formatAmount(price)
                    }
                    this.contains("Kilogram", true) -> {
                        WFormatter.formatAmount(price) + " (" + WFormatter.formatAmount(kilogramPrice) + "/kg)"
                    }
                    else -> WFormatter.formatAmount(price)
                }
            }
        }

        @SuppressLint("DefaultLocale")
        private fun showFromPriceLabel(priceType: String?,fromPricePlaceHolder:TextView) {
            priceType?.let {
                if (it.toLowerCase().contains("from", true)) {
                    fromPricePlaceHolder?.visibility = View.VISIBLE
                    fromPricePlaceHolder?.text = "From " // add space on StrikeThrough only
                } else {
                    fromPricePlaceHolder?.visibility = View.GONE
                    fromPricePlaceHolder?.text = ""
                }
            }
        }
    }
}