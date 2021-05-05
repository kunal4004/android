package za.co.woolworths.financial.services.android.ui.fragments.shop

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.fragment_shop_department.*
import kotlinx.android.synthetic.main.no_connection_layout.*
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.ValidateSelectedSuburbResponse
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.CartSummaryResponse
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.models.dto.RootCategories
import za.co.woolworths.financial.services.android.models.dto.RootCategory
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.DashDetailsActivity
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.ui.activities.click_and_collect.EditDeliveryLocationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.adapters.DepartmentAdapter
import za.co.woolworths.financial.services.android.ui.fragments.click_and_collect.DeliveryOrClickAndCollectSelectorDialogFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.ProductListingFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.sub_category.SubCategoryFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.list.DepartmentExtensionFragment
import za.co.woolworths.financial.services.android.ui.fragments.store.StoresNearbyFragment1
import za.co.woolworths.financial.services.android.util.*

class DepartmentsFragment : DepartmentExtensionFragment(), DeliveryOrClickAndCollectSelectorDialogFragment.IDeliveryOptionSelection {

    private var isFirstCallToLocationModal: Boolean = false
    private var isLocationModalShown: Boolean = false
    private var isRootCallInProgress: Boolean = false
    private var location: Location? = null
    private var rootCategoryCall: Call<RootCategories>? = null
    private var mDepartmentAdapter: DepartmentAdapter? = null
    private var isFragmentVisible: Boolean = false
    private var parentFragment: ShopFragment? = null
    private var version: String? = ""
    private var deliveryType: DeliveryType = DeliveryType.DELIVERY
    private var isDashEnabled = false
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = createLocationRequest()


    companion object {
        var DEPARTMENT_LOGIN_REQUEST = 1717
        const val REQUEST_CODE_FINE_GPS = 4771
    }


    init {
        isDashEnabled = Utils.isFeatureEnabled(WoolworthsApplication.getInstance()?.dashConfig?.minimumSupportedAppBuildNumber?.toString())
                ?: false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_shop_department, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.viewTreeObserver?.addOnWindowFocusChangeListener { hasFocus -> onWindowFocusChanged(hasFocus) }
        activity?.apply {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        }

        isDashEnabled = WoolworthsApplication.getInstance()?.dashConfig?.isEnabled ?: false

        parentFragment = (activity as? BottomNavigationActivity)?.currentFragment as? ShopFragment
        setUpRecyclerView(mutableListOf())
        setListener()

        var isPermissionGranted: Boolean = false
        activity?.apply {
            isPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }

        if (isDashEnabled && isFragmentVisible) {
            if (isPermissionGranted && Utils.isLocationEnabled(context)) {
                fusedLocationClient?.lastLocation?.addOnSuccessListener {
                    this@DepartmentsFragment.location = it
                    initializeRootCategoryList()
                }
            } else {
                // when permission granted and location is not enabled
                if (isPermissionGranted) {
                    initializeRootCategoryList()
                }
                //When Location permission not granted.
                else if (!checkLocationPermission() && !isLocationModalShown) {
                    initializeRootCategoryList()
                }
            }
        } else if (isFragmentVisible) {
            initializeRootCategoryList()
        }
    }

    private fun initializeRootCategoryList() {
        if (parentFragment?.getCategoryResponseData() != null) bindDepartment() else executeDepartmentRequest()
        showDeliveryOptionDialog()
    }

    private fun onWindowFocusChanged(hasFocus: Boolean) {

        if (!hasFocus) {
            return
        }

        if (context != null && !Utils.isLocationEnabled(context)) {
            onProviderDisabled()
            if (isFirstCallToLocationModal) {
                executeDepartmentRequest()
            }
        } else {
            startLocationUpdates()
        }

        showDeliveryOptionDialog()
    }

    private fun setListener() {
        btnRetry.setOnClickListener {
            if (networkConnectionStatus()) {
                executeDepartmentRequest()
            }
        }
    }

