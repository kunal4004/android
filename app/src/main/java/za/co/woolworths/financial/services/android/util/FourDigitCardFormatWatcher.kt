package za.co.woolworths.financial.services.android.util

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import com.crashlytics.android.Crashlytics

open class FourDigitCardFormatWatcher(private var mEditText: EditText) : TextWatcher {
    private var mAvoidRecursiveCall = false
    private var mShouldDeleteSpace = false
    private var mKeyListenerSet = false
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        if (!mKeyListenerSet) {
            mEditText.setOnKeyListener { _: View?, keyCode: Int, _: KeyEvent? ->
                try {
                    mShouldDeleteSpace = keyCode == KeyEvent.KEYCODE_DEL && mEditText.selectionEnd - mEditText.selectionStart <= 1 && mEditText.selectionStart > 0 && mEditText.text.toString()[mEditText.selectionEnd - 1] == '-'
                } catch (e: IndexOutOfBoundsException) { // never to happen because of checks
                    Crashlytics.logException(e)
                }
                false
            }
            mKeyListenerSet = true
        }
    }

    @SuppressLint("DefaultLocale")
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (mAvoidRecursiveCall) // to avoid recursive calls
            return
        mAvoidRecursiveCall = true
        val currentPos = mEditText.selectionStart
        val string = mEditText.text.toString().toUpperCase()
        val newString = makePrettyString(string)
        mEditText.setText(newString)
        try {
            mEditText.setSelection(getCursorPos(string, newString, currentPos, mShouldDeleteSpace))
        } catch (e: IndexOutOfBoundsException) {
            mEditText.setSelection(mEditText.length()) // last resort never to happen
        }
        mShouldDeleteSpace = false
        mAvoidRecursiveCall = false
    }

    override fun afterTextChanged(s: Editable) {}
    private fun makePrettyString(string: String): String {
        val number = string.replace(" ".toRegex(), "")
        val isEndSpace = string.endsWith(" ") && number.length % 4 == 0
        return number.replace("(.{4}(?!$))".toRegex(), "$1 ") + if (isEndSpace) " " else ""
    }

    private fun getCursorPos(oldString: String, newString: String, oldPos: Int, isDeleteSpace: Boolean): Int {
        var cursorPos = newString.length
        if (oldPos != oldString.length) {
            val stringWithMarker =
                    oldString.substring(0, oldPos) + MARKER + oldString.substring(oldPos)
            cursorPos = makePrettyString(stringWithMarker).indexOf(MARKER)
            if (isDeleteSpace) cursorPos -= 1
        }
        return cursorPos
    }

    companion object {
        const val MARKER = "|" // filtered in layout not to be in the string
    }

}