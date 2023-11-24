package za.co.woolworths.financial.services.android.ui.fragments.shop

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentShopBinding
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.DASH_DELIVERY_BROWSE_MODE
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.DASH_SWITCH_BROWSE_MODE
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.PropertyNames.Companion.BROWSE_MODE
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.PropertyNames.Companion.DELIVERY_MODE
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidateLocationResponse
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.models.dto.OrdersResponse
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams.SearchType
import za.co.woolworths.financial.services.android.models.dto.RootCategories
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse
import za.co.woolworths.financial.services.android.models.dto.cart.FulfillmentDetails
import za.co.woolworths.financial.services.android.shoptoggle.presentation.ShopToggleActivity
import za.co.woolworths.financial.services.android.shoptoggle.presentation.ToggleFulfilmentResult
import za.co.woolworths.financial.services.android.ui.activities.BarcodeScanActivity
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.INDEX_ACCOUNT
import za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity
import za.co.woolworths.financial.services.android.ui.adapters.ShopPagerAdapter
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.ProductListingFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.ShopFragment.SelectedTabIndex.CLICK_AND_COLLECT_TAB
import za.co.woolworths.financial.services.android.ui.fragments.shop.ShopFragment.SelectedTabIndex.DASH_TAB
import za.co.woolworths.financial.services.android.ui.fragments.shop.ShopFragment.SelectedTabIndex.STANDARD_TAB
import za.co.woolworths.financial.services.android.ui.fragments.shop.StandardDeliveryFragment.Companion.DEPARTMENT_LOGIN_REQUEST
import za.co.woolworths.financial.services.android.ui.fragments.shop.domain.ContextualTooltipShowcaseManager
import za.co.woolworths.financial.services.android.ui.fragments.shop.domain.CotextualTooltipShowcase
import za.co.woolworths.financial.services.android.ui.fragments.shop.domain.ShopLandingAutoNavigateChecker
import za.co.woolworths.financial.services.android.ui.fragments.shop.domain.ShopLandingAutoNavigateCheckerImpl
import za.co.woolworths.financial.services.android.ui.fragments.shop.domain.TooltipShown
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.OnChildFragmentEvents
import za.co.woolworths.financial.services.android.ui.views.shop.dash.ChangeFulfillmentCollectionStoreFragment
import za.co.woolworths.financial.services.android.ui.views.shop.dash.DashDeliveryAddressFragment
import za.co.woolworths.financial.services.android.ui.views.tooltip.CustomText
import za.co.woolworths.financial.services.android.ui.views.tooltip.TooltipDialog
import za.co.woolworths.financial.services.android.ui.views.tooltip.WMaterialShowcaseViewV2
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.REQUEST_CODE_BARCODE_ACTIVITY
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.REQUEST_CODE_ORDER_DETAILS_PAGE
import za.co.woolworths.financial.services.android.util.AppConstant.Keys.Companion.ARG_FROM_NOTIFICATION
import za.co.woolworths.financial.services.android.util.BundleKeysConstants
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.CNC_SET_ADDRESS_REQUEST_CODE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.DASH_SET_ADDRESS_REQUEST_CODE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.REQUEST_CODE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.UPDATE_LOCATION_REQUEST
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.UPDATE_STORE_REQUEST
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.getDeliveryType
import za.co.woolworths.financial.services.android.util.PermissionResultCallback
import za.co.woolworths.financial.services.android.util.PermissionUtils
import za.co.woolworths.financial.services.android.util.ScreenManager.SHOPPING_LIST_DETAIL_ACTIVITY_REQUEST_CODE
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.StoreUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.AnalyticsManager
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import za.co.woolworths.financial.services.android.viewmodels.shop.ShopViewModel
import java.util.Timer
import kotlin.concurrent.timerTask


