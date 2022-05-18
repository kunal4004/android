package za.co.woolworths.financial.services.android.checkout.view

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.activity_checkout.*
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment.Companion.SAVED_ADDRESS_KEY
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressManagementBaseFragment.Companion.DASH_SLOT_SELECTION
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressManagementBaseFragment.Companion.GEO_SLOT_SELECTION
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressManagementBaseFragment.Companion.IS_DELIVERY
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressManagementBaseFragment.Companion.baseFragBundle
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.fragments.click_and_collect.UnsellableItemsFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CheckOutFragment.REQUEST_CHECKOUT_ON_DESTROY
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CheckOutFragment.RESULT_RELOAD_CART
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.OrderConfirmationFragment
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_FROM_CNC_SELETION
import za.co.woolworths.financial.services.android.util.KeyboardUtils
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils


/**
 * Created by Kunal Uttarwar on 26/05/21.
 */
class CheckoutActivity : AppCompatActivity(), View.OnClickListener {

    private var geoSlotSelection: Boolean? = false
    private var dashSlotSelection: Boolean? = false
    private var navHostFrag = NavHostFragment()
    private var savedAddressResponse: SavedAddressResponse? = null
    private var whoIsCollectingString: String? = null
    private var isComingFromCnc: Boolean? = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)
        setActionBar()
        intent?.extras?.apply {
            savedAddressResponse = getSerializable(SAVED_ADDRESS_KEY) as? SavedAddressResponse
            geoSlotSelection = getBoolean(GEO_SLOT_SELECTION , false)
            dashSlotSelection = getBoolean(DASH_SLOT_SELECTION , false)
            whoIsCollectingString = getString(CheckoutReturningUserCollectionFragment.KEY_COLLECTING_DETAILS, "")
            isComingFromCnc = getBoolean(IS_COMING_FROM_CNC_SELETION , false)
            baseFragBundle = Bundle()
            baseFragBundle?.putString(
                SAVED_ADDRESS_KEY,
                Utils.toJson(savedAddressResponse)
            )
            baseFragBundle?.putString(
                CheckoutReturningUserCollectionFragment.KEY_COLLECTING_DETAILS,
                whoIsCollectingString
            )
            baseFragBundle?.putBoolean(IS_DELIVERY, if (containsKey(IS_DELIVERY)) getBoolean(IS_DELIVERY) else true)
        }
        loadNavHostFragment()
    }

    fun setActionBar() {
        toolbar?.visibility = View.VISIBLE
        setSupportActionBar(toolbar)
        KotlinUtils.setTransparentStatusBar(this)
        supportActionBar?.apply {
            title = ""
            setDisplayShowTitleEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.back24)
        }
    }

    fun showBackArrowWithoutTitle() {
        toolbar?.visibility = View.VISIBLE
        setSupportActionBar(toolbar)
        toolbarText?.text = ""
        supportActionBar?.apply {
            title = ""
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.back24)
        }
    }

    fun showBackArrowWithTitle(titleText: String) {
        toolbar?.visibility = View.VISIBLE
        setSupportActionBar(toolbar)
        toolbarText?.text = titleText
        supportActionBar?.apply {
            title = ""
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.back24)
        }
    }

    fun showTitleWithCrossButton(titleText: String) {
        btnClose?.visibility = View.VISIBLE
        btnClose?.setOnClickListener(this)
        toolbar?.visibility = View.VISIBLE
        toolbarText?.text = titleText
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = ""
            setDisplayHomeAsUpEnabled(false)
            show()
        }
    }

    fun hideBackArrow() {
        toolbar?.visibility = View.VISIBLE
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = ""
            setDisplayHomeAsUpEnabled(false)
        }
    }

    fun hideToolbar() {
        toolbar?.visibility = View.GONE
    }

    private fun loadNavHostFragment() {
        navHostFrag = navHostFragment as NavHostFragment
        val graph =
            navHostFrag.navController.navInflater.inflate(R.navigation.nav_graph_checkout)

        graph.startDestination = when {


            whoIsCollectingString.isNullOrEmpty() == false || isComingFromCnc==true -> {
                R.id.checkoutReturningUserCollectionFragment
            }

            geoSlotSelection == true -> {
                R.id.CheckoutAddAddressReturningUserFragment
            }

            dashSlotSelection == true -> {
                R.id.checkoutDashCollectionFragment
            }

            baseFragBundle?.containsKey(IS_DELIVERY) == true && baseFragBundle?.getBoolean(IS_DELIVERY) == false -> {
                R.id.checkoutWhoIsCollectingFragment
            }

            savedAddressResponse?.addresses.isNullOrEmpty() -> {
                R.id.CheckoutAddAddressNewUserFragment
            }
            TextUtils.isEmpty(savedAddressResponse?.defaultAddressNickname) -> {
                R.id.checkoutAddressConfirmationFragment
            }
            else -> R.id.CheckoutAddAddressReturningUserFragment
        }
        findNavController(R.id.navHostFragment).setGraph(graph, baseFragBundle)
    }

    private fun finishActivityOnCheckoutSuccess() {
        setResult(REQUEST_CHECKOUT_ON_DESTROY)
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> onBackPressed()
            android.R.id.home -> onBackPressed()
        }
        return false
    }

    override fun onBackPressed() {
        // Hide keyboard in case it was visible from a previous screen
        KeyboardUtils.hideKeyboardIfVisible(this)

        val fragmentList: MutableList<androidx.fragment.app.Fragment> =
            navHostFrag.childFragmentManager.fragments

        //in Navigation component if Back stack entry count is 0 means it has last fragment presented.
        // if > 0 means others are in backstack but fragment list size will always be 1
        if (fragmentList.isNullOrEmpty() || navHostFrag.childFragmentManager.backStackEntryCount == 0) {
            setReloadResultAndFinish()
            return
        }

        when (fragmentList[0]) {

            is UnsellableItemsFragment -> {
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.CHECKOUT_CANCEL_REMOVE_UNSELLABLE_ITEMS, hashMapOf(
                    FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                            FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_NATIVE_CHECKOUT_CANCEL_REMOVE_ITEMS
                ), this)
                setReloadResultAndFinish()
            }
            is CheckoutAddAddressReturningUserFragment, is CheckoutReturningUserCollectionFragment -> {
                setReloadResultAndFinish()
            }
            is OrderConfirmationFragment -> {
                setResult(REQUEST_CHECKOUT_ON_DESTROY)
                closeActivity()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    private fun setReloadResultAndFinish() {
        setResult(RESULT_RELOAD_CART)
        closeActivity()
    }

    fun closeActivity() {
        finish()
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnClose -> {
                // Hide keyboard in case it was visible from a previous screen
                KeyboardUtils.hideKeyboardIfVisible(this)
                onBackPressed()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECKOUT_ON_DESTROY && resultCode == REQUEST_CHECKOUT_ON_DESTROY) {
            finishActivityOnCheckoutSuccess()
            return
        }
        navHostFrag.childFragmentManager.fragments.let {
            if (it.isNullOrEmpty()) {
                return
            }
            it[0].onActivityResult(requestCode, resultCode, data)
        }
    }
}