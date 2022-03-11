package za.co.woolworths.financial.services.android.ui.activities.click_and_collect


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.edit_delivery_location_activity.toolbar
import za.co.woolworths.financial.services.android.geolocation.view.DeliveryAddressConfirmationFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CartFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.DepartmentsFragment
import za.co.woolworths.financial.services.android.util.*

@AndroidEntryPoint
class EditDeliveryLocationActivity : AppCompatActivity() {

    private var bundle: Bundle? = null
    private var delivery: String? = null
    private var placeId: String? = null

    companion object {
        var REQUEST_CODE = 1515
        var DELIVERY_TYPE = "DELIVERY_TYPE"
        var PLACE_ID = "placeId"
        var IS_LIQUOR = "IS_LIQUOR"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_delivery_location_activity)
        Utils.updateStatusBarBackground(this)
        bundle = intent.getBundleExtra("bundle")
        bundle?.apply {
            delivery = this.getString(DELIVERY_TYPE, "")
            placeId = this.getString(PLACE_ID, "")
        }
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
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)}

    private fun loadNavHostFragment() {
        onEditDeliveryLocation()
    }

    private fun onEditDeliveryLocation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.editAddressNavHost) as NavHostFragment
        val navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.confirm_location_nav_host)
        val placeId = bundle?.getString(PLACE_ID, "")
        if (placeId.isNullOrEmpty()) {
            navGraph.startDestination = R.id.confirmDeliveryLocationFragment
            navController.graph = navGraph
            navController
                .setGraph(
                    navGraph,
                    bundleOf("bundle" to bundle)
                )
        } else {
            navGraph.startDestination = R.id.deliveryAddressConfirmationFragment
            navController.graph = navGraph
            bundle?.apply {
                putString(
                    DeliveryAddressConfirmationFragment.KEY_PLACE_ID, placeId
                )
            }
            navController
                .setGraph(
                    navGraph,
                    bundleOf("bundle" to bundle)
                )
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CartFragment.REQUEST_PAYMENT_STATUS){
            onBackPressed()
        }
    }

}