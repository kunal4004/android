package za.co.woolworths.financial.services.android.ui.views


import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.text.InputType
import android.util.AttributeSet
import androidx.annotation.RequiresApi
import com.awfs.coordination.R
import com.google.android.material.textfield.TextInputEditText
import za.co.woolworths.financial.services.android.util.CurrencyInputWatcher
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.getLocaleFromTag
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.parseMoneyValueWithLocale
import java.math.BigDecimal
import java.util.*

class CurrencyEditText(context: Context, attrs: AttributeSet?) : TextInputEditText(context, attrs) {
    private lateinit var currencySymbolPrefix: String
    private var textWatcher: CurrencyInputWatcher
    private var locale: Locale = Locale.getDefault()

    init {
        var useCurrencySymbolAsHint = false
        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        var localeTag: String?
        val prefix: String
        context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.CurrencyEditText,
                0, 0
        ).run {
            try {
                prefix = getString(R.styleable.CurrencyEditText_currencySymbol).orEmpty()
                localeTag = getString(R.styleable.CurrencyEditText_localeTag)
                useCurrencySymbolAsHint = getBoolean(R.styleable.CurrencyEditText_useCurrencySymbolAsHint, false)
            } finally {
                recycle()
            }
        }
        currencySymbolPrefix = if (prefix.isBlank()) "" else "$prefix "
        if (useCurrencySymbolAsHint) hint = currencySymbolPrefix
        if (!localeTag.isNullOrBlank()) locale = getLocaleFromTag(localeTag!!)
        textWatcher = CurrencyInputWatcher(this, currencySymbolPrefix, locale)
    }

    fun setLocale(locale: Locale) {
        this.locale = locale
        invalidateTextWatcher()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun setLocale(localeTag: String) {
        locale = getLocaleFromTag(localeTag)
        invalidateTextWatcher()
    }

    fun setCurrencySymbol(currencySymbol: String, useCurrencySymbolAsHint: Boolean = false) {
        currencySymbolPrefix = "$currencySymbol "
        if (useCurrencySymbolAsHint) hint = currencySymbolPrefix
        invalidateTextWatcher()
    }

    private fun invalidateTextWatcher() {
        removeTextChangedListener(textWatcher)
        textWatcher = CurrencyInputWatcher(this, currencySymbolPrefix, locale)
        addTextChangedListener(textWatcher)
    }

    fun getNumericValue(): Double {
        return parseMoneyValueWithLocale(
                locale,
                text.toString(),
                textWatcher.decimalFormatSymbols.groupingSeparator.toString(),
                currencySymbolPrefix
        ).toDouble()
    }

    fun getNumericValueBigDecimal(): BigDecimal {
        return BigDecimal(
                parseMoneyValueWithLocale(
                        locale,
                        text.toString(),
                        textWatcher.decimalFormatSymbols.groupingSeparator.toString(),
                        currencySymbolPrefix
                ).toString()
        )
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        if (focused) {
            removeTextChangedListener(textWatcher)
            addTextChangedListener(textWatcher)
            if (text.toString().isEmpty()) setText(currencySymbolPrefix)
        } else {
            removeTextChangedListener(textWatcher)
            if (text.toString() == currencySymbolPrefix) setText("")
        }
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        if (::currencySymbolPrefix.isInitialized.not()) return
        val symbolLength = currencySymbolPrefix.length
        if (selEnd < symbolLength && text.toString().length >= symbolLength) {
            setSelection(symbolLength)
        } else {
            super.onSelectionChanged(selStart, selEnd)
        }
    }
}