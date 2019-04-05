package za.co.woolworths.financial.services.android.ui.activities.card

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.my_card_activity.*
import za.co.woolworths.financial.services.android.contracts.IPermanentCardBlock
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.card.BlockMyCardReasonFragment
import za.co.woolworths.financial.services.android.util.Utils


class BlockMyCardActivity : AppCompatActivity(), IPermanentCardBlock {

    companion object {
        const val BLOCK_MY_CARD_REQUEST_CODE = 5002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.block_my_card_activity)
        Utils.updateStatusBarBackground(this)
        actionBar()

        if (savedInstanceState == null) {
            addFragment(
                    fragment = BlockMyCardReasonFragment.newInstance(),
                    tag = BlockMyCardReasonFragment::class.java.simpleName,
                    containerViewId = R.id.flMyCard
            )
        }
    }

    private fun actionBar() {
        toolbarText?.text = ""
        setSupportActionBar(tbMyCard)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
        }
    }

    override fun onBackPressed() {
        navigateBack()
    }

    private fun navigateBack() {
        supportFragmentManager?.apply {
            if (backStackEntryCount > 0)
                popBackStack()
            else
                finishActivity()
        }
    }

    override fun onBlockPermanentCardPermissionGranted() {
        Log.e("onBlockPermane", "onBlockPermanentCardPermissionGranted")
        if (getCurrentFragment() is BlockMyCardReasonFragment) {
            (getCurrentFragment() as? BlockMyCardReasonFragment)?.processBlockCardRequest()
        }
    }

    private fun finishActivity() {
        this.finish()
        this.overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater?.inflate(R.menu.search_item, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> finishActivity()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getCurrentFragment(): Fragment? {
        return supportFragmentManager?.findFragmentById(R.id.flMyCard)
    }
}