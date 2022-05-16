package za.co.woolworths.financial.services.android.ui.fragments.shop

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
import kotlinx.android.synthetic.main.shop_custom_tab.*
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
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.models.dto.OrdersResponse
import za.co.woolworths.financial.services.android.models.dto.RootCategories
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse
import za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity.Companion.ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE
import za.co.woolworths.financial.services.android.ui.activities.BarcodeScanActivity
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.INDEX_PRODUCT
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.PDP_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity
import za.co.woolworths.financial.services.android.ui.adapters.ShopPagerAdapter
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.deviceWidth
import za.co.woolworths.financial.services.android.ui.fragments.shop.DepartmentsFragment.Companion.DEPARTMENT_LOGIN_REQUEST
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList.Companion.DISPLAY_TOAST_RESULT_CODE
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.OnChildFragmentEvents
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView
import za.co.woolworths.financial.services.android.ui.views.shop.dash.ChangeFullfilmentCollectionStoreFragment
import za.co.woolworths.financial.services.android.ui.views.shop.dash.DashDeliveryAddressFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.DELAY_3000_MS
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.DELAY_4000_MS
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.REQUEST_CODE_ORDER_DETAILS_PAGE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.DASH_SET_ADDRESS_REQUEST_CODE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.REQUEST_CODE
import za.co.woolworths.financial.services.android.util.ScreenManager.SHOPPING_LIST_DETAIL_ACTIVITY_REQUEST_CODE
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import za.co.woolworths.financial.services.android.viewmodels.shop.ShopViewModel


/**
 * A simple [Fragment] subclass.
 *
 */
