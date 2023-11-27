package za.co.woolworths.financial.services.android.checkout.view

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ActivityCheckoutBinding
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment.Companion.SAVED_ADDRESS_KEY
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressManagementBaseFragment.Companion.DASH_SLOT_SELECTION
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressManagementBaseFragment.Companion.GEO_SLOT_SELECTION
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressManagementBaseFragment.Companion.IS_DELIVERY
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressManagementBaseFragment.Companion.baseFragBundle
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.geolocation.viewmodel.UpdateScreenLiveData
import za.co.woolworths.financial.services.android.models.dto.CommerceItem
import za.co.woolworths.financial.services.android.shoptoggle.presentation.ShopToggleActivity
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CheckOutFragment.*
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.OrderConfirmationFragment
import za.co.woolworths.financial.services.android.ui.views.UnsellableItemsBottomSheetDialog
import za.co.woolworths.financial.services.android.util.BundleKeysConstants
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_FROM_CNC_SELETION
import za.co.woolworths.financial.services.android.util.Constant.Companion.IS_MIXED_BASKET
import za.co.woolworths.financial.services.android.util.KeyboardUtils
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.Constant.Companion.LIQUOR_ORDER
import za.co.woolworths.financial.services.android.util.Constant.Companion.NO_LIQUOR_IMAGE_URL

/**
 * Created by Kunal Uttarwar on 26/05/21.
 */
