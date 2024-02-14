package za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.core.functions

import java.util.regex.Pattern

const val EMAIL_PATTERN = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}" //pattern same as ios
    fun isEmailValid(email: String): Boolean {
        return Pattern.compile(EMAIL_PATTERN).matcher(email).matches()
    }