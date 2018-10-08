package za.co.woolworths.financial.services.android.ui.activities.bpi

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.fragments.bpi.BPIOverviewFragment
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.addFragment

class BPIBalanceProtectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bpi_activity)
        Utils.updateStatusBarBackground(this)

        val bundle = intent.extras
        var accountInfo: String? = ""
        if (bundle != null) {
            accountInfo = bundle.getString("account_info")
        }

        if (savedInstanceState == null) {
            addFragment(
                    fragment = BPIOverviewFragment.newInstance(accountInfo),
                    tag = BPIOverviewFragment::class.java.simpleName,
                    containerViewId = R.id.flBPIContainer
            )
        }
    }

    fun finishActivity() {
        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
        } else {
            finish()
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
        }
    }

    override fun onBackPressed() {
        finishActivity()
    }
}