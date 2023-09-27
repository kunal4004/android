package za.co.woolworths.financial.services.android.ui.fragments.shop

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentShopDepartmentBinding
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.CartSummaryResponse
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.models.dto.RootCategories
import za.co.woolworths.financial.services.android.models.dto.RootCategory
import za.co.woolworths.financial.services.android.models.network.AppContextProviderImpl
import za.co.woolworths.financial.services.android.models.network.NetworkConfig
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.*
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.response.DyHomePageViewModel
import za.co.woolworths.financial.services.android.ui.adapters.DepartmentAdapter
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.ProductListingFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.sub_category.SubCategoryFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.list.DepartmentExtensionFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.REQUEST_CODE
import za.co.woolworths.financial.services.android.util.location.Locator
import za.co.woolworths.financial.services.android.util.wenum.Delivery

@AndroidEntryPoint
class StandardDeliveryFragment : DepartmentExtensionFragment(R.layout.fragment_shop_department) {

    private lateinit var binding: FragmentShopDepartmentBinding

    private var locator: Locator? = null
    private var location: Location? = null
    private var rootCategoryCall: Call<RootCategories>? = null
    private var mDepartmentAdapter: DepartmentAdapter? = null
    private var isFragmentVisible: Boolean = false
    private var parentFragment: ShopFragment? = null
    private var localPlaceId: String? = null
    private var dyHomePageViewModel: DyHomePageViewModel? = null

    companion object {
        var DEPARTMENT_LOGIN_REQUEST = 1717
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentShopDepartmentBinding.bind(view)

        view.viewTreeObserver?.addOnWindowFocusChangeListener { hasFocus ->
            onWindowFocusChanged(
                hasFocus
            )
        }
        val parentFragment =
            (activity as? BottomNavigationActivity)?.currentFragment as? ShopFragment
        if (parentFragment?.getCurrentFragmentIndex() == ShopFragment.SelectedTabIndex.STANDARD_TAB.index) {
            initView()
        }
    }

    private fun dyHomePageViewModel() {
        dyHomePageViewModel = ViewModelProvider(this)[DyHomePageViewModel::class.java]
    }

    private fun prepareDynamicYieldRequestEvent() {
        val config = NetworkConfig(AppContextProviderImpl())
        val dyData = ArrayList<String>()
        val device = Device(Utils.IPAddress, config.getDeviceModel())
        val page = Page(dyData, Utils.MOBILE_LANDING_PAGE, Utils.HOME_PAGE, null, null)
        val context = Context(device, page, Utils.DY_CHANNEL)
        val options = Options(true)
        val homePageRequestEvent = HomePageRequestEvent(null, null, context, options)
        dyHomePageViewModel?.createDyRequest(homePageRequestEvent)
    }

    override fun noConnectionLayout(isVisible: Boolean) {
        binding.incConnectionLayout?.root?.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    fun initView() {
        locator = (activity as? AppCompatActivity)?.let { Locator(it) }

        parentFragment = (activity as? BottomNavigationActivity)?.currentFragment as? ShopFragment
        setUpRecyclerView(mutableListOf())
        setListener()
        localPlaceId = KotlinUtils.getPreferredPlaceId()

        if (isFragmentVisible) {
            initializeRootCategoryList()
        }
        AppConfigSingleton.dynamicYieldConfig?.apply {
            if (isDynamicYieldEnabled == true) {
                dyHomePageViewModel()
                prepareDynamicYieldRequestEvent()
            }
        }
    }

    private fun initializeRootCategoryList() {
        if (parentFragment?.getCategoryResponseData()?.rootCategories != null) bindDepartment() else executeDepartmentRequest(
            mDepartmentAdapter,
            parentFragment,
            location)
    }

    private fun onWindowFocusChanged(hasFocus: Boolean) {
        if (!hasFocus) {
            return
        }

        if (context != null && !Utils.isLocationEnabled(context)) {
            onProviderDisabled()
        }
    }

    private fun setListener() {
        binding.incConnectionLayout.btnRetry.setOnClickListener {
            if (networkConnectionStatus()) {
                executeDepartmentRequest(mDepartmentAdapter, parentFragment, location)
            }
        }
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
            binding.rclDepartment?.apply {
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
                bundle.putBoolean(AppConstant.Keys.EXTRA_SEND_DELIVERY_DETAILS_PARAMS,
                    arguments?.getBoolean(AppConstant.Keys.EXTRA_SEND_DELIVERY_DETAILS_PARAMS,
                        false) ?: false)
                bundle.putString(SubCategoryFragment.KEY_ARGS_VERSION, version)
                bundle.putBoolean(
                    SubCategoryFragment.KEY_ARGS_IS_LOCATION_ENABLED,
                    if (context != null) Utils.isLocationEnabled(context) else false
                )
                bundle.putBoolean(
                    AppConstant.Keys.EXTRA_SEND_DELIVERY_DETAILS_PARAMS,
                    arguments?.getBoolean(AppConstant.Keys.EXTRA_SEND_DELIVERY_DETAILS_PARAMS,
                        false) ?: false
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
                sendDeliveryDetails = arguments?.getBoolean(AppConstant.Keys.EXTRA_SEND_DELIVERY_DETAILS_PARAMS,
                    false)
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

    fun scrollToTop() {
        if (::binding.isInitialized) {
            binding.rclDepartment?.smoothScrollToPosition(0)
        }
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

    fun reloadRequest() {
        executeDepartmentRequest(mDepartmentAdapter, parentFragment, location)
    }
}
