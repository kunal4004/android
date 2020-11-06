package za.co.woolworths.financial.services.android.util

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.parseMoneyValue
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.ParseException
import java.util.Locale
import kotlin.math.min

class CurrencyInputWatcher(private val editText: EditText, private val currencySymbol: String, locale: Locale) : TextWatcher {

    companion object {
        const val MAX_NO_OF_DECIMAL_PLACES = 2
        const val FRACTION_FORMAT_PATTERN_PREFIX = "#,##0."
    }

    private var hasDecimalPoint = false
    private val wholeNumberDecimalFormat =
            (NumberFormat.getNumberInstance(locale) as DecimalFormat).apply {
                applyPattern("#,##0")
            }

    private val fractionDecimalFormat = (NumberFormat.getNumberInstance(locale) as DecimalFormat)

    val decimalFormatSymbols: DecimalFormatSymbols
        get() = wholeNumberDecimalFormat.decimalFormatSymbols

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        fractionDecimalFormat.isDecimalSeparatorAlwaysShown = true
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        hasDecimalPoint = s.toString().contains(decimalFormatSymbols.decimalSeparator.toString())
    }

    @SuppressLint("SetTextI18n")
    override fun afterTextChanged(s: Editable) {
        val newInputString = s.toString()
        if (newInputString.length < currencySymbol.length) {
            editText.setText(currencySymbol)
            editText.setSelection(currencySymbol.length)
            return
        }

        if (newInputString == currencySymbol) {
            editText.setSelection(currencySymbol.length)
            return
        }

        editText.removeTextChangedListener(this)
        val startLength = editText.text.length
        try {
            val numberWithoutGroupingSeparator = parseMoneyValue(
                    newInputString,
                    decimalFormatSymbols.groupingSeparator.toString(),
                    currencySymbol
            )
            val parsedNumber = fractionDecimalFormat.parse(numberWithoutGroupingSeparator)!!
            val selectionStartIndex = editText.selectionStart
            if (hasDecimalPoint) {
                fractionDecimalFormat.applyPattern(FRACTION_FORMAT_PATTERN_PREFIX + getFormatSequenceAfterDecimalSeparator(numberWithoutGroupingSeparator))
                editText.setText("$currencySymbol${fractionDecimalFormat.format(parsedNumber)}")
            } else {
                editText.setText("$currencySymbol${wholeNumberDecimalFormat.format(parsedNumber)}")
            }
            val endLength = editText.text.length
            val selection = selectionStartIndex + (endLength - startLength)
            if (selection > 0 && selection <= editText.text.length) {
                editText.setSelection(selection)
            } else {
                editText.setSelection(editText.text.length - 1)
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        editText.addTextChangedListener(this)
    }

    /**
     * @param number the original number to format
     * @return the appropriate zero sequence for the input number. e.g 156.1 returns "0",
     *  14.98 returns "00"
     */
    private fun getFormatSequenceAfterDecimalSeparator(number: String): String {
        val noOfCharactersAfterDecimalPoint = number.length - number.indexOf(decimalFormatSymbols.decimalSeparator) - 1
        return "0".repeat(min(noOfCharactersAfterDecimalPoint, MAX_NO_OF_DECIMAL_PLACES))
    }
}
