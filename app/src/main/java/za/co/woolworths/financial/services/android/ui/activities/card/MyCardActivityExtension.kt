package za.co.woolworths.financial.services.android.ui.activities.card

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.fragments.npc.ProcessBlockCardFragment

open class MyCardActivityExtension : AppCompatActivity() {

    fun navigateToMyCardActivity(storeCard: String?,cardIsBlocked: Boolean) {
        val openCardDetailActivity = Intent(this, MyCardDetailActivity::class.java)
        openCardDetailActivity.putExtra(ProcessBlockCardFragment.CARD_BLOCKED, cardIsBlocked)
        openCardDetailActivity.putExtra(MyCardDetailActivity.STORE_CARD_DETAIL, storeCard)
        startActivity(openCardDetailActivity)
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
    }

    fun hideBackIcon() = supportActionBar?.apply { setDisplayHomeAsUpEnabled(false) }

    fun showBackIcon() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.back24)
        }
    }
}