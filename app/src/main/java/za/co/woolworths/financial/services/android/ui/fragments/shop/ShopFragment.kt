package za.co.woolworths.financial.services.android.ui.fragments.shop


import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_shop.*
import kotlinx.android.synthetic.main.shop_custom_tab.view.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.dash.view.DashCollectionStoreFragment
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.models.dto.OrdersResponse
import za.co.woolworths.financial.services.android.models.dto.RootCategories
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse
import za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity.Companion.ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE
import za.co.woolworths.financial.services.android.ui.activities.BarcodeScanActivity
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.ui.activities.click_and_collect.EditDeliveryLocationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.INDEX_PRODUCT
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.PDP_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity
import za.co.woolworths.financial.services.android.ui.adapters.ShopPagerAdapter
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.shop.DepartmentsFragment.Companion.DEPARTMENT_LOGIN_REQUEST
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList.Companion.DISPLAY_TOAST_RESULT_CODE
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.OnChildFragmentEvents
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.REQUEST_CODE_ORDER_DETAILS_PAGE
import za.co.woolworths.financial.services.android.util.ScreenManager.SHOPPING_LIST_DETAIL_ACTIVITY_REQUEST_CODE


/**
 * A simple [Fragment] subclass.
 *
 */
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
    private var blackToolTipDialog: Dialog? = null
    private var user: String = ""

    enum class Delivery_Types(val value: String) {
        STANDARD("standard"),
        CLICK_AND_COLLECT("click_and_collect"),
        DASH("dash");
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
        activity?.supportFragmentManager?.let {
            shopPagerAdapter = ShopPagerAdapter(it, mTabTitle, this)
        }
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
                            showBlackToolTip(Delivery_Types.STANDARD)
                        }
                        1 -> {
                            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOPMYLISTS,
                                this)
                            showBlackToolTip(Delivery_Types.CLICK_AND_COLLECT)
                        }
                        2 -> {
                            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOPMYORDERS,
                                this)
                            showBlackToolTip(Delivery_Types.DASH)
                        }
                    }
                }
            }
        })
        tabs_main?.setupWithViewPager(viewpager_main)
        updateTabIconUI(0)
        showShopFeatureWalkThrough()
        showBlackToolTip(Delivery_Types.STANDARD)
        setupToolbar(0)
    }

    private fun setupToolbar(tabPosition: Int) {
        if (tabPosition < 0) {
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
                tvToolbarTitle?.text = requireContext().getString(R.string.dash_delivery)
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
            for (i in mTabTitle?.indices!!) {
                tab.getTabAt(i)?.customView = prepareTabView(i, mTabTitle)
            }
            tab.getTabAt(selectedTab)?.customView?.isSelected = true
        }
    }

    private fun prepareTabView(pos: Int, tabTitle: MutableList<String>?): View? {
        val view = activity?.layoutInflater?.inflate(R.layout.shop_custom_tab, null)
        view?.tvTitle?.text = tabTitle?.get(pos)
        if (pos == 2) {
            foodOnlyText?.visibility = View.VISIBLE
        }
        if (pos == 1) {
            clickCollectText?.visibility = View.VISIBLE
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

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            //do when hidden
            (activity as? BottomNavigationActivity)?.apply {
                fadeOutToolbar(R.color.recent_search_bg)
                showBackNavigationIcon(false)
                showBottomNavigationMenu()
                refreshViewPagerFragment(false)
                Handler().postDelayed({
                    hideToolbar()
                }, AppConstant.DELAY_1000_MS)
            }
        }

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
            ) as? DashCollectionStoreFragment
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
                    refreshViewPagerFragment(true)
                }
                ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE -> {
                    refreshViewPagerFragment(true)
                }
                CancelOrderProgressFragment.RESULT_CODE_CANCEL_ORDER_SUCCESS -> {
                    refreshViewPagerFragment(true)
                }
            }
        }

        if (requestCode == CancelOrderProgressFragment.REQUEST_CODE_CANCEL_ORDER
            && resultCode == CancelOrderProgressFragment.RESULT_CODE_CANCEL_ORDER_SUCCESS
        ) {
            refreshViewPagerFragment(true)
        }

        if (resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()) {
            refreshViewPagerFragment(true)
        }

        if (requestCode == PDP_REQUEST_CODE && resultCode == ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE) {
            navigateToMyListFragment()
            refreshViewPagerFragment(true)
        }

        if (requestCode == SHOPPING_LIST_DETAIL_ACTIVITY_REQUEST_CODE) {
            refreshViewPagerFragment(true)
        }

        if (requestCode == EditDeliveryLocationActivity.REQUEST_CODE || requestCode == DEPARTMENT_LOGIN_REQUEST && viewpager_main.currentItem == 0) {
            val fragment = viewpager_main?.adapter?.instantiateItem(
                viewpager_main,
                viewpager_main.currentItem
            ) as? DepartmentsFragment
            fragment?.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun refreshViewPagerFragment(isNewSession: Boolean) {
        when (viewpager_main.currentItem) {
            1 -> {
                val myListsFragment = viewpager_main?.adapter?.instantiateItem(
                    viewpager_main,
                    viewpager_main.currentItem
                ) as? MyListsFragment
                myListsFragment?.authenticateUser(isNewSession)
            }
            2 -> {
                val myOrdersFragment = viewpager_main?.adapter?.instantiateItem(
                    viewpager_main,
                    viewpager_main.currentItem
                ) as? MyOrdersFragment
                myOrdersFragment?.configureUI(isNewSession)
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
                val myListsFragment = viewpager_main?.adapter?.instantiateItem(
                    viewpager_main,
                    viewpager_main.currentItem
                ) as? MyListsFragment
                myListsFragment?.scrollToTop()
            }
            2 -> {
                val myOrdersFragment = viewpager_main?.adapter?.instantiateItem(
                    viewpager_main,
                    viewpager_main.currentItem
                ) as? MyOrdersFragment
                myOrdersFragment?.scrollToTop()
            }
        }
    }

    fun setCategoryResponseData(rootCategories: RootCategories) {
        this.rootCategories = rootCategories
    }

    fun setShoppingListResponseData(shoppingListsResponse: ShoppingListsResponse?) {
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

    fun isDifferentUser(): Boolean? {
        return user != AppInstanceObject.get()?.currentUserObject?.id ?: false
    }

    fun clearCachedData() {
        if (isDifferentUser()!!) {
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

    private fun showBlackToolTip(deliveryType: Delivery_Types) {
        if (blackToolTipDialog != null && blackToolTipDialog!!.isShowing) {
            blackToolTipDialog!!.dismiss()
        }
        blackToolTipDialog = activity?.let { activity ->
            Dialog(activity,
                android.R.style.ThemeOverlay_DeviceDefault_Accent_DayNight)
        }
        blackToolTipDialog?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            val view = layoutInflater.inflate(R.layout.black_tool_tip_layout, null)
            view.findViewById<ImageButton>(R.id.closeWhiteBtn).setOnClickListener {
                if (this != null && this.isShowing) {
                    this.dismiss()
                }
            }
            when (deliveryType) {
                Delivery_Types.STANDARD -> {
                    showStandardDeliveryToolTip(view)
                }
                Delivery_Types.CLICK_AND_COLLECT -> {
                    showClickAndCollectToolTip(view)
                }
                Delivery_Types.DASH -> {
                    showDashToolTip(view)
                }
            }
            setContentView(view)
            window?.apply {
                setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
                setBackgroundDrawableResource(R.color.transparent)
                setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
                clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                setGravity(Gravity.TOP)
            }

            setTitle(null)
            setCanceledOnTouchOutside(true)
            show()
        }
    }

    private fun showStandardDeliveryToolTip(view: View) {
        //ToDo: Remove this hardcoded value in WOP-15382
        view.findViewById<TextView>(R.id.deliveryCollectionTitle).text =
            getString(R.string.earliest_delivery_dates)
        view.findViewById<TextView>(R.id.foodItemDateText).text = "Sun, 19 Aug 1pm - 2pm"
        view.findViewById<TextView>(R.id.fashionItemDateText).text =
            "Mon, 22 Aug 10:30am - 11:30am"
        view.findViewById<TextView>(R.id.productAvailableText).text = "All products available"
        view.findViewById<TextView>(R.id.deliveryFeeText).text = "R50 Delivery Fee"
    }

    private fun showClickAndCollectToolTip(view: View) {
        view.findViewById<TextView>(R.id.deliveryCollectionTitle).text =
            getString(R.string.earliest_collection_Date)
        view.findViewById<TextView>(R.id.foodItemTitle).visibility = View.GONE
        view.findViewById<TextView>(R.id.fashionItemDateText).visibility = View.GONE
        view.findViewById<ConstraintLayout>(R.id.deliveryIconLayout).visibility = View.GONE
        view.findViewById<TextView>(R.id.fashionItemTitle).visibility = View.VISIBLE

        //ToDo: Remove this hardcoded value in WOP-15382
        view.findViewById<TextView>(R.id.foodItemDateText).text = "Mon, 22 Aug 10:30am - 11:30am"
        view.findViewById<TextView>(R.id.fashionItemTitle).text =
            getString(R.string.all_products_available)
        view.findViewById<TextView>(R.id.productAvailableText).text = "Free Collection"
        view.findViewById<com.daasuu.bl.BubbleLayout>(R.id.bubbleLayout).arrowPosition = 640.0F
        view.findViewById<ImageButton>(R.id.cartIcon)
            .setImageResource(R.drawable.white_shopping_bag_icon)
    }

    private fun showDashToolTip(view: View) {
        view.findViewById<TextView>(R.id.deliveryCollectionTitle).text =
            getString(R.string.next_dash_delivery_timeslot_text)
        view.findViewById<TextView>(R.id.foodItemTitle).visibility = View.GONE
        view.findViewById<TextView>(R.id.fashionItemDateText).visibility = View.GONE
        view.findViewById<ConstraintLayout>(R.id.deliveryIconLayout).visibility = View.VISIBLE
        view.findViewById<ConstraintLayout>(R.id.cartIconLayout).visibility = View.VISIBLE
        view.findViewById<TextView>(R.id.fashionItemTitle).visibility = View.GONE

        //ToDo: Remove this hardcoded value in WOP-15382
        view.findViewById<TextView>(R.id.foodItemDateText).text = "1pm - 2pm, Today"
        view.findViewById<ImageButton>(R.id.cartIcon).setImageResource(R.drawable.icon_cart_white)
        view.findViewById<ImageButton>(R.id.deliveryIcon)
            .setImageResource(R.drawable.icon_scooter_white)
        view.findViewById<com.daasuu.bl.BubbleLayout>(R.id.bubbleLayout).arrowPosition = 1060.0F
        view.findViewById<TextView>(R.id.productAvailableText).text = "42 Item Limit"
        view.findViewById<TextView>(R.id.deliveryFeeText).text = "Free for orders over R75"

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
        }
    }
}
