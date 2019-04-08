package za.co.woolworths.financial.services.android.ui.fragments.card

import android.app.Activity
import android.content.Intent
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.activities.card.BlockMyCardActivity
import za.co.woolworths.financial.services.android.ui.activities.card.BlockMyCardActivity.Companion.BLOCK_MY_CARD_REQUEST_CODE

open class MyCardExtension : Fragment() {

    fun maskedCardNumberWithSpaces(cardNumber: String?): String {
        return " **** **** **** ".plus(cardNumber?.let { it.substring(it.length - 4, it.length) }
                ?: "")
    }

    fun navigateToBlockMyCardActivity(activity: Activity?) {
        activity?.apply {
            val openBlockMyCardActivity = Intent(this, BlockMyCardActivity::class.java)
            this.startActivityForResult(openBlockMyCardActivity, BLOCK_MY_CARD_REQUEST_CODE)
            this.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
        }
    }

    fun navigateToPermanentCardBlockFragment(activity: AppCompatActivity?) {
        activity?.supportFragmentManager?.apply {
            val permanentCardBlockDialogFragment = PermanentCardBlockDialogFragment.newInstance()
            permanentCardBlockDialogFragment.show((this), PermanentCardBlockDialogFragment::class.java.simpleName
            )
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
}