package za.co.woolworths.financial.services.android.ui.fragments.shop

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.ViewGroup.VISIBLE
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.awfs.coordination.R
import com.daasuu.bl.ArrowDirection
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.black_tool_tip_layout.*
import kotlinx.android.synthetic.main.fragment_shop.*
import kotlinx.android.synthetic.main.geo_location_delivery_address.*
import kotlinx.android.synthetic.main.shop_custom_tab.view.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.geolocation.GeoUtils
import za.co.woolworths.financial.services.android.geolocation.network.apihelper.GeoLocationApiHelper
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidateLocationResponse
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.geolocation.viewmodel.GeoLocationViewModelFactory
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.models.dto.OrdersResponse
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams.SearchType
import za.co.woolworths.financial.services.android.models.dto.RootCategories
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse
import za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity.Companion.ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE
import za.co.woolworths.financial.services.android.ui.activities.BarcodeScanActivity
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.*
import za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity
import za.co.woolworths.financial.services.android.ui.adapters.ShopPagerAdapter
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.ProductListingFragment.Companion.newInstance
import za.co.woolworths.financial.services.android.ui.fragments.shop.ShopFragment.SelectedTabIndex.*
import za.co.woolworths.financial.services.android.ui.fragments.shop.StandardDeliveryFragment.Companion.DEPARTMENT_LOGIN_REQUEST
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList.Companion.DISPLAY_TOAST_RESULT_CODE
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.OnChildFragmentEvents
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView
import za.co.woolworths.financial.services.android.ui.views.shop.dash.ChangeFullfilmentCollectionStoreFragment
import za.co.woolworths.financial.services.android.ui.views.shop.dash.DashDeliveryAddressFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.DELAY_3000_MS
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.DELAY_4000_MS
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.REQUEST_CODE_BARCODE_ACTIVITY
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.REQUEST_CODE_ORDER_DETAILS_PAGE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.CNC_SET_ADDRESS_REQUEST_CODE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.DASH_SET_ADDRESS_REQUEST_CODE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.REQUEST_CODE
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.getDeliveryType
import za.co.woolworths.financial.services.android.util.ScreenManager.SHOPPING_LIST_DETAIL_ACTIVITY_REQUEST_CODE
import za.co.woolworths.financial.services.android.util.wenum.Delivery


/**
 * A simple [Fragment] subclass.
 *
 */
