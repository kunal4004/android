package za.co.woolworths.financial.services.android.ui.activities.card

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.awfs.coordination.R
import com.awfs.coordination.databinding.BlockMyCardActivityBinding
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity.Companion.CARD_NUMBER
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity.Companion.STORE_CARD_DETAIL
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.npc.EnterOtpFragment
import za.co.woolworths.financial.services.android.ui.fragments.npc.ICREnterCardNumberFragment
import za.co.woolworths.financial.services.android.util.Utils

class InstantStoreCardReplacementActivity : MyCardActivityExtension() {

    private lateinit var binding: BlockMyCardActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BlockMyCardActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Utils.updateStatusBarBackground(this)
        actionBar()

        intent?.extras?.apply {
            mStoreCardDetail = getString(STORE_CARD_DETAIL)
            mCardNumber = getString(CARD_NUMBER)
        }

        if (savedInstanceState == null) {
            addFragment(
                    fragment = ICREnterCardNumberFragment.newInstance(),
                    tag = ICREnterCardNumberFragment::class.java.simpleName,
                    containerViewId = R.id.flMyCard)
        }
    }

    private fun actionBar() {
        binding.toolbarText?.text = ""
        setSupportActionBar(binding.tbMyCard)
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
        if (requestOTPFragmentIsActivated) return //prevent back navigation when getOTP call is running
        when (supportFragmentManager.findFragmentById(R.id.flMyCard)) {
            is EnterOtpFragment -> {
                val openLinkNewCardActivity = Intent(this, InstantStoreCardReplacementActivity::class.java)
                openLinkNewCardActivity.putExtra(CARD_NUMBER, getCardNumber())
                openLinkNewCardActivity.putExtra(STORE_CARD_DETAIL, mStoreCardDetail)
                startActivity(openLinkNewCardActivity)
                overridePendingTransition(0, 0)
                finish()
            }
            else -> finishActivity()
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