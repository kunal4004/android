package za.co.woolworths.financial.services.android.ui.activities.temporary_store_card

import android.os.Bundle
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ActivityGetTempStoreCardPopupBinding
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardActivityExtension
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.temporary_store_card.GetTemporaryStoreCardPopupFragment
import za.co.woolworths.financial.services.android.util.Utils

class GetTemporaryStoreCardPopupActivity : MyCardActivityExtension() {

    private lateinit var binding: ActivityGetTempStoreCardPopupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetTempStoreCardPopupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Utils.updateStatusBarBackground(this)
        Utils.updateUserVirtualTempCardState(true)
        actionBar()

        intent?.extras?.apply { mStoreCardDetail = getString(MyCardDetailActivity.STORE_CARD_DETAIL, "") }

        addFragment(
                fragment = GetTemporaryStoreCardPopupFragment.newInstance(mStoreCardDetail),
                tag = GetTemporaryStoreCardPopupFragment::class.java.simpleName,
                containerViewId = R.id.flMyCard)
    }

    private fun actionBar() {
        setSupportActionBar(binding.tbMyCard)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
        }
    }

    override fun onStart() {
        super.onStart()
        overridePendingTransition(R.anim.slide_up_anim_duration_1000, R.anim.stay_duration_1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        overridePendingTransition(R.anim.stay_duration_1000, R.anim.slide_down_anim_1000)
    }

}

