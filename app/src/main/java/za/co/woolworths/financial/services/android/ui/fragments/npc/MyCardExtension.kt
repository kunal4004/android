package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import android.text.Html
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.link_card_fragment.*
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity.Companion.REQUEST_CODE_BLOCK_MY_STORE_CARD
import za.co.woolworths.financial.services.android.ui.activities.card.BlockMyCardActivity
import za.co.woolworths.financial.services.android.ui.activities.card.BlockMyCardActivity.Companion.REQUEST_CODE_BLOCK_MY_CARD
import za.co.woolworths.financial.services.android.ui.activities.card.InstantStoreCardReplacementActivity
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity.Companion.STORE_CARD_DETAIL
import za.co.woolworths.financial.services.android.ui.fragments.npc.ProcessBlockCardFragment.Companion.CARD_BLOCKED
import za.co.woolworths.financial.services.android.util.KeyboardUtil

open class MyCardExtension : Fragment() {

    companion object {
        @SuppressLint("DefaultLocale")
        const val INSTANT_STORE_CARD_REPLACEMENT_REQUEST_CODE = 555
        fun toTitleCase(name: String?): String {
            val words = name?.toLowerCase()?.trim()?.split(" ")?.toMutableList() ?: mutableListOf()
            var output = ""
            for (word in words) {
                output += word.capitalize() + " "
            }
            return output.trim()
        }
    }

    fun maskedCardNumberWithSpaces(cardNumber: String?): String {
        return " **** **** **** ".plus(cardNumber?.let { it.substring(it.length - 4, it.length) }
                ?: "")
    }

    fun navigateToBlockMyCardActivity(activity: Activity?, storeCardDetail: String?) {
        activity?.apply {
            val openBlockMyCardActivity = Intent(this, BlockMyCardActivity::class.java)
            openBlockMyCardActivity.putExtra(STORE_CARD_DETAIL, storeCardDetail)
            startActivityForResult(openBlockMyCardActivity, REQUEST_CODE_BLOCK_MY_CARD)
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
        }
    }

    fun navigateToPermanentCardBlockFragment(activity: AppCompatActivity?) {
        activity?.supportFragmentManager?.apply {
            val permanentCardBlockDialogFragment = BlockMyCardReasonConfirmationFragment.newInstance()
            permanentCardBlockDialogFragment.show((this), BlockMyCardReasonConfirmationFragment::class.java.simpleName)
        }
    }

    internal fun navigateToLinkNewCardActivity(activity: AppCompatActivity?, storeCard: String?) {
        activity?.apply {
            val openLinkNewCardActivity = Intent(this, InstantStoreCardReplacementActivity::class.java)
            openLinkNewCardActivity.putExtra(STORE_CARD_DETAIL, storeCard)
            startActivityForResult(openLinkNewCardActivity,INSTANT_STORE_CARD_REPLACEMENT_REQUEST_CODE)
            overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
        }
    }

    fun showSoftKeyboard(activity: Activity, editTextView: EditText) {
        activity.apply {
            (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.apply {
                showSoftInput(editTextView, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    open fun hideKeyboard() {
        activity?.apply { KeyboardUtil.hideSoftKeyboard(this) }
    }

    fun TextView.htmlText(text: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY))
        } else {
            setText(Html.fromHtml(text))
        }
    }

    fun navigateToMyCardActivity(storeCardDetail: String?) {
        activity?.apply {
            startActivityForResult(Intent(this, MyCardDetailActivity::class.java).putExtra(STORE_CARD_DETAIL, storeCardDetail), REQUEST_CODE_BLOCK_MY_STORE_CARD)
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
        }
    }

    fun navigateToMyCardActivity(cardIsBlocked: Boolean) {
        activity?.apply {
            startActivity(Intent(this, MyCardDetailActivity::class.java).putExtra(CARD_BLOCKED, cardIsBlocked))
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
            finish()
        }
    }

    fun showToolbar() {
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
        }
    }

    fun hideToolbarIcon() {
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
        }
    }

    fun invalidCardNumberUI() {
        activity?.apply {
            cardNumberEditText?.background = ContextCompat.getDrawable(this, R.drawable.input_box_error_bg)
            invalidCardNumberLabel?.visibility = View.VISIBLE
            navigateToEnterOTPFragmentImageView?.isEnabled = false
        }
    }

    fun validCardNumberUI() {
        activity?.apply {
            cardNumberEditText?.background = ContextCompat.getDrawable(this, R.drawable.input_box_inactive_bg)
            invalidCardNumberLabel?.visibility = View.GONE
            navigateToEnterOTPFragmentImageView?.isEnabled = true
        }
    }
}