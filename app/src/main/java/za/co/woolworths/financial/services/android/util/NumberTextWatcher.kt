package za.co.woolworths.financial.services.android.util

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.text.DecimalFormat
import java.text.ParseException

internal class NumberTextWatcher(et: EditText) : TextWatcher {
    private val df: DecimalFormat = DecimalFormat("#,###,##0.00")
    private val dfnd: DecimalFormat
    private var hasFractionalPart: Boolean
    private val et: EditText
    override fun afterTextChanged(s: Editable) {
        et.removeTextChangedListener(this)
        try {
            val inilen: Int = et.text.length
            val v: String = s.toString().replace(java.lang.String.valueOf(df.decimalFormatSymbols.groupingSeparator), "")
            val n: Number = df.parse(v)
            val cp = et.selectionStart
            if (hasFractionalPart) {
                et.setText(df.format(n))
            } else {
                et.setText(dfnd.format(n))
            }
            val endlen: Int = et.text.length
            val sel = cp + (endlen - inilen)
            if (sel > 0 && sel <= et.text.length) {
                et.setSelection(sel)
            } else {
                // place cursor at the end?
                et.setSelection(et.text.length - 1)
            }
        } catch (nfe: NumberFormatException) {
            // do nothing?
        } catch (e: ParseException) {
            // do nothing?
        }
        et.addTextChangedListener(this)
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        hasFractionalPart = s.toString().contains(java.lang.String.valueOf(df.decimalFormatSymbols.decimalSeparator))
    }

    companion object {
        private const val TAG = "NumberTextWatcher"
    }

    init {
        df.isDecimalSeparatorAlwaysShown = true
        dfnd = DecimalFormat("#,###,##0")
        this.et = et
        hasFractionalPart = false
    }
}