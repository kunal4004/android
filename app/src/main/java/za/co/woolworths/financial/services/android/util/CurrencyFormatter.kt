package za.co.woolworths.financial.services.android.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.*

class CurrencyFormatter {

    companion object {

        private const val currency = "R"
        private const val decimalPlaces = 2

        fun formatAmountToRandAndCent(amount: Any): String {
            val amountStr = amount.toString()
            val amountLength = amountStr.length
            var cents: String = amountStr.takeLast(decimalPlaces)
            if (cents.length == 1) cents += "0"
            val randMaxRange = amountLength - decimalPlaces
            val rand = if (randMaxRange <= 0) 0 else amountStr.take(randMaxRange).toInt()
            val numberFormatter = NumberFormat.getInstance(Locale.US) as? DecimalFormat

            numberFormatter?.apply {
                decimalFormatSymbols = DecimalFormatSymbols().apply {
                    decimalSeparator = ','
                    groupingSeparator = ' '
                }
                applyPattern("#,###,###")
            }

            return "$currency ${numberFormatter?.format(rand)}.$cents"
        }



        fun escapeDecimalFormat(amount: Any?): String {
            return formatAmountToRandAndCent((amount as? Int)
                    ?: 0 * 100).replace(Regex("\\.0*$"), "")
        }

    }
}