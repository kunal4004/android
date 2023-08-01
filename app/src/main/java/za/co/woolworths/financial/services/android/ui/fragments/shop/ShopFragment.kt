package za.co.woolworths.financial.services.android.ui.fragments.shop

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.ViewGroup.VISIBLE
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentShopBinding
import com.awfs.coordination.databinding.ShopCustomTabBinding
import com.daasuu.bl.ArrowDirection
import com.google.android.material.tabs.TabLayout
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.DASH_DELIVERY_BROWSE_MODE
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.DASH_SWITCH_BROWSE_MODE
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.PropertyNames.Companion.BROWSE_MODE
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.PropertyNames.Companion.DELIVERY_MODE
import za.co.woolworths.financial.services.android.geolocation.GeoUtils
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidateLocationResponse
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.models.dto.OrdersResponse
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams.SearchType
import za.co.woolworths.financial.services.android.models.dto.RootCategories
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse
import za.co.woolworths.financial.services.android.models.dto.cart.FulfillmentDetails
import za.co.woolworths.financial.services.android.models.dto.dash.LastOrderDetailsResponse
import za.co.woolworths.financial.services.android.models.network.Parameter
import za.co.woolworths.financial.services.android.onecartgetstream.OCChatActivity
import za.co.woolworths.financial.services.android.ui.activities.BarcodeScanActivity
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.INDEX_ACCOUNT
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.INDEX_PRODUCT
import za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity
import za.co.woolworths.financial.services.android.ui.adapters.ShopPagerAdapter
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.ProductListingFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.OrderDetailsFragment.Companion.getInstance
import za.co.woolworths.financial.services.android.ui.fragments.shop.ShopFragment.SelectedTabIndex.CLICK_AND_COLLECT_TAB
import za.co.woolworths.financial.services.android.ui.fragments.shop.ShopFragment.SelectedTabIndex.DASH_TAB
import za.co.woolworths.financial.services.android.ui.fragments.shop.ShopFragment.SelectedTabIndex.STANDARD_TAB
import za.co.woolworths.financial.services.android.ui.fragments.shop.StandardDeliveryFragment.Companion.DEPARTMENT_LOGIN_REQUEST
import za.co.woolworths.financial.services.android.ui.fragments.shop.component.ShopTooltipUiState
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.OnChildFragmentEvents
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView
import za.co.woolworths.financial.services.android.ui.views.shop.dash.ChangeFulfillmentCollectionStoreFragment
import za.co.woolworths.financial.services.android.ui.views.shop.dash.DashDeliveryAddressFragment
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.DELAY_3000_MS
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.REQUEST_CODE_BARCODE_ACTIVITY
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.REQUEST_CODE_ORDER_DETAILS_PAGE
import za.co.woolworths.financial.services.android.util.AppConstant.Keys.Companion.ARG_FROM_NOTIFICATION
import za.co.woolworths.financial.services.android.util.BundleKeysConstants
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.CNC_SET_ADDRESS_REQUEST_CODE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.DASH_SET_ADDRESS_REQUEST_CODE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.REQUEST_CODE
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


