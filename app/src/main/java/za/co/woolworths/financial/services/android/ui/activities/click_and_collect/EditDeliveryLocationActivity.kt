package za.co.woolworths.financial.services.android.ui.activities.click_and_collect


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.edit_delivery_location_activity.toolbar
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.geolocation.view.DeliveryAddressConfirmationFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CartFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.wenum.Delivery

@AndroidEntryPoint
class EditDeliveryLocationActivity : AppCompatActivity() {

   private var bundle: Bundle? = null
   private var deliveryType: Delivery? = null
   private var placeId: String? = null
   private var isComingFromCheckout: Boolean = false
   private var isComingFromSlotSelection: Boolean = false
   private var savedAddressResponse: SavedAddressResponse? = null

    companion object {
        var REQUEST_CODE = 1515
        var DELIVERY_TYPE = "DELIVERY_TYPE"
        var IS_COMING_FROM_CHECKOUT = "isComingFromCheckout"
        var IS_COMING_FROM_SLOT_SELECTION = "isComingFromSlotSelection"
        var SAVED_ADDRESS_RESPONSE = "savedAddressResponse"
        var DEFAULT_ADDRESS = "defaultAddress"
        var PLACE_ID = "placeId"
        var IS_LIQUOR = "IS_LIQUOR"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_delivery_location_activity)
        Utils.updateStatusBarBackground(this)
        bundle = intent.getBundleExtra("bundle")
        bundle?.apply {
            deliveryType = Delivery.getType(this.getString(DELIVERY_TYPE, ""))
            placeId =  this.getString(PLACE_ID, "")
            isComingFromCheckout =  this.getBoolean(IS_COMING_FROM_CHECKOUT, false)
            isComingFromSlotSelection =  this.getBoolean(IS_COMING_FROM_SLOT_SELECTION, false)
            if (bundle?.containsKey(SAVED_ADDRESS_RESPONSE) == true
                && this.getSerializable(SAVED_ADDRESS_RESPONSE) != null) {
                savedAddressResponse =  this.getSerializable(SAVED_ADDRESS_RESPONSE) as SavedAddressResponse
            }
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

        /*TODO : need to refactor this condition*/
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.editAddressNavHost) as NavHostFragment
        val navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.confirm_location_nav_host)

        if (placeId.isNullOrEmpty()) {
            // naviagte to confirm address screen
            navGraph.startDestination = R.id.confirmAddressLocationFragment
            navController.graph = navGraph
            navController
                .setGraph(
                    navGraph,
                    bundleOf("bundle" to bundle)
            )
        } else {
            if (isComingFromCheckout) {
                if (isComingFromSlotSelection) {
                    //Change fullfiment screen
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
                } else if (deliveryType == Delivery.CNC) {
                    //  collection screen
                    navGraph.startDestination = R.id.geoCheckoutCollectingFragment
                    navController.graph = navGraph
                    navController
                        .setGraph(
                            navGraph,
                            bundleOf("bundle" to bundle)
                        )
                } else {
                    // STANDARD
                    if (TextUtils.isEmpty(savedAddressResponse?.defaultAddressNickname)) {
                        // confirm address screen
                        navGraph.startDestination = R.id.confirmAddressLocationFragment
                        navController.graph = navGraph
                        navController
                            .setGraph(
                                navGraph,
                                bundleOf("bundle" to bundle)
                            )
                    } else if (savedAddressResponse?.addresses?.isEmpty() == true) {
                        // add address
                        navGraph.startDestination = R.id.checkoutAddAddressNewUserFragment
                        navController.graph = navGraph
                        navController
                            .setGraph(
                                navGraph,
                                bundleOf("bundle" to bundle)
                        )
                    }
                }
            } else {
                //  geo flow
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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CartFragment.REQUEST_PAYMENT_STATUS){
            onBackPressed()
        }
    }

}