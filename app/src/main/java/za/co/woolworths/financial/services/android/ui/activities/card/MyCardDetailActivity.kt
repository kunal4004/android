package za.co.woolworths.financial.services.android.ui.activities.card

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.my_card_activity.*
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.card.GetReplacementCardFragment
import za.co.woolworths.financial.services.android.ui.fragments.card.MyCardBlockedFragment
import za.co.woolworths.financial.services.android.ui.fragments.card.MyCardDetailFragment
import za.co.woolworths.financial.services.android.util.Utils
import android.net.ParseException as ParseException1


class MyCardDetailActivity : AppCompatActivity() {

    private var mCardIsBlocked: Boolean? = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_card_activity)
        Utils.updateStatusBarBackground(this)
        actionBar()

        mCardIsBlocked = Gson().fromJson(getMyStoreCardDetail(), Account::class.java)?.primaryCard?.cardBlocked
                ?: false
        addCardDetailFragment()
    }

    /**
    If cardBlocked == TRUE, display generic card blocked screen
    Else cardBlocked == FALSE , Build the ‘My Card’ Screen using object with latest openedDate
     */

    private fun addCardDetailFragment() {
        when (mCardIsBlocked) {
            true -> {
                addFragment(
                        fragment = MyCardBlockedFragment.newInstance(),
                        tag = MyCardBlockedFragment::class.java.simpleName,
                        containerViewId = R.id.flMyCard)
            }
            else -> {
                addFragment(
                        fragment = MyCardDetailFragment.newInstance(),
                        tag = MyCardDetailFragment::class.java.simpleName,
                        containerViewId = R.id.flMyCard)
            }
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

    private fun getMyStoreCardDetail(): String? = Utils.getSessionDaoValue(this, SessionDao.KEY.STORE_CARD_DETAIL)
            ?: ""
}