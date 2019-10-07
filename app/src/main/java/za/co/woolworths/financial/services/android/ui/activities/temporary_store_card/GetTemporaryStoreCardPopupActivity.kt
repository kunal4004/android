package za.co.woolworths.financial.services.android.ui.activities.temporary_store_card

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.activity_get_temp_store_card_popup.*
import za.co.woolworths.financial.services.android.util.Utils

class GetTemporaryStoreCardPopupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_temp_store_card_popup)
        Utils.updateStatusBarBackground(this)
        actionBar()
    }

    private fun actionBar() {
        setSupportActionBar(tbHowToUse)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater?.inflate(R.menu.search_item, menu)
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
}

