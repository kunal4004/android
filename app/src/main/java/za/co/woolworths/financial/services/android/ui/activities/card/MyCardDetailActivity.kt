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
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.contracts.IStoreCardListener
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.activities.WPdfViewerActivity
import za.co.woolworths.financial.services.android.ui.activities.card.BlockMyCardActivity.Companion.REQUEST_CODE_BLOCK_MY_CARD
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.freeze.TemporaryFreezeStoreCard
import za.co.woolworths.financial.services.android.ui.fragments.account.freeze.TemporaryFreezeStoreCard.Companion.ACTIVATE_UNBLOCK_CARD_ON_LANDING
import za.co.woolworths.financial.services.android.ui.fragments.npc.GetReplacementCardFragment
import za.co.woolworths.financial.services.android.ui.fragments.npc.MyCardBlockedFragment
import za.co.woolworths.financial.services.android.ui.fragments.npc.MyCardDetailFragment
import za.co.woolworths.financial.services.android.ui.fragments.npc.MyCardExtension
import za.co.woolworths.financial.services.android.ui.fragments.npc.ProcessBlockCardFragment.Companion.RESULT_CODE_BLOCK_CODE_SUCCESS
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.Utils.PRIMARY_CARD_POSITION
import za.co.woolworths.financial.services.android.util.WFormatter
import za.co.woolworths.financial.services.android.util.wenum.StoreCardViewType
import java.util.*


class MyCardDetailActivity : AppCompatActivity(), IStoreCardListener {

    companion object {
        const val REFRESH_MY_CARD_DETAILS = "refreshMyCardDetails"
        const val STORE_CARD_DETAIL = "STORE_CARD_DETAIL"
        const val STORE_CARD_VIEW_TYPE = "STORE_CARD_VIEW_TYPE"
        const val CARD_NUMBER = "CARD_NUMBER"
        const val TEMPORARY_FREEZE_STORE_CARD_RESULT_CODE = 3212
        const val ACTIVATE_VIRTUAL_TEMP_CARD_RESULT_CODE = 3213
        const val REQUEST_CODE_GET_REPLACEMENT_CARD = 3214
    }

    private var mStoreCardScreenType: StoreCardViewType? = StoreCardViewType.DEFAULT
    var shouldActivateUnblockCardOnLanding: Boolean = false
    var shouldNotifyStateChanged = false
    private var mStoreCardDetail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_card_activity)
        Utils.updateStatusBarBackground(this)
        actionBar()

        intent?.extras?.apply {
            mStoreCardDetail = getString(STORE_CARD_DETAIL, "")
            mStoreCardScreenType = getSerializable(STORE_CARD_VIEW_TYPE) as? StoreCardViewType
                    ?: StoreCardViewType.DEFAULT
            shouldActivateUnblockCardOnLanding = getBoolean(ACTIVATE_UNBLOCK_CARD_ON_LANDING, false)
        }
        addCardDetailFragment()

        uniqueIdsForCardDetails()
    }

    private fun uniqueIdsForCardDetails() {
        tbMyCard?.contentDescription = getString(R.string.toolbar_title)

    }

    private fun addCardDetailFragment() {
        val primaryCard = Gson().fromJson(getMyStoreCardDetail(), StoreCardsResponse::class.java)?.storeCardsData?.primaryCards?.get(PRIMARY_CARD_POSITION)
        val blockType = primaryCard?.blockType?.toLowerCase(Locale.getDefault())
        val shouldDisplayStoreCardDetail = TextUtils.isEmpty(blockType) || blockType == TemporaryFreezeStoreCard.TEMPORARY
        val virtualCard = Gson().fromJson(getMyStoreCardDetail(), StoreCardsResponse::class.java)?.storeCardsData?.virtualCard
        // Determine if card is blocked: if blockCode is not null, card is blocked.
        when ((virtualCard != null && WoolworthsApplication.getVirtualTempCard()?.isEnabled == true)
                || shouldDisplayStoreCardDetail
                && blockType != TemporaryFreezeStoreCard.PERMANENT) {
            true -> {
                addFragment(
                        fragment = MyCardDetailFragment.newInstance(mStoreCardDetail, shouldActivateUnblockCardOnLanding),
                        tag = MyCardDetailFragment::class.java.simpleName,
                        containerViewId = R.id.flMyCard)
            }
            else -> {
                when (mStoreCardScreenType) {

                    StoreCardViewType.DEFAULT -> {
                        addFragment(
                                fragment = MyCardBlockedFragment.newInstance(mStoreCardDetail),
                                tag = MyCardBlockedFragment::class.java.simpleName,
                                containerViewId = R.id.flMyCard)
                    }
                }
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
            // Disable onBackPressed when temporary freeze card api is executing
            if (getCurrentFragment() is MyCardDetailFragment) {
                if ((getCurrentFragment() as? MyCardDetailFragment)?.isTemporaryCardFreezeInProgress() == true) return@apply
            }
            if (backStackEntryCount > 0) {
                popBackStack()
                when (getCurrentFragment()) {
                    // back pressed from replacement card
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
                if (shouldNotifyStateChanged) {
                    setResult(TEMPORARY_FREEZE_STORE_CARD_RESULT_CODE)
                }
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
        } else if ((requestCode == MyCardExtension.INSTANT_STORE_CARD_REPLACEMENT_REQUEST_CODE && resultCode == RESULT_OK) ||
                (requestCode == REQUEST_CODE_BLOCK_MY_CARD && resultCode == RESULT_CODE_BLOCK_CODE_SUCCESS) ||
                (requestCode == MyCardExtension.INSTANT_STORE_CARD_REPLACEMENT_REQUEST_CODE && resultCode == RESULT_CODE_BLOCK_CODE_SUCCESS)) { // close previous cart detail
            setResult(TEMPORARY_FREEZE_STORE_CARD_RESULT_CODE)
            finish() // will close previous activity in stack
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