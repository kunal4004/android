package za.co.woolworths.financial.services.android.ui.activities.temporary_store_card

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.temp_card_how_to_use_layout.*
import za.co.woolworths.financial.services.android.models.dto.Transaction
import za.co.woolworths.financial.services.android.models.dto.npc.Transition
import za.co.woolworths.financial.services.android.util.Utils

class HowToUseTemporaryStoreCardActivity : AppCompatActivity() {

    companion object {
        var TRANSACTION_TYPE = "TRANSACTION_TYPE"
    }

    var type: Transition = Transition.SLIDE_LEFT
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.temp_card_how_to_use_layout)
        Utils.updateStatusBarBackground(this)
        type = intent?.extras?.getSerializable(TRANSACTION_TYPE) as Transition
        actionBar()
    }

    private fun actionBar() {
        setSupportActionBar(tbHowToUse)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            if (type == Transition.SLIDE_LEFT) {
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.drawable.back24)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (type != Transition.SLIDE_LEFT)
            menuInflater?.inflate(R.menu.search_item, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() {
        this.finish()
        if (type == Transition.SLIDE_LEFT)
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
        else
            overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search, android.R.id.home -> onBackPressed()
        }
        return false
    }
}

