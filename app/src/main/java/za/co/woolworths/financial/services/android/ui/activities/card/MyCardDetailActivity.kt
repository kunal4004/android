package za.co.woolworths.financial.services.android.ui.activities.card

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.my_card_activity.*
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.activities.card.BlockMyCardActivity.Companion.REQUEST_CODE_BLOCK_MY_CARD
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.npc.GetReplacementCardFragment
import za.co.woolworths.financial.services.android.ui.fragments.npc.MyCardBlockedFragment
import za.co.woolworths.financial.services.android.ui.fragments.npc.MyCardDetailFragment
import za.co.woolworths.financial.services.android.util.Utils


class MyCardDetailActivity : AppCompatActivity() {

    companion object {
        const val STORE_CARD_DETAIL = "STORE_CARD_DETAIL"
        const val VISION_ACCOUNT_NUMBER = "VISION_ACCOUNT_NUMBER"
        const val PRODUCT_OFFERING_ID = "PRODUCT_OFFERING_ID"
    }

    private var mStoreCardDetail: String? = null
    private var visionAccountNumber: String? = null
    private var productOfferingId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_card_activity)
        Utils.updateStatusBarBackground(this)
        actionBar()

        intent?.extras?.apply {
            mStoreCardDetail = getString(STORE_CARD_DETAIL, "")
            visionAccountNumber = getString(VISION_ACCOUNT_NUMBER, "")
            productOfferingId = getString(PRODUCT_OFFERING_ID, "")
        }
        addCardDetailFragment()
    }

    private fun addCardDetailFragment() {

        when (Gson().fromJson(getMyStoreCardDetail(), StoreCardsResponse::class.java)?.storeCardsData?.primaryCards?.get(0)?.blockCode != null) {
            true -> {
                addFragment(
                        fragment = MyCardBlockedFragment.newInstance(mStoreCardDetail),
                        tag = MyCardBlockedFragment::class.java.simpleName,
                        containerViewId = R.id.flMyCard)
            }
            else -> {
                addFragment(
                        fragment = MyCardDetailFragment.newInstance(mStoreCardDetail, visionAccountNumber, productOfferingId),
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
        }else{
            supportFragmentManager.findFragmentById(R.id.flMyCard)?.onActivityResult(requestCode, resultCode, data)
        }
    }

    // Required to delegate permission result to fragment
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        supportFragmentManager.findFragmentById(R.id.flMyCard)?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}