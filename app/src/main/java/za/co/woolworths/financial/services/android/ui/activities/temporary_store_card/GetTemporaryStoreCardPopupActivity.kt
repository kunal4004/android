package za.co.woolworths.financial.services.android.ui.activities.temporary_store_card

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_get_temp_store_card_popup.*
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardActivityExtension
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.temporary_store_card.GetTemporaryStoreCardPopupFragment
import za.co.woolworths.financial.services.android.util.Utils

class GetTemporaryStoreCardPopupActivity : MyCardActivityExtension() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_temp_store_card_popup)
        Utils.updateStatusBarBackground(this)
        Utils.setAsVirtualTemporaryStoreCardPopupShown(true)
        actionBar()

        intent?.extras?.apply {
            mStoreCardDetail = getString(MyCardDetailActivity.STORE_CARD_DETAIL, "")
        }

        addFragment(
                fragment = GetTemporaryStoreCardPopupFragment.newInstance(mStoreCardDetail),
                tag = GetTemporaryStoreCardPopupFragment::class.java.simpleName,
                containerViewId = R.id.flMyCard)
    }

    private fun actionBar() {
        setSupportActionBar(tbMyCard)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_item, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() {
        this.finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> onBackPressed()
        }
        return false
    }

    fun getStoreCardDetail(): StoreCardsResponse = Gson().fromJson(mStoreCardDetail, StoreCardsResponse::class.java)
}

