package za.co.woolworths.financial.services.android.ui.activities.credit_card_delivery

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.credit_card_delivery_activity.*
import za.co.woolworths.financial.services.android.util.Utils

class CreditCardDeliveryActivity : AppCompatActivity() {

    var bundle: Bundle? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.credit_card_delivery_activity)
        Utils.updateStatusBarBackground(this,R.color.grey_bg)
        bundle = intent.getBundleExtra("bundle")
        actionBar()
        loadNavHostFragment()
    }

    private fun actionBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            setDisplayHomeAsUpEnabled(true)
        }
    }


    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }

    private fun loadNavHostFragment() {
        findNavController(R.id.nav_host_fragment)
                .setGraph(
                        R.navigation.nav_graph_credit_card_delivery,
                        bundleOf("bundle" to bundle)
                )
    }
}