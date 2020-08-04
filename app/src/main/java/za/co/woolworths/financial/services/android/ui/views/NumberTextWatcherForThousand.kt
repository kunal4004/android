package za.co.woolworths.financial.services.android.ui.views

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.widget.EditText
import java.util.*

class NumberTextWatcherForThousand(private var editText: EditText) : TextWatcher {
    private var previousLength: Int = 0
    private var backSpace: Boolean = false

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        previousLength = s.length
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        //If s is empty disable next arrow,else enable s arrow
    }

    @SuppressLint("SetTextI18n")
    override fun afterTextChanged(s: Editable) {
        val initLength: Int = editText.text.length
        val value = editText.text.toString()
        val editLength: Int
        backSpace = previousLength > s.length
        if (value.startsWith("0") && !value.startsWith("0.")) {
            editText.setText("") //Prevents "0" while starting but not "0."
        }
        if (backSpace) {
            editText.removeTextChangedListener(this)
            if (s.isNotEmpty()) {
                var loanAmount = s.toString()
                        .replace(".0", "")
                        .replace(" ", "")
                loanAmount = loanAmount.substring(0, loanAmount.length - 1)
                if (TextUtils.isEmpty(loanAmount)) {
                    editText.setText("")
                    backSpace = false
                    editText.addTextChangedListener(this)
                    return
                }
                val cp = editText.selectionStart
                loanAmount = getDecimalFormat(trimCommaOfString(loanAmount)) + ".00"
                editText.setText(loanAmount)
                editLength = editText.text.length
                val sel = cp + (editLength - initLength)
                if (sel > 0) {
                    editText.setSelection(editLength)
                }
            }
            editText.addTextChangedListener(this)
        } else {
            try {
                editText.removeTextChangedListener(this)
                val value = editText.text.toString()

                if (value != "") {

                    if (value.startsWith(".")) { //adds "0." when only "." is pressed on beginning of writing
                        editText.setText("0.")
                    }
                    if (value.startsWith("0") && !value.startsWith("0.")) {
                        editText.setText("") //Prevents "0" while starting but not "0."
                    }

                    val str = editText.text.toString().replace(" ".toRegex(), "")
                    if (value != "")
                        editText.setText(getDecimalFormat(str) + ".00")
                    editText.setSelection(editText.text.toString().length)
                }
                editText.addTextChangedListener(this)
            } catch (ex: Exception) {
                editText.addTextChangedListener(this)
            }
        }
    }

    private fun getDecimalFormat(number: String): String {
        var value = number
        value = value.replace(".00", "")
        val lst = StringTokenizer(value, ".")
        var str1 = value
        var str2 = ""
        if (lst.countTokens() > 1) {
            str1 = lst.nextToken()
            str2 = lst.nextToken()
        }
        var str3 = ""
        var i = 0
        var j = -1 + str1.length
        if (str1[-1 + str1.length] == '.') {
            j--
            str3 = "."
        }
        var k = j
        while (true) {
            if (k < 0) {
                if (str2.isNotEmpty())
                    str3 = "$str3.$str2"
                return str3
            }
            if (i == 3) {
                str3 = " $str3"
                i = 0
            }
            str3 = str1[k] + str3
            i++
            k--
        }

    }

    //Trims all the comma of the string and returns
    private fun trimCommaOfString(string: String): String {
        //        String returnString;
        return if (string.contains(" ")) {
            string.replace(" ", "")
        } else {
            string
        }

    }
}