    private fun executeDepartmentRequest() {
        if (networkConnectionStatus()) {
            noConnectionLayout(false)
            if (isRootCallInProgress) {
                return
            }

            isRootCallInProgress = true
            val isLocationEnabled = if (context != null) Utils.isLocationEnabled(context) else false
            rootCategoryCall = OneAppService.getRootCategory(isLocationEnabled, location)
            rootCategoryCall?.enqueue(CompletionHandler(object : IResponseListener<RootCategories> {
                override fun onSuccess(response: RootCategories?) {
                    isRootCallInProgress = false
                    when (response?.httpCode) {
                        200 -> {
                            version = response.response?.version
                            parentFragment?.setCategoryResponseData(response)
                            bindDepartment()
                        }
                        else -> response?.response?.desc?.let { showErrorDialog(it) }
                    }
                }

                override fun onFailure(error: Throwable?) {
                    if (isAdded) {
                        activity?.runOnUiThread {
                            if (networkConnectionStatus())
                                noConnectionLayout(true)
                        }
                    }
                }
            }, RootCategories::class.java))
        } else {
            noConnectionLayout(true)
        }
    }

    private fun bindDepartment() {
        mDepartmentAdapter?.setRootCategories(parentFragment?.getCategoryResponseData()?.rootCategories)
        // Add dash banner if only present
        if (isDashEnabled && isFragmentVisible && context != null && Utils.isLocationEnabled(context)) {
            mDepartmentAdapter?.setDashBanner(parentFragment?.getCategoryResponseData()?.dash, parentFragment?.getCategoryResponseData()?.rootCategories,
                    getUpdatedBannerText())
        }
        mDepartmentAdapter?.notifyDataSetChanged()
        executeValidateSuburb()
    }

    private fun setUpRecyclerView(categories: MutableList<RootCategory>?) {
        mDepartmentAdapter = DepartmentAdapter(categories, ::departmentItemClicked, ::onEditDeliveryLocation, ::onDashBannerClicked) //{ rootCategory: RootCategory -> departmentItemClicked(rootCategory)}
        activity?.let {
            rclDepartment?.apply {
                layoutManager = LinearLayoutManager(it, LinearLayoutManager.VERTICAL, false)
                adapter = mDepartmentAdapter
            }
        }
    }

    private fun departmentItemClicked(rootCategory: RootCategory) {
        (activity as? BottomNavigationActivity)?.pushFragmentSlideUp(openNextFragment(rootCategory))
    }

    private fun onEditDeliveryLocation() {
        if (SessionUtilities.getInstance().isUserAuthenticated) {
            /* if (Utils.getPreferredDeliveryLocation() != null) {
                 activity?.apply { KotlinUtils.presentEditDeliveryLocationActivity(this, if (Utils.getPreferredDeliveryLocation().suburb.storePickup) DeliveryType.STORE_PICKUP else DeliveryType.DELIVERY) }
             } else*/
            activity?.apply { KotlinUtils.presentEditDeliveryLocationActivity(this, EditDeliveryLocationActivity.REQUEST_CODE) }
        } else {
            ScreenManager.presentSSOSignin(activity, DEPARTMENT_LOGIN_REQUEST)
        }
    }


    private fun getUpdatedBannerText(): String {
        context?.apply {
            return if (KotlinUtils.isAppInstalled(activity, WoolworthsApplication.getInstance()?.dashConfig?.appURI))
                this.getString(R.string.dash_banner_text_open_app) else this.getString(R.string.dash_banner_text_download_app)
        }
        return ""
    }

    private fun onDashBannerClicked() {
        activity?.apply {
            KotlinUtils.postOneAppEvent(OneAppEvents.AppScreen.DASH_BANNER_SCREEN_NAME, OneAppEvents.FeatureName.DASH_FEATURE_NAME)

            val intent: Intent? = this.packageManager.getLaunchIntentForPackage(WoolworthsApplication.getInstance()?.dashConfig?.appURI
                    ?: "")
            if (intent == null) {
                presentDashDetailsActivity(this, parentFragment?.getCategoryResponseData()?.dash?.dashBreakoutLink)
            } else {
                // Launch the woolies dash if already downloaded/installed
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                this.startActivity(intent)
            }
        }
    }