@AndroidEntryPoint
class ShopFragment : Fragment(R.layout.fragment_shop), PermissionResultCallback,
    OnChildFragmentEvents,
    WMaterialShowcaseView.IWalkthroughActionListener {

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
                    (requireActivity() as? BottomNavigationActivity)?.pushFragment(newInstance(
                        searchType,
                        "",
                        searchTerm,
                        false))
                }
            }
        }

    companion object {
        private const val LOGIN_MY_LIST_REQUEST_CODE = 9876
    }

    enum class SelectedTabIndex(val index: Int) {
        STANDARD_TAB(0),
        CLICK_AND_COLLECT_TAB(1),
        DASH_TAB(2)
    }

    private val confirmAddressViewModel: ConfirmAddressViewModel by lazy {
        ViewModelProvider(
            this,
            GeoLocationViewModelFactory(GeoLocationApiHelper())
        ).get(ConfirmAddressViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mTabTitle = mutableListOf(
            bindString(R.string.standard_delivery),
            bindString(R.string.click_and_collect),
            bindString(R.string.dash_delivery)
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.apply {
            permissionUtils = PermissionUtils(this, this@ShopFragment)
            permissions.add(android.Manifest.permission.CAMERA)
        }
        tvSearchProduct?.setOnClickListener { navigateToProductSearch() }
        imBarcodeScanner?.setOnClickListener { checkCameraPermission() }
        shopToolbar?.setOnClickListener { onEditDeliveryLocation() }

        shopPagerAdapter = ShopPagerAdapter(childFragmentManager, mTabTitle, this)
        viewpager_main?.offscreenPageLimit = 2
        viewpager_main?.adapter = shopPagerAdapter
        viewpager_main?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

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
                            showBlackToolTip(Delivery.STANDARD)
                            KotlinUtils.browsingDeliveryType = Delivery.STANDARD
                        }
                        CLICK_AND_COLLECT_TAB.index -> {
                            //Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOPMYLISTS, this)
                            showBlackToolTip(Delivery.CNC)
                            KotlinUtils.browsingDeliveryType = Delivery.CNC
                        }
                        DASH_TAB.index -> {
                            // Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOPMYORDERS, this)
                            showBlackToolTip(Delivery.DASH)
                            KotlinUtils.browsingDeliveryType = Delivery.DASH
                        }
                    }
                    setupToolbar(position)
                }
                shopPagerAdapter?.notifyDataSetChanged()
                updateTabIconUI(position)
            }
        })
        tabs_main?.setupWithViewPager(viewpager_main)
        updateTabIconUI(STANDARD_TAB.index)
        showShopFeatureWalkThrough()
    }

    fun showSearchAndBarcodeUi() {
        tvSearchProduct?.visibility = View.VISIBLE
        imBarcodeScanner?.visibility = View.VISIBLE
    }

    fun showClickAndCollectToolTipUi(browsingStoreId: String?) {
        showClickAndCollectToolTip(true, browsingStoreId)
        timer?.cancel()
        if (AppConfigSingleton.tooltipSettings?.isAutoDismissEnabled == true && blackToolTipLayout?.visibility == VISIBLE) {
            val timeDuration =
                AppConfigSingleton.tooltipSettings?.autoDismissDuration?.times(1000) ?: return
            timer =  object : CountDownTimer(timeDuration, 100) {
                override fun onTick(millisUntilFinished: Long) {}
                override fun onFinish() {
                    KotlinUtils.isCncTabCrossClicked = true
                    blackToolTipLayout?.visibility = View.GONE
                }
            }.start()
        }
    }

    fun hideSerachAndBarcodeUi() {
        tvSearchProduct?.visibility = View.GONE
        imBarcodeScanner?.visibility = View.GONE
    }

    private fun executeValidateSuburb() {
        val placeId = getDeliveryType()?.address?.placeId ?: return
        placeId?.let {
            shopProgressbar?.visibility = View.VISIBLE
            tabs_main?.isClickable = false
            lifecycleScope.launch {
                try {
                    validateLocationResponse =
                        confirmAddressViewModel.getValidateLocation(it)
                    shopProgressbar?.visibility = View.GONE
                    tabs_main?.isClickable = true

                    geoDeliveryView?.visibility = View.VISIBLE
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
                                updateCurrentTab(getDeliveryType()?.deliveryType)
                                setDeliveryView()
                                viewLifecycleOwner.lifecycleScope.launch {
                                    delay(DELAY_3000_MS)
                                    Delivery.getType(getDeliveryType()?.deliveryType)?.let {
                                        showBlackToolTip(it)
                                    }
                                }
                            }
                            else -> {
                                blackToolTipLayout?.visibility = View.GONE
                            }
                        }
                    }
                } catch (e: HttpException) {
                    shopProgressbar?.visibility = View.GONE
                    tabs_main?.isClickable = true
                    FirebaseManager.logException(e)
                    /*TODO : show error screen*/
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

        if ((KotlinUtils.isLocationSame == false && KotlinUtils.placeId != null) || WoolworthsApplication.getValidatePlaceDetails() == null) {
            executeValidateSuburb()
        }
        if (Utils.getPreferredDeliveryLocation()?.fulfillmentDetails == null && KotlinUtils.getAnonymousUserLocationDetails()?.fulfillmentDetails == null) {
            return
        }
        if (Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.deliveryType.isNullOrEmpty() && KotlinUtils.getAnonymousUserLocationDetails()?.fulfillmentDetails?.deliveryType.isNullOrEmpty()) {
            return
        }
        if (KotlinUtils.isLocationSame == true && KotlinUtils.placeId != null) {
            Delivery.getType(getDeliveryType()?.deliveryType)?.let {
                showBlackToolTip(it)
            }
        }
        setDeliveryView()
    }

    private fun updateCurrentTab(deliveryType: String?) {
        when (deliveryType) {
            BundleKeysConstants.STANDARD -> {
                viewpager_main.currentItem = STANDARD_TAB.index
            }
            BundleKeysConstants.CNC -> {
                viewpager_main.currentItem = CLICK_AND_COLLECT_TAB.index
            }
            BundleKeysConstants.DASH -> {
                viewpager_main.currentItem = DASH_TAB.index
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

        when (tabPosition) {
            CLICK_AND_COLLECT_TAB.index -> {
                imgToolbarStart?.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_collection_circle
                    )
                )
                tvToolbarTitle?.text = requireContext().getString(R.string.collecting_from)
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
                tvToolbarSubtitle?.text = requireContext().getString(R.string.set_your_address)
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

    private fun checkCameraPermission() {
        activity?.apply {
            Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.SHOPBARCODE,
                this
            )
        }
        permissionUtils?.check_permission(
            permissions,
            "Explain here why the app needs permissions",
            1
        )
    }

    private fun updateTabIconUI(selectedTab: Int) {
        if (selectedTab == STANDARD_TAB.index) {
            showSearchAndBarcodeUi()
        } else if (selectedTab == CLICK_AND_COLLECT_TAB.index && KotlinUtils.browsingCncStore == null && getDeliveryType()?.deliveryType != Delivery.CNC.type) {
            hideSerachAndBarcodeUi()
        }
        tabs_main?.let { tabLayout ->
            tabLayout.getTabAt(selectedTab)?.customView?.isSelected = true
            for (i in mTabTitle?.indices!!) {
                tabLayout.getTabAt(i)?.customView = prepareTabView(tabLayout, i, mTabTitle)
            }

            val margin = requireContext().resources.getDimensionPixelSize(R.dimen.sixteen_dp)
            for (i in 0 until tabLayout.tabCount) {
                val tab = (tabLayout.getChildAt(0) as ViewGroup).getChildAt(i)
                val layoutParams = tab.layoutParams as MarginLayoutParams
                if(i == 0) {
                    layoutParams.setMargins(margin, 0, 0, 0)
                } else if(i == 2) {
                    layoutParams.setMargins(0, 0, margin, 0)
                }
                tab.requestLayout()
            }

        }
    }

    private fun prepareTabView(tabLayout: TabLayout, pos: Int, tabTitle: MutableList<String>?): View? {
        val view = requireActivity().layoutInflater.inflate(R.layout.shop_custom_tab, null)
        tabWidth = view?.width?.let {
            it.toFloat()
        }

        view?.tvTitle?.text = tabTitle?.get(pos)
        view?.foodOnlyText?.visibility = if(pos == 0) View.GONE else View.VISIBLE
        if (tabLayout.getTabAt(pos)?.view?.isSelected == true) {
            val myRiadFont =
                Typeface.createFromAsset(requireActivity().assets, "fonts/MyriadPro-Semibold.otf")
            view?.tvTitle?.typeface = myRiadFont
        }
        return view
    }

    private fun navigateToProductSearch() {
        activity?.apply {
            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOPSEARCHBAR, this)
            val openProductSearch = Intent(this, ProductSearchActivity::class.java)
            startActivity(openProductSearch)
            overridePendingTransition(0, 0)
        }
    }

    fun setDeliveryView() {
        activity?.let {
            getDeliveryType()?.let { fulfillmentDetails ->
                KotlinUtils.setDeliveryAddressView(
                    it,
                    fulfillmentDetails,
                    tvToolbarTitle,
                    tvToolbarSubtitle,
                    imgToolbarStart
                )
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            //do when hidden
            (activity as? BottomNavigationActivity)?.apply {
                fadeOutToolbar(R.color.recent_search_bg)
                showBackNavigationIcon(false)
                showBottomNavigationMenu()
                refreshViewPagerFragment()
                Handler().postDelayed({
                    hideToolbar()
                }, AppConstant.DELAY_1000_MS)
            }
        }

        if (getDeliveryType() == null) {
            setupToolbar(STANDARD_TAB.index)
            viewpager_main.currentItem = STANDARD_TAB.index
        } else {
            setDeliveryView()
        }
        when (viewpager_main?.currentItem) {
            STANDARD_TAB.index -> {
                val standardDeliveryFragment = viewpager_main?.adapter?.instantiateItem(
                    viewpager_main,
                    viewpager_main.currentItem
                ) as? StandardDeliveryFragment
                standardDeliveryFragment?.onHiddenChanged(hidden)
            }
        }
    }

    override fun permissionGranted(request_code: Int) {
        navigateToBarcode()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
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
                DISPLAY_TOAST_RESULT_CODE -> {
                    navigateToMyListFragment()
                    refreshViewPagerFragment()
                }
                ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE -> {
                    refreshViewPagerFragment()
                }
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
            val fragment = viewpager_main?.adapter?.instantiateItem(
                viewpager_main,
                viewpager_main.currentItem
            ) as? DashDeliveryAddressFragment
            fragment?.onActivityResult(requestCode, resultCode, data)
            refreshViewPagerFragment()
        }

        if (requestCode == PDP_REQUEST_CODE && resultCode == ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE) {
            navigateToMyListFragment()
            refreshViewPagerFragment()
        }

        if (requestCode == SHOPPING_LIST_DETAIL_ACTIVITY_REQUEST_CODE) {
            refreshViewPagerFragment()
        }

        if ((requestCode == REQUEST_CODE && resultCode == RESULT_OK)
            || requestCode == DEPARTMENT_LOGIN_REQUEST && viewpager_main.currentItem == STANDARD_TAB.index
        ) {
            updateCurrentTab(getDeliveryType()?.deliveryType)
            var fragment = viewpager_main?.adapter?.instantiateItem(
                viewpager_main,
                viewpager_main.currentItem
            )
            fragment = when (viewpager_main.currentItem) {
                STANDARD_TAB.index -> {
                    fragment as? StandardDeliveryFragment
                }
                CLICK_AND_COLLECT_TAB.index -> {
                    fragment as? ChangeFullfilmentCollectionStoreFragment
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
                refreshViewPagerFragment()
                showDashToolTip(validateLocationResponse) // externally showing dash tooltip as delivery type is not same.
            }
        }
        if (requestCode == CNC_SET_ADDRESS_REQUEST_CODE) {
            // Set Address done on CNC Tab. update the response and Refresh the Tab now.
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                // delay added because onResume() sets current item back to deliveryType tab.
                // But we want forcefully user to come on CNC tab even though the location is not CNC.
                delay(AppConstant.DELAY_500_MS)
                updateCurrentTab(BundleKeysConstants.CNC)
                val changeFullfilmentCollectionStoreFragment =
                    viewpager_main?.adapter?.instantiateItem(
                        viewpager_main,
                        viewpager_main.currentItem
                    ) as? ChangeFullfilmentCollectionStoreFragment
                changeFullfilmentCollectionStoreFragment?.init()
            }
        }

        if (requestCode == LOGIN_MY_LIST_REQUEST_CODE) {
            if (resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()) {
                (activity as? BottomNavigationActivity)?.let {
                    it.bottomNavigationById?.setCurrentItem(INDEX_ACCOUNT)
                    val fragment = MyListsFragment()
                    it.pushFragment(fragment)
                }
            }
        }
    }

    fun refreshViewPagerFragment() {
        when (viewpager_main.currentItem) {
            STANDARD_TAB.index -> {
                val departmentsFragment =
                    viewpager_main?.adapter?.instantiateItem(
                        viewpager_main,
                        viewpager_main.currentItem
                    ) as?  StandardDeliveryFragment
                departmentsFragment?.initView()
            }
            CLICK_AND_COLLECT_TAB.index -> {
                val changeFullfilmentCollectionStoreFragment =
                    viewpager_main?.adapter?.instantiateItem(
                        viewpager_main,
                        viewpager_main.currentItem
                    ) as? ChangeFullfilmentCollectionStoreFragment
                changeFullfilmentCollectionStoreFragment?.init()
            }
            DASH_TAB.index -> {
                val dashDeliveryAddressFragment = viewpager_main?.adapter?.instantiateItem(
                    viewpager_main,
                    viewpager_main.currentItem
                ) as? DashDeliveryAddressFragment
                dashDeliveryAddressFragment?.initViews()
            }
        }
    }


    override fun onStartShopping() {
        viewpager_main?.setCurrentItem(0, true)
    }

    fun navigateToMyListFragment() {
        (activity as? BottomNavigationActivity)?.let {
            it.bottomNavigationById.currentItem = INDEX_ACCOUNT
            val fragment = MyListsFragment()
            it.pushFragment(fragment)
        }
    }

    fun scrollToTop() {
        when (viewpager_main?.currentItem) {
            STANDARD_TAB.index -> {
                val detailsFragment = viewpager_main?.adapter?.instantiateItem(
                    viewpager_main,
                    viewpager_main.currentItem
                ) as? StandardDeliveryFragment
                detailsFragment?.scrollToTop()
            }
            CLICK_AND_COLLECT_TAB.index -> {
                val changeFullfilmentCollectionStoreFragment =
                    viewpager_main?.adapter?.instantiateItem(
                        viewpager_main,
                        viewpager_main.currentItem
                    ) as? ChangeFullfilmentCollectionStoreFragment
                changeFullfilmentCollectionStoreFragment?.scrollToTop()
            }
            DASH_TAB.index -> {
                val dashDeliveryAddressFragment = viewpager_main?.adapter?.instantiateItem(
                    viewpager_main,
                    viewpager_main.currentItem
                ) as? DashDeliveryAddressFragment
                dashDeliveryAddressFragment?.scrollToTop()
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

    fun getShoppingListResponseData(): ShoppingListsResponse? {
        return shoppingListsResponse
    }

    fun getOrdersResponseData(): OrdersResponse? {
        return ordersResponse
    }

    fun isDifferentUser(): Boolean {
        return user != AppInstanceObject.get()?.currentUserObject?.id ?: false
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
        imBarcodeScanner?.performClick()
    }

    fun switchToDepartmentTab() {
        viewpager_main.currentItem = STANDARD_TAB.index
    }

    fun refreshCategories() {
        when (viewpager_main.currentItem) {
            STANDARD_TAB.index -> {
                val detailsFragment = viewpager_main?.adapter?.instantiateItem(
                    viewpager_main,
                    viewpager_main.currentItem
                ) as? StandardDeliveryFragment
                detailsFragment?.reloadRequest()
            }
        }
    }

    private fun showBlackToolTip(deliveryType: Delivery) {
        if (validateLocationResponse == null || getDeliveryType() == null) {
            blackToolTipLayout?.visibility = View.GONE
            return
        }
        closeWhiteBtn?.setOnClickListener {
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
            blackToolTipLayout?.visibility = View.GONE
        }
        changeLocationButton?.setOnClickListener {

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
                showClickAndCollectToolTip(KotlinUtils.isStoreSelectedForBrowsing, KotlinUtils.browsingCncStore?.storeId)
            }
            Delivery.DASH -> {
                showDashToolTip(validateLocationResponse)
            }
        }

        if (AppConfigSingleton.tooltipSettings?.isAutoDismissEnabled == true && blackToolTipLayout?.visibility == VISIBLE) {
            val timeDuration =
                AppConfigSingleton.tooltipSettings?.autoDismissDuration?.times(1000) ?: return
            timer =  object : CountDownTimer(timeDuration, 100) {
                override fun onTick(millisUntilFinished: Long) {}
                override fun onFinish() {
                    when(KotlinUtils.fullfillmentTypeClicked) {
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
                    blackToolTipLayout?.visibility = View.GONE
                }
            }.start()
        }
    }

    private fun showStandardDeliveryToolTip() {


        if (KotlinUtils.isLocationSame == false) {
            blackToolTipLayout?.visibility = View.VISIBLE
        }

        if (KotlinUtils.isDeliveryLocationTabCrossClicked == true) {
            blackToolTipLayout?.visibility = View.GONE
            return
        }

        if (validateLocationResponse?.validatePlace?.firstAvailableFoodDeliveryDate.isNullOrEmpty()
            && validateLocationResponse?.validatePlace?.firstAvailableOtherDeliveryDate.isNullOrEmpty()
        ) {
            blackToolTipLayout?.visibility = View.GONE
            return
        }

        blackToolTipLayout?.visibility = View.VISIBLE
        if (getDeliveryType() == null || Delivery.getType(getDeliveryType()?.deliveryType)?.type == Delivery.STANDARD.type) {
            changeButtonLayout?.visibility = View.GONE
        } else {
            changeButtonLayout?.visibility = View.VISIBLE
            changeText?.text = getText(R.string.shop_using_standard_delivery)
        }
        KotlinUtils.fullfillmentTypeClicked = Delivery.STANDARD.name
        validateLocationResponse?.validatePlace?.let {
            fashionItemDateText?.visibility = View.VISIBLE
            foodItemTitle?.visibility = View.VISIBLE
            fashionItemTitle?.visibility = View.VISIBLE
            deliveryIconLayout?.visibility = View.GONE

            if (it.firstAvailableFoodDeliveryDate?.isNullOrEmpty() == true) {
                deliveryCollectionTitle?.visibility = View.GONE
                foodItemDateText?.visibility = View.GONE
            }

            if (it.firstAvailableOtherDeliveryDate?.isNullOrEmpty() == true) {
                fashionItemTitle?.visibility = View.GONE
                fashionItemDateText?.visibility = View.GONE
            }

            deliveryCollectionTitle?.text = getString(R.string.earliest_delivery_dates)
            foodItemDateText?.text = it.firstAvailableFoodDeliveryDate
            fashionItemDateText?.text = it.firstAvailableOtherDeliveryDate
            productAvailableText?.text = getString(R.string.all_products_available)
            cartIcon.setImageResource(R.drawable.icon_cart_white)
            bubbleLayout?.arrowDirection = ArrowDirection.TOP
            if (tabs_main?.getTabAt(STANDARD_TAB.index)?.view != null) {
                bubbleLayout?.arrowPosition = tabs_main?.let {
                    it?.getTabAt(STANDARD_TAB.index)?.view?.width?.div(2)?.toFloat()
                }!!
            }
        }
    }

    fun showClickAndCollectToolTip(
        isStoreSelectedForBrowsing: Boolean = false,
        browsingStoreId: String? = "",
    ) {
        if (KotlinUtils.isCncTabCrossClicked == true || browsingStoreId == null) {
            blackToolTipLayout?.visibility = View.GONE
            return
        }
        if (isUserAuthenticated() && getFirstAvailableFoodDeliveryDate(isStoreSelectedForBrowsing,
                browsingStoreId).isNullOrEmpty() == true
        ) {
            blackToolTipLayout?.visibility = View.GONE
            return
        } else {
            if (getFirstAvailableFoodDeliveryDate(isStoreSelectedForBrowsing,
                    browsingStoreId).isNullOrEmpty() == true
            ) {
                blackToolTipLayout?.visibility = View.GONE
                return
            }
        }
        blackToolTipLayout?.visibility = View.VISIBLE
        if (getDeliveryType() == null || Delivery.getType(getDeliveryType()?.deliveryType)?.type == Delivery.CNC.type) {
            changeButtonLayout?.visibility = View.GONE
        } else {
            changeButtonLayout?.visibility = View.VISIBLE
            changeText?.text = getText(R.string.shop_using_cnc)
        }
        KotlinUtils.fullfillmentTypeClicked = Delivery.CNC.name
        validateLocationResponse?.validatePlace?.let { validatePlace ->
            deliveryCollectionTitle?.visibility = View.VISIBLE
            foodItemDateText?.visibility = View.VISIBLE
            deliveryCollectionTitle?.text = getString(R.string.earliest_collection_Date)
            foodItemTitle?.visibility = View.GONE
            fashionItemDateText?.visibility = View.GONE
            fashionItemTitle?.visibility = View.GONE
            deliveryIconLayout?.visibility = View.VISIBLE

            val store = GeoUtils.getStoreDetails(
                getStoreId(isStoreSelectedForBrowsing, browsingStoreId),
                validatePlace.stores
            )
            foodItemDateText?.text = store?.firstAvailableFoodDeliveryDate
            productAvailableText?.text = resources.getString(
                R.string.dash_item_limit,
                store?.quantityLimit?.foodMaximumQuantity
            )

            cartIcon?.setImageResource(R.drawable.icon_cart_white)
            deliveryIcon?.setImageResource(R.drawable.white_shopping_bag_icon)
            deliveryFeeText?.text = resources.getString(R.string.dash_free_collection)
            bubbleLayout?.setArrowDirection(ArrowDirection.TOP_CENTER)
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

    private fun getFirstAvailableFoodDeliveryDate(
        isStoreSelectedForBrowsing: Boolean,
        browsingStoreId: String,
    ): String? {
        var storeId: String? = getStoreId(isStoreSelectedForBrowsing, browsingStoreId)
        validateLocationResponse?.validatePlace?.let { validatePlace ->
            val store = GeoUtils.getStoreDetails(
                storeId,
                validatePlace.stores
            )
            return store?.firstAvailableFoodDeliveryDate
        }
        return ""
    }

    private fun showDashToolTip(validateLocationResponse: ValidateLocationResponse?) {
        val dashDeliverable = validateLocationResponse?.validatePlace?.onDemand?.deliverable
        if (KotlinUtils.isLocationSame == false) {
            blackToolTipLayout?.visibility = View.VISIBLE
        }

        if (KotlinUtils.isDashTabCrossClicked == true || dashDeliverable == null || dashDeliverable == false) {
            blackToolTipLayout?.visibility = View.GONE
            return
        }

        if (validateLocationResponse?.validatePlace?.onDemand?.firstAvailableFoodDeliveryTime?.isNullOrEmpty() == true) {
            blackToolTipLayout?.visibility = View.GONE
            return
        }

        blackToolTipLayout?.visibility = View.VISIBLE
        if (getDeliveryType() == null || Delivery.getType(getDeliveryType()?.deliveryType)?.type == Delivery.DASH.type) {
            changeButtonLayout?.visibility = View.GONE
        } else {
            changeButtonLayout?.visibility = View.VISIBLE
            changeText?.text = getText(R.string.shop_using_dash_delivery)
        }
        KotlinUtils.fullfillmentTypeClicked = Delivery.DASH.name
        validateLocationResponse?.validatePlace?.let {
            deliveryCollectionTitle?.text = getString(R.string.next_dash_delivery_timeslot_text)
            foodItemTitle?.visibility = View.GONE
            fashionItemDateText?.visibility = View.GONE
            deliveryIconLayout?.visibility = View.VISIBLE
            cartIconLayout?.visibility = View.VISIBLE
            fashionItemTitle?.visibility = View.GONE
            deliveryIcon?.visibility = View.VISIBLE
            deliveryFeeText?.visibility = View.VISIBLE

            foodItemDateText?.text = it.onDemand?.firstAvailableFoodDeliveryTime
            cartIcon?.setImageResource(R.drawable.icon_cart_white)
            deliveryIcon?.setImageResource(R.drawable.icon_scooter_white)
            bubbleLayout?.setArrowDirection(ArrowDirection.TOP)
            bubbleLayout?.arrowPosition =
                tabs_main.width - tabs_main.getTabAt(DASH_TAB.index)?.view?.width?.div(2)
                    ?.toFloat()!!
            productAvailableText?.text = resources.getString(
                R.string.dash_item_limit,
                it.onDemand?.quantityLimit?.foodMaximumQuantity
            )
            deliveryFeeText?.text = resources.getString(
                R.string.dash_delivery_fee,
                it.onDemand?.firstAvailableFoodDeliveryCost
            )
        }
    }

    private fun showShopFeatureWalkThrough() {
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
        (requireActivity() as? BottomNavigationActivity)?.let {
            // Prevent dialog to display in other section when fragment is not visible
            if (it.currentFragment !is ShopFragment || !isAdded || AppInstanceObject.get().featureWalkThrough.dash || !Utils.isFeatureWalkThroughTutorialsEnabled())
                return
            FirebaseManager.setCrashlyticsString(
                bindString(R.string.crashlytics_materialshowcase_key),
                this.javaClass.canonicalName
            )
            it.walkThroughPromtView =
                WMaterialShowcaseView.Builder(it, WMaterialShowcaseView.Feature.DASH)
                    .setTarget(tabs_main?.getChildAt(0))
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
        (requireActivity() as? BottomNavigationActivity)?.let {
            // Prevent dialog to display in other section when fragment is not visible
            if (it.currentFragment !is ShopFragment || !isAdded || AppInstanceObject.get().featureWalkThrough.delivery_details || !Utils.isFeatureWalkThroughTutorialsEnabled())
                return
            FirebaseManager.setCrashlyticsString(
                bindString(R.string.crashlytics_materialshowcase_key),
                this.javaClass.canonicalName
            )
            it.walkThroughPromtView =
                WMaterialShowcaseView.Builder(it, WMaterialShowcaseView.Feature.DELIVERY_DETAILS)
                    .setTarget(shopToolbar)
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
            if (it.currentFragment !is ShopFragment || imBarcodeScanner == null || !isAdded || AppInstanceObject.get().featureWalkThrough.barcodeScan || !Utils.isFeatureWalkThroughTutorialsEnabled())
                return
            FirebaseManager.setCrashlyticsString(
                bindString(R.string.crashlytics_materialshowcase_key),
                this.javaClass.canonicalName
            )
            it.walkThroughPromtView =
                WMaterialShowcaseView.Builder(it, WMaterialShowcaseView.Feature.BARCODE_SCAN)
                    .setTarget(imBarcodeScanner)
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
        when (feature) {
            WMaterialShowcaseView.Feature.DASH -> {
                viewpager_main?.apply {
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
        }
    }

    override fun onPromptDismiss(feature: WMaterialShowcaseView.Feature) {
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
        }
    }

    fun isUserAuthenticated() = SessionUtilities.getInstance().isUserAuthenticated

    fun getCurrentFragmentIndex() = viewpager_main?.currentItem
}
