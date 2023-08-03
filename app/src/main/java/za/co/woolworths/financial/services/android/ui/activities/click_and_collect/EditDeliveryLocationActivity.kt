package za.co.woolworths.financial.services.android.ui.activities.click_and_collect

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.EditDeliveryLocationActivityBinding
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.cart.view.CartFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.BUNDLE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.DELIVERY_TYPE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_FROM_CHECKOUT
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_FROM_SLOT_SELECTION
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.KEY_PLACE_ID
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.PLACE_ID
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.SAVED_ADDRESS_RESPONSE
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import za.co.woolworths.financial.services.android.checkout.view.CheckoutWhoIsCollectingFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CheckOutFragment
import za.co.woolworths.financial.services.android.geolocation.view.ConfirmAddressFragment

@AndroidEntryPoint
class EditDeliveryLocationActivity : AppCompatActivity() {

    private lateinit var binding: EditDeliveryLocationActivityBinding
    private var bundle: Bundle? = null
    private var deliveryType: Delivery? = null
    private var placeId: String? = null
    private var isComingFromCheckout: Boolean = false
    private var isComingFromSlotSelection: Boolean = false
    private var savedAddressResponse: SavedAddressResponse? = null
    private var navHostFragment = NavHostFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditDeliveryLocationActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Utils.updateStatusBarBackground(this)
        bundle = intent.getBundleExtra(BUNDLE)
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
        setSupportActionBar(binding.toolbar)
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

    private fun loadNavHostFragment() {
        onEditDeliveryLocation()
    }

    private fun onEditDeliveryLocation() {
        navHostFragment =
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
                            KEY_PLACE_ID, placeId
                        )
                    }
                    navController
                        .setGraph(
                            navGraph,
                            bundleOf(BUNDLE to bundle)
                        )
                } else if (deliveryType == Delivery.CNC) {
                    //  collection screen
                    navGraph.startDestination = R.id.geoCheckoutCollectingFragment
                    navController.graph = navGraph
                    navController
                        .setGraph(
                            navGraph,
                            bundleOf(BUNDLE to bundle)
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
                                bundleOf(BUNDLE to bundle)
                            )
                    } else if (savedAddressResponse?.addresses?.isEmpty() == true) {
                        // add address
                        navGraph.startDestination = R.id.checkoutAddAddressNewUserFragment
                        navController.graph = navGraph
                        navController
                            .setGraph(
                                navGraph,
                                bundleOf(BUNDLE to bundle)
                        )
                    }
                }
            } else {
                //  geo flow
                navGraph.startDestination = R.id.deliveryAddressConfirmationFragment
                navController.graph = navGraph
                bundle?.apply {
                    putString(
                       KEY_PLACE_ID, placeId
                    )
                }
                navController
                    .setGraph(
                        navGraph,
                        bundleOf(BUNDLE to bundle)
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

    override fun onBackPressed() {
        KeyboardUtils.hideKeyboardIfVisible(this)
        val fragmentList: MutableList<androidx.fragment.app.Fragment> =
            navHostFragment.childFragmentManager.fragments
        //in Navigation component if Back stack entry count is 0 means it has last fragment presented.
        // if > 0 means others are in backstack but fragment list size will always be 1
        if (fragmentList.isNullOrEmpty() || navHostFragment.childFragmentManager.backStackEntryCount == 0) {
            setReloadResultAndFinish()
            return
        }
        when (fragmentList[0]) {
            is CheckoutWhoIsCollectingFragment -> {
                setReloadResultAndFinish()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }
    private fun setReloadResultAndFinish() {
        setResult(CheckOutFragment.RESULT_RELOAD_CART)
        closeActivity()
    }
    fun closeActivity() {
        finish()
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(supportFragmentManager.fragments.single {it is NavHostFragment }.childFragmentManager.fragments.size > 0) {
            val fragment: Fragment = supportFragmentManager.fragments.single {it is NavHostFragment }.childFragmentManager.fragments[0]
            // redirects to utils
            (fragment as? ConfirmAddressFragment)?.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
  }
}