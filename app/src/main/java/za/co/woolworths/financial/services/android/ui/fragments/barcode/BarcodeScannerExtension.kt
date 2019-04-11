package za.co.woolworths.financial.services.android.ui.fragments.barcode

import android.app.Activity
import android.content.Context
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import za.co.woolworths.financial.services.android.ui.views.WLoanEditTextView


fun showSoftKeyboard(activity: Activity, editTextView: WLoanEditTextView) {
    activity.apply {
        window?.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.apply {
            showSoftInput(editTextView, InputMethodManager.SHOW_IMPLICIT)
        }
    }
}