@AndroidEntryPoint
class CheckoutActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityCheckoutBinding
    private var geoSlotSelection: Boolean? = false
    private var dashSlotSelection: Boolean? = false
    private var navHostFrag = NavHostFragment()
    private var savedAddressResponse: SavedAddressResponse? = null
    private var whoIsCollectingString: String? = null
    private var isComingFromCnc: Boolean? = false
    private var mSavedAddressPosition = 0
    var isEditAddressScreenNeeded = true
    var cartItemList:ArrayList<CommerceItem>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setActionBar()
        intent?.extras?.apply {
            savedAddressResponse = getSerializable(SAVED_ADDRESS_KEY) as? SavedAddressResponse
            isEditAddressScreenNeeded = getBoolean(CheckoutAddressConfirmationFragment.IS_EDIT_ADDRESS_SCREEN, false)
            geoSlotSelection = getBoolean(GEO_SLOT_SELECTION, false)
            dashSlotSelection = getBoolean(DASH_SLOT_SELECTION, false)
            cartItemList = getSerializable(CheckoutAddressManagementBaseFragment.CART_ITEM_LIST) as? ArrayList<CommerceItem>?
            whoIsCollectingString =
                getString(CheckoutReturningUserCollectionFragment.KEY_COLLECTING_DETAILS, "")
            isComingFromCnc = getBoolean(IS_COMING_FROM_CNC_SELETION, false)

            baseFragBundle = Bundle()
            baseFragBundle?.putString(
                SAVED_ADDRESS_KEY,
                Utils.toJson(savedAddressResponse)
            )
            baseFragBundle?.putString(
                CheckoutReturningUserCollectionFragment.KEY_COLLECTING_DETAILS,
                whoIsCollectingString
            )
            baseFragBundle?.putBoolean(IS_DELIVERY,
                if (containsKey(IS_DELIVERY)) getBoolean(IS_DELIVERY) else true)
            if (containsKey(LIQUOR_ORDER) && containsKey(NO_LIQUOR_IMAGE_URL)) {
                baseFragBundle?.putBoolean(LIQUOR_ORDER, getBoolean(LIQUOR_ORDER))
                baseFragBundle?.putString(NO_LIQUOR_IMAGE_URL, getString(NO_LIQUOR_IMAGE_URL))
            }
            baseFragBundle?.putBoolean(BundleKeysConstants.IS_COMING_FROM_CHECKOUT, true)
            baseFragBundle?.putSerializable(CheckoutAddressManagementBaseFragment.CART_ITEM_LIST, cartItemList)
            baseFragBundle?.putBoolean(IS_MIXED_BASKET, getBoolean(IS_MIXED_BASKET, false))
        }
        loadNavHostFragment()
    }

    fun setActionBar() {
        binding.toolbar?.visibility = View.VISIBLE
        setSupportActionBar(binding.toolbar)
        KotlinUtils.setTransparentStatusBar(this)
        supportActionBar?.apply {
            title = ""
            setDisplayShowTitleEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.back24)
        }
    }

    fun showBackArrowWithoutTitle() {
        binding.toolbar?.visibility = View.VISIBLE
        setSupportActionBar(binding.toolbar)
        binding.toolbarText?.text = ""
        supportActionBar?.apply {
            title = ""
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.back24)
        }
    }

    fun showBackArrowWithTitle(titleText: String) {
        lifecycleScope.launchWhenCreated {
            binding.toolbar.visibility = View.VISIBLE
            setSupportActionBar(binding.toolbar)
            binding.toolbarText.text = titleText
            supportActionBar?.apply {
                title = ""
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.drawable.back24)
            }
        }
    }

    fun hideBackArrow() {
        lifecycleScope.launchWhenCreated {
            binding.toolbar.visibility = View.VISIBLE
            setSupportActionBar(binding.toolbar)
            supportActionBar?.apply {
                title = ""
                setDisplayHomeAsUpEnabled(false)
            }
        }
    }

    fun hideToolbar() {
        binding.toolbar?.visibility = View.GONE
    }

    private fun loadNavHostFragment() {
        navHostFrag = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val graph =
            navHostFrag.navController.navInflater.inflate(R.navigation.nav_graph_checkout)

        graph.startDestination = getStartDestinationGraph()
        findNavController(R.id.navHostFragment).setGraph(graph, baseFragBundle)
    }

    fun getStartDestinationGraph(): Int {
        return when {
            !whoIsCollectingString.isNullOrEmpty() || isComingFromCnc == true -> {
                R.id.checkoutReturningUserCollectionFragment
            }

            geoSlotSelection == true -> {
                R.id.CheckoutAddAddressReturningUserFragment
            }

            dashSlotSelection == true -> {
                R.id.checkoutDashFragment
            }

            baseFragBundle?.containsKey(IS_DELIVERY) == true && baseFragBundle?.getBoolean(
                IS_DELIVERY) == false -> {
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
        if (this == null) return
        // Hide keyboard in case it was visible from a previous screen
        KeyboardUtils.hideKeyboardIfVisible(this)

        val fragmentList: MutableList<androidx.fragment.app.Fragment>? =
            navHostFrag?.childFragmentManager?.fragments

        //in Navigation component if Back stack entry count is 0 means it has last fragment presented.
        // if > 0 means others are in backstack but fragment list size will always be 1
        if (fragmentList.isNullOrEmpty() || navHostFrag.childFragmentManager.backStackEntryCount == 0) {
            setReloadResultAndFinish()
            return
        }

        when (fragmentList[0]) {

            is UnsellableItemsBottomSheetDialog -> {
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.CHECKOUT_CANCEL_REMOVE_UNSELLABLE_ITEMS,
                    hashMapOf(
                        FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                                FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_NATIVE_CHECKOUT_CANCEL_REMOVE_ITEMS
                    ),
                    this)
                setReloadResultAndFinish()
            }
            is CheckoutAddAddressReturningUserFragment, is CheckoutReturningUserCollectionFragment, is CheckoutDashFragment -> {
                setReloadResultAndFinish()
            }
            is OrderConfirmationFragment -> {
                if (isComingFromCnc == true) {
                    //set BR to update cart fragment
                    LocalBroadcastManager.getInstance(this)
                        .sendBroadcast(Intent(TAG_CART_BROADCAST_RECEIVER))
                } else {
                    setResult(REQUEST_CHECKOUT_ON_DESTROY)
                }
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

    private fun checkIfAddressHasNoUnitComplexNo(): Boolean {
        savedAddressResponse?.addresses?.forEachIndexed { index, address ->
            if (savedAddressResponse?.defaultAddressNickname.equals(address?.nickname)) {
                mSavedAddressPosition = index
                return address?.address2.isNullOrEmpty()
            }
        }
        return true
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
        if (resultCode == ShopToggleActivity.REQUEST_DESTROY_CHECKOUT) {
            closeActivity()
            return
        }
        navHostFrag.childFragmentManager.fragments.let {
            if (it.isNullOrEmpty()) {
                return
            }
            it[0].onActivityResult(requestCode, resultCode, data)
        }

    }

    override fun onResume() {
        super.onResume()
        screenRefresh()
    }

    override fun onPause() {
        super.onPause()
        UpdateScreenLiveData.removeObservers(this)
    }

    private fun screenRefresh(){
        UpdateScreenLiveData.observe(this) {
            if(it==1)
            { UpdateScreenLiveData.value=0
                onBackPressed()
            }
        }
    }


}