    fun presentDashDetailsActivity(activity: Activity, link: String?) {
        activity.apply {
            val mIntent = Intent(this, DashDetailsActivity::class.java)
            mIntent.putExtra("bundle", bundleOf(
                    AppConstant.KEY_DASH_WOOLIES_DOWNLOAD_LINK to link
            ))
            startActivity(mIntent)
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
        }
    }

    private fun openNextFragment(rootCategory: RootCategory): Fragment {
        val drillDownCategoryFragment = SubCategoryFragment()
        val bundle = Bundle()
        return when (rootCategory.hasChildren) {
            // navigate to drill down of categories
            true -> {
                bundle.putString("ROOT_CATEGORY", Utils.toJson(rootCategory))
                bundle.putString("VERSION", version)
                drillDownCategoryFragment.arguments = bundle
                return drillDownCategoryFragment
            }
            else -> ProductListingFragment.newInstance(ProductsRequestParams.SearchType.NAVIGATE, rootCategory.categoryName, rootCategory.dimValId)
        }
    }

    fun noConnectionLayout(isVisible: Boolean) {
        incConnectionLayout?.visibility = if (isVisible) VISIBLE else GONE
    }

    fun networkConnectionStatus(): Boolean = activity?.let { NetworkManager.getInstance().isConnectedToNetwork(it) }
            ?: false

