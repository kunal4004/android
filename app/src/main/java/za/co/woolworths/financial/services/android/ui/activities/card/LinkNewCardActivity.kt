package za.co.woolworths.financial.services.android.ui.activities.card

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.my_card_activity.*
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.card.LinkCardFragment
import za.co.woolworths.financial.services.android.util.Utils


class LinkNewCardActivity : AppCompatActivity() {

    companion object {
        const val LINK_NEW_CARD_REQUEST_CODE = 5001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.block_my_card_activity)
        Utils.updateStatusBarBackground(this)
        actionBar()
        if (savedInstanceState == null) {
            addFragment(
                    fragment = LinkCardFragment.newInstance(),
                    tag = LinkCardFragment::class.java.simpleName,
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
        supportFragmentManager?.apply {
            if (backStackEntryCount > 0) {
                fragments[backStackEntryCount - 1]?.onResume()
                popBackStack()

            } else {
                finishActivity()
            }
        }
    }


    private fun finishActivity() {
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
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

    fun showBackIcon() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.back24)
        }
    }

    fun hideBackIcon() = supportActionBar?.apply { setDisplayHomeAsUpEnabled(false) }
}