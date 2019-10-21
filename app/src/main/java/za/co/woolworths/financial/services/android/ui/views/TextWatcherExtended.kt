package za.co.woolworths.financial.services.android.ui.views

import android.text.Editable
import android.text.TextWatcher

abstract class TextWatcherExtended : TextWatcher {

    private var isBackspaceClicked: Boolean = false

    abstract fun afterTextChanged(s: Editable, backSpace: Boolean)

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        isBackspaceClicked = after < count
    }

    override fun afterTextChanged(s: Editable) {
        afterTextChanged(s, isBackspaceClicked)
    }
}