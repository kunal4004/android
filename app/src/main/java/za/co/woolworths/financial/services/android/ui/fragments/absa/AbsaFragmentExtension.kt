package za.co.woolworths.financial.services.android.ui.fragments.absa

import android.content.Context
import android.support.v4.app.Fragment
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

open class AbsaFragmentExtension : Fragment() {

    fun showKeyboard(editText: EditText) {
        editText.requestFocus()
        activity?.let {
            editText.requestFocus()
            editText.isFocusableInTouchMode = true
            val imm = it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    fun hideKeyboard() {
        activity?.let {
            if (it.currentFocus != null && it.currentFocus.windowToken != null) {
                (it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                        .hideSoftInputFromWindow(it.currentFocus.windowToken, 0)
            }
        }
    }

    fun alwaysHideWindowSoftInputMode() {
        activity?.apply {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        }
    }

    fun maskedCardNumberWithSpaces(cardNumber: String?): String {
        return " **** **** **** ".plus(cardNumber?.let { it.substring(it.length-4, it.length) } ?: "")
    }
}