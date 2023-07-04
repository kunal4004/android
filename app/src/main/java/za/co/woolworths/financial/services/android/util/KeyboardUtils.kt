package za.co.woolworths.financial.services.android.util

import android.app.Activity
import android.content.Context
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import android.widget.EditText
import za.co.woolworths.financial.services.android.ui.views.WLoanEditTextView
import java.lang.reflect.Method

class KeyboardUtils {
    companion object {
        fun showKeyboard(editText: EditText?, activity: Activity?) {
            editText?.requestFocus()
            val imm: InputMethodManager? = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
        }

        fun hideKeyboard(activity: Activity?) {
            activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
            val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        }

        fun hideKeyboardIfVisible(activity: Activity?) {
            activity?.let {
                if (isSystemKeyboardVisible(it)) {
                    hideKeyboard(activity)
                }
            }
        }

        fun showSoftKeyboard(autoCompleteTextView: AutoCompleteTextView?, activity: Activity? ) {
            activity?.let {
                val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(autoCompleteTextView, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        fun isSystemKeyboardVisible(activity: Activity): Boolean {
            return try {
                val manager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                val windowHeightMethod: Method = InputMethodManager::class.java.getMethod("getInputMethodWindowVisibleHeight")
                val height = windowHeightMethod.invoke(manager) as Int
                height > 0
            } catch (e: Exception) {
                false
            }
        }
    }
}