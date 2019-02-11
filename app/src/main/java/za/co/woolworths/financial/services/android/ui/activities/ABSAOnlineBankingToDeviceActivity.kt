package za.co.woolworths.financial.services.android.ui.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.absa_online_banking_to_device_activity.*
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.absa.ABSABiometricFragment
import za.co.woolworths.financial.services.android.util.Utils

class ABSAOnlineBankingToDeviceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.absa_online_banking_to_device_activity)
        Utils.updateStatusBarBackground(this)
        actionBar()
        if (savedInstanceState == null) {
            addFragment(
                    fragment = ABSABiometricFragment.newInstance(),
                    tag = ABSABiometricFragment::class.java.simpleName,
                    containerViewId = R.id.flAbsaOnlineBankingToDevice
            )
        }
    }

    private fun actionBar() {
        setSupportActionBar(tbOnlineBanking)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            setHomeAsUpIndicator(R.drawable.back24)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                this.finish()
                this.overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
                return true
            }
            else -> {
            }
        }
        return false
    }
}