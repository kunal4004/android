package za.co.woolworths.financial.services.android.ui.activities.card

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity.Companion.STORE_CARD_DETAIL
import za.co.woolworths.financial.services.android.ui.fragments.card.ProcessBlockCardFragment


open class MyCardActivityExtension : AppCompatActivity() {


    fun navigateToMyCardActivity(cardIsBlocked: Boolean) {
        val openCardDetailActivity = Intent(this, MyCardDetailActivity::class.java)
        openCardDetailActivity.putExtra(ProcessBlockCardFragment.CARD_BLOCKED, cardIsBlocked)
        startActivity(openCardDetailActivity)
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
    }

    fun navigateToMyCardActivity(cardIsBlocked: Boolean, storeCardDetail: String?) {
        val openCardDetailActivity = Intent(this, MyCardDetailActivity::class.java)
        openCardDetailActivity.putExtra(ProcessBlockCardFragment.CARD_BLOCKED, cardIsBlocked)
        openCardDetailActivity.putExtra(STORE_CARD_DETAIL, storeCardDetail)
        startActivity(openCardDetailActivity)
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
    }

    fun navigateToCardActivity(cardIsBlocked: Boolean, storeCardDetail: String?) {
        val openCardDetailActivity = Intent(this, MyCardDetailActivity::class.java)
        openCardDetailActivity.putExtra(ProcessBlockCardFragment.CARD_BLOCKED, cardIsBlocked)
        openCardDetailActivity.putExtra(STORE_CARD_DETAIL, storeCardDetail)
        startActivity(openCardDetailActivity)
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }


    fun hideBackIcon() = supportActionBar?.apply { setDisplayHomeAsUpEnabled(false) }

    fun showBackIcon() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.back24)
        }
    }
}