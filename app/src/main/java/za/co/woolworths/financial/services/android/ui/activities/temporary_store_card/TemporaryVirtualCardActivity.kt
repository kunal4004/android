package za.co.woolworths.financial.services.android.ui.activities.temporary_store_card

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.my_card_activity.*
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.temp_virtual_card.TemporaryVirtualCardDetailsFragment
import za.co.woolworths.financial.services.android.util.Utils

class TemporaryVirtualCardActivity : AppCompatActivity() {

    companion object {
        const val STORE_CARD_DETAIL = "STORE_CARD_DETAIL"
    }

    private var mStoreCardDetail: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.temporary_virtual_card_activity)
        Utils.updateStatusBarBackground(this)
        actionBar()

        intent?.extras?.apply {
            mStoreCardDetail = getString(STORE_CARD_DETAIL, "")
        }
        addCardDetailFragment()
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

    private fun addCardDetailFragment() {
        addFragment(
                fragment = TemporaryVirtualCardDetailsFragment.newInstance(),
                tag = TemporaryVirtualCardDetailsFragment::class.java.simpleName,
                containerViewId = R.id.flMyCard)
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
            if (backStackEntryCount > 0) popBackStack() else finishActivity()
        }
    }

    private fun finishActivity() {
        this.finish()
        this.overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
    }


}