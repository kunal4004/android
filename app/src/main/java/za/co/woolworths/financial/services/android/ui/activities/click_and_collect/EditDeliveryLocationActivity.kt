package za.co.woolworths.financial.services.android.ui.activities.click_and_collect

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.edit_delivery_location_activity.*
import za.co.woolworths.financial.services.android.util.Utils

class EditDeliveryLocationActivity : AppCompatActivity() {

    var bundle: Bundle? = null


    companion object {
        var REQUEST_CODE = 1515
        var DELIVERY_TYPE = "DELIVERY_TYPE"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_delivery_location_activity)
        Utils.updateStatusBarBackground(this)
        bundle = intent.getBundleExtra("bundle")
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
        setResult(Activity.RESULT_CANCELED)
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }

    private fun loadNavHostFragment() {
        findNavController(R.id.nav_host_fragment)
                .setGraph(
                        R.navigation.edit_delivery_location_nav_host,
                        bundleOf("bundle" to bundle)
                )
    }
}