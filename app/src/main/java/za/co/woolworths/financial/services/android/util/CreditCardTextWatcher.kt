package za.co.woolworths.financial.services.android.util

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

open class CreditCardTextWatcher(text: EditText?) : TextWatcher {

    private val blockLengths = intArrayOf(4, 4, 4, 4)
    private var mUnformatted = ""
    private val mInput = text

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        val unformattedSeq = s.toString().replace(" ", "")
        if (mUnformatted.length == unformattedSeq.length) {
            return  //length of text has not changed
        }

        mUnformatted = unformattedSeq
        //formatting sequence
        val formatted = StringBuilder()
        var blockIndex = 0
        var currentBlock = 0
        for (i in 0 until mUnformatted.length) {
            if (currentBlock == blockLengths[blockIndex]) {
                formatted.append(" ")
                currentBlock = 0
                blockIndex++
            }
            formatted.append(mUnformatted[i])
            currentBlock++
        }

        mInput?.apply {
            setText(formatted.toString())
            setSelection(formatted.length)
        }
    }

    override fun afterTextChanged(s: Editable) {
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
    }
}