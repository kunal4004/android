package za.co.woolworths.financial.services.android.ui.activities.click_and_collect

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.activity_checkout.*
import kotlinx.android.synthetic.main.edit_delivery_location_activity.*
import kotlinx.android.synthetic.main.edit_delivery_location_activity.toolbar
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressManagementBaseFragment
import za.co.woolworths.financial.services.android.geolocation.view.ConfirmAddressFragment
import za.co.woolworths.financial.services.android.geolocation.view.DeliveryAddressConfirmationFragment
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.fragments.shop.DepartmentsFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.wenum.Delivery

class EditDeliveryLocationActivity : AppCompatActivity() {

    var bundle: Bundle? = null
    var deliveryType: DeliveryType? = null

    var delivery: String? = null
    var placeId: String? = null

    private var navHostFrag = NavHostFragment()


    companion object {
        var REQUEST_CODE = 1515
        var DELIVERY_TYPE = "DELIVERY_TYPE"
        var PLACE_ID = "PLACE_ID"
        var IS_LIQUOR = "IS_LIQUOR"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_delivery_location_activity)
        Utils.updateStatusBarBackground(this)
        bundle = intent.getBundleExtra("bundle")
        bundle?.apply {

            delivery = this.getString(DELIVERY_TYPE, "")
            placeId =  this.getString(DELIVERY_TYPE, "")

 //           deliveryType = DeliveryType.valueOf(getString(DELIVERY_TYPE, DeliveryType.DELIVERY.name))
//            if (deliveryType == DeliveryType.DELIVERY_LIQUOR) {
//                putString(DELIVERY_TYPE, DeliveryType.DELIVERY.name)
//                putBoolean(IS_LIQUOR, true)
//            }

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
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)    }

    private fun loadNavHostFragment() {
        onEditDeliveryLocation()
    }

    private fun onEditDeliveryLocation() {
//
//        navHostFrag = nav_host_fragment as NavHostFragment
//        val graph =
//            navHostFrag.navController.navInflater.inflate(R.navigation.confirm_location_nav_host)
//
//        graph.startDestination = when {
//            SessionUtilities.getInstance().isUserAuthenticated == true
//            && Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address != null-> {
//                R.id.confirmDeliveryLocationFragment
//            }
//
//            SessionUtilities.getInstance().isUserAuthenticated == true
//                    && Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address == null -> {
//                R.id.confirmDeliveryLocationFragment
//            }
//            else -> {
//                R.id.confirmDeliveryLocationFragment
//            }
//        }


        val navController = findNavController(R.id.nav_host_fragment)
        if (SessionUtilities.getInstance().isUserAuthenticated) {
            if (Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address != null) {
                Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.let {
                    navController
                        .setGraph(
                            R.navigation.edit_delivery_location_nav_host,
                            bundleOf("bundle" to bundle)
                        )
                }
            } else {
                navController
                    .setGraph(R.navigation.confirm_location_nav_host)
            }
        } else {
            ScreenManager.presentSSOSignin(this, DepartmentsFragment.DEPARTMENT_LOGIN_REQUEST)
        }
   }

}