@AndroidEntryPoint
class ShopFragment : Fragment(R.layout.fragment_shop), PermissionResultCallback,
    OnChildFragmentEvents,
    WMaterialShowcaseView.IWalkthroughActionListener {

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

    private val shopViewModel: ShopViewModel by viewModels(
        ownerProducer = { this }
    )
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
                shopPagerAdapter?.notifyDataSetChanged()
                updateTabIconUI(position)
                activity?.apply {
                    when (position) {
                        0 -> {
                            Utils.triggerFireBaseEvents(
                                FirebaseManagerAnalyticsProperties.SHOP_CATEGORIES,
                                this
                            )
                            showBlackToolTip(Delivery.STANDARD)
                        }
                        1 -> {
                            //Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOPMYLISTS, this)
                            showBlackToolTip(Delivery.CNC)
                        }
                        2 -> {
                            // Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOPMYORDERS, this)
                            showBlackToolTip(Delivery.DASH)
                        }
                    }
                    setupToolbar(position)
                }
            }
        })
        tabs_main?.setupWithViewPager(viewpager_main)
        updateTabIconUI(0)
        showShopFeatureWalkThrough()
    }


    private fun executeValidateSuburb() {
        var placeId: String? = null

        if (isUserAuthenticated()) {
            Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.let {
                placeId = it.address?.placeId
            }
        } else {
            KotlinUtils.getAnonymousUserLocationDetails()?.fulfillmentDetails?.let {
                placeId = it.address?.placeId
            }
        }

        if (placeId == null) {
            return
        }

        placeId?.let {
            lifecycleScope.launch {
                progressBar?.visibility = View.VISIBLE
                try {
                    validateLocationResponse =
                        confirmAddressViewModel.getValidateLocation(it)
                    progressBar?.visibility = View.GONE
                    geoDeliveryView?.visibility = View.VISIBLE
                    if (validateLocationResponse != null) {
                        when (validateLocationResponse?.httpCode) {
                            AppConstant.HTTP_OK -> {
                                WoolworthsApplication.setValidatedSuburbProducts(
                                    validateLocationResponse?.validatePlace
                                )
                                setDeliveryView()
                                viewLifecycleOwner.lifecycleScope.launch {
                                    delay(DELAY_3000_MS)
                                    if (isUserAuthenticated()) {
                                        Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.let { fulfillmentDetails ->
                                            Delivery.getType(fulfillmentDetails.deliveryType)
                                                ?.let {
                                                    showBlackToolTip(it)
                                                }
                                        }
                                    } else {
                                        KotlinUtils.getAnonymousUserLocationDetails()?.fulfillmentDetails?.let { fulfillmentDetails ->
                                            Delivery.getType(fulfillmentDetails.deliveryType)
                                                ?.let {
                                                    showBlackToolTip(it)
                                                }
                                        }
                                    }
                                }
                            }
                            else -> {
                                blackToolTipLayout?.visibility = View.GONE
                            }
                        }
                    }
                } catch (e: HttpException) {
                    FirebaseManager.logException(e)
                    /*TODO : show error screen*/
                }
            }
        }
    }

    private fun onEditDeliveryLocation() {
        var deliveryType: Delivery? = Delivery.STANDARD
        var placeId = ""

        if (isUserAuthenticated()) {
            Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.let {
                deliveryType = Delivery.getType(it.deliveryType)
                placeId = it.address?.placeId ?: ""
            }
        } else {
            KotlinUtils.getAnonymousUserLocationDetails()?.fulfillmentDetails?.let {
                deliveryType = Delivery.getType(it.deliveryType)
                placeId = it.address?.placeId ?: ""
            }
        }

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
            deliveryType,
            placeId
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
        setDeliveryView()
    }

    private fun updateCurrentTab(deliveryType: String?) {
        when (deliveryType) {
            BundleKeysConstants.STANDARD -> {
                viewpager_main.currentItem = 0
            }
            BundleKeysConstants.CNC -> {
                viewpager_main.currentItem = 1
            }
            BundleKeysConstants.DASH -> {
                viewpager_main.currentItem = 2
            }
        }
    }

    private fun setupToolbar(tabPosition: Int) {
        if (tabPosition < 0) {
            return
        }

        if (isUserAuthenticated()) {
            if (Utils.getPreferredDeliveryLocation() != null) {
                return
            }
        } else {
            if (KotlinUtils.getAnonymousUserLocationDetails()?.fulfillmentDetails != null) {
                return
            }
        }

        when (tabPosition) {
            1 -> {
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
            2 -> {
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
        tabs_main?.let { tab ->
            tab.getTabAt(selectedTab)?.customView?.isSelected = true
            for (i in mTabTitle?.indices!!) {
                tab.getTabAt(i)?.customView = prepareTabView(tab, i, mTabTitle)
            }
        }
    }

    private fun prepareTabView(tab: TabLayout, pos: Int, tabTitle: MutableList<String>?): View? {
        val view = activity?.layoutInflater?.inflate(R.layout.shop_custom_tab, null)
        tabWidth = view?.width?.let {
            it.toFloat()
        }
        view?.tvTitle?.text = tabTitle?.get(pos)
        if (tab.getTabAt(pos)?.view?.isSelected == true) {
            val futuraFont =
                Typeface.createFromAsset(activity?.assets, "fonts/MyriadPro-Semibold.otf")
            view?.tvTitle?.typeface = futuraFont
        }
        if (pos == 0) {
            view?.foodOnlyText?.visibility = View.INVISIBLE
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

    private fun setDeliveryView() {
        if (isUserAuthenticated()) {
            Utils.getPreferredDeliveryLocation()?.apply {
                updateCurrentTab(this?.fulfillmentDetails?.deliveryType)
                activity?.let {
                    KotlinUtils.setDeliveryAddressView(
                        it,
                        this,
                        tvToolbarTitle,
                        tvToolbarSubtitle,
                        imgToolbarStart
                    )
                }
            }
        } else {
            KotlinUtils.getAnonymousUserLocationDetails()?.apply {
                updateCurrentTab(this?.fulfillmentDetails?.deliveryType)
                activity?.let {
                    KotlinUtils.setDeliveryAddressView(
                        it,
                        this,
                        tvToolbarTitle,
                        tvToolbarSubtitle,
                        imgToolbarStart
                    )
                }
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
        setDeliveryView()
        when (viewpager_main?.currentItem) {
            0 -> {
                val departmentFragment = viewpager_main?.adapter?.instantiateItem(
                    viewpager_main,
                    viewpager_main.currentItem
                ) as? DepartmentsFragment
                departmentFragment?.onHiddenChanged(hidden)
            }
        }
    }

    override fun PermissionGranted(request_code: Int) {
        navigateToBarcode()

    }

    override fun PartialPermissionGranted(
        request_code: Int,
        granted_permissions: ArrayList<String>?,
    ) {
    }

    override fun PermissionDenied(request_code: Int) {
    }

    override fun NeverAskAgain(request_code: Int) {
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == DepartmentsFragment.REQUEST_CODE_FINE_GPS && viewpager_main.currentItem == 0) {
            val fragment = viewpager_main?.adapter?.instantiateItem(
                viewpager_main,
                viewpager_main.currentItem
            ) as? DepartmentsFragment
            callOnActivityResult(grantResults, fragment, requestCode)
        } else if (requestCode == DepartmentsFragment.REQUEST_CODE_FINE_GPS && viewpager_main.currentItem == 1) {
            val fragment = viewpager_main?.adapter?.instantiateItem(
                viewpager_main,
                viewpager_main.currentItem
            ) as? ChangeFullfilmentCollectionStoreFragment
            callOnActivityResult(grantResults, fragment, requestCode)
        }
        permissionUtils?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun callOnActivityResult(
        grantResults: IntArray,
        fragment: Fragment?,
        requestCode: Int,
    ) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            // permission was granted, yay! Do the
            // contacts-related task you need to do.
            fragment?.onActivityResult(requestCode, RESULT_OK, null)
        } else {
            fragment?.onActivityResult(requestCode, RESULT_CANCELED, null)
        }
    }

    private fun navigateToBarcode() {
        activity?.apply {
            val openBarcodeActivity = Intent(this, BarcodeScanActivity::class.java)
            startActivityForResult(
                openBarcodeActivity,
                BarcodeScanActivity.BARCODE_ACTIVITY_REQUEST_CODE
            )
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
            refreshViewPagerFragment()
        }

        if (requestCode == PDP_REQUEST_CODE && resultCode == ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE) {
            navigateToMyListFragment()
            refreshViewPagerFragment()
        }

        if (requestCode == SHOPPING_LIST_DETAIL_ACTIVITY_REQUEST_CODE) {
            refreshViewPagerFragment()
        }

        if (requestCode == REQUEST_CODE || requestCode == DEPARTMENT_LOGIN_REQUEST && viewpager_main.currentItem == 0) {
            val fragment = viewpager_main?.adapter?.instantiateItem(
                viewpager_main,
                viewpager_main.currentItem
            ) as? DepartmentsFragment
            fragment?.onActivityResult(requestCode, resultCode, data)
        }
        if (requestCode == DASH_SET_ADDRESS_REQUEST_CODE) {
            // Set Address done on Dash Tab. update the response and Refresh the Tab now.
            val validateLocationResponse = data?.getSerializableExtra(
                BundleKeysConstants.VALIDATE_RESPONSE
            ) as? ValidateLocationResponse
            validateLocationResponse?.validatePlace?.let { shopViewModel.setValidatePlaceResponse(it) }
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                // delay added because onResume() sets current item back to deliveryType tab.
                // But we want forcefully user to come on Dash tab even though the location is not dash.
                delay(AppConstant.DELAY_500_MS)
                updateCurrentTab(BundleKeysConstants.DASH)
                refreshViewPagerFragment()
                showDashToolTip(validateLocationResponse) // externally showing dash tooltip as delivery type is not same.
            }
        }
    }

    fun refreshViewPagerFragment() {
        when (viewpager_main.currentItem) {
            0 -> {
                val departmentsFragment =
                    viewpager_main?.adapter?.instantiateItem(
                        viewpager_main,
                        viewpager_main.currentItem
                    ) as? DepartmentsFragment
                departmentsFragment?.initView()
            }
            1 -> {
                val changeFullfilmentCollectionStoreFragment =
                    viewpager_main?.adapter?.instantiateItem(
                        viewpager_main,
                        viewpager_main.currentItem
                    ) as? ChangeFullfilmentCollectionStoreFragment
                changeFullfilmentCollectionStoreFragment?.init()
            }
            2 -> {
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
        viewpager_main?.setCurrentItem(1, true)
    }

    fun navigateToMyShoppingListFragment() {
        viewpager_main?.setCurrentItem(1, false)
    }

    fun scrollToTop() {
        when (viewpager_main?.currentItem) {
            0 -> {
                val detailsFragment = viewpager_main?.adapter?.instantiateItem(
                    viewpager_main,
                    viewpager_main.currentItem
                ) as? DepartmentsFragment
                detailsFragment?.scrollToTop()
            }
            1 -> {
                val changeFullfilmentCollectionStoreFragment =
                    viewpager_main?.adapter?.instantiateItem(
                        viewpager_main,
                        viewpager_main.currentItem
                    ) as? ChangeFullfilmentCollectionStoreFragment
                changeFullfilmentCollectionStoreFragment?.scrollToTop()
            }
            2 -> {
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
        viewpager_main.currentItem = 0
    }

    fun refreshCategories() {
        when (viewpager_main.currentItem) {
            0 -> {
                val detailsFragment = viewpager_main?.adapter?.instantiateItem(
                    viewpager_main,
                    viewpager_main.currentItem
                ) as? DepartmentsFragment
                detailsFragment?.reloadRequest()
            }
        }
    }

    private fun showBlackToolTip(deliveryType: Delivery) {
        if (validateLocationResponse == null) {
            blackToolTipLayout?.visibility = View.GONE
            return
        }
        closeWhiteBtn?.setOnClickListener {
            blackToolTipLayout?.visibility = View.GONE
        }
        when (deliveryType) {
            Delivery.STANDARD -> {
                showStandardDeliveryToolTip()
            }
            Delivery.CNC -> {
                showClickAndCollectToolTip()
            }
            Delivery.DASH -> {
                showDashToolTip(validateLocationResponse)
            }
        }

        object : CountDownTimer(DELAY_4000_MS, 100) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                blackToolTipLayout?.visibility = View.GONE
            }
        }.start()
    }

    private fun showStandardDeliveryToolTip() {
        if (KotlinUtils.isDeliveryLocationTabClicked == true) {
            blackToolTipLayout?.visibility = View.GONE
            return
        }

        if (validateLocationResponse?.validatePlace?.firstAvailableFoodDeliveryDate.isNullOrEmpty()
            && validateLocationResponse?.validatePlace?.firstAvailableOtherDeliveryDate.isNullOrEmpty()) {
            blackToolTipLayout?.visibility = View.GONE
            return
        }

        blackToolTipLayout?.visibility = View.VISIBLE
        KotlinUtils.isDeliveryLocationTabClicked = true
        validateLocationResponse?.validatePlace?.let {
            fashionItemDateText?.visibility = View.VISIBLE
            foodItemTitle?.visibility = View.VISIBLE
            deliveryIconLayout?.visibility = View.VISIBLE
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
            bubbleLayout?.setArrowDirection(ArrowDirection.TOP)
            if (tabs_main?.getTabAt(0)?.view != null) {
                bubbleLayout?.arrowPosition = tabs_main?.let {
                    it?.getTabAt(0)?.view?.width?.div(2)?.toFloat()
                }!!
            }
        }
    }

    private fun showClickAndCollectToolTip() {
        if (KotlinUtils.isCncTabClicked == true) {
            blackToolTipLayout?.visibility = View.GONE
            return
        }

        if (isUserAuthenticated() && getFirstAvailableFoodDeliveryDate().isNullOrEmpty() == true) {
            blackToolTipLayout?.visibility = View.GONE
            return
        } else {
            if (getFirstAvailableFoodDeliveryDate().isNullOrEmpty() == true) {
                blackToolTipLayout?.visibility = View.GONE
                return
            }
        }
        blackToolTipLayout?.visibility = View.VISIBLE
        KotlinUtils.isCncTabClicked = true
        validateLocationResponse?.validatePlace?.let { validatePlace ->
            deliveryCollectionTitle?.text = getString(R.string.earliest_collection_Date)
            foodItemTitle?.visibility = View.GONE
            fashionItemDateText?.visibility = View.GONE
            fashionItemTitle?.visibility = View.GONE
            deliveryIconLayout?.visibility = View.VISIBLE

            if (isUserAuthenticated()) {
                Utils.getPreferredDeliveryLocation()?.let {
                    val store = GeoUtils.getStoreDetails(
                        it.fulfillmentDetails?.storeId,
                        validatePlace.stores
                    )
                    foodItemDateText?.text = store?.firstAvailableFoodDeliveryDate
                    productAvailableText?.text = resources.getString(
                        R.string.dash_item_limit,
                        store?.quantityLimit?.foodMaximumQuantity
                    )
                }
            } else {
                KotlinUtils.getAnonymousUserLocationDetails()?.let {
                    val store = GeoUtils.getStoreDetails(
                        it.fulfillmentDetails.storeId,
                        validatePlace.stores
                    )

                    foodItemDateText?.text = store?.firstAvailableFoodDeliveryDate
                    productAvailableText?.text = resources.getString(
                        R.string.dash_item_limit,
                        store?.quantityLimit?.foodMaximumQuantity
                    )
                }
            }

            cartIcon?.setImageResource(R.drawable.icon_cart_white)
            deliveryIcon?.setImageResource(R.drawable.white_shopping_bag_icon)
            deliveryFeeText?.text = resources.getString(R.string.dash_free_collection)
            bubbleLayout?.setArrowDirection(ArrowDirection.TOP_CENTER)
        }
    }

    private fun getFirstAvailableFoodDeliveryDate(): String? {
        validateLocationResponse?.validatePlace?.let { validatePlace ->
            if (isUserAuthenticated()) {
                Utils.getPreferredDeliveryLocation()?.let {
                    val store = GeoUtils.getStoreDetails(
                        it.fulfillmentDetails?.storeId,
                        validatePlace.stores
                    )
                    return store?.firstAvailableFoodDeliveryDate
                }

            } else {
                KotlinUtils.getAnonymousUserLocationDetails()?.let {
                    val store = GeoUtils.getStoreDetails(
                        it.fulfillmentDetails.storeId,
                        validatePlace.stores
                    )
                    return store?.firstAvailableFoodDeliveryDate
                }
            }
        }
        return "";
    }


    private fun showDashToolTip(validateLocationResponse: ValidateLocationResponse?) {
        val dashDeliverable = validateLocationResponse?.validatePlace?.onDemand?.deliverable
        if (KotlinUtils.isDashTabClicked == true || dashDeliverable == null || dashDeliverable == false) {
            blackToolTipLayout?.visibility = View.GONE
            return
        }

        if (validateLocationResponse?.validatePlace?.
            onDemand?.firstAvailableFoodDeliveryTime?.isNullOrEmpty() == true) {
            blackToolTipLayout?.visibility = View.GONE
            return
        }

        blackToolTipLayout?.visibility = View.VISIBLE
        KotlinUtils.isDashTabClicked = true
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
            bubbleLayout?.arrowPosition =  tabs_main.width - tabs_main.getTabAt(2)?.view?.width?.div(2)?.toFloat()!!
            productAvailableText?.text = resources.getString(
                R.string.dash_item_limit,
                it.onDemand?.quantityLimit?.foodMaximumQuantity
            )
            /*TODO deliveryFee value will come from config*/
            deliveryFeeText?.text = "Free for orders over R75"
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
                    .setTarget(tabs_main?.getTabAt(2)?.customView?.tvTitle)
                    .setTitle(R.string.walkthrough_dash_title)
                    .setDescription(R.string.walkthrough_dash_desc)
                    .setActionText(R.string.walkthrough_dash_action)
                    .setImage(R.drawable.dash_delivery_icon)
                    .setShapePadding(48)
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
                    .setTarget(imgToolbarStart)
                    .setTitle(R.string.walkthrough_delivery_details_title)
                    .setDescription(R.string.walkthrough_delivery_details_desc)
                    .setActionText(R.string.walkthrough_delivery_details_action)
                    .setImage(R.drawable.ic_delivery_truck)
                    .setShapePadding(48)
                    .setAction(this@ShopFragment)
                    .setArrowPosition(WMaterialShowcaseView.Arrow.TOP_LEFT)
                    .setMaskColour(ContextCompat.getColor(it, R.color.semi_transparent_black))
                    .build()
            it.walkThroughPromtView.show(it)
        }
    }

    override fun onWalkthroughActionButtonClick(feature: WMaterialShowcaseView.Feature?) {
        when (feature) {
            WMaterialShowcaseView.Feature.DASH -> {
                viewpager_main?.apply {
                    currentItem = 2
                    adapter?.notifyDataSetChanged()
                }
                updateTabIconUI(2)
                showDeliveryDetailsFeatureWalkThrough()
            }
            WMaterialShowcaseView.Feature.SHOPPING -> {
                showDashFeatureWalkThrough()
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
            }
        }
    }

    fun isUserAuthenticated() = SessionUtilities.getInstance().isUserAuthenticated
}
