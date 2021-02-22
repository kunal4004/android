package za.co.woolworths.financial.services.android.util.controller

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import za.co.woolworths.financial.services.android.util.CurrencyEditText
import za.co.woolworths.financial.services.android.util.CurrencySymbols

open class CLIFragment : Fragment() {
    @JvmField
	var mCliStepIndicatorListener: CLIStepIndicatorListener? = null
    fun setStepIndicatorListener(cliStepIndicatorListener: CLIStepIndicatorListener?) {
        mCliStepIndicatorListener = cliStepIndicatorListener
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        //show keyboard when any fragment of this class has been attached
        hideSoftKeyboard()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        hideSoftKeyboard()
    }

    override fun onDetach() {
        super.onDetach()
        //hide keyboard when any fragment of this class has been detached
        hideSoftKeyboard()
    }

    /**
     * Hides the soft keyboard
     */
    private fun hideSoftKeyboard() {
        val activity: Activity? = activity
        if (activity != null) {
            if (activity.currentFocus != null) {
                val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
            }
        }
    }


     fun currencyEditTextParams(currencyEditText: CurrencyEditText?) {
        currencyEditText?.apply {
            setCurrencySymbol(CurrencySymbols.DEFAULT)
            setDelimiter(false)
            setSpacing(true)
            setDecimals(true)
            setSeparator(".")
        }
    }
}