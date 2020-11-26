package za.co.woolworths.financial.services.android.ui.fragments.shop

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_shop_department.*
import kotlinx.android.synthetic.main.no_connection_layout.*
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.ValidateSelectedSuburbResponse
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.models.dto.RootCategories
import za.co.woolworths.financial.services.android.models.dto.RootCategory
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
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

class DepartmentsFragment : DepartmentExtensionFragment(), DeliveryOrClickAndCollectSelectorDialogFragment.IDeliveryOptionSelection, LocationListener {

    private lateinit var locationManager: LocationManager
    private var isRootCatCallInProgress: Boolean = false
    private var rootCategoryCall: Call<RootCategories>? = null
    private var mDepartmentAdapter: DepartmentAdapter? = null
    private var isFragmentVisible: Boolean = false
    private var parentFragment: ShopFragment? = null
    private var version: String? = ""
    private var deliveryType: DeliveryType = DeliveryType.DELIVERY
    private var isDashEnabled = false
    companion object {
        var DEPARTMENT_LOGIN_REQUEST = 1717
        const val REQUEST_CODE_FINE_GPS = 4771
    }

    init {
        isDashEnabled = Utils.isFeatureEnabled(WoolworthsApplication.getInstance().dashConfig.minimumSupportedAppBuildNumber.toString())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_shop_department, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parentFragment = (activity as? BottomNavigationActivity)?.currentFragment as? ShopFragment
        setUpRecyclerView(mutableListOf())
        setListener()
        if(checkLocationPermission() && isDashEnabled) {
            startLocationUpdates()
        }
        if (isFragmentVisible) {
            if (parentFragment?.getCategoryResponseData() != null) bindDepartment() else executeDepartmentRequest()
            if (!Utils.isDeliverySelectionModalShown()) {
                showDeliveryOptionDialog()
            }
        }
    }

    private fun setListener() {
        btnRetry.setOnClickListener {
            if (networkConnectionStatus()) {
                executeDepartmentRequest()
            }
        }
    }

    private fun startLocationUpdates() {
        activity?.apply {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, this@DepartmentsFragment)
        }
    }

    private fun stopLocationUpdate() {
        // stop location updates
        locationManager.removeUpdates(this@DepartmentsFragment)
    }

    private fun executeDepartmentRequest() {
        if (networkConnectionStatus()) {
            noConnectionLayout(false)
            isRootCatCallInProgress = true

            rootCategoryCall = OneAppService.getRootCategory(Utils.isLocationEnabled(context))
            rootCategoryCall?.enqueue(CompletionHandler(object : IResponseListener<RootCategories> {
                override fun onSuccess(response: RootCategories?) {
                    isRootCatCallInProgress = false
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
                    isRootCatCallInProgress = false
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
        if(isDashEnabled) {
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
            return if(KotlinUtils.isAppInstalled(activity, WoolworthsApplication.getInstance()?.dashConfig?.appURI))
                this.getString(R.string.dash_banner_text_open_app) else this.getString(R.string.dash_banner_text_download_app)
        }
        return ""
    }

    private fun onDashBannerClicked() {
        activity?.apply {
            KotlinUtils.postOneAppEvent(OneAppEvents.AppScreen.DASH_BANNER_SCREEN_NAME, OneAppEvents.FeatureName.DASH_FEATURE_NAME)

            val intent: Intent? = this.packageManager.getLaunchIntentForPackage(WoolworthsApplication.getInstance()?.dashConfig?.appURI ?: "")
            if (intent == null) {
                KotlinUtils.presentDashDetailsActivity(this, parentFragment?.getCategoryResponseData()?.dash?.dashBreakoutLink)
            } else {
                // Launch the woolies dash if already downloaded/installed
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                this.startActivity(intent)
            }
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
        incConnectionLayout.visibility = if (isVisible) VISIBLE else GONE
    }

    fun networkConnectionStatus(): Boolean = activity?.let { NetworkManager.getInstance().isConnectedToNetwork(it) }
            ?: false

    override fun onDestroy() {
        mDepartmentAdapter?.removeDashBanner()
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
                refreshLocationUpdates()
            }
        }
    }

    private fun refreshLocationUpdates() {
        if (context != null && Utils.isLocationEnabled(context) && PermissionUtils.hasPermissions(context, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            mDepartmentAdapter?.updateDashBanner(getUpdatedBannerText(), isDashEnabled)
            startLocationUpdates()
        } else {
            mDepartmentAdapter?.removeDashBanner()
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
            activity?.apply { KotlinUtils.presentEditDeliveryLocationActivity(this, EditDeliveryLocationActivity.REQUEST_CODE, deliveryType) }
        } else if(requestCode == REQUEST_CODE_FINE_GPS ){
            when(resultCode){
                RESULT_OK -> {
                    activity?.apply {
                        if(!Utils.isLocationEnabled(context)) {
                            val locIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            startActivityForResult(locIntent, StoresNearbyFragment1.REQUEST_CHECK_SETTINGS)
                            overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
                        }
                    }
                }
            }
        } else if (resultCode == RESULT_OK || resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()) {
            mDepartmentAdapter?.notifyDataSetChanged()
            executeValidateSuburb()
        }
    }


    override fun onResume() {
        super.onResume()
        activity?.apply {
            //When moved from other bottom nav tabs except My Cart
            refreshLocationUpdates()
            mDepartmentAdapter?.notifyDataSetChanged()
            executeValidateSuburb()
        }
    }

    private fun showDeliveryOptionDialog() {
        (activity as? AppCompatActivity)?.supportFragmentManager?.beginTransaction()?.let { fragmentTransaction -> DeliveryOrClickAndCollectSelectorDialogFragment.newInstance(this).show(fragmentTransaction, DeliveryOrClickAndCollectSelectorDialogFragment::class.java.simpleName) }
    }

    private fun executeValidateSuburb() {
        Utils.getPreferredDeliveryLocation().let {
            if (it == null) {
                mDepartmentAdapter?.hideDeliveryDates()
            } else {
                if (it.suburb.id.equals(WoolworthsApplication.getValidatedSuburbProducts()?.suburbId, true)) {
                    updateDeliveryDates()
                } else {
                    mDepartmentAdapter?.showDeliveryDatesProgress(true)
                    OneAppService.validateSelectedSuburb(it.suburb.id, it.suburb.storePickup).enqueue(CompletionHandler(object : IResponseListener<ValidateSelectedSuburbResponse> {
                        override fun onSuccess(response: ValidateSelectedSuburbResponse?) {
                            when (response?.httpCode) {
                                200 -> response.validatedSuburbProducts?.let { it1 ->
                                    it1.suburbId = it.suburb.id
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

    fun updateDeliveryDates() {
        mDepartmentAdapter?.updateDeliveryDate(WoolworthsApplication.getValidatedSuburbProducts())
    }

    override fun onLocationChanged(location: Location?) {
        activity?.apply {
            Utils.saveLastLocation(location, this)
            stopLocationUpdate()

            if (!isRootCatCallInProgress) {
                executeDepartmentRequest()
            }
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        //Do nothing
    }

    override fun onProviderEnabled(provider: String?) {
        //Do nothing
    }

    override fun onProviderDisabled(provider: String?) {
        //Do nothing
    }

    @SuppressLint("NewApi")
    private fun checkLocationPermission(): Boolean {
        if(!isFragmentVisible){
            return false
        }

        activity?.apply {
            val perms =
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            return if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                } else {
                    //we can request the permission.
                    ActivityCompat.requestPermissions(this, perms, REQUEST_CODE_FINE_GPS)
                }
                false
            } else {
                true
            }
        }
        return false
    }
}
