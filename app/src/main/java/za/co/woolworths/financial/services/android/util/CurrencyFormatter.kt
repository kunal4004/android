package za.co.woolworths.financial.services.android.util

import androidx.annotation.VisibleForTesting
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.*

class CurrencyFormatter {

    companion object {

        private const val currency = "R"
        private const val decimalPlaces = 2
        private const val dot = "."
        private const val formatPattern = "#,###,###"
        private val regexRemoveAllNonNumericCharactersExceptNegativeSymbol = Regex("[^\\d-]")

        @VisibleForTesting
        fun formatAmountToRandAndCent(amount: Any): String {
            var amountStr = amount.toString()

            if (amountStr.contains(dot)) {
                val indexOfDot = amountStr.indexOf(dot)+1
                val amountLength = amountStr.length
                val cent: String = amountStr.substring(indexOfDot, amountLength)
                amountStr = when {
                    cent.length == 1 -> "${amountStr}0"
                    cent.length > decimalPlaces -> "${amountStr.substring(0, indexOfDot)}${amountStr.substring(indexOfDot, indexOfDot + decimalPlaces)}"
                    else -> amountStr
                }
            }

            amountStr = amountStr.replace(regexRemoveAllNonNumericCharactersExceptNegativeSymbol, "")

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
                applyPattern(formatPattern)
            }

            return "${numberFormatter?.format(rand)}.$cents"
        }

        fun formatAmountToRandAndCentWithSpace(amount: Any?): String {
            return "$currency ${amount?.let { formatAmountToRandAndCent(it) } ?: 0.00}"
        }

        fun formatAmountToRandAndCentNoSpace(amount: Any?): String {
            return "$currency${amount?.let { formatAmountToRandAndCent(it) } ?: 0.00}"
        }

        fun formatAmountToCentNoGroupingSeparator(amount: Any?): String {
            return "${amount?.toString()?.replace(" ", "")?.let { formatAmountToRandAndCent(it).replace(" ", "") } ?: 0.00}"
        }

        fun escapeDecimal(amount: Any?): String {
            return (formatAmountToRandAndCent(amount ?: 0 * 100)).replace(Regex("\\.0*$"), "")
        }
    }

    @VisibleForTesting
    fun formatAmountsToRandAndCent(value: Any): String {
        return formatAmountToRandAndCent(value)
    }

    @VisibleForTesting
    fun formatAmountToRandAndCentWithSpacing(value: Any): String {
        return formatAmountToRandAndCentWithSpace(value)
    }

    @VisibleForTesting
    fun formatAmountToRandAndCentWithoutSpace(value: Any): String {
        return formatAmountToRandAndCentNoSpace(value)
    }

}