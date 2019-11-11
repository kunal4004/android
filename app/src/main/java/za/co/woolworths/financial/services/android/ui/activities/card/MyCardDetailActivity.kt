package za.co.woolworths.financial.services.android.ui.activities.card

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.my_card_activity.*
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.contracts.IStoreCardListener
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.activities.card.BlockMyCardActivity.Companion.REQUEST_CODE_BLOCK_MY_CARD
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.npc.GetReplacementCardFragment
import za.co.woolworths.financial.services.android.ui.fragments.npc.MyCardBlockedFragment
import za.co.woolworths.financial.services.android.ui.fragments.npc.MyCardDetailFragment
import za.co.woolworths.financial.services.android.ui.fragments.npc.MyCardExtension
import za.co.woolworths.financial.services.android.util.Utils


class MyCardDetailActivity : AppCompatActivity(), IStoreCardListener {

    companion object {
        const val STORE_CARD_DETAIL = "STORE_CARD_DETAIL"
    }

    private var mStoreCardDetail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_card_activity)
        Utils.updateStatusBarBackground(this)
        actionBar()

        intent?.extras?.apply {
            mStoreCardDetail = getString(STORE_CARD_DETAIL, "")
        }
        addCardDetailFragment()
    }

    private fun addCardDetailFragment() {
        val blockCode = Gson().fromJson(getMyStoreCardDetail(), StoreCardsResponse::class.java)?.storeCardsData?.primaryCards?.get(0)?.blockCode
        val virtualCard = Gson().fromJson(getMyStoreCardDetail(), StoreCardsResponse::class.java)?.storeCardsData?.virtualCard
        // Determine if card is blocked: if blockCode is not null, card is blocked.
        when ((virtualCard != null && WoolworthsApplication.getVirtualTempCard()?.isEnabled == true) || TextUtils.isEmpty(blockCode)) {
            true -> {
                addFragment(
                        fragment = MyCardDetailFragment.newInstance(mStoreCardDetail),
                        tag = MyCardDetailFragment::class.java.simpleName,
                        containerViewId = R.id.flMyCard)
            }
            else -> {
                addFragment(
                        fragment = MyCardBlockedFragment.newInstance(mStoreCardDetail),
                        tag = MyCardBlockedFragment::class.java.simpleName,
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
                    is MyCardBlockedFragment, is MyCardDetailFragment -> {
                        finishActivity()
                    }
                }
            } else
                fragmentStack()
        }
    }

    private fun fragmentStack() {
        when (getCurrentFragment()) {
            // back pressed from replacement card
            is MyCardBlockedFragment, is MyCardDetailFragment -> {
                this.finish()
                this.overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
            }
            else -> {
                finishActivity()
            }
        }
    }

    private fun finishActivity() {
        setResult(RESULT_OK, Intent().putExtra(STORE_CARD_DETAIL, mStoreCardDetail))
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

    private fun getMyStoreCardDetail(): String? = mStoreCardDetail ?: ""

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_BLOCK_MY_CARD && resultCode == RESULT_OK) {
            mStoreCardDetail = data?.getStringExtra(STORE_CARD_DETAIL)
            addCardDetailFragment()
        } else if (requestCode == MyCardExtension.INSTANT_STORE_CARD_REPLACEMENT_REQUEST_CODE && resultCode == RESULT_OK) { // close previous cart detail
            finish()
        } else {
            supportFragmentManager.findFragmentById(R.id.flMyCard)?.onActivityResult(requestCode, resultCode, data)
        }
    }

    // Required to delegate permission result to fragment
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        supportFragmentManager.findFragmentById(R.id.flMyCard)?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun getStoreCardDetail() = mStoreCardDetail
}