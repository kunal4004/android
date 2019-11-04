package za.co.woolworths.financial.services.android.ui.activities.temporary_store_card

import android.os.Bundle
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.activity_get_temp_store_card_popup.*
import za.co.woolworths.financial.services.android.contracts.IStoreCardListener
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardActivityExtension
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.extension.replaceFragmentSafely
import za.co.woolworths.financial.services.android.ui.fragments.npc.EnterOtpFragment
import za.co.woolworths.financial.services.android.ui.fragments.temporary_store_card.GetTemporaryStoreCardPopupFragment
import za.co.woolworths.financial.services.android.util.Utils


class GetTemporaryStoreCardPopupActivity : MyCardActivityExtension(), IStoreCardListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_temp_store_card_popup)
        Utils.updateStatusBarBackground(this)
        Utils.setAsVirtualTemporaryStoreCardPopupShown(true)
        actionBar()

        intent?.extras?.apply { mStoreCardDetail = getString(MyCardDetailActivity.STORE_CARD_DETAIL, "") }

        addFragment(
                fragment = GetTemporaryStoreCardPopupFragment.newInstance(mStoreCardDetail),
                tag = GetTemporaryStoreCardPopupFragment::class.java.simpleName,
                containerViewId = R.id.flMyCard)
    }

    private fun actionBar() {
        setSupportActionBar(tbMyCard)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
        }
    }


    override fun onBackPressed() {
        this@GetTemporaryStoreCardPopupActivity.finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }

    override fun navigateToPreviousFragment(errorDescription: String?) {
        super.navigateToPreviousFragment(errorDescription)
        showBackIcon()
        replaceFragmentSafely(
                fragment = EnterOtpFragment.newInstance(mStoreCardDetail),
                tag = EnterOtpFragment::class.java.simpleName,
                containerViewId = R.id.flMyCard,
                allowBackStack = false)
    }
}

