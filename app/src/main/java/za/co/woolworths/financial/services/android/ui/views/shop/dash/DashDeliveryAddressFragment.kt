package za.co.woolworths.financial.services.android.ui.views.shop.dash

import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_dash_delivery.*
import kotlinx.android.synthetic.main.layout_dash_set_address_fragment.*
import za.co.woolworths.financial.services.android.contracts.IProductListing
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.models.dto.RootCategory
import za.co.woolworths.financial.services.android.models.dto.shop.Banner
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.adapters.shop.dash.DashDeliveryAdapter
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.ProductListingFragment
import za.co.woolworths.financial.services.android.util.BundleKeysConstants
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.getAnonymousUserLocationDetails
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import za.co.woolworths.financial.services.android.viewmodels.shop.ShopViewModel

@AndroidEntryPoint
class DashDeliveryAddressFragment : Fragment(R.layout.fragment_dash_delivery), IProductListing,
    View.OnClickListener, OnDemandNavigationListener, OnDashLandingNavigationListener {

    private val viewModel: ShopViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    private lateinit var dashDeliveryAdapter: DashDeliveryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dashDeliveryAdapter = DashDeliveryAdapter(requireContext(), onDemandNavigationListener = this,
            dashLandingNavigationListener = this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isVisible) {
            return
        }
        initViews()
    }

    fun initViews() {
        val savedLocation = Utils.getPreferredDeliveryLocation()
        if (!SessionUtilities.getInstance().isUserAuthenticated) {
            val anonymousUserLocation = getAnonymousUserLocationDetails()?.fulfillmentDetails
            if (anonymousUserLocation != null) {
                // AnonymousUser who has location
                val validatePlace = WoolworthsApplication.getValidatePlaceDetails()
                if (validatePlace?.onDemand != null && validatePlace?.onDemand?.deliverable == true) {
                    // Show categories.
                    setupRecyclerView()
                    initData()
                } else {
                    // AnonymousUser has location but don't have Dash deliverable.
                    showSetAddressScreen() // show set Address screen
                }
            } else {
                // AnonymousUser who don't have location.
                showSetAddressScreen() // show set Address screen
            }
        } else {
            // user logged in
            if (savedLocation?.fulfillmentDetails?.deliveryType.isNullOrEmpty()) {
                // logged in but don't have location
                showSetAddressScreen() // show set Address screen
            } else {
                // User Logged in and have location.
                val validatePlace = viewModel.getValidatePlaceResponse()
                    ?: WoolworthsApplication.getValidatePlaceDetails()
                if (validatePlace?.onDemand != null && validatePlace?.onDemand?.deliverable == true) {
                    // Show categories.
                    setupRecyclerView()
                    initData()
                } else {
                    // user has location but don't have Dash deliverable.
                    showSetAddressScreen() // show set Address screen
                }
            }
        }
    }

    private fun showSetAddressScreen() {
        layoutDashSetAddress?.visibility = View.VISIBLE
        btn_dash_set_address?.setOnClickListener(this)
    }

    private fun hideSetAddressScreen() {
        layoutDashSetAddress?.visibility = View.GONE
    }

    private fun initData() {
        when {
            // Both API data available
            viewModel.isDashCategoriesAvailable.value == true &&
                    viewModel.isOnDemandCategoriesAvailable.value == true -> {
                // set data to views
                layoutDashSetAddress?.visibility = View.GONE
                dashDeliveryAdapter.setData(
                    viewModel.onDemandCategories.value?.peekContent()?.data?.onDemandCategories,
                    viewModel.dashLandingDetails.value?.peekContent()?.data?.productCatalogues
                )
            }
            // Either of API data available
            viewModel.isDashCategoriesAvailable.value == true ||
                    viewModel.isOnDemandCategoriesAvailable.value == true -> {
                // set data to views
                layoutDashSetAddress?.visibility = View.GONE

                // Data will be set in observers when api successful/failure
                when (viewModel.isDashCategoriesAvailable.value) {
                    true -> viewModel.getOnDemandCategories()
                    else -> viewModel.getDashLandingDetails()
                }
            }
            else -> {
                viewModel.getOnDemandCategories()
                viewModel.getDashLandingDetails()
            }
        }

        subscribeToObservers()
    }

    private fun subscribeToObservers() {

        //Dash API.
        viewModel.dashLandingDetails.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        //TODO need to confirm loading screens between shimmer view or progressbar
                        progressBar.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        layoutDashSetAddress?.visibility = View.GONE
                        if (viewModel.isOnDemandCategoriesAvailable.value == true) {
                            dashDeliveryAdapter.setData(
                                viewModel.onDemandCategories.value?.peekContent()?.data?.onDemandCategories,
                                resource.data?.productCatalogues
                            )
                        }
                        progressBar.visibility = View.GONE
                    }
                    Status.ERROR -> {
                        progressBar.visibility = View.GONE
                        showErrorView(resource.message, resource.data)
                    }
                }
            }
        }

        // Root Category API
        viewModel.onDemandCategories.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        progressBar.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        layoutDashSetAddress?.visibility = View.GONE
                        if (viewModel.isDashCategoriesAvailable.value == true) {
                            dashDeliveryAdapter.setData(
                                resource.data?.onDemandCategories,
                                viewModel.dashLandingDetails.value?.peekContent()?.data?.productCatalogues,
                            )
                        }
                        progressBar.visibility = View.GONE
                    }
                    Status.ERROR -> {
                        //Ignore error view for On Demand Categories,
                        // Instead remove on demand category block from list
                        // i.e. pass null in setData
                        if (viewModel.isDashCategoriesAvailable.value == true) {
                            dashDeliveryAdapter.setData(
                                null,
                                viewModel.dashLandingDetails.value?.peekContent()?.data?.productCatalogues,
                            )
                        }
                        progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        rvDashDelivery?.apply {
            adapter = dashDeliveryAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        }
    }

    fun scrollToTop() {
        rvDashDelivery?.scrollToPosition(0)
    }

    private fun navigateToConfirmAddressScreen() {
        // navigate to confirm address screen
        activity?.apply {
            KotlinUtils.presentEditDeliveryGeoLocationActivity(
                this,
                BundleKeysConstants.DASH_SET_ADDRESS_REQUEST_CODE,
                Delivery.DASH,
                null,
                true
            )
        }
    }

    private fun showErrorView(message: String?, data: Any?) {
        //TODO: get error screens from UI/UX team
    }

    // Product Items interface
    override fun openProductDetailView(productList: ProductList) {

    }

    override fun queryInventoryForStore(
        fulfilmentTypeId: String,
        addItemToCart: AddItemToCart?,
        productList: ProductList,
    ) {
        TODO("Not yet implemented")
    }

    override fun addFoodProductTypeToCart(addItemToCart: AddItemToCart?) {
        TODO("Not yet implemented")
    }

    override fun queryStoreFinderProductByFusedLocation(location: Location?) {
        TODO("Not yet implemented")
    }

    override fun showLiquorDialog() {
        TODO("Not yet implemented")
    }

    override fun openBrandLandingPage() {
        TODO("Not yet implemented")
    }

    override fun onDemandNavigationClicked(view: View?, categoryItem: RootCategory) {
        (requireActivity() as? BottomNavigationActivity)?.apply {
            pushFragment(
                ProductListingFragment.newInstance(
                    searchType = ProductsRequestParams.SearchType.NAVIGATE,
                    sub_category_name = categoryItem.categoryName,
                    searchTerm = categoryItem.dimValId
                )
            )
        }
    }

    override fun onDashLandingNavigationClicked(view: View?, item: Banner) {
        (requireActivity() as? BottomNavigationActivity)?.apply {
            pushFragment(
                ProductListingFragment.newInstance(
                    searchType = ProductsRequestParams.SearchType.NAVIGATE,
                    sub_category_name = item.displayName,
                    searchTerm = item.navigationState
                )
            )
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_dash_set_address -> {
                navigateToConfirmAddressScreen()
            }
        }
    }

}