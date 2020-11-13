package za.co.woolworths.financial.services.android.ui.fragments.absa

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import za.co.absa.openbankingapi.woolworths.integration.service.VolleySingleton
import za.co.woolworths.financial.services.android.ui.views.actionsheet.OkButtonErrorMessageFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.SingleButtonDialogFragment

open class AbsaFragmentExtension : Fragment() {

    fun showKeyboard(editText: EditText) {
        editText.requestFocus()
        activity?.let {
            editText.isFocusableInTouchMode = true
            editText.requestFocus()
            val imm = it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    fun hideKeyboard() {
        activity?.let {
            if (it.currentFocus != null && it.currentFocus?.windowToken != null) {
                (it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                        .hideSoftInputFromWindow(it.currentFocus?.windowToken, 0)
            }
        }
    }

    fun alwaysHideWindowSoftInputMode() {
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    fun tapAndNavigateBackErrorDialog(message: String) {
        activity?.let {
            val fm = it.supportFragmentManager
            val singleButtonDialogFragment = SingleButtonDialogFragment.newInstance(message)
            singleButtonDialogFragment.show(fm, SingleButtonDialogFragment::class.java.simpleName)
        }
    }

    fun tapAndDismissErrorDialog(text: String) {
        activity?.let {
            val fm = it.supportFragmentManager
            val okButtonErrorMessageFragment = OkButtonErrorMessageFragment.newInstance(text)
            okButtonErrorMessageFragment.show(fm, OkButtonErrorMessageFragment::class.java.simpleName)
        }
    }

    fun cancelVolleyRequest(name: String?) {
        VolleySingleton.getInstance()?.apply {
            cancelRequest(name)
        }
    }
    fun alwaysShowWindowSoftInputMode() {
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }
}