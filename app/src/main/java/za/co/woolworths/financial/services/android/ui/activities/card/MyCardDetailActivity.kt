package za.co.woolworths.financial.services.android.ui.activities.card

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.my_card_activity.*
import za.co.woolworths.financial.services.android.ui.activities.card.BlockMyCardActivity.Companion.BLOCK_MY_CARD_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.activities.card.LinkNewCardActivity.Companion.LINK_NEW_CARD_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.extension.replaceFragmentSafely
import za.co.woolworths.financial.services.android.ui.fragments.card.GetReplacementCardFragment
import za.co.woolworths.financial.services.android.ui.fragments.card.MyCardBlockedFragment
import za.co.woolworths.financial.services.android.ui.fragments.card.MyCardDetailFragment
import za.co.woolworths.financial.services.android.ui.fragments.card.MyCardDetailFragment.Companion.CARD
import za.co.woolworths.financial.services.android.ui.fragments.card.ProcessBlockCardFragment.Companion.NPC_CARD_LINKED_SUCCESS_RESULT_CODE
import za.co.woolworths.financial.services.android.util.Utils


class MyCardDetailActivity : AppCompatActivity() {

    private var mCardDetail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_card_activity)
        Utils.updateStatusBarBackground(this)
        actionBar()

        if (savedInstanceState == null) {
            intent?.extras?.apply { mCardDetail = getString(CARD) }

            addCardDetailFragment()
        }
    }

    private fun addCardDetailFragment() {
        mCardDetail?.apply {
            addFragment(
                    fragment = MyCardDetailFragment.newInstance(this),
                    tag = MyCardDetailFragment::class.java.simpleName,
                    containerViewId = R.id.flMyCard)
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
        return when (item.itemId) {
            android.R.id.home -> {
                navigateBack()
                return true
            }
            else -> false
        }
    }

    override fun onBackPressed() {
        navigateBack()
    }

    private fun navigateBack() {
        supportFragmentManager?.apply {
            if (backStackEntryCount > 0) {
                popBackStack()
                when (getCurrentFragment()) {
                    // back pressed from replacement card
                    is GetReplacementCardFragment -> {
                        changeToolbarBackground(R.color.grey_bg)
                        showToolbarTitle()
                    }
                    else -> {
                    }
                }
            } else
                finishActivity()
        }
    }

    private fun finishActivity() {
        this.finish()
        this.overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
    }

    private fun getCurrentFragment(): Fragment? = supportFragmentManager?.findFragmentById(R.id.flMyCard)

    private fun showToolbarTitle() {
        toolbarText?.visibility = VISIBLE
    }

    fun hideToolbarTitle() {
        toolbarText?.visibility = GONE
    }

    fun changeToolbarBackground(colorId: Int) {
        tbMyCard?.setBackgroundColor(ContextCompat.getColor(this, colorId))
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            LINK_NEW_CARD_REQUEST_CODE -> {
                when (resultCode) {
                    NPC_CARD_LINKED_SUCCESS_RESULT_CODE -> addCardDetailFragment()
                }
            }
            BLOCK_MY_CARD_REQUEST_CODE -> {
                when (resultCode) {
                    RESULT_OK -> replaceFragmentSafely(
                            fragment = MyCardBlockedFragment.newInstance(),
                            tag = MyCardBlockedFragment::class.java.simpleName,
                            containerViewId = R.id.flMyCard,
                            allowBackStack = false,
                            allowStateLoss = false
                     )
                }
            }
        }
    }
}
