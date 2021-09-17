package za.co.woolworths.financial.services.android.ui.activities

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.credit_card_activation_activity.*
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.fragments.mypreferences.LinkDeviceOTPFragment
import za.co.woolworths.financial.services.android.util.Utils

class CreditCardActivationActivity : AppCompatActivity() {

    var bundle: Bundle? = null
    var applyNowState: ApplyNowState? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.credit_card_activation_activity)
        Utils.updateStatusBarBackground(this)
        bundle = intent.getBundleExtra("bundle")
        applyNowState = intent.getSerializableExtra(
            AccountSignedInPresenterImpl.APPLY_NOW_STATE) as ApplyNowState
        actionBar()
        loadNavHostFragment()
    }

    private fun actionBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_item, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if(applyNowState != null){
            setResult(LinkDeviceOTPFragment.GO_TO_PRODUCT)
            finish()
        }else{
            setResult(Activity.RESULT_CANCELED)
            finish()
            overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
        }
    }

    private fun loadNavHostFragment() {
        findNavController(R.id.nav_host_fragment)
                .setGraph(
                        R.navigation.nav_graph_credit_card_activation,
                        bundleOf("bundle" to bundle)
                )
    }
}