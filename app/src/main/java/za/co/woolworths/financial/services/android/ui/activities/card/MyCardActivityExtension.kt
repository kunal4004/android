package za.co.woolworths.financial.services.android.ui.activities.card

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.fragments.card.ProcessBlockCardFragment


open class MyCardActivityExtension : AppCompatActivity() {


    fun navigateToMyCardActivity(cardIsBlocked: Boolean) {
        val openCardDetailActivity = Intent(this, MyCardDetailActivity::class.java)
        openCardDetailActivity.putExtra(ProcessBlockCardFragment.CARD_BLOCKED, cardIsBlocked)
        startActivity(openCardDetailActivity)
        overridePendingTransition(0,0)
    }


    fun hideBackIcon() = supportActionBar?.apply { setDisplayHomeAsUpEnabled(false) }

    fun showBackIcon() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.back24)
        }
    }
}