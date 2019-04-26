package za.co.woolworths.financial.services.android.ui.activities.card

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.MenuItem
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.my_card_activity.*
import za.co.woolworths.financial.services.android.contracts.IPermanentCardBlock
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.card.BlockMyCardReasonFragment
import za.co.woolworths.financial.services.android.util.Utils


class BlockMyCardActivity : MyCardActivityExtension(), IPermanentCardBlock {

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
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            setHomeAsUpIndicator(R.drawable.back24)
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
        if (getCurrentFragment() is BlockMyCardReasonFragment) {
            (getCurrentFragment() as? BlockMyCardReasonFragment)?.processBlockCardRequest()
        }
    }

    private fun finishActivity() {
        this.finish()
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
        navigateToMyCardActivity(false)
    }

    private fun getCurrentFragment(): Fragment? {
        return supportFragmentManager?.findFragmentById(R.id.flMyCard)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                navigateBack()
                return true
            }
            else -> false
        }
    }

}