    override fun onDestroy() {
        super.onDestroy()
        rootCategoryCall?.apply {
            if (isCanceled)
                cancel()
        }
    }


    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        isFragmentVisible = isVisibleToUser
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        if (!hidden) {
            activity?.apply {
                executeValidateSuburb()
                //When moved from My Cart to department
                startLocationUpdates()

                showDeliveryOptionDialog()
            }
        }
    }

    fun scrollToTop() {
        rclDepartment?.scrollToPosition(0)
    }

    override fun onDeliveryOptionSelected(deliveryType: DeliveryType) {
        if (SessionUtilities.getInstance().isUserAuthenticated) {
            activity?.apply { KotlinUtils.presentEditDeliveryLocationActivity(this, EditDeliveryLocationActivity.REQUEST_CODE, deliveryType) }
        } else {
            this.deliveryType = deliveryType
            ScreenManager.presentSSOSignin(activity, DEPARTMENT_LOGIN_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DEPARTMENT_LOGIN_REQUEST && resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()) {
            if (Utils.getPreferredDeliveryLocation() != null) {
                activity?.apply { KotlinUtils.presentEditDeliveryLocationActivity(this, EditDeliveryLocationActivity.REQUEST_CODE, deliveryType) }
            } else {
                requestCartSummary()
            }
        } else if (requestCode == REQUEST_CODE_FINE_GPS) {
            when (resultCode) {
                RESULT_OK -> {
                    activity?.apply {
                        if (!Utils.isLocationEnabled(context)) {
                            val locIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            startActivityForResult(locIntent, StoresNearbyFragment1.REQUEST_CHECK_SETTINGS)
                            overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
                        }
                        isFirstCallToLocationModal = true
                    }
                }
                RESULT_CANCELED -> {
                    //When user clicks deny location
                    executeDepartmentRequest()
                }
            }
        } else if (resultCode == RESULT_OK || resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()) {
            mDepartmentAdapter?.notifyDataSetChanged()
            executeValidateSuburb()
        }
    }

    private fun requestCartSummary() {
        GetCartSummary().getCartSummary(object : IResponseListener<CartSummaryResponse> {
            override fun onSuccess(response: CartSummaryResponse?) {
                when (response?.httpCode) {
                    AppConstant.HTTP_OK -> {
                        if (Utils.getPreferredDeliveryLocation() != null) {
                            activity?.apply {
                                KotlinUtils.presentEditDeliveryLocationActivity(this, ProductListingFragment.SET_DELIVERY_LOCATION_REQUEST_CODE)
                            }
                        }
                    }
                }
            }
            override fun onFailure(error: Throwable?) {
            }

        })
    }


    override fun onResume() {
        super.onResume()
        activity?.apply {
            mDepartmentAdapter?.notifyDataSetChanged()
            executeValidateSuburb()
        }
    }

    private fun showDeliveryOptionDialog() {
        if (!Utils.isDeliverySelectionModalShown()) {
            (activity as? AppCompatActivity)?.supportFragmentManager?.beginTransaction()?.let { fragmentTransaction -> DeliveryOrClickAndCollectSelectorDialogFragment.newInstance(this).show(fragmentTransaction, DeliveryOrClickAndCollectSelectorDialogFragment::class.java.simpleName) }
        }
    }

    private fun executeValidateSuburb() {
        Utils.getPreferredDeliveryLocation().let {
            if (it == null) {
                mDepartmentAdapter?.hideDeliveryDates()
            } else {
                val storeOrSuburbId = if (it.storePickup) it.store.id else it.suburb.id
                if (storeOrSuburbId.equals(WoolworthsApplication.getValidatedSuburbProducts()?.suburbId, true)) {
                    updateDeliveryDates()
                } else {
                    mDepartmentAdapter?.showDeliveryDatesProgress(true)
                    storeOrSuburbId?.let { it1 ->
                        OneAppService.validateSelectedSuburb(it1, it.storePickup).enqueue(CompletionHandler(object : IResponseListener<ValidateSelectedSuburbResponse> {
                            override fun onSuccess(response: ValidateSelectedSuburbResponse?) {
                                when (response?.httpCode) {
                                    200 -> response.validatedSuburbProducts?.let { it1 ->
                                        it1.suburbId = storeOrSuburbId
                                        WoolworthsApplication.setValidatedSuburbProducts(it1)
                                        updateDeliveryDates()
                                    }
                                    else -> mDepartmentAdapter?.hideDeliveryDates()
                                }
                            }

                            override fun onFailure(error: Throwable?) {
                                mDepartmentAdapter?.hideDeliveryDates()
                            }
                        }, ValidateSelectedSuburbResponse::class.java))
                    }
                }
            }
        }
    }

    fun updateDeliveryDates() {
        mDepartmentAdapter?.updateDeliveryDate(WoolworthsApplication.getValidatedSuburbProducts())
    }

/*
    override fun onLocationChanged(location: Location?) {

    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        //Do nothing
    }

    override fun onProviderEnabled(provider: String?) {
        if (isDashEnabled) {
            executeDepartmentRequest()
        }
    }
*/

    private fun onProviderDisabled() {
        location = null
        mDepartmentAdapter?.apply {
            removeDashBanner(parentFragment?.getCategoryResponseData()?.rootCategories)
        }
        parentFragment?.getCategoryResponseData()?.dash = null
    }

    @SuppressLint("NewApi")
    private fun checkLocationPermission(): Boolean {
        if (!isFragmentVisible) {
            return false
        }

        activity?.apply {
            val perms =
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            return if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    //Asking only once.
//                    ActivityCompat.requestPermissions(this, perms, REQUEST_CODE_FINE_GPS)
                } else {
                    //we can request the permission.
                    ActivityCompat.requestPermissions(this, perms, REQUEST_CODE_FINE_GPS)
                    isLocationModalShown = true
                }
                false
            } else {
                true
            }
        }
        return false
    }

    private fun startLocationUpdates() {
        context?.apply {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            fusedLocationClient?.requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper())
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient?.removeLocationUpdates(locationCallback)
    }

    private fun createLocationRequest() = LocationRequest.create()?.apply {
        interval = 100
        fastestInterval = 1000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private var locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            for (location in locationResult.locations) {
                this@DepartmentsFragment.location = location
                executeDepartmentRequest()
                stopLocationUpdates()
                break
            }
        }
    }
}
