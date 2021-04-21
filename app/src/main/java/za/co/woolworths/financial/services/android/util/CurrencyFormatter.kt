package za.co.woolworths.financial.services.android.util

import androidx.annotation.VisibleForTesting
import java.lang.NumberFormatException
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.*

class CurrencyFormatter {

    companion object {

        private const val currency = "R"
        private const val decimalPlaces = 2
        private const val dot = "."
        private const val NEGATIVE_SYMBOL = "-"
        private const val formatPattern = "#,###,###"
        private const val REGEX_REMOVE_ALL_ALPHABETS_SPACE_AND_DOT = "[^\\d-]"
        private const val REGEX_REMOVE_ALL_ALPHABETS_AND_SPACE = "[^\\d.-]"

        @VisibleForTesting
        fun formatAmountToRandAndCent(amount: Any): String {
            var amountStr = amount.toString().replace((REGEX_REMOVE_ALL_ALPHABETS_AND_SPACE).toRegex(), "")

            if (amountStr.startsWith("-.")) {
                amountStr = amountStr.replace(NEGATIVE_SYMBOL, "-0")
            }
            if (amountStr.startsWith(".")) {
                amountStr = amountStr.replace(".", "0.")
            }
            if (amountStr.contains(dot)) {
                val indexOfDot = amountStr.indexOf(dot) + 1
                val amountLength = amountStr.length
                val cent: String = amountStr.substring(indexOfDot, amountLength)
                val amountOnly = amountStr.substring(0, indexOfDot)
                amountStr = when {
                    cent.isEmpty() -> "${amountOnly}00"
                    cent.length == 1 -> "${amountOnly}${cent}0"
                    cent.length > decimalPlaces -> "${amountStr.substring(0, indexOfDot)}${amountStr.substring(indexOfDot, indexOfDot + decimalPlaces)}"
                    else -> amountStr
                }
            }

            amountStr = amountStr.replace(REGEX_REMOVE_ALL_ALPHABETS_SPACE_AND_DOT.toRegex(), "")

            val amountLength = amountStr.length
            var cents: String = amountStr.takeLast(decimalPlaces)
            cents = if (cents.length == 1) "0${cents}" else cents
            cents = if (cents.length == 2 && cents.contains(NEGATIVE_SYMBOL)) "-0${cents.replace(NEGATIVE_SYMBOL.toRegex(), "")}" else cents
            val randMaxRange = amountLength - decimalPlaces
            var rand = 0
            try {
                rand = if (randMaxRange <= 0) 0 else amountStr.take(randMaxRange).toInt()
            } catch (e: NumberFormatException) {
                FirebaseManager.logException("CurrencyFormatter NumberFormatException $amountStr Exception $e")
            }
            val numberFormatter = NumberFormat.getInstance(Locale.US) as? DecimalFormat

            numberFormatter?.apply {
                decimalFormatSymbols = DecimalFormatSymbols().apply {
                    decimalSeparator = ','
                    groupingSeparator = ' '
                }
                applyPattern(formatPattern)
            }

            var finalAmount = "${numberFormatter?.format(rand)}.${if (cents.length == 1) "0$cents" else cents}"

            if (finalAmount.contains(NEGATIVE_SYMBOL)) {
                finalAmount = "-${finalAmount.replace(NEGATIVE_SYMBOL.toRegex(), "")}"
            }

            if (amountStr.contains(NEGATIVE_SYMBOL) && !finalAmount.contains(NEGATIVE_SYMBOL) && finalAmount != "0.00") {
                finalAmount = "-$finalAmount"
            }

            return finalAmount
        }

        fun formatAmountToRandAndCentWithSpace(amount: Any?): String {
            val amt = "${amount?.let { formatAmountToRandAndCent(it) } ?: 0.00}"
            return if (amt.contains(NEGATIVE_SYMBOL)) "-$currency ${amt.replace(NEGATIVE_SYMBOL.toRegex(), "")}" else "$currency $amt"
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