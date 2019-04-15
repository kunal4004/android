package za.co.woolworths.financial.services.android.ui.fragments.card

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.activities.card.BarcodeScannerActivity
import za.co.woolworths.financial.services.android.ui.activities.card.BlockMyCardActivity
import za.co.woolworths.financial.services.android.ui.activities.card.BlockMyCardActivity.Companion.BLOCK_MY_CARD_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.activities.card.LinkNewCardActivity
import za.co.woolworths.financial.services.android.ui.activities.card.LinkNewCardActivity.Companion.LINK_NEW_CARD_REQUEST_CODE
import za.co.woolworths.financial.services.android.util.KeyboardUtil

open class MyCardExtension : Fragment() {

    fun maskedCardNumberWithSpaces(cardNumber: String?): String {
        return " **** **** **** ".plus(cardNumber?.let { it.substring(it.length - 4, it.length) }
                ?: "")
    }

    fun navigateToBlockMyCardActivity(activity: Activity?) {
        activity?.apply {
            startActivityForResult(Intent(this, BlockMyCardActivity::class.java), BLOCK_MY_CARD_REQUEST_CODE)
            overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
        }
    }

    fun navigateToPermanentCardBlockFragment(activity: AppCompatActivity?) {
        activity?.supportFragmentManager?.apply {
            val permanentCardBlockDialogFragment = PermanentCardBlockDialogFragment.newInstance()
            permanentCardBlockDialogFragment.show((this), PermanentCardBlockDialogFragment::class.java.simpleName)
        }
    }

    fun navigateToResendOTPFragment(activity: AppCompatActivity?) {
        activity?.supportFragmentManager?.apply {
            val resendOTPFragment = ResendOTPFragment.newInstance()
            resendOTPFragment.show((this), ResendOTPFragment::class.java.simpleName)
        }
    }

    internal fun navigateToLinkNewCardActivity(activity: AppCompatActivity?) {
        activity?.apply {
            startActivityForResult(Intent(this, LinkNewCardActivity::class.java), LINK_NEW_CARD_REQUEST_CODE)
            overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
        }
    }

    internal fun navigateToBarCodeScannerActivity(activity: AppCompatActivity?) {
        activity?.apply {
            startActivity(Intent(this, BarcodeScannerActivity::class.java))
            overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
        }
    }

    fun toTitleCase(name: String?): String {
        val words = name?.toLowerCase()?.trim()?.split(" ")?.toMutableList() ?: mutableListOf()
        var output = ""
        for (word in words) {
            output += word.capitalize() + " "
        }
        return output.trim()
    }

    fun showSoftKeyboard(activity: Activity, editTextView: EditText) {
        activity.apply {
            (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.apply {
                showSoftInput(editTextView, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    fun hideKeyboard() {
        activity?.apply { KeyboardUtil.hideSoftKeyboard(this) }
    }

    fun TextView.htmlText(text: String){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY))
        } else {
            setText(Html.fromHtml(text))
        }
    }
}