@AndroidEntryPoint
class ShopFragment : BaseFragmentBinding<FragmentShopBinding>(FragmentShopBinding::inflate),
    PermissionResultCallback,
    OnChildFragmentEvents {

    private val confirmAddressViewModel: ConfirmAddressViewModel by activityViewModels()
    private var toggleScreenTimer: Timer? = null
    private val shopLandingAutoNavigator: ShopLandingAutoNavigateChecker by lazy { ShopLandingAutoNavigateCheckerImpl() }
    private val contextualTooltipShowcase: CotextualTooltipShowcase by lazy { ContextualTooltipShowcaseManager() }
    private var mTabTitle: MutableList<String>? = null
    private var permissionUtils: PermissionUtils? = null
    var permissions: ArrayList<String> = arrayListOf()
    var shopPagerAdapter: ShopPagerAdapter? = null
    private var rootCategories: RootCategories? = null
    private var ordersResponse: OrdersResponse? = null
    private var shoppingListsResponse: ShoppingListsResponse? = null
    private var user: String = ""
    private var validateLocationResponse: ValidateLocationResponse? = null
    private var isScreenRefreshing = false
    private var needToDisplayTooltip = false
    private var isNewTooltipSession = true
    private val fragmentResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != RESULT_OK) {
                return@registerForActivityResult
            }
            it.data?.extras?.let { extras ->
                val requestCode = extras.getInt(AppConstant.REQUEST_CODE)
                if (requestCode == REQUEST_CODE_BARCODE_ACTIVITY) {
                    val searchType =
                        SearchType.valueOf(extras.getString(AppConstant.Keys.EXTRA_SEARCH_TYPE, ""))
                    val searchTerm: String =
                        extras.getString(AppConstant.Keys.EXTRA_SEARCH_TERM, "")
                    (requireActivity() as? BottomNavigationActivity)?.pushFragment(
                        ProductListingFragment.newInstance(
                            searchType,
                            "",
                            searchTerm,
                            isBrowsing = false,
                            sendDeliveryDetails = false // false because barcode result
                        )
                    )
                }
            }
        }

    companion object {
        private const val LOGIN_MY_LIST_REQUEST_CODE = 9876
        private const val DASH_DIVIDER = 1.25
        private const val TIME_SLOT_SEPARATOR = "\t\u2022\t "
        private const val TOGGLE_SCREEN_DELAY = 2000L
    }

    enum class SelectedTabIndex(val index: Int) {
        STANDARD_TAB(0),
        CLICK_AND_COLLECT_TAB(1),
        DASH_TAB(2)
    }

    private val shopViewModel: ShopViewModel by viewModels(
        ownerProducer = { this }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mTabTitle = mutableListOf(
            bindString(R.string.standard_delivery),
            bindString(R.string.click_and_collect),
            bindString(R.string.dash_delivery)
        )
    }

    private fun setEventForDeliveryTypeAndBrowsingType() {
        if (getDeliveryType()?.deliveryType == null) {
            return
        }

        val dashParams = bundleOf(
            DELIVERY_MODE to
                    KotlinUtils.getPreferredDeliveryType()?.type,
            BROWSE_MODE to KotlinUtils.browsingDeliveryType?.type
        )

        AnalyticsManager.setUserProperty(
            DELIVERY_MODE,
            KotlinUtils.getPreferredDeliveryType()?.type
        )
        AnalyticsManager.setUserProperty(
            BROWSE_MODE, KotlinUtils.browsingDeliveryType?.type
        )
        AnalyticsManager.logEvent(
            DASH_DELIVERY_BROWSE_MODE,
            dashParams
        )
    }

    private fun setEventsForSwitchingBrowsingType(browsingType: String?) {
        if (KotlinUtils.getPreferredDeliveryType() == null) {
            return
        }
        val dashParams = bundleOf(
            DELIVERY_MODE to KotlinUtils.getPreferredDeliveryType()?.name,
            BROWSE_MODE to browsingType
        )
        AnalyticsManager.setUserProperty(
            DELIVERY_MODE,
            KotlinUtils.getPreferredDeliveryType()?.type
        )
        browsingType?.let {
            AnalyticsManager.setUserProperty(BROWSE_MODE, browsingType)
        }
        AnalyticsManager.logEvent(DASH_SWITCH_BROWSE_MODE, dashParams)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.apply {
            permissionUtils = PermissionUtils(this, this@ShopFragment)
            permissions.add(Manifest.permission.CAMERA)
        }

        binding?.apply {
            tvSearchProduct.setOnClickListener { navigateToProductSearch() }
            imBarcodeScanner.setOnClickListener { checkCameraPermission() }
            fulfilmentAndLocationLayout.layoutFulfilment.root.setOnClickListener {
                hideTooltipIfVisible()
                launchShopToggleScreen()
            }

            fulfilmentAndLocationLayout.layoutLocation.root.setOnClickListener {
                hideTooltipIfVisible()
                launchStoreOrLocationSelection()
            }

            shopPagerAdapter = ShopPagerAdapter(childFragmentManager, mTabTitle, this@ShopFragment)
            viewpagerMain.offscreenPageLimit = 2
            viewpagerMain.adapter = shopPagerAdapter
            viewpagerMain.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

                override fun onPageScrollStateChanged(state: Int) {
                }

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int,
                ) {

                }

                override fun onPageSelected(position: Int) {
                    activity?.apply {
                        when (position) {
                            STANDARD_TAB.index -> {
                                Utils.triggerFireBaseEvents(
                                    FirebaseManagerAnalyticsProperties.SHOP_CATEGORIES,
                                    this
                                )
                                setEventsForSwitchingBrowsingType(Delivery.STANDARD.name)
                                KotlinUtils.browsingDeliveryType = Delivery.STANDARD
                                setSearchText(STANDARD_TAB)
                            }

                            CLICK_AND_COLLECT_TAB.index -> {
                                setEventsForSwitchingBrowsingType(Delivery.CNC.name)
                                KotlinUtils.browsingDeliveryType = Delivery.CNC
                                setSearchText(CLICK_AND_COLLECT_TAB)
                            }

                            DASH_TAB.index -> {
                                setEventsForSwitchingBrowsingType(Delivery.DASH.name)
                                KotlinUtils.browsingDeliveryType = Delivery.DASH
                                setSearchText(DASH_TAB)
                            }
                        }
                    }
                    shopPagerAdapter?.notifyDataSetChanged()
                    updateTabIconUI(position)
                }
            })
            updateTabIconUI(currentTabPositionBasedOnDeliveryType())
            viewpagerMain.currentItem = currentTabPositionBasedOnDeliveryType()
        }
    }

    private fun hideTooltipIfVisible() {
        (activity as? BottomNavigationActivity)?.apply {
            if(walkThroughPromtView != null && !walkThroughPromtView.isDismissed()) {
                walkThroughPromtView.hide()
            }
        }
    }

    private fun setSearchText(selectedTab: SelectedTabIndex, location: CharSequence? = null) {
        binding.apply {
            when (selectedTab) {
                STANDARD_TAB -> {
                    tvSearchProduct.text = getString(R.string.shop_landing_product_all_search)
                    fulfilmentAndLocationLayout.layoutFulfilment.tvTitle.text = getString(R.string.standard_delivery)
                    fulfilmentAndLocationLayout.layoutFulfilment.tvSubTitle.text = getString(R.string.shop_landing_fulfilment_title_cnc_and_standard)
                    fulfilmentAndLocationLayout.layoutLocation.tvTitle.text = location ?: getString(R.string.set_location_title)
                }
                CLICK_AND_COLLECT_TAB -> {
                    tvSearchProduct.text = getCncSearchText()
                    fulfilmentAndLocationLayout.layoutFulfilment.tvTitle.text = getString(R.string.click_and_collect)
                    fulfilmentAndLocationLayout.layoutFulfilment.tvSubTitle.text = getString(R.string.shop_landing_fulfilment_title_cnc_and_standard)
                    fulfilmentAndLocationLayout.layoutLocation.tvTitle.text = location ?: getString(R.string.select_your_preferred_store)
                }
                DASH_TAB -> {
                    tvSearchProduct.text = getString(R.string.shop_landing_product_food_search)
                    fulfilmentAndLocationLayout.layoutFulfilment.tvTitle.text = getString(R.string.dash_delivery)
                    fulfilmentAndLocationLayout.layoutFulfilment.tvSubTitle.text = getString(R.string.shop_landing_fulfilment_title_dash).plus(TIME_SLOT_SEPARATOR).plus(dashTimeslots())
                    fulfilmentAndLocationLayout.layoutLocation.tvTitle.text = location ?: getString(R.string.set_location_title)
                }
            }
        }
    }

    private fun getCncSearchText(): String {
        var storeDeliveryType = KotlinUtils.browsingCncStore?.storeDeliveryType
        if (storeDeliveryType.isNullOrEmpty()) {
            storeDeliveryType = KotlinUtils.getStoreDeliveryType(getDeliveryType())
        }

        return when (storeDeliveryType?.lowercase()) {
            StoreUtils.Companion.StoreDeliveryType.OTHER.type.lowercase() -> {
                getString(R.string.shop_landing_product_other_search)
            }
            StoreUtils.Companion.StoreDeliveryType.FOOD.type.lowercase() -> {
                getString(R.string.shop_landing_product_food_search)
            }
            else -> {
                getString(R.string.shop_landing_product_all_search)
            }
        }
    }

    fun showSearchAndBarcodeUi(isFromCnc: Boolean = false) {
        binding.apply {
            tvSearchProduct.visibility = View.VISIBLE
            imBarcodeScanner.visibility = View.VISIBLE
        }
    }

    fun hideSearchAndBarcodeUi() {
        binding?.apply {
            tvSearchProduct.visibility = View.GONE
            imBarcodeScanner.visibility = View.GONE
        }
    }

    private fun executeValidateSuburb() {
        val placeId = getDeliveryType()?.address?.placeId ?: return
        binding?.apply {
            placeId?.let {
                shopProgressbar.visibility = View.VISIBLE
                lifecycleScope.launch {
                    try {
                        validateLocationResponse =
                            confirmAddressViewModel.getValidateLocation(it)
                        shopProgressbar.visibility = View.GONE
                        if (validateLocationResponse != null) {
                            when (validateLocationResponse?.httpCode) {
                                AppConstant.HTTP_OK -> {
                                    if (WoolworthsApplication.getCncBrowsingValidatePlaceDetails() == null) {
                                        WoolworthsApplication.setCncBrowsingValidatePlaceDetails(
                                            validateLocationResponse?.validatePlace
                                        )
                                    }
                                    WoolworthsApplication.setValidatedSuburbProducts(
                                        validateLocationResponse?.validatePlace
                                    )

                                    // APP1-1316 : nickname update in fulfillment details object

                                    val fulfillmentDeliveryLocation =
                                        Utils.getPreferredDeliveryLocation()
                                    val nickname =
                                        validateLocationResponse?.validatePlace?.placeDetails?.nickname
                                    fulfillmentDeliveryLocation?.fulfillmentDetails?.address?.nickname =
                                        nickname
                                    Utils.savePreferredDeliveryLocation(fulfillmentDeliveryLocation)

                                    updateCurrentTab(getDeliveryType()?.deliveryType)
                                    setEventForDeliveryTypeAndBrowsingType()
                                    setDeliveryView()
                                    if (needToDisplayTooltip) {
                                        showTooltipIfRequired()
                                        needToDisplayTooltip = false
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        shopProgressbar.visibility = View.GONE
                        FirebaseManager.logException(e)
                        needToDisplayTooltip = false
                        /*TODO : show error screen*/
                    } catch (e: JsonSyntaxException) {
                        shopProgressbar.visibility = View.GONE
                        FirebaseManager.logException(e)
                        needToDisplayTooltip = false
                    }
                }
            }
        }
    }

    private fun launchStoreOrLocationSelection() {
        val delivery = Delivery.getType(getDeliveryType()?.deliveryType)
        if (delivery == Delivery.CNC) {
            launchStoreSelection()
        } else {
            launchGeoLocationFlow()
        }
    }
    private fun launchStoreSelection() {
        KotlinUtils.presentEditDeliveryGeoLocationActivity(
            activity,
            UPDATE_STORE_REQUEST,
            Delivery.getType(getDeliveryType()?.deliveryType)
                ?: KotlinUtils.browsingDeliveryType,
            getDeliveryType()?.address?.placeId ?: "",
            isFromNewToggleFulfilmentScreen = true,
            newDelivery = Delivery.CNC,
            needStoreSelection = true,
            validateLocationResponse = validateLocationResponse
        )
    }

    private fun launchGeoLocationFlow() {
        KotlinUtils.presentEditDeliveryGeoLocationActivity(
            activity,
            UPDATE_LOCATION_REQUEST,
            Delivery.getType(getDeliveryType()?.deliveryType) ?: KotlinUtils.browsingDeliveryType,
            getDeliveryType()?.address?.placeId ?: "",
            isLocationUpdateRequest = true,
            newDelivery = Delivery.getType(getDeliveryType()?.deliveryType) ?: KotlinUtils.browsingDeliveryType
        )
    }

    override fun onResume() {
        super.onResume()
        if (isVisible) {
            if (((KotlinUtils.isLocationPlaceIdSame == false || KotlinUtils.isNickNameChanged == true) && KotlinUtils.placeId != null) || WoolworthsApplication.getValidatePlaceDetails() == null) {
                if (isScreenRefreshing) {
                    isScreenRefreshing = false
                    return
                }
                executeValidateSuburb()
                return
            } else if (Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.deliveryType.isNullOrEmpty() && KotlinUtils.getAnonymousUserLocationDetails()?.fulfillmentDetails?.deliveryType.isNullOrEmpty()) {
                return
            } else if (KotlinUtils.isLocationPlaceIdSame == true && KotlinUtils.placeId != null) {
                setDeliveryView()
            } else {
                setDeliveryView()
            }
        }
    }

    private fun updateCurrentTab(deliveryType: String?) {
        binding.apply {
            when (deliveryType) {
                BundleKeysConstants.STANDARD -> {
                    viewpagerMain.currentItem = STANDARD_TAB.index
                }

                BundleKeysConstants.CNC -> {
                    viewpagerMain.currentItem = CLICK_AND_COLLECT_TAB.index
                }

                BundleKeysConstants.DASH -> {
                    viewpagerMain.currentItem = DASH_TAB.index
                }
            }
        }
    }

    private fun checkCameraPermission() {
        activity?.apply {
            Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.SHOPBARCODE,
                this
            )
        }
        permissionUtils?.checkPermission(
            permissions,
            1
        )
    }

    private fun updateTabIconUI(selectedTab: Int) {
        when (selectedTab) {
            STANDARD_TAB.index -> {
                showSearchAndBarcodeUi()
            }

            CLICK_AND_COLLECT_TAB.index -> {
                if (KotlinUtils.browsingCncStore == null
                    && getDeliveryType()?.deliveryType != Delivery.CNC.type
                ) {
                    hideSearchAndBarcodeUi()
                }
            }
        }
    }

    private fun navigateToProductSearch() {
        requireActivity().apply {
            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOPSEARCHBAR, this)

            val openProductSearch = Intent(this, ProductSearchActivity::class.java).also {
                it.putExtra(
                    AppConstant.Keys.EXTRA_SEND_DELIVERY_DETAILS_PARAMS, true
                )
            }
            startActivity(openProductSearch)
            overridePendingTransition(0, 0)
        }
    }

    private fun currentTabPositionBasedOnDeliveryType(): Int {
        return when(Delivery.getType(getDeliveryType()?.deliveryType)) {
            Delivery.CNC -> CLICK_AND_COLLECT_TAB.index
            Delivery.DASH -> DASH_TAB.index
            else -> STANDARD_TAB.index
        }
    }

    fun setDeliveryView() {
        if (!isAdded) {
            return
        }
        val fulfillmentDetails: FulfillmentDetails? = getDeliveryType()
        fulfillmentDetails?.apply {
            when (Delivery.getType(deliveryType)) {
                Delivery.CNC -> {
                        setSearchText(CLICK_AND_COLLECT_TAB, location = KotlinUtils.capitaliseFirstLetter(storeName))
                }

                Delivery.STANDARD -> {
                    val fullAddress = KotlinUtils.capitaliseFirstLetter(address?.address1 ?: "")
                    val formattedNickName = KotlinUtils.getFormattedNickName(
                        address?.nickname,
                        fullAddress, context
                    )
                    formattedNickName.append(fullAddress)
                    setSearchText(STANDARD_TAB, location = formattedNickName)
                }

                Delivery.DASH -> {
                    val fullAddress = KotlinUtils.capitaliseFirstLetter(address?.address1 ?: "")
                    val formattedNickName = KotlinUtils.getFormattedNickName(
                        address?.nickname,
                        fullAddress, context
                    )
                    val location = "".plus(formattedNickName).plus(
                        KotlinUtils.capitaliseFirstLetter(
                            WoolworthsApplication.getValidatePlaceDetails()?.placeDetails?.address1
                                ?: address?.address1 ?: ""
                        )
                    )
                    setSearchText(DASH_TAB, location = location)
                }

                else -> {
                    setSearchText(STANDARD_TAB)
                }
            }
        }
    }

    private fun dashTimeslots(): String {
        var timeSlot: String? = WoolworthsApplication.getValidatePlaceDetails()?.onDemand?.firstAvailableFoodDeliveryTime
        if(timeSlot.isNullOrEmpty()) {
            timeSlot = getString(R.string.no_timeslots_available_title)
        }
        return timeSlot
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            //do when hidden
            if (WoolworthsApplication.getValidatePlaceDetails() == null || validateLocationResponse == null) {
                executeValidateSuburb()
            }
            (activity as? BottomNavigationActivity)?.apply {
                fadeOutToolbar(R.color.recent_search_bg)
                showBackNavigationIcon(false)
                showBottomNavigationMenu()
                if (isResumed && isVisible)
                    refreshViewPagerFragment()
                Handler().postDelayed({
                    hideToolbar()
                }, AppConstant.DELAY_1000_MS)
            }
            if (getDeliveryType() == null) {
                setSearchText(STANDARD_TAB)
            }
            refreshCnc()
            binding.viewpagerMain.currentItem = currentTabPositionBasedOnDeliveryType()
            setDeliveryView()
        } else {
            toggleScreenTimer?.cancel()
        }
    }

    private fun refreshCnc() {
        if (binding.viewpagerMain.currentItem != 1) {
            return
        }
        val cncFragment =
            binding.viewpagerMain?.adapter?.instantiateItem(
                binding.viewpagerMain,
                binding.viewpagerMain.currentItem
            ) as? ChangeFulfillmentCollectionStoreFragment
        if (cncFragment?.isAdded == true ) {
            (activity as? BottomNavigationActivity)?.let {
                if (it.currentFragment is ShopFragment) {
                    binding.viewpagerMain.currentItem = currentTabPositionBasedOnDeliveryType()
                    cncFragment.setParentFragment(it.currentFragment as ShopFragment)
                    cncFragment.init()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        toggleScreenTimer?.cancel()
    }

    override fun permissionGranted(requestCode: Int) {
        if (requestCode == 1) navigateToBarcode()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionUtils?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun navigateToBarcode() {
        requireActivity().apply {
            fragmentResultLauncher.launch(Intent(this, BarcodeScanActivity::class.java))
            overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ORDER_DETAILS_PAGE) {
            when (resultCode) {
                CancelOrderProgressFragment.RESULT_CODE_CANCEL_ORDER_SUCCESS -> {
                    refreshViewPagerFragment()
                }
            }
        }

        if (requestCode == CancelOrderProgressFragment.REQUEST_CODE_CANCEL_ORDER
            && resultCode == CancelOrderProgressFragment.RESULT_CODE_CANCEL_ORDER_SUCCESS
        ) {
            refreshViewPagerFragment()
        }

        if (resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()) {
            refreshViewPagerFragment()
            var fragment = binding.viewpagerMain?.adapter?.instantiateItem(
                binding.viewpagerMain,
                binding.viewpagerMain.currentItem
            )
            if (fragment is DashDeliveryAddressFragment) {
                fragment.onActivityResult(requestCode, resultCode, data)
            }
        }

        if (requestCode == SHOPPING_LIST_DETAIL_ACTIVITY_REQUEST_CODE) {
            refreshViewPagerFragment()
        }

        if ((requestCode == REQUEST_CODE && resultCode == RESULT_OK)
            || requestCode == DEPARTMENT_LOGIN_REQUEST && binding.viewpagerMain.currentItem == STANDARD_TAB.index
        ) {
            updateCurrentTab(getDeliveryType()?.deliveryType)
            var fragment = binding.viewpagerMain?.adapter?.instantiateItem(
                binding.viewpagerMain,
                binding.viewpagerMain.currentItem
            )
            fragment = when (binding.viewpagerMain.currentItem) {
                STANDARD_TAB.index -> {
                    fragment as? StandardDeliveryFragment
                }

                CLICK_AND_COLLECT_TAB.index -> {
                    fragment as? ChangeFulfillmentCollectionStoreFragment
                }

                DASH_TAB.index -> {
                    fragment as? DashDeliveryAddressFragment
                }

                else -> {
                    fragment as? StandardDeliveryFragment
                }
            }

            fragment?.onActivityResult(requestCode, resultCode, data)
        }
        if (requestCode == DASH_SET_ADDRESS_REQUEST_CODE) {
            // Set Address done on Dash Tab. update the response and Refresh the Tab now.
            val validateLocationResponse = data?.getSerializableExtra(
                BundleKeysConstants.VALIDATE_RESPONSE
            ) as? ValidateLocationResponse
            validateLocationResponse?.validatePlace?.let {
                WoolworthsApplication.setDashBrowsingValidatePlaceDetails(it)
            }
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                // delay added because onResume() sets current item back to deliveryType tab.
                // But we want forcefully user to come on Dash tab even though the location is not dash.
                delay(AppConstant.DELAY_500_MS)
                updateCurrentTab(BundleKeysConstants.DASH)
                val dashDeliveryAddressFragment =
                    binding.viewpagerMain?.adapter?.instantiateItem(
                        binding.viewpagerMain,
                        binding.viewpagerMain.currentItem
                    ) as? DashDeliveryAddressFragment
                dashDeliveryAddressFragment?.initViews()
                KotlinUtils.browsingDeliveryType = Delivery.DASH
                setEventsForSwitchingBrowsingType(Delivery.DASH.name)
            }
        }
        if (requestCode == CNC_SET_ADDRESS_REQUEST_CODE) {
            // Set Address done on CNC Tab. update the response and Refresh the Tab now.
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                // delay added because onResume() sets current item back to deliveryType tab.
                // But we want forcefully user to come on CNC tab even though the location is not CNC.
                delay(AppConstant.DELAY_500_MS)
                updateCurrentTab(BundleKeysConstants.CNC)
                val changeFulfillmentCollectionStoreFragment =
                    binding.viewpagerMain?.adapter?.instantiateItem(
                        binding.viewpagerMain,
                        binding.viewpagerMain.currentItem
                    ) as? ChangeFulfillmentCollectionStoreFragment
                changeFulfillmentCollectionStoreFragment?.init()
            }
        }

        if (requestCode == LOGIN_MY_LIST_REQUEST_CODE) {
            if (resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()) {
                (activity as? BottomNavigationActivity)?.let {
                    it.bottomNavigationById?.currentItem = INDEX_ACCOUNT
                    val fragment = MyListsFragment()
                    it.pushFragment(fragment)
                }
            }
        }

        if (resultCode == RESULT_OK && requestCode == ShopToggleActivity.REQUEST_DELIVERY_TYPE) {
            val toggleFulfilmentResult = getToggleFulfilmentResult(data)
            if (toggleFulfilmentResult != null) {
                if (toggleFulfilmentResult.needRefresh) {
                    val placeId = getDeliveryType()?.address?.placeId
                    if (!placeId.isNullOrEmpty()) {
                        isScreenRefreshing = true
                        needToDisplayTooltip = true
                        executeValidateSuburb()
                    }
                } else {
                    //DO nothing here, will keep the standard selected by default
                    //Just Browsing or Not Now for set location
                    showTooltipIfRequired()
                }
            }
        }

        if (resultCode == RESULT_OK && (requestCode == UPDATE_LOCATION_REQUEST || requestCode == UPDATE_STORE_REQUEST)) {
            setDeliveryView()
        }
    }

    private fun getToggleFulfilmentResult(intent: Intent?): ToggleFulfilmentResult? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.extras?.getParcelable(ShopToggleActivity.INTENT_DATA_TOGGLE_FULFILMENT, ToggleFulfilmentResult::class.java)
        } else {
            intent?.extras?.getParcelable(ShopToggleActivity.INTENT_DATA_TOGGLE_FULFILMENT)
        }
    }

    fun refreshViewPagerFragment() {
        binding.apply {
            when (viewpagerMain.currentItem) {
                STANDARD_TAB.index -> {
                    viewpagerMain?.adapter?.instantiateItem(
                        viewpagerMain,
                        viewpagerMain.currentItem
                    ) as? StandardDeliveryFragment
                }

                CLICK_AND_COLLECT_TAB.index -> {
                    viewpagerMain?.adapter?.instantiateItem(
                        viewpagerMain,
                        viewpagerMain.currentItem
                    ) as? ChangeFulfillmentCollectionStoreFragment
                }

                DASH_TAB.index -> {
                    viewpagerMain?.adapter?.instantiateItem(
                        viewpagerMain,
                        viewpagerMain.currentItem
                    ) as? DashDeliveryAddressFragment
                }
            }
        }
    }


    override fun onStartShopping() {
        binding.viewpagerMain.setCurrentItem(0, true)
    }

    override fun isSendDeliveryDetails(): Boolean {
        val fromNotification: Boolean = arguments?.getBoolean(ARG_FROM_NOTIFICATION, false) ?: false
        if (fromNotification) {
            return false
        }
        return true
    }

    fun navigateToMyListFragment() {
        (activity as? BottomNavigationActivity)?.let {
            it.bottomNavigationById.currentItem = INDEX_ACCOUNT
            val fragment = MyListsFragment()
            it.pushFragment(fragment)
        }
    }

    fun scrollToTop() {
        binding.apply {
            when (viewpagerMain.currentItem) {
                STANDARD_TAB.index -> {
                    if (isResumed && isVisible) {
                        val detailsFragment = viewpagerMain?.adapter?.instantiateItem(
                            viewpagerMain,
                            viewpagerMain.currentItem
                        ) as? StandardDeliveryFragment
                        detailsFragment?.scrollToTop()
                    }
                }

                CLICK_AND_COLLECT_TAB.index -> {
                    val changeFulfillmentCollectionStoreFragment =
                        viewpagerMain?.adapter?.instantiateItem(
                            viewpagerMain,
                            viewpagerMain.currentItem
                        ) as? ChangeFulfillmentCollectionStoreFragment
                    changeFulfillmentCollectionStoreFragment?.scrollToTop()
                }

                DASH_TAB.index -> {
                    activity?.lifecycleScope?.launchWhenCreated {
                        val dashDeliveryAddressFragment = viewpagerMain?.adapter?.instantiateItem(
                            viewpagerMain,
                            viewpagerMain.currentItem
                        ) as? DashDeliveryAddressFragment
                        if (dashDeliveryAddressFragment is DashDeliveryAddressFragment)
                            dashDeliveryAddressFragment.scrollToTop()
                    }
                }
            }
        }
    }

    fun setCategoryResponseData(rootCategories: RootCategories) {
        this.rootCategories = rootCategories
    }

    private fun setShoppingListResponseData(shoppingListsResponse: ShoppingListsResponse?) {
        this.shoppingListsResponse = shoppingListsResponse
    }

    fun setOrdersResponseData(ordersResponse: OrdersResponse?) {
        this.ordersResponse = ordersResponse
    }

    fun getCategoryResponseData(): RootCategories? {
        return rootCategories
    }

    fun getOrdersResponseData(): OrdersResponse? {
        return ordersResponse
    }

    fun isDifferentUser(): Boolean {
        return user != (AppInstanceObject.get()?.currentUserObject?.id ?: false)
    }

    fun clearCachedData() {
        if (isDifferentUser()) {
            setOrdersResponseData(null)
            setShoppingListResponseData(null)
        }
        user = AppInstanceObject.get()?.currentUserObject?.id ?: ""
    }

    fun refreshCategories() {
        binding.apply {
            when (viewpagerMain.currentItem) {
                STANDARD_TAB.index -> {
                    val detailsFragment = viewpagerMain?.adapter?.instantiateItem(
                        viewpagerMain,
                        viewpagerMain.currentItem
                    ) as? StandardDeliveryFragment
                    detailsFragment?.reloadRequest()
                }
            }
        }
    }

    private fun getCustomToolTipText(context: Context): SpannableString {
        val descriptionText=getString(R.string.description_tooltip)

        val spannableString = SpannableString(descriptionText)
        val customTypeface1: Typeface? = ResourcesCompat.getFont(context, R.font.futura_semi_bold)
        val customTypeface2: Typeface? =
            ResourcesCompat.getFont(context, R.font.futura_medium)

        val yellowColor = ContextCompat.getColor(context, R.color.color_yellow_FEE600)
        val whiteColor =  ContextCompat.getColor(context, R.color.white)
      // Apply the custom typefaces to specific text
        spannableString.setSpan(CustomText(customTypeface1, whiteColor) , 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(CustomText(customTypeface1, yellowColor) , 2, 19, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(CustomText(customTypeface2, whiteColor) , 20, 29, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        spannableString.setSpan(CustomText(customTypeface1, whiteColor) , 29, 30, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(CustomText(customTypeface1, yellowColor) , 30, 49, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(CustomText(customTypeface2, whiteColor) , 50, 58, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        spannableString.setSpan(CustomText(customTypeface1, whiteColor) , 59, 60, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(CustomText(customTypeface1, yellowColor) , 61, 76, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableString
    }


    private fun formatNewToolTipTitle(context: Context, start: String, coloredText: String, end: String): Spanned {
        val labelColor = ContextCompat.getColor(context, R.color.color_yellow_FEE600)
        val сolor: String = String.format("%X", labelColor).substring(2)
        return HtmlCompat.fromHtml(
            "$start <font color=\"#$сolor\"><br>$coloredText</font><br><br>$end",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
    }

    fun showToggleFulfilmentScreen() {
        if (BottomNavigationActivity.preventShopTooltip) {
            BottomNavigationActivity.preventShopTooltip = false
            return
        }
        if (!shopLandingAutoNavigator.isShopLandingVisited()) {
            toggleScreenTimer = Timer()
            toggleScreenTimer?.schedule(timerTask {
                (activity as? BottomNavigationActivity)?.let {
                    if (it.currentFragment !is ShopFragment || !isVisible || !isAdded) {
                        return@let
                    }
                    shopLandingAutoNavigator.markShopLandingVisited()
                    launchShopToggleScreen(autoNavigation = true)
                }
            }, TOGGLE_SCREEN_DELAY)
        } else {
            showTooltipIfRequired()
        }
    }

    fun showTooltipIfRequired() {
        val delivery = currentDeliveryType()
        val tooltipShown = contextualTooltipShowcase.toolTipToDisplay(delivery, isNewTooltipSession, isUserAuthenticated())
        if (isNewTooltipSession) {
            isNewTooltipSession = false
        }
        when (tooltipShown) {
            TooltipShown.FULFILMENT -> {
                val shown = showFulfilmentTooltip()
                if (shown) {
                    contextualTooltipShowcase.markTooltipShown(
                        delivery = delivery,
                        tooltipShown = TooltipShown.FULFILMENT,
                        isUserAuthenticated()
                    )
                }
            }

            TooltipShown.LOCATION -> {
                val shown = showLocationTooltip()
                if (shown) {
                    contextualTooltipShowcase.markTooltipShown(
                        delivery = delivery,
                        tooltipShown = TooltipShown.LOCATION,
                        isUserAuthenticated()
                    )
                }
            }

            TooltipShown.FULFILMENT_SECOND -> {
                val shown = showFulfilmentTooltip(isSecondTimeFlow = true)
                if (shown) {
                    contextualTooltipShowcase.markTooltipShown(
                        delivery = delivery,
                        tooltipShown = TooltipShown.COMPLETED,
                        isUserAuthenticated()
                    )
                }
            }

            else -> {
                //DO nothing here, as of now
            }
        }
    }

    private fun currentDeliveryType(): Delivery {
        return Delivery.getType(getDeliveryType()?.deliveryType) ?: Delivery.STANDARD
    }

    private fun launchShopToggleScreen(autoNavigation: Boolean = false) {
        Intent(requireActivity(), ShopToggleActivity::class.java).apply {
            putExtra(BundleKeysConstants.TOGGLE_FULFILMENT_AUTO_NAVIGATION, autoNavigation)
            startActivityForResult(this, ShopToggleActivity.REQUEST_DELIVERY_TYPE)
        }
    }

    private fun showFulfilmentTooltip(isSecondTimeFlow: Boolean = false): Boolean {
        // Prevent dialog to display in other section when fragment is not visible
        (activity as? BottomNavigationActivity)?.let {
            if (it.currentFragment !is ShopFragment || !isAdded) {
                return false
            }
            FirebaseManager.setCrashlyticsString(
                bindString(R.string.crashlytics_materialshowcase_key),
                this.javaClass.canonicalName
            )
            val deliveryType = when (currentTabPositionBasedOnDeliveryType()) {
                DASH_TAB.index ->  getString(R.string.tooltip_dash)
                CLICK_AND_COLLECT_TAB.index -> "\n".plus(getString(R.string.tooltip_cnc))
                else -> "\n".plus(getString(R.string.tooltip_standard_delivery))
            }

            //New formatToolTip
            val titleToolTip = formatNewToolTipTitle(
                it,
                getString(R.string.you_re_shopping_with),
                deliveryType,
                getString(R.string.tooltip_fulfilment_message)
            )

            val descriptionText=getCustomToolTipText(it)

            it.walkThroughPromtView =
                WMaterialShowcaseViewV2.Builder(it, TooltipDialog.Feature.SHOP_FULFILMENT, isSecondTimeFlow = isSecondTimeFlow)
                    .setTarget(binding.fulfilmentAndLocationLayout.layoutFulfilment.root)
                    .setTitle(titleToolTip)
                    .setDescription(descriptionText)
                    .setActionText(if (isSecondTimeFlow) getString(R.string.got_it) else getString(R.string.next))
                    .withRectangleShape()
                    .setTargetTouchable(true)
                    .setDismissOnTouch(false).setDismissOnTargetTouch(false).setShapePadding(0)
                    .setAction(walkThroughListener).setDelay(0).setFadeDuration(0).setArrowIcon(R.drawable.ic_arrow_tooltip_spinning)
                    .setMaskColour(ContextCompat.getColor(it, R.color.semi_transparent_black_e6000000)).build()
            it.walkThroughPromtView?.show(it)
            return true
        }
        return false
    }

    private fun showLocationTooltip(): Boolean {
        // Prevent dialog to display in other section when fragment is not visible
        (activity as? BottomNavigationActivity)?.let {
            if (it.currentFragment !is ShopFragment || !isAdded) {
                return false
            }
            FirebaseManager.setCrashlyticsString(
                bindString(R.string.crashlytics_materialshowcase_key),
                this.javaClass.canonicalName
            )
            val (title, description, message) = getLocationTooltipArguments()

            it.walkThroughPromtView =
                WMaterialShowcaseViewV2.Builder(it, TooltipDialog.Feature.SHOP_LOCATION)
                    .setTarget(binding.fulfilmentAndLocationLayout.layoutLocation.root)
                    .setTitle(title)
                    .setDescription(description)
                    .setMessage(message)
                    .setActionText(getString(R.string.got_it)).withRectangleShape().setTargetTouchable(true)
                    .setDismissOnTouch(false).setDismissOnTargetTouch(false).setShapePadding(0)
                    .setAction(walkThroughListener).setDelay(0).setFadeDuration(0).setArrowIcon(R.drawable.ic_arrow_tooltip_simple)
                    .setMaskColour(ContextCompat.getColor(it, R.color.semi_transparent_black_e6000000)).build()
            it.walkThroughPromtView?.show(it)
            return true
        }
        return false
    }

    private fun getLocationTooltipArguments(): Triple<String, String, String> {
        val title: String
        val description: String
        val message: String

        when (currentTabPositionBasedOnDeliveryType()) {
            DASH_TAB.index -> {
                title = getString(R.string.tooltip_location_title_usage)
                description = getString(R.string.tooltip_location_description)
                message = getString(R.string.tooltip_location_message_to_change_your_location)
            }
            CLICK_AND_COLLECT_TAB.index -> {
                title = getString(R.string.tooltip_location_title_cnc)
                description = getString(R.string.tooltip_location_decription_cnc)
                message = getString(R.string.tooltip_location_message_to_change_your_store)
            }
            else -> {
                if(binding.fulfilmentAndLocationLayout.layoutLocation.tvTitle.text == getString(R.string.set_location_title)) {
                    //Location is not yet set
                    title = getString(R.string.tooltip_location_title_set_location)
                    description = getString(R.string.tooltip_location_description)
                    message = getString(R.string.tooltip_location_to_set_your_location)
                } else {
                    //Location has already been set
                    title = getString(R.string.tooltip_location_title_usage)
                    description = getString(R.string.tooltip_location_description)
                    message = getString(R.string.tooltip_location_message_to_change_your_location)
                }
            }
        }
        return Triple(title, description, message)
    }

    private val walkThroughListener = object : WMaterialShowcaseViewV2.IWalkthroughActionListener {
        override fun onWalkthroughActionButtonClick(feature: TooltipDialog.Feature?) {
            if (feature == TooltipDialog.Feature.SHOP_FULFILMENT) {
                showTooltipIfRequired()
            }
        }

        override fun onPromptDismiss(feature: TooltipDialog.Feature?) {
            //TODO("Not yet implemented")
        }
    }

    fun isUserAuthenticated() = SessionUtilities.getInstance().isUserAuthenticated

    fun getCurrentFragmentIndex() = binding.viewpagerMain?.currentItem
}
