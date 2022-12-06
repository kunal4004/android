package za.co.woolworths.financial.services.android.ui.activities.card

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.MenuItem
import com.awfs.coordination.R
import com.awfs.coordination.databinding.BlockMyCardActivityBinding
import za.co.woolworths.financial.services.android.contracts.IStoreCardListener
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCard
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity.Companion.STORE_CARD_DETAIL
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.npc.BlockMyCardReasonFragment
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.Utils.PRIMARY_CARD_POSITION

class BlockMyCardActivity : MyCardActivityExtension(), IStoreCardListener {

    companion object {
        const val REQUEST_CODE_BLOCK_MY_CARD = 8073
    }

    private lateinit var binding: BlockMyCardActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BlockMyCardActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Utils.updateStatusBarBackground(this)
        actionBar()

        intent?.extras?.apply {
            mStoreCardDetail = getString(STORE_CARD_DETAIL)
        }

        if (savedInstanceState == null) {
            addFragment(
                    fragment = BlockMyCardReasonFragment.newInstance(),
                    tag = BlockMyCardReasonFragment::class.java.simpleName,
                    containerViewId = R.id.flMyCard
            )
        }

        binding.imCloseIcon?.setOnClickListener {
            finishActivity()
        }
    }

    private fun actionBar() {
        binding.toolbarText?.text = ""
        setSupportActionBar(binding.tbMyCard)
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
        val storeCardIntent = Intent()
        storeCardIntent.putExtra(STORE_CARD_DETAIL, mStoreCardDetail)
        setResult(RESULT_OK, storeCardIntent)
        this.finish()
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
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

    fun getAccountStoreCards(): StoreCard? = getStoreCardDetail().storeCardsData?.primaryCards?.get(PRIMARY_CARD_POSITION)

    fun iconVisibility(state: Int) {
        binding.imCloseIcon?.visibility = state
    }
}