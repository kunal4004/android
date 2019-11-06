package za.co.woolworths.financial.services.android.ui.activities.card

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.my_card_activity.*
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.npc.InstantStoreCardFragment
import za.co.woolworths.financial.services.android.util.Utils

class InstantStoreCardReplacementActivity : MyCardActivityExtension(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.block_my_card_activity)
        Utils.updateStatusBarBackground(this)
        actionBar()

        intent?.extras?.apply {
            mStoreCardDetail = getString(MyCardDetailActivity.STORE_CARD_DETAIL)
        }

        if (savedInstanceState == null) {
            addFragment(
                    fragment = InstantStoreCardFragment.newInstance(),
                    tag = InstantStoreCardFragment::class.java.simpleName,
                    containerViewId = R.id.flMyCard)
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
        with(supportFragmentManager) {
            if (backStackEntryCount > 0) {
                popBackStack()

            } else {
                finishActivity()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_item, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> finishActivity()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun finishActivity() {
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
        navigateToMyCardActivity(mStoreCardDetail, true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val instanceFragment = supportFragmentManager.findFragmentById(R.id.flMyCard)
        instanceFragment?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        supportFragmentManager.findFragmentById(R.id.flMyCard)?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}