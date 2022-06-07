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
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.models.dto.OrdersResponse
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams.SearchType
import za.co.woolworths.financial.services.android.models.dto.RootCategories
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse
import za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity.Companion.ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE
import za.co.woolworths.financial.services.android.ui.activities.BarcodeScanActivity
import za.co.woolworths.financial.services.android.ui.activities.BarcodeScanActivity.Companion.BARCODE_ACTIVITY_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.*
import za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity
import za.co.woolworths.financial.services.android.ui.adapters.ShopPagerAdapter
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.ProductListingFragment.Companion.newInstance
import za.co.woolworths.financial.services.android.ui.fragments.shop.StandardDeliveryFragment.Companion.DEPARTMENT_LOGIN_REQUEST
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList.Companion.DISPLAY_TOAST_RESULT_CODE
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.OnChildFragmentEvents
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView
import za.co.woolworths.financial.services.android.ui.views.shop.dash.ChangeFullfilmentCollectionStoreFragment
import za.co.woolworths.financial.services.android.ui.views.shop.dash.DashDeliveryAddressFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.DELAY_3000_MS
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.DELAY_4000_MS
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
    private val fragmentResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode != RESULT_OK) {
            return@registerForActivityResult
        }
        it.data?.extras?.let { extras ->
            val requestCode = extras.getInt(AppConstant.REQUEST_CODE)
            if(requestCode == BARCODE_ACTIVITY_REQUEST_CODE) {
                val searchType = SearchType.valueOf(extras.getString("searchType", ""))
                val searchTerm: String = extras.getString("searchTerm", "")
                (requireActivity() as? BottomNavigationActivity)?.pushFragment(newInstance(searchType, "", searchTerm, false))
            }
        }
    }

    companion object {
        private const val LOGIN_MY_LIST_REQUEST_CODE = 9876
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
                        0 -> {
                            Utils.triggerFireBaseEvents(
                                FirebaseManagerAnalyticsProperties.SHOP_CATEGORIES,
                                this
                            )
                            showBlackToolTip(Delivery.STANDARD)
                            KotlinUtils.browsingDeliveryType = Delivery.STANDARD
                        }
                        1 -> {
                            //Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOPMYLISTS, this)
                            showBlackToolTip(Delivery.CNC)
                            KotlinUtils.browsingDeliveryType = Delivery.CNC
                        }
                        2 -> {
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
        updateTabIconUI(0)
        showShopFeatureWalkThrough()
    }

    fun showSerachAndBarcodeUi() {
        tvSearchProduct?.visibility = View.VISIBLE
        imBarcodeScanner?.visibility = View.VISIBLE
    }

    fun hideSerachAndBarcodeUi() {
        tvSearchProduct?.visibility = View.GONE
        imBarcodeScanner?.visibility = View.GONE
    }

    private fun executeValidateSuburb() {
        val placeId = getDeliveryType()?.address?.placeId ?: return
        placeId.let {
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
            Delivery.getType(getDeliveryType()?.deliveryType),
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
        if (getDeliveryType() != null) {
            return
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
        if (selectedTab == 0) {
            showSerachAndBarcodeUi()
        } else if (selectedTab == 1 && KotlinUtils.browsingCncStore == null) {
            hideSerachAndBarcodeUi()
        }
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
            val myRiadFont =
                Typeface.createFromAsset(activity?.assets, "fonts/MyriadPro-Semibold.otf")
            view?.tvTitle?.typeface = myRiadFont
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
            setupToolbar(0)
            viewpager_main.currentItem = 0
        } else {
            setDeliveryView()
        }
        when (viewpager_main?.currentItem) {
            0 -> {
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
        if (requestCode == StandardDeliveryFragment.REQUEST_CODE_FINE_GPS && viewpager_main.currentItem == 0) {
            val fragment = viewpager_main?.adapter?.instantiateItem(
                viewpager_main,
                viewpager_main.currentItem
            ) as? StandardDeliveryFragment
            callOnActivityResult(grantResults, fragment, requestCode)
        } else if (requestCode == StandardDeliveryFragment.REQUEST_CODE_FINE_GPS && viewpager_main.currentItem == 1) {
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
            || requestCode == DEPARTMENT_LOGIN_REQUEST && viewpager_main.currentItem == 0
        ) {
            updateCurrentTab(getDeliveryType()?.deliveryType)
            val fragment = viewpager_main?.adapter?.instantiateItem(
                viewpager_main,
                viewpager_main.currentItem
            ) as? StandardDeliveryFragment
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
            (activity as? BottomNavigationActivity)?.let {
                it.bottomNavigationById.setCurrentItem(INDEX_ACCOUNT)
                val fragment = MyListsFragment()
                it.pushFragment(fragment)
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
                    ) as? StandardDeliveryFragment
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
                ) as? StandardDeliveryFragment
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
                ) as? StandardDeliveryFragment
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
            && validateLocationResponse?.validatePlace?.firstAvailableOtherDeliveryDate.isNullOrEmpty()
        ) {
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

            val store = GeoUtils.getStoreDetails(
                getDeliveryType()?.storeId,
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

    private fun getFirstAvailableFoodDeliveryDate(): String? {
        validateLocationResponse?.validatePlace?.let { validatePlace ->
            val store = GeoUtils.getStoreDetails(
                getDeliveryType()?.storeId,
                validatePlace.stores
            )
            return store?.firstAvailableFoodDeliveryDate
        }
        return "";
    }

    private fun showDashToolTip(validateLocationResponse: ValidateLocationResponse?) {
        val dashDeliverable = validateLocationResponse?.validatePlace?.onDemand?.deliverable
        if (KotlinUtils.isDashTabClicked == true || dashDeliverable == null || dashDeliverable == false) {
            blackToolTipLayout?.visibility = View.GONE
            return
        }

        if (validateLocationResponse?.validatePlace?.onDemand?.firstAvailableFoodDeliveryTime?.isNullOrEmpty() == true) {
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
            bubbleLayout?.arrowPosition =
                tabs_main.width - tabs_main.getTabAt(2)?.view?.width?.div(2)?.toFloat()!!
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
                    .setShapePadding(48)
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
                    currentItem = 2
                    adapter?.notifyDataSetChanged()
                }
                updateTabIconUI(2)
                showDeliveryDetailsFeatureWalkThrough()
            }
            WMaterialShowcaseView.Feature.SHOPPING -> {
                showDashFeatureWalkThrough()
            }
            WMaterialShowcaseView.Feature.BARCODE_SCAN -> {
                checkCameraPermission()
            }
            WMaterialShowcaseView.Feature.MY_LIST -> {
                if (SessionUtilities.getInstance().isUserAuthenticated) {
                    (activity as? BottomNavigationActivity)?.let {
                        it.bottomNavigationById.setCurrentItem(INDEX_ACCOUNT)
                        val fragment = MyListsFragment()
                        it.pushFragment(fragment)
                    }
                } else {
                    ScreenManager.presentSSOSignin(activity, LOGIN_MY_LIST_REQUEST_CODE)
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
}
