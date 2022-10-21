package za.co.woolworths.financial.services.android.ui.fragments.shop

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_shop_department.*
import kotlinx.android.synthetic.main.no_connection_layout.*
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.CartSummaryResponse
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.models.dto.RootCategories
import za.co.woolworths.financial.services.android.models.dto.RootCategory
import za.co.woolworths.financial.services.android.ui.activities.DashDetailsActivity
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.adapters.DepartmentAdapter
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.ProductListingFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.sub_category.SubCategoryFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.list.DepartmentExtensionFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.EnableLocationSettingsFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.REQUEST_CODE
import za.co.woolworths.financial.services.android.util.location.Event
import za.co.woolworths.financial.services.android.util.location.EventType
import za.co.woolworths.financial.services.android.util.location.Locator
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import za.co.woolworths.financial.services.android.viewmodels.shop.ShopViewModel

@AndroidEntryPoint
class StandardDeliveryFragment : DepartmentExtensionFragment() {

    private var locator: Locator? = null
    private var isRootCallInProgress: Boolean = false
    private var location: Location? = null
    private var rootCategoryCall: Call<RootCategories>? = null
    private var mDepartmentAdapter: DepartmentAdapter? = null
    private var isFragmentVisible: Boolean = false
    private var parentFragment: ShopFragment? = null
    private var isDashEnabled = false
    private var localPlaceId: String? = null
    private val shopViewModel: ShopViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )
    companion object {
        var DEPARTMENT_LOGIN_REQUEST = 1717
    }

    init {
        isDashEnabled =
            Utils.isFeatureEnabled(
                AppConfigSingleton.dashConfig?.minimumSupportedAppBuildNumber ?: 0
            )
                ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_shop_department, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.viewTreeObserver?.addOnWindowFocusChangeListener { hasFocus ->
            onWindowFocusChanged(
                hasFocus
            )
        }
        initView()
    }

    fun initView() {
        locator = (activity as? AppCompatActivity)?.let { Locator(it) }

        isDashEnabled = AppConfigSingleton.dashConfig?.isEnabled ?: false

        parentFragment = (activity as? BottomNavigationActivity)?.currentFragment as? ShopFragment
        setUpRecyclerView(mutableListOf())
        setListener()
        localPlaceId = KotlinUtils.getPreferredPlaceId()

        var isPermissionGranted = false
        activity?.apply {
            isPermissionGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }

        if (isDashEnabled && isFragmentVisible) {
            startLocationDiscoveryProcess()
        } else if (isFragmentVisible) {
            initializeRootCategoryList()
        }
    }

    private fun startLocationDiscoveryProcess() {
        locator?.getCurrentLocation { locationEvent ->
            when (locationEvent) {
                is Event.Location -> handleLocationEvent(locationEvent)
                is Event.Permission -> handlePermissionEvent(locationEvent)
            }
        }
    }

    private fun handlePermissionEvent(permissionEvent: Event.Permission) {
        if (permissionEvent.event == EventType.LOCATION_PERMISSION_NOT_GRANTED) {
            Utils.saveLastLocation(null, activity)
            handleLocationEvent(null)
        }
    }

    private fun handleLocationEvent(locationEvent: Event.Location?) {
        Utils.saveLastLocation(locationEvent?.locationData, context)
        location = locationEvent?.locationData
        initializeRootCategoryList()
    }

    private fun initializeRootCategoryList() {
        if (parentFragment?.getCategoryResponseData()?.rootCategories != null) bindDepartment() else executeDepartmentRequest(mDepartmentAdapter, parentFragment, location)
    }

    private fun onWindowFocusChanged(hasFocus: Boolean) {

        if (!hasFocus) {
            return
        }

        if (context != null && !Utils.isLocationEnabled(context)) {
            onProviderDisabled()
        } else {
            startLocationDiscoveryProcess()
        }
    }

    private fun setListener() {
        btnRetry.setOnClickListener {
            if (networkConnectionStatus()) {
                executeDepartmentRequest(mDepartmentAdapter, parentFragment, location)
            }
        }
    }



    private fun getDeliveryType(): String {
        if (SessionUtilities.getInstance().isUserAuthenticated) {
            Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.let { fulfillmentDetails ->
               return Delivery.getType(fulfillmentDetails.deliveryType)?.name ?: BundleKeysConstants.STANDARD
            }
        } else {
            KotlinUtils.getAnonymousUserLocationDetails()?.fulfillmentDetails?.let { fulfillmentDetails ->
                return Delivery.getType(fulfillmentDetails.deliveryType)?.name ?: BundleKeysConstants.STANDARD
            }
        }
        return BundleKeysConstants.STANDARD
    }

    private fun bindDepartment() {
        mDepartmentAdapter?.setRootCategories(parentFragment?.getCategoryResponseData()?.rootCategories)
        mDepartmentAdapter?.notifyDataSetChanged()
    }

    private fun setUpRecyclerView(categories: MutableList<RootCategory>?) {
        mDepartmentAdapter = DepartmentAdapter(
            categories,
            ::departmentItemClicked) //{ rootCategory: RootCategory -> departmentItemClicked(rootCategory)}
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
        var deliveryType: Delivery? = Delivery.STANDARD
        var placeId = ""

        if (SessionUtilities.getInstance().isUserAuthenticated) {
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


    private fun presentDashDetailsActivity(activity: Activity, link: String?) {
        activity.apply {
            val mIntent = Intent(this, DashDetailsActivity::class.java)
            mIntent.putExtra(
                "bundle", bundleOf(
                    AppConstant.KEY_DASH_WOOLIES_DOWNLOAD_LINK to link
                )
            )
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
                bundle.putString(
                    SubCategoryFragment.KEY_ARGS_ROOT_CATEGORY,
                    Utils.toJson(rootCategory)
                )
                bundle.putString(SubCategoryFragment.KEY_ARGS_VERSION, version)
                bundle.putBoolean(
                    SubCategoryFragment.KEY_ARGS_IS_LOCATION_ENABLED,
                    if (context != null) Utils.isLocationEnabled(context) else false
                )
                bundle.putBoolean(
                    AppConstant.Keys.EXTRA_SEND_DELIVERY_DETAILS_PARAMS,
                    arguments?.getBoolean(AppConstant.Keys.EXTRA_SEND_DELIVERY_DETAILS_PARAMS, false) ?: false
                )
                location?.let { bundle.putParcelable(SubCategoryFragment.KEY_ARGS_LOCATION, it) }
                drillDownCategoryFragment.arguments = bundle
                return drillDownCategoryFragment
            }
            else -> ProductListingFragment.newInstance(
                ProductsRequestParams.SearchType.NAVIGATE,
                rootCategory.categoryName,
                rootCategory.dimValId,
                isBrowsing = true,
                sendDeliveryDetails = arguments?.getBoolean(AppConstant.Keys.EXTRA_SEND_DELIVERY_DETAILS_PARAMS, false)
            )
        }
    }

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
                //When moved from My Cart to department
                startLocationDiscoveryProcess()
            }
        }
    }

    fun scrollToTop() {
        rclDepartment?.scrollToPosition(0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DEPARTMENT_LOGIN_REQUEST && resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()) {
            if (Utils.getPreferredDeliveryLocation() != null) {
                activity?.apply {
                    KotlinUtils.presentEditDeliveryGeoLocationActivity(
                        this,
                        REQUEST_CODE,
                        KotlinUtils.getPreferredDeliveryType(),
                        Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId
                    )
                }
            } else {
                requestCartSummary()
            }
        } else if (resultCode == RESULT_OK || resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()) {
            mDepartmentAdapter?.notifyDataSetChanged()
        } else if (requestCode == EnableLocationSettingsFragment.ACCESS_MY_LOCATION_REQUEST_CODE) {
            startLocationDiscoveryProcess()
        }
    }

    private fun requestCartSummary() {
        GetCartSummary().getCartSummary(object : IResponseListener<CartSummaryResponse> {
            override fun onSuccess(response: CartSummaryResponse?) {
                when (response?.httpCode) {
                    AppConstant.HTTP_OK -> {
                        activity?.apply {
                            onEditDeliveryLocation()
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
        }
    }

    private fun onProviderDisabled() {
        location = null
        parentFragment?.getCategoryResponseData()?.dash = null
    }

    public fun reloadRequest(){
        executeDepartmentRequest(mDepartmentAdapter, parentFragment, location)
    }
}
