package za.co.woolworths.financial.services.android.ui.views.shop.dash

import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.google.gson.Gson
import com.skydoves.balloon.balloon
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_dash_delivery.*
import kotlinx.android.synthetic.main.grid_layout.*
import kotlinx.android.synthetic.main.layout_dash_set_address_fragment.*
import za.co.woolworths.financial.services.android.contracts.IProductListing
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.shop.Banner
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.adapters.holder.RecyclerViewViewHolderItems
import za.co.woolworths.financial.services.android.ui.adapters.shop.dash.DashDeliveryAdapter
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment.Companion.newInstance
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.ProductListingFragment
import za.co.woolworths.financial.services.android.ui.views.AddedToCartBalloonFactory
import za.co.woolworths.financial.services.android.ui.views.ToastFactory
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ProductListingFindInStoreNoQuantityFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.SelectYourQuantityFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.REQUEST_CODE_QUERY_INVENTORY_FOR_STORE
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.getAnonymousUserLocationDetails
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import za.co.woolworths.financial.services.android.viewmodels.shop.ShopViewModel
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.*

@AndroidEntryPoint
class DashDeliveryAddressFragment : Fragment(R.layout.fragment_dash_delivery), IProductListing,
    View.OnClickListener, OnDemandNavigationListener, OnDashLandingNavigationListener {

    private val viewModel: ShopViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    private lateinit var dashDeliveryAdapter: DashDeliveryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dashDeliveryAdapter = DashDeliveryAdapter(
            requireContext(), onDemandNavigationListener = this,
            dashLandingNavigationListener = this, this
        )
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
                    showChangeLocationScreen() // show change Address screen
                }
            }
        }
    }

    private fun showSetAddressScreen() {
        layoutDashSetAddress?.visibility = View.VISIBLE
        img_view?.setImageResource(R.drawable.img_dash_delivery)
        txt_dash_title?.text = getString(R.string.dash_delivery_msg)
        txt_dash_sub_title?.text = getString(R.string.dash_delivery_title)
        btn_dash_set_address?.text = getString(R.string.set_address)
        btn_dash_set_address?.setOnClickListener(this)
    }

    private fun showChangeLocationScreen() {
        layoutDashSetAddress?.visibility = View.VISIBLE
        img_view?.setImageResource(R.drawable.location_disabled)
        txt_dash_title?.text = getString(R.string.no_location_title)
        txt_dash_sub_title?.text = getString(R.string.no_location_desc)
        btn_dash_set_address?.text = getString(R.string.change_location)
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
                        } else {
                            dashDeliveryAdapter.setData(
                                null,
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

        // Inventory For Store API
        viewModel.inventorySkuForStore.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        progressBar.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        layoutDashSetAddress?.visibility = View.GONE
                        progressBar.visibility = View.GONE
                        val skuInventoryList = resource.data?.skuInventory
                        val addItemToCart = viewModel.addItemToCart.value
                        when {
                            skuInventoryList?.size == 0 ||
                                    skuInventoryList?.get(0)?.quantity == 0 -> {
                                addItemToCart?.catalogRefId?.let { skuId ->
                                    productOutOfStockErrorMessage(
                                        skuId
                                    )
                                }
                            }
                            skuInventoryList?.get(0)?.quantity == 1 -> {
                                addFoodProductTypeToCart(
                                    AddItemToCart(
                                        addItemToCart?.productId,
                                        addItemToCart?.catalogRefId,
                                        1
                                    )
                                )
                            }
                            else -> {
                                try {
                                    val cartItem = AddItemToCart(
                                        addItemToCart?.productId ?: "",
                                        addItemToCart?.catalogRefId ?: "",
                                        skuInventoryList?.get(0)?.quantity ?: 0
                                    )
                                    val selectYourQuantityFragment =
                                        SelectYourQuantityFragment.newInstance(
                                            cartItem,
                                            this@DashDeliveryAddressFragment
                                        )
                                    activity?.supportFragmentManager?.beginTransaction()?.apply {
                                        selectYourQuantityFragment.show(
                                            this,
                                            SelectYourQuantityFragment::class.java.simpleName
                                        )
                                    }
                                } catch (ex: IllegalStateException) {
                                    FirebaseManager.logException(ex)
                                }
                            }
                        }
                    }
                    Status.ERROR -> {
                        progressBar.visibility = View.GONE
                        resource.data?.response?.desc?.let { desc ->
                            Utils.displayValidationMessage(
                                activity,
                                CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                                desc
                            )
                            return@observe
                        }
                        onFailureHandler(Throwable(ConnectException()))
                    }
                }
            }
        }

        // Add item to cart API when quantity dialog clicked.
        viewModel.addItemToCartResp.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { resource ->
                val response = resource.data
                when (response?.httpCode) {
                    AppConstant.HTTP_OK -> {
                        // Preferred Delivery Location has been reset on server
                        // As such, we give the user the ability to set their location again
                        val addToCartList = response.data
                        addToCartList?.get(0)?.formexceptions?.get(0)?.let { formException ->
                            when {
                                formException.message.lowercase(Locale.getDefault())
                                    .contains("unfortunately this product is now out of stock, please try again tomorrow") -> {
                                    viewModel.addItemToCart.value?.catalogRefId?.let { catalogRefId ->
                                        productOutOfStockErrorMessage(
                                            catalogRefId
                                        )
                                    }
                                }
                                else -> {
                                    response.response.desc = formException.message
                                    Utils.displayValidationMessage(
                                        requireContext(),
                                        CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                                        response.response.desc
                                    )
                                }
                            }
                            return@observe
                        }

                        when {
                            KotlinUtils.isDeliveryOptionClickAndCollect() &&
                                    response.data?.get(0)?.productCountMap?.quantityLimit?.foodLayoutColour != null -> {

                                response.data?.get(0)?.productCountMap?.let {
                                    viewModel.addItemToCart.value?.quantity?.let { count ->
                                        ToastFactory.showItemsLimitToastOnAddToCart(
                                            productsRecyclerView,
                                            it,
                                            requireActivity(),
                                            count
                                        )
                                    }
                                }
                            }
                            else -> {
                                val addToCartBalloon by balloon(AddedToCartBalloonFactory::class)
                                val bottomView =
                                    (activity as? BottomNavigationActivity)?.bottomNavigationById
                                val buttonView: Button =
                                    addToCartBalloon.getContentView().findViewById(R.id.btnView)
                                val tvAddedItem: TextView = addToCartBalloon.getContentView()
                                    .findViewById(R.id.tvAddedItem)
                                val quantityAdded =
                                    viewModel.addItemToCart.value?.quantity?.toString()
                                val quantityDesc =
                                    "$quantityAdded ITEM${if (viewModel.addItemToCart.value?.quantity == 0) "" else "s"}"
                                tvAddedItem.text = quantityDesc

                                buttonView.setOnClickListener {
                                    //                                    openCartActivity()
                                    addToCartBalloon.dismiss()
                                }

                                bottomView?.let { bottomNavigationView ->
                                    addToCartBalloon.showAlignBottom(
                                        bottomNavigationView,
                                        0,
                                        16
                                    )
                                }
                                Handler(Looper.getMainLooper()).postDelayed({
                                    addToCartBalloon.dismiss()
                                }, 3000)
                            }
                        }
                    }

                    AppConstant.HTTP_EXPECTATION_FAILED_417 -> {
                        KotlinUtils.presentEditDeliveryGeoLocationActivity(
                            requireActivity(),
                            ProductListingFragment.SET_DELIVERY_LOCATION_REQUEST_CODE,
                            KotlinUtils.getPreferredDeliveryType(),
                            Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId
                        )
                    }
                    AppConstant.HTTP_SESSION_TIMEOUT_440 -> {
                        SessionUtilities.getInstance()
                            .setSessionState(SessionDao.SESSION_STATE.INACTIVE)
                        ScreenManager.presentSSOSignin(requireActivity())
                    }

                    else -> response?.response?.desc?.let { desc ->
                        Utils.displayValidationMessage(
                            requireContext(),
                            CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                            desc
                        )
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

    override fun openProductDetailView(productList: ProductList) {
        val productDetailsFragment = newInstance()
        productDetailsFragment.arguments = bundleOf(
            ProductDetailsFragment.STR_PRODUCT_LIST to Gson().toJson(productList),
            ProductDetailsFragment.STR_PRODUCT_CATEGORY to productList.productName,
            ProductDetailsFragment.STR_BRAND_HEADER to productList.brandHeaderDescription
        )
        (activity as? BottomNavigationActivity)?.apply {
            Utils.updateStatusBarBackground(this)
            pushFragment(productDetailsFragment)
        }
    }

    override fun queryInventoryForStore(
        fulfilmentTypeId: String,
        addItemToCart: AddItemToCart?,
        productList: ProductList,
    ) {

        val mStoreId =
            fulfilmentTypeId.let { it1 -> RecyclerViewViewHolderItems.getFulFillmentStoreId(it1) }

        if (!SessionUtilities.getInstance().isUserAuthenticated) {
            ScreenManager.presentSSOSignin(
                activity,
                REQUEST_CODE_QUERY_INVENTORY_FOR_STORE
            )
            return
        }

        if (productList.isLiquor == true && !KotlinUtils.isCurrentSuburbDeliversLiquor() && !KotlinUtils.isLiquorModalShown()) {
            KotlinUtils.setLiquorModalShown()
            showLiquorDialog()
            AppConfigSingleton.productItemForLiquorInventory = productList
            return
        }

        if (mStoreId.isEmpty()) {
            addItemToCart?.catalogRefId?.let { skuId -> productOutOfStockErrorMessage(skuId) }
            return
        }

        viewModel.setAddItemToCart(addItemToCart)
        viewModel.fetchInventorySkuForStore(
            mStoreId, addItemToCart?.catalogRefId
                ?: ""
        )
    }

    private fun onFailureHandler(error: Throwable) {
        activity?.let { activity ->
            when (error) {
                is ConnectException, is UnknownHostException -> {
                    ErrorHandlerView(activity).showToast(getString(R.string.no_connection))
                }
                else -> return
            }
        }
    }

    private fun productOutOfStockErrorMessage(skuId: String) {
        try {
            activity?.supportFragmentManager?.beginTransaction()?.apply {
                val productListingFindInStoreNoQuantityFragment =
                    ProductListingFindInStoreNoQuantityFragment.newInstance(
                        skuId,
                        this@DashDeliveryAddressFragment
                    )
                productListingFindInStoreNoQuantityFragment.show(
                    this,
                    ProductListingFindInStoreNoQuantityFragment::class.java.simpleName
                )
            }
        } catch (ex: IllegalStateException) {
            FirebaseManager.logException(ex)
        }
    }

    override fun addFoodProductTypeToCart(addItemToCart: AddItemToCart?) {
        val mAddItemsToCart = mutableListOf<AddItemToCart>()
        addItemToCart?.let { cartItem -> mAddItemsToCart.add(cartItem) }
        viewModel.callToAddItemsToCart(mAddItemsToCart)
        PostItemToCart().make(mAddItemsToCart, object : IResponseListener<AddItemToCartResponse> {
            override fun onSuccess(response: AddItemToCartResponse?) {
                activity?.apply {
                    when (response?.httpCode) {
                        AppConstant.HTTP_OK -> {
                            // Preferred Delivery Location has been reset on server
                            // As such, we give the user the ability to set their location again
                            val addToCartList = response.data
                            if (addToCartList != null && addToCartList.size > 0 && addToCartList[0].formexceptions != null) {
                                val formException = addToCartList[0].formexceptions[0]
                                if (formException != null) {
                                    if (formException.message.toLowerCase(Locale.getDefault())
                                            .contains("unfortunately this product is now out of stock, please try again tomorrow")
                                    ) {
                                        addItemToCart?.catalogRefId?.let { catalogRefId ->
                                            productOutOfStockErrorMessage(
                                                catalogRefId
                                            )
                                        }
                                    } else {
                                        response.response.desc = formException.message
                                        Utils.displayValidationMessage(
                                            this,
                                            CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                                            response.response.desc
                                        )
                                    }
                                    return
                                }
                            }
                            if (KotlinUtils.isDeliveryOptionClickAndCollect() && response.data[0]?.productCountMap?.quantityLimit?.foodLayoutColour != null) {
                                response.data[0]?.productCountMap?.let {
                                    addItemToCart?.quantity?.let { it1 ->
                                        ToastFactory.showItemsLimitToastOnAddToCart(
                                            productsRecyclerView,
                                            it,
                                            this,
                                            it1
                                        )
                                    }
                                }
                            } else {
                                val addToCartBalloon by balloon(AddedToCartBalloonFactory::class)
                                val bottomView =
                                    (activity as? BottomNavigationActivity)?.bottomNavigationById
                                val buttonView: Button =
                                    addToCartBalloon.getContentView().findViewById(R.id.btnView)
                                val tvAddedItem: TextView = addToCartBalloon.getContentView()
                                    .findViewById(R.id.tvAddedItem)
                                val quantityAdded = addItemToCart?.quantity?.toString()
                                val quantityDesc =
                                    "$quantityAdded ITEM${if (addItemToCart?.quantity == 0) "" else "s"}"
                                tvAddedItem.text = quantityDesc

                                buttonView.setOnClickListener {
//                                    openCartActivity()
                                    addToCartBalloon.dismiss()
                                }

                                bottomView?.let { bottomNavigationView ->
                                    addToCartBalloon.showAlignBottom(
                                        bottomNavigationView,
                                        0,
                                        16
                                    )
                                }
                                Handler().postDelayed({
                                    addToCartBalloon.dismiss()
                                }, 3000)
                            }
                        }

                        AppConstant.HTTP_EXPECTATION_FAILED_417 -> resources?.let {
                            activity?.apply {
                                KotlinUtils.presentEditDeliveryGeoLocationActivity(
                                    this,
                                    ProductListingFragment.SET_DELIVERY_LOCATION_REQUEST_CODE,
                                    KotlinUtils.getPreferredDeliveryType(),
                                    Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId
                                )
                            }
                        }
                        AppConstant.HTTP_SESSION_TIMEOUT_440 -> {
                            SessionUtilities.getInstance()
                                .setSessionState(SessionDao.SESSION_STATE.INACTIVE)
                            ScreenManager.presentSSOSignin(this)
                        }

                        else -> response?.response?.desc?.let { desc ->
                            Utils.displayValidationMessage(
                                this,
                                CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                                desc
                            )
                        }
                    }
                }
            }

            override fun onFailure(error: Throwable?) {
//                activity?.runOnUiThread { dismissProgressBar() }
            }
        })
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