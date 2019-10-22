package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.awfs.coordination.R

class OTPViewTextWatcher(private val previousEditText: EditText?, private val currentEditText: EditText,
                         private val nextEditText: EditText?, private val method: () -> Unit) : TextWatcher {
    override fun afterTextChanged(s: Editable?) {
        if (s.isNullOrEmpty()) {
            previousEditText?.requestFocus()
        } else {
            if (s.length > 1) {
                if (currentEditText.selectionEnd > 1) {
                    // If stand on second position of EditText and enter new symbol,
                    // will move to next EditText copying second symbol.
                    val secondSymbol = s.substring(1, 2)
                    nextEditText?.setText(secondSymbol)
                }
                // Remove second symbol.
                s.delete(1, s.length)
            }
            nextEditText?.requestFocus()
            nextEditText?.setSelection(nextEditText.length(), nextEditText.length())
            method()

            currentEditText.isCursorVisible = !(currentEditText.id == R.id.edtVerificationCode5 && currentEditText.text.isNotEmpty())
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }
}