@AndroidEntryPoint
class ShopFragment : BaseFragmentBinding<FragmentShopBinding>(FragmentShopBinding::inflate),
    PermissionResultCallback,
    OnChildFragmentEvents,
    WMaterialShowcaseView.IWalkthroughActionListener, View.OnClickListener {

    private val confirmAddressViewModel: ConfirmAddressViewModel by activityViewModels()

    private var timer: CountDownTimer? = null
    private var mTabTitle: MutableList<String>? = null
    private var permissionUtils: PermissionUtils? = null
    var permissions: ArrayList<String> = arrayListOf()
    var shopPagerAdapter: ShopPagerAdapter? = null
    private var rootCategories: RootCategories? = null
    private var ordersResponse: OrdersResponse? = null
    private var shoppingListsResponse: ShoppingListsResponse? = null
    private var user: String = ""
    private var validateLocationResponse: ValidateLocationResponse? = null
    private var tabWidth: Float? = 0f
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

        shopViewModel.tooltipUiState
            .flowWithLifecycle(lifecycle = lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                when (state) {
                    is ShopTooltipUiState.DashTooltip -> {
                        if (state.visibility)
                            showBlackToolTip(Delivery.DASH)
                        else
                            binding.blackToolTipLayout.root.visibility = View.GONE
                    }
                    // TODO StandardTooltip, CNCTooltip
                    is ShopTooltipUiState.StandardTooltip,
                    is ShopTooltipUiState.CNCTooltip,
                    -> {
                    }

                    else -> binding.blackToolTipLayout.root.visibility = View.GONE
                }
            }.launchIn(lifecycleScope)
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
            shopToolbar.setOnClickListener { onEditDeliveryLocation() }

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
                                shopViewModel.onTabClick(position = position)
                                Utils.triggerFireBaseEvents(
                                    FirebaseManagerAnalyticsProperties.SHOP_CATEGORIES,
                                    this
                                )
                                showBlackToolTip(Delivery.STANDARD)
                                setEventsForSwitchingBrowsingType(Delivery.STANDARD.name)
                                KotlinUtils.browsingDeliveryType = Delivery.STANDARD
                                setSearchText(STANDARD_TAB)
                            }

                            CLICK_AND_COLLECT_TAB.index -> {
                                shopViewModel.onTabClick(position = position)
                                showBlackToolTip(Delivery.CNC)
                                setEventsForSwitchingBrowsingType(Delivery.CNC.name)
                                KotlinUtils.browsingDeliveryType = Delivery.CNC
                                setSearchText(CLICK_AND_COLLECT_TAB)
                            }

                            DASH_TAB.index -> {
                                shopViewModel.onTabClick(validateLocationResponse, position)
                                setSearchText(DASH_TAB)
                            }
                        }
                        setupToolbar(position)
                    }
                    shopPagerAdapter?.notifyDataSetChanged()
                    updateTabIconUI(position)
                }
            })
            tabsMain.setupWithViewPager(viewpagerMain)
            updateTabIconUI(STANDARD_TAB.index)
        }
    }

    private fun setSearchText(selectedTab: SelectedTabIndex, location: CharSequence? = null) {
        when (selectedTab) {
            STANDARD_TAB -> {
                binding.tvSearchProduct.text = getString(R.string.shop_landing_product_all_search)
                binding.fulfilmentAndLocationLayout.layoutFulfilment.tvTitle.text = getString(R.string.standard_delivery)
                binding.fulfilmentAndLocationLayout.layoutFulfilment.tvSubTitle.text = getString(R.string.shop_landing_fulfilment_title_cnc_and_standard)
                binding.fulfilmentAndLocationLayout.layoutLocation.tvTitle.text = location ?: getString(R.string.default_location)
            }
            CLICK_AND_COLLECT_TAB -> {
                binding.tvSearchProduct.text = getCncSearchText()
                binding.fulfilmentAndLocationLayout.layoutFulfilment.tvTitle.text = getString(R.string.click_and_collect)
                binding.fulfilmentAndLocationLayout.layoutFulfilment.tvSubTitle.text = getString(R.string.shop_landing_fulfilment_title_cnc_and_standard)
                binding.fulfilmentAndLocationLayout.layoutLocation.tvTitle.text = location ?: getString(R.string.select_your_preferred_store)
            }
            DASH_TAB -> {
                binding.tvSearchProduct.text = getString(R.string.shop_landing_product_food_search)
                binding.fulfilmentAndLocationLayout.layoutFulfilment.tvTitle.text = getString(R.string.dash_delivery)
                binding.fulfilmentAndLocationLayout.layoutFulfilment.tvSubTitle.text = getString(R.string.shop_landing_fulfilment_title_dash)
                binding.fulfilmentAndLocationLayout.layoutLocation.tvTitle.text = location ?: getString(R.string.set_location_title)
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
            /*if (isFromCnc) {
                setSearchText(CLICK_AND_COLLECT_TAB)
            }*/
        }
    }

    fun showClickAndCollectToolTipUi(browsingStoreId: String?) {
        showClickAndCollectToolTip(true, browsingStoreId)
        timer?.cancel()
        if (AppConfigSingleton.tooltipSettings?.isAutoDismissEnabled == true && binding.blackToolTipLayout.root.visibility == VISIBLE) {
            val timeDuration =
                AppConfigSingleton.tooltipSettings?.autoDismissDuration?.times(1000) ?: return
            timer = object : CountDownTimer(timeDuration, 100) {
                override fun onTick(millisUntilFinished: Long) {}
                override fun onFinish() {
                    KotlinUtils.isCncTabCrossClicked = true
                    binding.blackToolTipLayout.root.visibility = View.GONE
                }
            }.start()
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
                tabsMain.isClickable = false
                lifecycleScope.launch {
                    try {
                        validateLocationResponse =
                            confirmAddressViewModel.getValidateLocation(it)
                        shopProgressbar.visibility = View.GONE
                        tabsMain.isClickable = true
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
                                    viewLifecycleOwner.lifecycleScope.launch {
                                        delay(DELAY_3000_MS)
                                        Delivery.getType(getDeliveryType()?.deliveryType)?.let {
                                            showBlackToolTip(it)
                                        }
                                    }
                                }

                                else -> {
                                    blackToolTipLayout.root.visibility = View.GONE
                                }
                            }
                        }
                    } catch (e: Exception) {
                        shopProgressbar.visibility = View.GONE
                        tabsMain.isClickable = true
                        FirebaseManager.logException(e)
                        /*TODO : show error screen*/
                    } catch (e: JsonSyntaxException) {
                        shopProgressbar.visibility = View.GONE
                        tabsMain.isClickable = true
                        FirebaseManager.logException(e)
                    }
                }
            }
        }
    }

    private fun onEditDeliveryLocation() {
        Utils.triggerFireBaseEvents(
            FirebaseManagerAnalyticsProperties.SHOP_DELIVERY_CLICK_COLLECT,
            hashMapOf(
                FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                        FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_SHOP_DELIVERY_CLICK_COLLECT
            ),
            activity
        )

        KotlinUtils.presentEditDeliveryGeoLocationActivity(
            requireActivity(),
            REQUEST_CODE,
            Delivery.getType(getDeliveryType()?.deliveryType) ?: KotlinUtils.browsingDeliveryType,
            getDeliveryType()?.address?.placeId ?: ""
        )
    }

    override fun onResume() {
        super.onResume()
        if (isVisible) {
            if (((KotlinUtils.isLocationPlaceIdSame == false || KotlinUtils.isNickNameChanged == true) && KotlinUtils.placeId != null) || WoolworthsApplication.getValidatePlaceDetails() == null) {
                executeValidateSuburb()
            } else if (Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.deliveryType.isNullOrEmpty() && KotlinUtils.getAnonymousUserLocationDetails()?.fulfillmentDetails?.deliveryType.isNullOrEmpty()) {
                return
            } else if (KotlinUtils.isLocationPlaceIdSame == true && KotlinUtils.placeId != null) {
                setDeliveryView()
                (KotlinUtils.browsingDeliveryType
                    ?: Delivery.getType(getDeliveryType()?.deliveryType))?.let {
                    showBlackToolTip(it)
                }
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

    private fun setupToolbar(tabPosition: Int) {
        if (tabPosition < 0) {
            return
        }
        if (getDeliveryType()?.address?.placeId != null) {
            return
        }

        binding.apply {
            when (tabPosition) {
                CLICK_AND_COLLECT_TAB.index -> {
                    imgToolbarStart?.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_collection_circle
                        )
                    )
                    tvToolbarTitle?.text = requireContext().getString(R.string.click_and_collect)
                    tvToolbarSubtitle?.text =
                        requireContext().getString(R.string.select_your_preferred_store)
                }

                DASH_TAB.index -> {
                    imgToolbarStart?.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_dash_delivery_circle
                        )
                    )
                    tvToolbarTitle?.text = requireContext().getString(R.string.dash_delivery)
                    tvToolbarSubtitle?.text =
                        requireContext().getString(R.string.set_location_title)
                }

                else -> {
                    imgToolbarStart?.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_delivery_circle
                        )
                    )
                    tvToolbarTitle?.text = requireContext().getString(R.string.standard_delivery)
                    tvToolbarSubtitle?.text = requireContext().getString(R.string.default_location)
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

    fun checkRunTimePermissionForLocation(): Boolean {
        permissionUtils?.apply {
            val permissions = ArrayList<String>()
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            return checkAndRequestPermissions(permissions, 3)
        }
        return false
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

        binding.tabsMain?.let { tabLayout ->
            tabLayout.getTabAt(selectedTab)?.customView?.isSelected = true
            for (i in mTabTitle?.indices!!) {
                tabLayout.getTabAt(i)?.customView = prepareTabView(tabLayout, i, mTabTitle)
            }

            val margin = requireContext().resources.getDimensionPixelSize(R.dimen.sixteen_dp)
            for (i in 0 until tabLayout.tabCount) {
                val tab = (tabLayout.getChildAt(0) as ViewGroup).getChildAt(i)
                val layoutParams = tab.layoutParams as MarginLayoutParams
                if (i == 0) {
                    layoutParams.setMargins(margin, 0, 0, 0)
                } else if (i == 2) {
                    layoutParams.setMargins(0, 0, margin, 0)
                }
                tab.requestLayout()
            }

        }
    }

    fun setShopDefaultTab() {
        binding.viewpagerMain.currentItem = 0
    }

    private fun prepareTabView(
        tabLayout: TabLayout,
        pos: Int,
        tabTitle: MutableList<String>?,
    ): View? {
        val shopCustomTabBinding =
            ShopCustomTabBinding.inflate(requireActivity().layoutInflater, null, false)
        tabWidth = shopCustomTabBinding.root?.width?.toFloat()
        shopCustomTabBinding?.tvTitle?.text = tabTitle?.getOrNull(pos)
        shopCustomTabBinding?.foodOnlyText?.visibility = if (pos == 2) View.VISIBLE else View.GONE
        if (tabLayout.getTabAt(pos)?.view?.isSelected == true) {
            val typeface = ResourcesCompat.getFont(requireContext(), R.font.opensans_semi_bold)
            shopCustomTabBinding.tvTitle.typeface = typeface
        }
        return shopCustomTabBinding.root
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

    fun setDeliveryView() {
        setNewDeliveryAndFulfilmentText()
        binding.apply {
            activity?.let {
                getDeliveryType()?.let { fulfillmentDetails ->
                    KotlinUtils.setDeliveryAddressViewFoShop(
                        it,
                        fulfillmentDetails,
                        tvToolbarTitle,
                        tvToolbarSubtitle,
                        imgToolbarStart
                    )
                }
            }
        }
    }

    private fun setNewDeliveryAndFulfilmentText() {
        if (!isAdded) {
            return
        }
        binding.apply {
            val fulfillmentDetails: FulfillmentDetails? = getDeliveryType()
            fulfillmentDetails?.apply {
                when (Delivery.getType(deliveryType)) {
                    Delivery.CNC -> {
//                        fulfilmentAndLocationLayout.layoutFulfilment.tvTitle.text = getString(R.string.click_collect)
//                        fulfilmentAndLocationLayout.layoutLocation.tvTitle.text = KotlinUtils.capitaliseFirstLetter(storeName)
                        setSearchText(CLICK_AND_COLLECT_TAB, location = KotlinUtils.capitaliseFirstLetter(storeName))
                    }

                    Delivery.STANDARD -> {
                        //fulfilmentAndLocationLayout.layoutFulfilment.tvTitle.text = getString(R.string.standard_delivery)
                        val fullAddress = KotlinUtils.capitaliseFirstLetter(address?.address1 ?: "")

                        val formattedNickName = KotlinUtils.getFormattedNickName(
                            address?.nickname,
                            fullAddress, context
                        )
                        formattedNickName.append(fullAddress)
                        //fulfilmentAndLocationLayout.layoutLocation.tvTitle.text = formattedNickName
                        setSearchText(STANDARD_TAB, location = formattedNickName)
                    }

                    Delivery.DASH -> {
                        val timeSlot: String? =
                            WoolworthsApplication.getValidatePlaceDetails()?.onDemand?.firstAvailableFoodDeliveryTime

                        //binding.fulfilmentAndLocationLayout.layoutFulfilment.tvTitle.text = getString(R.string.dash_delivery_bold)

                        val fullAddress = KotlinUtils.capitaliseFirstLetter(address?.address1 ?: "")

                        val formattedNickName = KotlinUtils.getFormattedNickName(
                            address?.nickname,
                            fullAddress, context
                        )

                        val location = if (timeSlot.isNullOrEmpty()) {
                            getString(R.string.no_timeslots_available_title).plus("\t\u2022\t").plus(
                                        formattedNickName.append(
                                            KotlinUtils.capitaliseFirstLetter(
                                                WoolworthsApplication.getValidatePlaceDetails()?.placeDetails?.address1
                                                    ?: address?.address1 ?: ""
                                            )
                                        )
                                    )
                        } else {
                            timeSlot.plus("\t\u2022\t").plus(formattedNickName).plus(
                                    KotlinUtils.capitaliseFirstLetter(
                                        WoolworthsApplication.getValidatePlaceDetails()?.placeDetails?.address1
                                            ?: address?.address1 ?: ""
                                    )
                                )
                        }
                        setSearchText(DASH_TAB, location = location)
                    }

                    else -> {
//                        binding.fulfilmentAndLocationLayout.layoutFulfilment.tvTitle.text = getString(R.string.standard_delivery)
//                        fulfilmentAndLocationLayout.layoutLocation.tvTitle.text = getString(R.string.default_location)
                        setSearchText(STANDARD_TAB)
                    }
                }
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            //do when hidden
            timer?.start()
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
        } else {
            if (binding.blackToolTipLayout.root.isVisible) {
                timer?.cancel()
            }
        }

        if (getDeliveryType() == null) {
            setupToolbar(STANDARD_TAB.index)
            setSearchText(STANDARD_TAB)
            binding.viewpagerMain.currentItem = STANDARD_TAB.index
        } else {
            setDeliveryView()
        }
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
            if (fragment is DashDeliveryAddressFragment){
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
                shopViewModel?.onTabClick(
                    validateLocationResponse,
                    DASH_TAB.index
                ) // externally showing dash tooltip as delivery type is not same.
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

    /***
     * TODO:: Add barcode scanner in an Activity
     */
    fun openBarcodeScanner() {
        binding.imBarcodeScanner?.performClick()
    }

    fun switchToDepartmentTab() {
        binding.viewpagerMain.currentItem = STANDARD_TAB.index
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

    private fun showBlackToolTip(deliveryType: Delivery) {
        binding.apply {
            if (validateLocationResponse == null || getDeliveryType() == null) {
                blackToolTipLayout.root.visibility = View.GONE
                return
            }
            blackToolTipLayout.closeWhiteBtn?.setOnClickListener {
                when (deliveryType) {
                    Delivery.STANDARD -> {
                        KotlinUtils.isDeliveryLocationTabCrossClicked = true
                    }

                    Delivery.CNC -> {
                        KotlinUtils.isCncTabCrossClicked = true
                    }

                    Delivery.DASH -> {
                        KotlinUtils.isDashTabCrossClicked = true
                    }
                }
                blackToolTipLayout.root.visibility = View.GONE
            }
            blackToolTipLayout.changeLocationButton?.setOnClickListener {

                val browsingPlaceId = when (KotlinUtils.browsingDeliveryType) {
                    Delivery.STANDARD -> WoolworthsApplication.getValidatePlaceDetails()?.placeDetails?.placeId
                    Delivery.CNC -> WoolworthsApplication.getCncBrowsingValidatePlaceDetails()?.placeDetails?.placeId
                        ?: WoolworthsApplication.getValidatePlaceDetails()?.placeDetails?.placeId

                    Delivery.DASH -> WoolworthsApplication.getDashBrowsingValidatePlaceDetails()?.placeDetails?.placeId
                        ?: WoolworthsApplication.getValidatePlaceDetails()?.placeDetails?.placeId

                    else -> getDeliveryType()?.address?.placeId ?: ""

                }

                KotlinUtils.presentEditDeliveryGeoLocationActivity(
                    requireActivity(),
                    REQUEST_CODE,
                    KotlinUtils.browsingDeliveryType,
                    browsingPlaceId
                )
            }
            timer?.cancel()
            when (deliveryType) {
                Delivery.STANDARD -> {
                    showStandardDeliveryToolTip()
                }

                Delivery.CNC -> {
                    showClickAndCollectToolTip(
                        KotlinUtils.isStoreSelectedForBrowsing,
                        KotlinUtils.browsingCncStore?.storeId
                    )
                }

                Delivery.DASH -> {
                    showDashToolTip(validateLocationResponse)
                }
            }

            if (AppConfigSingleton.tooltipSettings?.isAutoDismissEnabled == true && blackToolTipLayout.root.visibility == VISIBLE) {
                val timeDuration =
                    AppConfigSingleton.tooltipSettings?.autoDismissDuration?.times(1000) ?: return
                timer = object : CountDownTimer(timeDuration, 100) {
                    override fun onTick(millisUntilFinished: Long) {}
                    override fun onFinish() {
                        when (KotlinUtils.fullfillmentTypeClicked) {
                            Delivery.STANDARD.name -> {
                                KotlinUtils.isDeliveryLocationTabCrossClicked = true
                            }

                            Delivery.CNC.name -> {
                                KotlinUtils.isCncTabCrossClicked = true
                            }

                            Delivery.DASH.name -> {
                                KotlinUtils.isDashTabCrossClicked = true
                            }
                        }
                        blackToolTipLayout.root.visibility = View.GONE
                    }
                }.start()
            }
        }
    }

    private fun showStandardDeliveryToolTip() {
        binding.apply {
            if (KotlinUtils.isLocationPlaceIdSame == false) {
                blackToolTipLayout.root.visibility = View.VISIBLE
            }

            if (KotlinUtils.isDeliveryLocationTabCrossClicked == true) {
                blackToolTipLayout.root.visibility = View.GONE
                return
            }

            if (validateLocationResponse?.validatePlace?.firstAvailableFoodDeliveryDate.isNullOrEmpty()
                && validateLocationResponse?.validatePlace?.firstAvailableOtherDeliveryDate.isNullOrEmpty()
            ) {
                blackToolTipLayout.root.visibility = View.GONE
                return
            }

            blackToolTipLayout.root.visibility = View.VISIBLE
            if (getDeliveryType() == null || Delivery.getType(getDeliveryType()?.deliveryType)?.type == Delivery.STANDARD.type) {
                blackToolTipLayout.changeButtonLayout?.visibility = View.GONE
            } else {
                blackToolTipLayout.changeButtonLayout?.visibility = View.VISIBLE
                blackToolTipLayout.changeText?.text = getText(R.string.shop_using_standard_delivery)
            }
            KotlinUtils.fullfillmentTypeClicked = Delivery.STANDARD.name
            validateLocationResponse?.validatePlace?.let {
                blackToolTipLayout.fashionItemDateText?.visibility = View.VISIBLE
                blackToolTipLayout.foodItemTitle?.visibility = View.VISIBLE
                blackToolTipLayout.foodItemDateText?.visibility = View.VISIBLE
                blackToolTipLayout.fashionItemTitle?.visibility = View.VISIBLE
                blackToolTipLayout.deliveryIconLayout?.visibility = View.GONE

                blackToolTipLayout.fashionItemTitle?.text = getString(R.string.fashion_beauty_home)

                if (it.firstAvailableFoodDeliveryDate.isNullOrEmpty() == true) {
                    blackToolTipLayout.deliveryCollectionTitle?.visibility = View.GONE
                    blackToolTipLayout.foodItemDateText?.visibility = View.GONE
                    blackToolTipLayout.foodItemTitle?.visibility = View.GONE
                }

                if (it.firstAvailableOtherDeliveryDate.isNullOrEmpty() == true) {
                    blackToolTipLayout.fashionItemTitle?.visibility = View.GONE
                    blackToolTipLayout.fashionItemDateText?.visibility = View.GONE
                }

                blackToolTipLayout.deliveryCollectionTitle?.text =
                    getString(R.string.earliest_delivery_dates)
                blackToolTipLayout.foodItemDateText?.text = it.firstAvailableFoodDeliveryDate
                blackToolTipLayout.fashionItemDateText?.text = it.firstAvailableOtherDeliveryDate
                blackToolTipLayout.productAvailableText?.text =
                    getString(R.string.all_products_available)
                blackToolTipLayout.cartIcon.setImageResource(R.drawable.icon_cart_white)
                blackToolTipLayout.bubbleLayout?.arrowDirection = ArrowDirection.TOP
                if (tabsMain?.getTabAt(STANDARD_TAB.index)?.view != null) {
                    blackToolTipLayout.bubbleLayout?.arrowPosition =
                        tabsMain?.getTabAt(STANDARD_TAB.index)?.view?.width?.div(2)?.toFloat()!!
                }
            }
        }
    }

    fun showClickAndCollectToolTip(
        isStoreSelectedForBrowsing: Boolean = false,
        browsingStoreId: String? = "",
    ) {
        var browsingStoreOrStoreId: String? = browsingStoreId
        if (browsingStoreOrStoreId.isNullOrEmpty()) {
            browsingStoreOrStoreId = getDeliveryType()?.storeId ?: ""
        }
        binding.apply {
            if (KotlinUtils.isCncTabCrossClicked == true || browsingStoreOrStoreId.isNullOrEmpty()) {
                blackToolTipLayout.root.visibility = View.GONE
                return
            }
            blackToolTipLayout.root.visibility = View.VISIBLE
            blackToolTipLayout.bubbleLayout.arrowDirection = ArrowDirection.TOP_CENTER
            if (getDeliveryType() == null || Delivery.getType(getDeliveryType()?.deliveryType)?.type == Delivery.CNC.type) {
                blackToolTipLayout.changeButtonLayout?.visibility = View.GONE
            } else {
                blackToolTipLayout.changeButtonLayout?.visibility = View.VISIBLE
                blackToolTipLayout.changeText?.text = context?.getText(R.string.shop_using_cnc)
            }
            KotlinUtils.fullfillmentTypeClicked = Delivery.CNC.name
            validateLocationResponse?.validatePlace?.let { validatePlace ->

                val store = GeoUtils.getStoreDetails(
                    getStoreId(isStoreSelectedForBrowsing, browsingStoreOrStoreId),
                    validatePlace.stores
                )

                store?.apply {
                    blackToolTipLayout.deliveryCollectionTitle?.text =
                        getString(R.string.earliest_collection_Date)
                    val collectionQuantity =
                        quantityLimit?.foodMaximumQuantity
                    blackToolTipLayout.deliveryIconLayout?.visibility = View.VISIBLE
                    //checking fbh products condition
                    if (locationId?.isNotEmpty() == true && firstAvailableFoodDeliveryDate.isNullOrEmpty()) {
                        enableOrDisableFashionItems(true)
                        enableOrDisableFoodItems(false)
                        blackToolTipLayout.fashionItemTitle?.visibility = View.GONE
                        blackToolTipLayout.fashionItemDateText?.text =
                            firstAvailableOtherDeliveryDate
                        blackToolTipLayout.productAvailableText?.text =
                            context?.getString(R.string.only_fashion_beauty_and_home_products_available_text)
                        blackToolTipLayout.deliveryFeeText?.text =
                            AppConfigSingleton.clickAndCollect?.collectionFeeDescription
                    }
                    //food products checking conditions
                    else if (firstAvailableOtherDeliveryDate.isNullOrEmpty() && !firstAvailableFoodDeliveryDate.isNullOrEmpty()) {
                        enableOrDisableFashionItems(false)
                        enableOrDisableFoodItems(true)
                        blackToolTipLayout.foodItemTitle?.visibility = View.GONE
                        if (!firstAvailableFoodDeliveryDate.isNullOrEmpty()) {
                            blackToolTipLayout.foodItemDateText?.text =
                                firstAvailableFoodDeliveryDate
                        }
                        blackToolTipLayout.productAvailableText?.text =
                            bindString(
                                R.string.cnc_title_text_2,
                                collectionQuantity.toString()
                            )
                        blackToolTipLayout.deliveryFeeText?.text =
                            context?.getString(R.string.dash_free_collection)
                    } else {
                        //mixed basket
                        enableOrDisableFashionItems(true)
                        enableOrDisableFoodItems(true)
                        blackToolTipLayout.fashionItemTitle?.visibility = View.VISIBLE
                        blackToolTipLayout.foodItemTitle?.visibility = View.VISIBLE
                        blackToolTipLayout.fashionItemDateText?.text =
                            firstAvailableOtherDeliveryDate
                        if (!firstAvailableFoodDeliveryDate.isNullOrEmpty()) {
                            blackToolTipLayout.foodItemDateText?.text =
                                firstAvailableFoodDeliveryDate
                        }
                        blackToolTipLayout.productAvailableText?.text =
                            context?.getString(R.string.food_fashion_beauty_and_home_products_available_tool_tip)
                        blackToolTipLayout.deliveryFeeText.text =
                            AppConfigSingleton.clickAndCollect?.collectionFeeDescription
                    }
                    blackToolTipLayout.cartIcon.setImageResource(R.drawable.icon_cart_white)
                    blackToolTipLayout.deliveryIcon.setImageResource(R.drawable.white_shopping_bag_icon)
                    blackToolTipLayout.bubbleLayout?.setArrowDirection(ArrowDirection.TOP_CENTER)
                } ?: run {
                    blackToolTipLayout.root.visibility = View.GONE
                }
            }
        }
    }

    private fun getStoreId(isStoreSelectedForBrowsing: Boolean, browsingStoreId: String): String? {
        return if (isStoreSelectedForBrowsing) {
            /* select store from store list */
            browsingStoreId
        } else {
            if (getDeliveryType()?.storeId == null) browsingStoreId else getDeliveryType()?.storeId
        }
    }

    private fun showDashToolTip(validateLocationResponse: ValidateLocationResponse?) {
        binding?.apply {
            if (KotlinUtils.isLocationPlaceIdSame == false) {
                blackToolTipLayout.root.visibility = View.VISIBLE
            }

            blackToolTipLayout.root.visibility = View.VISIBLE
            blackToolTipLayout.bubbleLayout.arrowDirection = ArrowDirection.TOP
            blackToolTipLayout.bubbleLayout.arrowPosition =
                tabsMain.width - tabsMain.getTabAt(DASH_TAB.index)?.view?.width?.div(
                    DASH_DIVIDER
                )?.toFloat()!!
            if (getDeliveryType() == null || Delivery.getType(getDeliveryType()?.deliveryType)?.type == Delivery.DASH.type) {
                blackToolTipLayout.changeButtonLayout.visibility = View.GONE
            } else {
                blackToolTipLayout.changeButtonLayout.visibility = View.VISIBLE
                blackToolTipLayout.changeText.text = getText(R.string.shop_using_dash_delivery)
            }
            KotlinUtils.fullfillmentTypeClicked = Delivery.DASH.name
            validateLocationResponse?.validatePlace?.let {

                val timeSlots = it?.onDemand?.deliveryTimeSlots

                blackToolTipLayout.foodItemTitle.visibility = View.GONE
                blackToolTipLayout.fashionItemDateText.visibility = View.GONE
                blackToolTipLayout.deliveryIconLayout.visibility = View.VISIBLE
                blackToolTipLayout.cartIconLayout.visibility = View.VISIBLE
                blackToolTipLayout.fashionItemTitle.visibility = View.GONE
                blackToolTipLayout.deliveryIcon.visibility = View.VISIBLE
                blackToolTipLayout.deliveryFeeText.visibility = View.VISIBLE

                if (timeSlots?.isNullOrEmpty() == true && it?.onDemand?.deliverable == true) {
                    blackToolTipLayout.deliveryCollectionTitle.text =
                        getString(R.string.next_dash_delivery_timeslot_text)
                    blackToolTipLayout.foodItemDateText.visibility = View.VISIBLE
                    blackToolTipLayout.foodItemDateText.text =
                        getString(R.string.no_timeslots_available_title)
                    blackToolTipLayout.fashionItemTitle.visibility = View.VISIBLE
                    blackToolTipLayout.fashionItemTitle.text = getString(R.string.timeslot_desc)
                } else {
                    blackToolTipLayout.deliveryCollectionTitle.text =
                        getString(R.string.next_dash_delivery_timeslot_text)
                    blackToolTipLayout.foodItemDateText.visibility = View.VISIBLE
                    blackToolTipLayout.foodItemDateText.text =
                        it.onDemand?.firstAvailableFoodDeliveryTime
                    blackToolTipLayout.fashionItemTitle.visibility = View.GONE
                }

                blackToolTipLayout.cartIcon.setImageResource(R.drawable.icon_cart_white)
                blackToolTipLayout.deliveryIcon.setImageResource(R.drawable.icon_scooter_white)
                blackToolTipLayout.productAvailableText.text =
                    HtmlCompat.fromHtml(
                        "<font><b>" + it.onDemand?.quantityLimit?.foodMaximumQuantity + "</b></font>"
                            .plus(" ").plus(
                                resources.getString(
                                    R.string.dash_item_limit
                                )
                            ),
                        HtmlCompat.FROM_HTML_MODE_COMPACT
                    )

                if (it.onDemand?.firstAvailableFoodDeliveryTime?.isNullOrEmpty() == true) {
                    blackToolTipLayout.deliveryIconLayout.visibility = View.GONE
                } else {
                    blackToolTipLayout.deliveryIconLayout.visibility = View.VISIBLE
                    blackToolTipLayout.deliveryFeeText.text =
                        HtmlCompat.fromHtml(
                            "<font><b>" + it.onDemand?.firstAvailableFoodDeliveryCost + "</b></font>"
                                .plus(" ").plus(
                                    resources.getString(
                                        R.string.dash_delivery_fee
                                    )
                                ),
                            HtmlCompat.FROM_HTML_MODE_COMPACT
                        )
                }
            }
        }
    }

    fun showShopFeatureWalkThrough() {
        (activity as? BottomNavigationActivity)?.let {
            // Prevent dialog to display in other section when fragment is not visible
            if (it.currentFragment !is ShopFragment || !isAdded || AppInstanceObject.get().featureWalkThrough.shopping || !Utils.isFeatureWalkThroughTutorialsEnabled())
                return
            FirebaseManager.setCrashlyticsString(
                bindString(R.string.crashlytics_materialshowcase_key),
                this.javaClass.canonicalName
            )
            it.walkThroughPromtView =
                WMaterialShowcaseView.Builder(it, WMaterialShowcaseView.Feature.SHOPPING)
                    .setTarget(it.bottomNavigationById?.getIconAt(INDEX_PRODUCT))
                    .setTitle(R.string.walkthrough_shop_title)
                    .setDescription(R.string.walkthrough_shop_desc)
                    .setActionText(R.string.walkthrough_shop_action)
                    .setImage(R.drawable.ic_drw_products)
                    .setShapePadding(48)
                    .setDescriptionTextColor()
                    .setHideTutorialTextColor()
                    .setAction(this@ShopFragment)
                    .setArrowPosition(WMaterialShowcaseView.Arrow.BOTTOM_LEFT)
                    .setMaskColour(ContextCompat.getColor(it, R.color.semi_transparent_black))
                    .build()
            it.walkThroughPromtView.show(it)
        }
    }

    private fun showDashFeatureWalkThrough() {
        (activity as? BottomNavigationActivity)?.let {
            // Prevent dialog to display in other section when fragment is not visible
            if (it.currentFragment !is ShopFragment || !isAdded || AppInstanceObject.get().featureWalkThrough.dash || !Utils.isFeatureWalkThroughTutorialsEnabled())
                return
            FirebaseManager.setCrashlyticsString(
                bindString(R.string.crashlytics_materialshowcase_key),
                this.javaClass.canonicalName
            )
            it.walkThroughPromtView =
                WMaterialShowcaseView.Builder(it, WMaterialShowcaseView.Feature.DASH)
                    .setTarget(binding.tabsMain?.getChildAt(0))
                    .setTitle(R.string.walkthrough_dash_title)
                    .setDescription(R.string.walkthrough_dash_desc)
                    .setActionText(R.string.walkthrough_dash_action)
                    .setImage(R.drawable.dash_delivery_icon)
                    .withRectangleShape(true)
                    .setShapePadding(0)
                    .setDescriptionTextColor()
                    .setHideTutorialTextColor()
                    .setAction(this@ShopFragment)
                    .setArrowPosition(WMaterialShowcaseView.Arrow.TOP_RIGHT)
                    .setMaskColour(ContextCompat.getColor(it, R.color.semi_transparent_black))
                    .build()
            it.walkThroughPromtView.show(it)
        }
    }

    private fun showDeliveryDetailsFeatureWalkThrough() {
        (activity as? BottomNavigationActivity)?.let {
            // Prevent dialog to display in other section when fragment is not visible
            if (it.currentFragment !is ShopFragment || !isAdded || AppInstanceObject.get().featureWalkThrough.delivery_details || !Utils.isFeatureWalkThroughTutorialsEnabled())
                return
            FirebaseManager.setCrashlyticsString(
                bindString(R.string.crashlytics_materialshowcase_key),
                this.javaClass.canonicalName
            )
            it.walkThroughPromtView =
                WMaterialShowcaseView.Builder(it, WMaterialShowcaseView.Feature.DELIVERY_DETAILS)
                    .setTarget(binding.shopToolbar)
                    .setTitle(R.string.walkthrough_delivery_details_title)
                    .setDescription(R.string.walkthrough_delivery_details_desc)
                    .setActionText(R.string.walkthrough_delivery_details_action)
                    .setImage(R.drawable.ic_delivery_truck)
                    .withRectangleShape()
                    .setShapePadding(0)
                    .setDescriptionTextColor()
                    .setHideTutorialTextColor()
                    .setAction(this@ShopFragment)
                    .setArrowPosition(WMaterialShowcaseView.Arrow.TOP_LEFT)
                    .setMaskColour(ContextCompat.getColor(it, R.color.semi_transparent_black))
                    .build()
            it.walkThroughPromtView.show(it)
        }
    }

    private fun showMyListsFeatureWalkThrough() {
        (activity as? BottomNavigationActivity)?.let {
            // Prevent dialog to display in other section when fragment is not visible
            if (it.currentFragment !is ShopFragment || !isAdded || AppInstanceObject.get().featureWalkThrough.my_lists || !Utils.isFeatureWalkThroughTutorialsEnabled())
                return
            FirebaseManager.setCrashlyticsString(
                bindString(R.string.crashlytics_materialshowcase_key),
                this.javaClass.canonicalName
            )
            it.walkThroughPromtView =
                WMaterialShowcaseView.Builder(it, WMaterialShowcaseView.Feature.MY_LIST)
                    .setTarget(it.bottomNavigationById?.getIconAt(INDEX_ACCOUNT))
                    .setTitle(R.string.new_location_list)
                    .setDescription(R.string.early_access_shopping)
                    .setActionText(R.string.view_shopping_list_action)
                    .setImage(R.drawable.add)
                    .setShapePadding(56)
                    .setDescriptionTextColor()
                    .setHideTutorialTextColor()
                    .setAction(this@ShopFragment)
                    .setArrowPosition(WMaterialShowcaseView.Arrow.BOTTOM_RIGHT)
                    .setMaskColour(ContextCompat.getColor(it, R.color.semi_transparent_black))
                    .build()
            it.walkThroughPromtView.show(it)
        }
    }

    private fun showBarcodeScannerFeatureWalkThrough() {
        (activity as? BottomNavigationActivity)?.let {
            // Prevent dialog to display in other section when fragment is not visible
            if (it.currentFragment !is ShopFragment || binding.imBarcodeScanner == null || !isAdded || AppInstanceObject.get().featureWalkThrough.barcodeScan || !Utils.isFeatureWalkThroughTutorialsEnabled())
                return
            FirebaseManager.setCrashlyticsString(
                bindString(R.string.crashlytics_materialshowcase_key),
                this.javaClass.canonicalName
            )
            it.walkThroughPromtView =
                WMaterialShowcaseView.Builder(it, WMaterialShowcaseView.Feature.BARCODE_SCAN)
                    .setTarget(binding.imBarcodeScanner)
                    .setTitle(R.string.feature_barcode_scanning_title)
                    .setDescription(R.string.feature_barcode_scanning_desc)
                    .setActionText(R.string.feature_barcode_scanning_action_text)
                    .setImage(R.drawable.tips_tricks_ic_scan)
                    .setShapePadding(20)
                    .setAction(this@ShopFragment)
                    .setArrowPosition(WMaterialShowcaseView.Arrow.TOP_RIGHT)
                    .setMaskColour(ContextCompat.getColor(it, R.color.semi_transparent_black))
                    .build()
            it.walkThroughPromtView.show(it)
        }
    }

    override fun onWalkthroughActionButtonClick(feature: WMaterialShowcaseView.Feature?) {
        if (activity == null) {
            return
        }
        when (feature) {
            WMaterialShowcaseView.Feature.DASH -> {
                binding.viewpagerMain?.apply {
                    currentItem = DASH_TAB.index
                    adapter?.notifyDataSetChanged()
                }
                updateTabIconUI(DASH_TAB.index)
                showDeliveryDetailsFeatureWalkThrough()
            }

            WMaterialShowcaseView.Feature.SHOPPING -> {
                showDashFeatureWalkThrough()
            }

            WMaterialShowcaseView.Feature.BARCODE_SCAN -> {
                checkCameraPermission()
            }

            WMaterialShowcaseView.Feature.DELIVERY_DETAILS -> {
                onEditDeliveryLocation()
            }

            WMaterialShowcaseView.Feature.MY_LIST -> {
                if (SessionUtilities.getInstance().isUserAuthenticated) {
                    navigateToMyListFragment()
                } else {
                    navigateToMyListFragment()
                }
            }

            else -> {}
        }
    }

    override fun onPromptDismiss(feature: WMaterialShowcaseView.Feature) {
        if (activity == null) {
            return
        }
        when (feature) {
            WMaterialShowcaseView.Feature.SHOPPING -> {
                showDashFeatureWalkThrough()
            }

            WMaterialShowcaseView.Feature.DASH -> {
                showDeliveryDetailsFeatureWalkThrough()
            }

            WMaterialShowcaseView.Feature.DELIVERY_DETAILS -> {
                executeValidateSuburb()
                showMyListsFeatureWalkThrough()
            }

            WMaterialShowcaseView.Feature.MY_LIST -> {
                showBarcodeScannerFeatureWalkThrough()
            }

            else -> {}
        }
    }

    fun isUserAuthenticated() = SessionUtilities.getInstance().isUserAuthenticated

    fun getCurrentFragmentIndex() = binding.viewpagerMain?.currentItem

    override fun onClick(v: View?) {
        when (v?.id) {
            // In App notification click, Navigate to Order Details
            R.id.inappOrderNotificationContainer -> {
                (requireActivity() as? BottomNavigationActivity)?.apply {
                    val orderId: String? = v.getTag(R.id.inappOrderNotificationContainer) as? String
                    orderId?.let {
                        pushFragment(getInstance(Parameter(it)))
                    }
                }
            }
            // In App notification Chat click, Navigate to Chat
            // Chat / Driver Tracking / Location
            R.id.inappOrderNotificationIcon -> {
                val params = v.getTag(R.id.inappOrderNotificationIcon) as? LastOrderDetailsResponse
                params?.apply {
                    // Chat
                    if (params.isChatEnabled) {
                        navigateToChat(orderId)
                    }
                    // Driver tracking
                    else if (params.isDriverTrackingEnabled) {
                        driverTrackingUrl?.let { navigateToOrderTrackingScreen(it) }
                    }
                }
            }
        }
    }

    private fun navigateToChat(orderId: String?) {
        orderId?.let {
            startActivity(OCChatActivity.newIntent(requireActivity(), it))
        }
    }

    private fun navigateToOrderTrackingScreen(url: String) {
        requireActivity().apply {
            startActivity(OrderTrackingWebViewActivity.newIntent(this, url))
            overridePendingTransition(
                R.anim.slide_from_right,
                R.anim.slide_out_to_left
            )
        }
    }

    private fun enableOrDisableFashionItems(isEnabled: Boolean) {
        binding.blackToolTipLayout?.apply {
            if (isEnabled) {
                fashionItemTitle.visibility = View.VISIBLE
                fashionItemDateText.visibility = View.VISIBLE
            } else {
                fashionItemTitle.visibility = View.GONE
                fashionItemDateText.visibility = View.GONE
            }
        }
    }

    private fun enableOrDisableFoodItems(isEnabled: Boolean) {
        binding.blackToolTipLayout?.apply {
            if (isEnabled) {
                foodItemTitle.visibility = View.VISIBLE
                foodItemDateText.visibility = View.VISIBLE
            } else {
                foodItemTitle.visibility = View.GONE
                foodItemDateText.visibility = View.GONE
            }
        }

    }
}
