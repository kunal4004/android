package za.co.woolworths.financial.services.android.ui.activities.temp_virtual_card

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.temp_card_how_to_use_layout.*
import za.co.woolworths.financial.services.android.util.Utils

class HowToUseTemporaryStoreCardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.temp_card_how_to_use_layout)
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

