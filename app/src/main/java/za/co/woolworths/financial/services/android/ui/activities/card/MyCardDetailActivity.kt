package za.co.woolworths.financial.services.android.ui.activities.card

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.my_card_activity.*
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.card.GetReplacementCardFragment
import za.co.woolworths.financial.services.android.ui.fragments.card.MyCardBlockedFragment
import za.co.woolworths.financial.services.android.ui.fragments.card.MyCardDetailFragment
import za.co.woolworths.financial.services.android.ui.fragments.card.ProcessBlockCardFragment
import za.co.woolworths.financial.services.android.util.Utils


class MyCardDetailActivity : AppCompatActivity() {

    companion object {
        const val STORE_CARD_DETAIL = "STORE_CARD_DETAIL"
    }

    private var mCardIsBlocked: Boolean? = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_card_activity)
        Utils.updateStatusBarBackground(this)
        actionBar()

        if (savedInstanceState == null) {
            intent?.extras?.apply {
                getString(STORE_CARD_DETAIL)
                mCardIsBlocked = getBoolean(ProcessBlockCardFragment.CARD_BLOCKED, false)
            }
            addCardDetailFragment()
        }
    }

    private fun addCardDetailFragment() {
//        SessionDao.getByKey(SessionDao.KEY.STORE_CARD).value?.apply {
//            when (mCardIsBlocked) {
//                true -> {
//                    addFragment(
//                            fragment = MyCardBlockedFragment.newInstance(),
//                            tag = MyCardBlockedFragment::class.java.simpleName,
//                            containerViewId = R.id.flMyCard)
//                }
//                else -> {
//                    addFragment(
//                            fragment = MyCardDetailFragment.newInstance(this),
//                            tag = MyCardDetailFragment::class.java.simpleName,
//                            containerViewId = R.id.flMyCard)
//                }
//            }
//        }
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

    override fun onBackPressed() = navigateBack()


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


    fun changeToolbarBackground(colorId: Int) = tbMyCard?.setBackgroundColor(ContextCompat.getColor(this, colorId))
}
