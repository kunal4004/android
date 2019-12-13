package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class OTPViewTextWatcher(private val previousEditText: EditText?, private val currentEditText: EditText,
                         private val nextEditText: EditText?, private val method: () -> Unit) : TextWatcher {

    var isEmptyFirstTime: Boolean = false
    override fun afterTextChanged(s: Editable?) {

        if (s.isNullOrEmpty()) {
            if (isEmptyFirstTime) {
                isEmptyFirstTime = false
                previousEditText?.requestFocus()
            } else {
                isEmptyFirstTime = true
                currentEditText.requestFocus()
            }
        } else {
            isEmptyFirstTime = false
            if (s.length > 1) {
                if (currentEditText.selectionEnd > 1) {
                    // If stand on second position of EditText and enter new symbol,
                    // will move to next EditText copying second symbol.
                    val secondSymbol = s.substring(1, 2)
                    currentEditText?.setText(secondSymbol)
                }
                // Remove second symbol.
                s.delete(1, s.length)
            }
            nextEditText?.requestFocus()
            nextEditText?.setSelection(nextEditText.length(), nextEditText.length())
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        method()
    }
}