package za.co.woolworths.financial.services.android.util

import java.math.RoundingMode
import java.text.NumberFormat
import java.util.*

class CurrencyFormatterUtil {

    companion object {

        private const val CURRENCY_CODE = "ZAR"
        private const val COUNTRY = "ZA"
        private const val LANGUAGE = "en"
        private const val RAND_SYMBOL = "R"
        private const val COMMA_SYMBOL = ","
        private const val DOT_SYMBOL = "."

        private fun convertNumberToRand(amount: Int): String? {
            val amountInRand: Float = amount.toFloat().div(100)
            val format: NumberFormat = NumberFormat.getCurrencyInstance(Locale(LANGUAGE, COUNTRY))
            with(format) {
                currency = Currency.getInstance(CURRENCY_CODE)
                maximumFractionDigits = 2
                roundingMode = RoundingMode.FLOOR
            }
            return format.format(amountInRand)
        }

        fun convertCurrencyToDot(amount: Int?): String? {
            val currencyAmount = amount?.let { number -> convertNumberToRand(number) }
            return currencyAmount?.replace(COMMA_SYMBOL, DOT_SYMBOL)
        }

        fun addSpaceBetweenRandSymbolAndAmount(amount: Int?): String? {
            val currencyAmount = convertCurrencyToDot(amount)
            return currencyAmount?.replace(RAND_SYMBOL, "$RAND_SYMBOL ")
        }

    }
}