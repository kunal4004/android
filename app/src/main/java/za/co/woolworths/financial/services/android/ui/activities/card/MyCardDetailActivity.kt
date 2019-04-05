package za.co.woolworths.financial.services.android.ui.activities.card

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.my_card_activity.*
import za.co.woolworths.financial.services.android.ui.activities.card.BlockMyCardActivity.Companion.BLOCK_MY_CARD_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.card.MyCardBlockedFragment
import za.co.woolworths.financial.services.android.ui.fragments.card.MyCardDetailFragment
import za.co.woolworths.financial.services.android.util.Utils

class MyCardDetailActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_card_activity)
        Utils.updateStatusBarBackground(this)
        actionBar()

        if (savedInstanceState == null) {
            addFragment(
                    fragment = MyCardDetailFragment.newInstance(),
                    tag = MyCardDetailFragment::class.java.simpleName,
                    containerViewId = R.id.flMyCard
            )
        }
    }

    private fun actionBar() {
        setSupportActionBar(tbMyCard)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            setHomeAsUpIndicator(R.drawable.back24)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                navigateBack()
                return true
            }
        }
        return false
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

    private fun finishActivity() {
        this.finish()
        this.overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
    }

    private fun getCurrentFragment(): Fragment? {
        return supportFragmentManager?.findFragmentById(R.id.flMyCard)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == BLOCK_MY_CARD_REQUEST_CODE && resultCode == RESULT_OK) {
            addFragment(
                    fragment = MyCardBlockedFragment.newInstance(),
                    tag = MyCardBlockedFragment::class.java.simpleName,
                    containerViewId = R.id.flMyCard
            )
        }
    }
}