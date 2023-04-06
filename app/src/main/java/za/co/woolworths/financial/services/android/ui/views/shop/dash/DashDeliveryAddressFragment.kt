package za.co.woolworths.financial.services.android.ui.views.shop.dash

import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentDashDeliveryBinding
import com.google.gson.Gson
import com.skydoves.balloon.balloon
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IProductListing
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.geolocation.GeoUtils
import za.co.woolworths.financial.services.android.geolocation.viewmodel.UnSellableItemsLiveData
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.shop.Banner
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.ui.activities.WStockFinderActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.INDEX_CART
import za.co.woolworths.financial.services.android.ui.adapters.holder.RecyclerViewViewHolderItems
import za.co.woolworths.financial.services.android.ui.adapters.shop.dash.DashDeliveryAdapter
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment.Companion.newInstance
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.ProductListingFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.ShopFragment
import za.co.woolworths.financial.services.android.ui.views.AddedToCartBalloonFactory
import za.co.woolworths.financial.services.android.ui.views.CustomBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.ui.views.ToastFactory
import za.co.woolworths.financial.services.android.ui.views.UnsellableItemsBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ProductListingFindInStoreNoQuantityFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.SelectYourQuantityFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_EXPECTATION_FAILED_502
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.REQUEST_CODE_QUERY_INVENTORY_FOR_STORE
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.REQUEST_CODE_QUERY_STORE_FINDER
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.SET_DELIVERY_LOCATION_REQUEST_CODE
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.getAnonymousUserLocationDetails
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.getDeliveryType
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.saveAnonymousUserLocationDetails
import za.co.woolworths.financial.services.android.util.analytics.AnalyticsManager
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import za.co.woolworths.financial.services.android.viewmodels.shop.ShopViewModel
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.*

@AndroidEntryPoint
class DashDeliveryAddressFragment : Fragment(R.layout.fragment_dash_delivery), IProductListing,
    View.OnClickListener, OnDemandNavigationListener, OnDashLandingNavigationListener {

    private val viewModel: ShopViewModel by viewModels()

    private lateinit var binding: FragmentDashDeliveryBinding
    private lateinit var dashDeliveryAdapter: DashDeliveryAdapter
    private var isQuickShopClicked = false
    private var isUnSellableItemsRemoved: Boolean? = false
    private var mStoreId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dashDeliveryAdapter =
            DashDeliveryAdapter(
                requireContext(), onDemandNavigationListener = this,
                dashLandingNavigationListener = this, this
            )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDashDeliveryBinding.bind(view)

        if (!isVisible) {
            return
        }
        initViews()
    }

    fun initViews() {
        addFragmentListner()
        isUnSellableItemsRemoved()

        val fulfillmentDetails = getDeliveryType() // fulfillment details of signin or signout user.
        if (fulfillmentDetails?.address?.placeId != null) {
            // User don't have location.
            // Now check if application class response has deliverable or local object of validatePlace has deliverable.
            // Continue with that object which has deliverable.
            val validatePlace =
                if (WoolworthsApplication.getDashBrowsingValidatePlaceDetails()?.onDemand?.deliverable == true)
                    WoolworthsApplication.getDashBrowsingValidatePlaceDetails()
                else if (WoolworthsApplication.getValidatePlaceDetails()?.onDemand?.deliverable == true)
                    WoolworthsApplication.getValidatePlaceDetails()
                else
                    WoolworthsApplication.getDashBrowsingValidatePlaceDetails()
                        ?: WoolworthsApplication.getValidatePlaceDetails()

            if (validatePlace == null) {
                // This means user has location but validatePlace response is not updated or is null.
                // So call validate place API again.
                subscribeToObservers()
                callValidatePlace(fulfillmentDetails.address?.placeId)
                return
            }
            if (validatePlace.onDemand != null && validatePlace.onDemand?.deliverable == true) {
                // Show categories.
                setupRecyclerView()
                initData()
            } else {
                // user has location but don't have Dash deliverable.
                showChangeLocationScreen() // show change Address screen
            }
        } else {
            //User who don't have location.
            showSetAddressScreen() // show set Address screen
        }
    }

    private fun addFragmentListner() {
        setFragmentResultListener(CustomBottomSheetDialogFragment.DIALOG_BUTTON_CLICK_RESULT) { _, _ ->
            // As User selects to change the delivery location. So we will call confirm place API and will change the users location.
            isQuickShopClicked = true
            val placeId = if (WoolworthsApplication.getDashBrowsingValidatePlaceDetails() != null)
                WoolworthsApplication.getDashBrowsingValidatePlaceDetails()?.placeDetails?.placeId
            else WoolworthsApplication.getValidatePlaceDetails()?.placeDetails?.placeId
            callValidatePlace(placeId)
        }
    }

    private fun callValidatePlace(placeId: String?) {
        if (placeId.isNullOrEmpty())
            return
        callValidatePlaceApi()
        viewModel.getValidateLocationResponse(placeId)
    }

    private fun callConfirmPlace() {
        viewModel.callConfirmPlace(KotlinUtils.getConfirmLocationRequest(Delivery.DASH))
    }

    private fun showSearchBar() {
        if (this.parentFragment is ShopFragment && KotlinUtils.browsingDeliveryType == Delivery.DASH)
            (this.parentFragment as ShopFragment).showSearchAndBarcodeUi() // show search bar.
    }

    private fun hideSearchBar() {
        if (this.parentFragment is ShopFragment && KotlinUtils.browsingDeliveryType == Delivery.DASH)
            (this.parentFragment as ShopFragment).hideSerachAndBarcodeUi() // hide search bar.
    }

    private fun showSetAddressScreen() {
        hideSearchBar()
        binding.layoutDashSetAddress?.root?.visibility = View.VISIBLE
        binding.layoutDashSetAddress.imgView?.setImageResource(R.drawable.img_dash_delivery)
        binding.layoutDashSetAddress.txtDashTitle?.text = getString(R.string.dash_delivery_msg)
        binding.layoutDashSetAddress.txtDashSubTitle?.text = getString(R.string.dash_delivery_title)
        binding.layoutDashSetAddress.btnDashSetAddress?.text = getString(R.string.set_address)
        binding.layoutDashSetAddress.btnDashSetAddress?.setOnClickListener(this)
    }

    private fun showChangeLocationScreen() {
        hideSearchBar()
        binding.layoutDashSetAddress?.root?.visibility = View.VISIBLE
        binding.layoutDashSetAddress.imgView?.setImageResource(R.drawable.location_disabled)
        binding.layoutDashSetAddress.txtDashTitle?.text = getString(R.string.no_location_title)
        binding.layoutDashSetAddress.txtDashSubTitle?.text = getString(R.string.no_location_desc)
        binding.layoutDashSetAddress.btnDashSetAddress?.text = getString(R.string.change_location)
        binding.layoutDashSetAddress.btnDashSetAddress?.setOnClickListener(this)
    }

    private fun initData() {
        showSearchBar()
        when {
            // Both API data available
            viewModel.isDashCategoriesAvailable.value == true &&
                    viewModel.isOnDemandCategoriesAvailable.value == true -> {

                // set data to views
                binding.layoutDashSetAddress?.root?.visibility = View.GONE
                binding.progressBar?.visibility = View.GONE
                dashDeliveryAdapter.setData(
                    viewModel.onDemandCategories.value?.peekContent()?.data?.onDemandCategories,
                    viewModel.dashLandingDetails.value?.peekContent()?.data?.productCatalogues
                )
            }
            // Either of API data available
            viewModel.isDashCategoriesAvailable.value == true ||
                    viewModel.isOnDemandCategoriesAvailable.value == true -> {
                // set data to views
                binding.layoutDashSetAddress?.root?.visibility = View.GONE

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
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        binding.layoutDashSetAddress?.root?.visibility = View.GONE
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
                        binding.progressBar.visibility = View.GONE
                    }
                    Status.ERROR -> {
                        binding.progressBar.visibility = View.GONE
                        showErrorView(requireContext().getString(resource.message), resource.data)
                    }
                }
            }
        }

        // Root Category API
        viewModel.onDemandCategories.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        binding.layoutDashSetAddress?.root?.visibility = View.GONE
                        if (viewModel.isDashCategoriesAvailable.value == true) {
                            dashDeliveryAdapter.setData(
                                resource.data?.onDemandCategories,
                                viewModel.dashLandingDetails.value?.peekContent()?.data?.productCatalogues,
                            )
                        }
                        binding.progressBar.visibility = View.GONE
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
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }

        // Inventory For Store API
        viewModel.inventorySkuForStore.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        binding.layoutDashSetAddress?.root?.visibility = View.GONE
                        binding.progressBar.visibility = View.GONE
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
                        binding.progressBar.visibility = View.GONE
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

        // confirm place API.
        viewModel.confirmPlaceDetails.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { resource ->
                val response = resource.data
                when (response?.httpCode) {
                    AppConstant.HTTP_OK -> {
                        if (SessionUtilities.getInstance().isUserAuthenticated) {
                            Utils.savePreferredDeliveryLocation(ShoppingDeliveryLocation(response.orderSummary?.fulfillmentDetails))
                            if (getAnonymousUserLocationDetails() != null)
                                KotlinUtils.clearAnonymousUserLocationDetails()
                        } else {
                            saveAnonymousUserLocationDetails(ShoppingDeliveryLocation(response.orderSummary?.fulfillmentDetails))
                        }
                        val savedPlaceId =
                            getDeliveryType()?.address?.placeId
                        KotlinUtils.apply {
                            response.orderSummary?.fulfillmentDetails?.address?.placeId.let { responsePlaceId ->
                                this.placeId = responsePlaceId
                                isLocationSame = responsePlaceId.equals(savedPlaceId)
                                isDeliveryLocationTabCrossClicked =
                                    responsePlaceId.equals(savedPlaceId)
                                isCncTabCrossClicked = responsePlaceId.equals(savedPlaceId)
                                isDashTabCrossClicked = responsePlaceId.equals(savedPlaceId)
                            }
                        }

                        val browsingPlaceDetails =
                            WoolworthsApplication.getDashBrowsingValidatePlaceDetails()
                        WoolworthsApplication.setValidatedSuburbProducts(browsingPlaceDetails)
                        // set latest response to browsing data.
                        WoolworthsApplication.setCncBrowsingValidatePlaceDetails(
                            browsingPlaceDetails
                        )
                        if (this.parentFragment is ShopFragment) {
                            (this.parentFragment as ShopFragment).setDeliveryView() // update main location UI.
                        }
                        addToCart(viewModel.addItemToCart.value) // This will again call addToCart
                    }
                }
            }
        }

        // Add item to cart API when quantity dialog clicked.
        viewModel.addItemToCartResp.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { resource ->
                val response = resource.data
                when (resource.status) {
                    Status.LOADING ->
                        binding.progressBar.visibility = View.VISIBLE
                    Status.ERROR -> {
                        binding.progressBar.visibility = View.GONE
                        // Preferred Delivery Location has been reset on server
                        // As such, we give the user the ability to set their location again
                        val addToCartList = response?.data
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

                        when (response?.httpCode) {
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

                            HTTP_EXPECTATION_FAILED_502 -> {
                                if (response.response.code == AppConstant.RESPONSE_ERROR_CODE_1235) {
                                    response.response.desc?.let { it1 ->
                                        KotlinUtils.showQuantityLimitErrror(
                                            activity?.supportFragmentManager,
                                            it1,
                                            "",
                                            context
                                        )
                                    }
                                }
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
                    else -> {
                        binding.progressBar.visibility = View.GONE
                        when (response?.httpCode) {
                            AppConstant.HTTP_OK -> {
                                when {
                                    (KotlinUtils.isDeliveryOptionClickAndCollect() || KotlinUtils.isDeliveryOptionDash()) &&
                                            response.data?.get(0)?.productCountMap?.quantityLimit?.foodLayoutColour != null -> {

                                        response.data?.get(0)?.productCountMap?.let {
                                            viewModel.addItemToCart.value?.quantity?.let { count ->
                                                ToastFactory.showItemsLimitToastOnAddToCart(
                                                    binding.rvDashDelivery,
                                                    it,
                                                    requireActivity(),
                                                    count
                                                )
                                            }
                                        }
                                    }
                                    else -> {
                                        val addToCartBalloon by balloon<AddedToCartBalloonFactory>()
                                        val bottomView =
                                            (requireActivity() as? BottomNavigationActivity)?.bottomNavigationById
                                        val buttonView: Button =
                                            addToCartBalloon.getContentView()
                                                .findViewById(R.id.btnView)
                                        val tvAddedItem: TextView =
                                            addToCartBalloon.getContentView()
                                                .findViewById(R.id.tvAddedItem)
                                        val quantityAdded =
                                            viewModel.addItemToCart.value?.quantity?.toString()
                                        val quantityDesc =
                                            "$quantityAdded ITEM${if ((viewModel.addItemToCart.value?.quantity ?: 0) >= 1) "" else "s"}"
                                        tvAddedItem.text = quantityDesc

                                        buttonView.setOnClickListener {
                                            openCartActivity()
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
        }

        // Find in store API.
        viewModel.productStoreFinder.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { resource ->
                val response = resource.data
                when (response?.httpCode) {
                    AppConstant.HTTP_OK -> {
                        if ((response.Locations?.size ?: 0) > 0) {
                            WoolworthsApplication.getInstance()?.wGlobalState?.storeDetailsArrayList =
                                response.Locations
                            val openStoreFinder = Intent(
                                WoolworthsApplication.getAppContext(),
                                WStockFinderActivity::class.java
                            ).putExtra(
                                AppConstant.Keys.EXTRA_PRODUCT_NAME,
                                viewModel.productList.value?.productName
                            )
                            requireActivity().apply {
                                startActivity(openStoreFinder)
                                overridePendingTransition(
                                    R.anim.slide_up_anim,
                                    R.anim.stay
                                )
                            }
                        } else {
                            Utils.displayValidationMessage(
                                requireActivity(),
                                CustomPopUpWindow.MODAL_LAYOUT.NO_STOCK,
                                ""
                            )
                        }
                    }
                    AppConstant.HTTP_SESSION_TIMEOUT_440 -> {
                        SessionUtilities.getInstance()
                            .setSessionState(SessionDao.SESSION_STATE.INACTIVE)
                        ScreenManager.presentSSOSignin(
                            requireActivity(),
                            REQUEST_CODE_QUERY_STORE_FINDER
                        )
                    }
                    else -> response?.response?.desc?.let { desc ->
                        Utils.displayValidationMessage(
                            WoolworthsApplication.getAppContext(),
                            CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                            desc
                        )
                    }
                }
            }
        }

    }

    private fun openCartActivity() {
        (activity as? BottomNavigationActivity)?.apply {
            navigateToTabIndex(INDEX_CART, null)
        }
        callValidatePlaceApi()
    }

    private fun callValidatePlaceApi() {
        // Validate Place API
        viewModel.validatePlaceDetails.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        resource.data?.validatePlace?.let { validatePlaceResponse ->
                            WoolworthsApplication.setDashBrowsingValidatePlaceDetails(
                                validatePlaceResponse
                            )
                            if (isQuickShopClicked) {
                                // This is for for add to cart clicked for quick shop functionality.
                                isQuickShopClicked = false
                                val unsellableList =
                                    validatePlaceResponse.onDemand?.unSellableCommerceItems
                                if (!unsellableList.isNullOrEmpty() && isUnSellableItemsRemoved == false) {
                                    // show unsellable items
                                    navigateToUnsellableItemsFragment(unsellableList as ArrayList<UnSellableCommerceItem>)
                                } else
                                    callConfirmPlace()
                            } else {
                                initViews()
                            }
                        }
                        binding.progressBar.visibility = View.GONE
                    }
                    Status.ERROR -> {
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun isUnSellableItemsRemoved() {
        UnSellableItemsLiveData.observe(viewLifecycleOwner) {
            isUnSellableItemsRemoved = it
            if (isUnSellableItemsRemoved == true && ((activity as? BottomNavigationActivity)?.mNavController?.currentFrag as? ShopFragment)?.getCurrentFragmentIndex() == ShopFragment.SelectedTabIndex.DASH_TAB.index) {
                callConfirmPlace()
                UnSellableItemsLiveData.value = false
            }
        }
    }

    private fun navigateToUnsellableItemsFragment(
        unSellableCommerceItems: ArrayList<UnSellableCommerceItem>,
    ) {
        val unsellableItemsBottomSheetDialog =
            UnsellableItemsBottomSheetDialog.newInstance(
                unSellableCommerceItems,
                Delivery.DASH.name
            )
        unsellableItemsBottomSheetDialog.show(
            requireActivity().supportFragmentManager,
            UnsellableItemsBottomSheetDialog::class.java.simpleName
        )
    }

    private fun setupRecyclerView() {
        binding.rvDashDelivery?.apply {
            adapter = dashDeliveryAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        }
    }

    fun scrollToTop() {
        if (::binding.isInitialized) {
            binding?.rvDashDelivery?.scrollToPosition(0)
        }
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
            ProductDetailsFragment.STR_BRAND_HEADER to productList.brandHeaderDescription,
            ProductDetailsFragment.IS_BROWSING to true
        )
        (activity as? BottomNavigationActivity)?.apply {
            Utils.updateStatusBarBackground(this)
            pushFragment(productDetailsFragment)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        subscribeToObservers()
        when (requestCode) {
            REQUEST_CODE_QUERY_INVENTORY_FOR_STORE, SET_DELIVERY_LOCATION_REQUEST_CODE -> {
                if (resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue() || resultCode == Activity.RESULT_OK) {
                    // check if user has any location.
                    if (Utils.getPreferredDeliveryLocation() != null) {
                        val browsingPlaceDetails =
                            WoolworthsApplication.getDashBrowsingValidatePlaceDetails()
                        WoolworthsApplication.setValidatedSuburbProducts(browsingPlaceDetails)
                        // set latest response to browsing data.
                        WoolworthsApplication.setCncBrowsingValidatePlaceDetails(
                            browsingPlaceDetails
                        )
                        if (this.parentFragment is ShopFragment) {
                            (this.parentFragment as ShopFragment).setDeliveryView() // update main location UI.
                        }
                        addToCart(viewModel.addItemToCart.value) // This will again call addToCart
                    } else {
                        // request cart summary to get the user's location.
                        requestCartSummary()
                    }
                }
            }

            REQUEST_CODE_QUERY_STORE_FINDER -> {
                if (resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()) {
                    queryStoreFinderProductByFusedLocation(null)
                }
            }
            BundleKeysConstants.REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    initViews()
                viewModel.getOnDemandCategories()
                 viewModel.getDashLandingDetails()
               }
            }
        }
    }

    override fun queryInventoryForStore(
        fulfilmentTypeId: String,
        addItemToCart: AddItemToCart?,
        productList: ProductList,
    ) {
        viewModel.setAddItemToCart(addItemToCart)
        mStoreId =
            fulfilmentTypeId.let { it1 -> RecyclerViewViewHolderItems.getFulFillmentStoreId(it1) }

        if (!SessionUtilities.getInstance().isUserAuthenticated) {
            ScreenManager.presentSSOSigninActivity(
                activity,
                REQUEST_CODE_QUERY_INVENTORY_FOR_STORE,
                true
            )
            return
        }

        viewModel.setProductList(productList)

        if (productList.isLiquor == true && !KotlinUtils.isCurrentSuburbDeliversLiquor() && !KotlinUtils.isLiquorModalShown()) {
            KotlinUtils.setLiquorModalShown()
            showLiquorDialog()
            AppConfigSingleton.productItemForLiquorInventory = productList
            return
        }

        // Now first check for if delivery location and browsing location is same.
        // if same no issues. If not then show changing delivery location popup.
        if (!getDeliveryType()?.deliveryType.equals(Delivery.DASH.type)) {
            KotlinUtils.showChangeDeliveryTypeDialog(
                requireContext(), requireFragmentManager(),
                KotlinUtils.browsingDeliveryType
            )
            return
        }
        addToCart(addItemToCart)
    }

    private fun requestCartSummary() {
        binding.progressBar?.visibility = View.VISIBLE
        GetCartSummary().getCartSummary(object : IResponseListener<CartSummaryResponse> {
            override fun onSuccess(response: CartSummaryResponse?) {
                binding.progressBar?.visibility = View.GONE
                when (response?.httpCode) {
                    AppConstant.HTTP_OK -> {
                        // If user have location then add to cart else go to geoLocation Flow.
                        if (Utils.getPreferredDeliveryLocation() != null) {
                            if (parentFragment is ShopFragment) {
                                (parentFragment as ShopFragment).setDeliveryView() // update main location UI.
                            }
                            addToCart(viewModel.addItemToCart.value) // This will again call addToCart
                        } else
                            onSetNewLocation()
                    }
                }
            }

            override fun onFailure(error: Throwable?) {
                binding.progressBar?.visibility = View.GONE
            }
        })
    }

    private fun onSetNewLocation() {
        activity?.apply {
            KotlinUtils.presentEditDeliveryGeoLocationActivity(
                this,
                ProductListingFragment.SET_DELIVERY_LOCATION_REQUEST_CODE,
                KotlinUtils.getPreferredDeliveryType(),
                GeoUtils.getPlaceId()
            )
        }
    }

    private fun addToCart(addItemToCart: AddItemToCart?) {
        val fulfilmentTypeId = AppConfigSingleton.quickShopDefaultValues?.foodFulfilmentTypeId
        mStoreId =
            fulfilmentTypeId?.let { it1 -> RecyclerViewViewHolderItems.getFulFillmentStoreId(it1) }
                ?: ""

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
    }

    override fun queryStoreFinderProductByFusedLocation(location: Location?) {
        val globalState = WoolworthsApplication.getInstance().wGlobalState
        with(globalState) {
            viewModel.callStoreFinder(
                sku = viewModel.productList.value?.sku ?: "",
                startRadius = startRadius.toString(),
                endRadius = endRadius.toString()
            )
        }
    }

    override fun openChangeFulfillmentScreen() {
        KotlinUtils.presentEditDeliveryGeoLocationActivity(
            requireActivity(),
            BundleKeysConstants.REQUEST_CODE,
            Delivery.DASH,
            getDeliveryType()?.address?.placeId ?: ""
        )
    }

    override fun showLiquorDialog() {
        TODO("Not yet implemented")
    }

    override fun openBrandLandingPage() {
        TODO("Not yet implemented")
    }

    override fun onDemandNavigationClicked(view: View?, categoryItem: RootCategory) {
        setEventsForCategoryClick(categoryItem)
        (requireActivity() as? BottomNavigationActivity)?.apply {
            pushFragment(
                ProductListingFragment.newInstance(
                    searchType = ProductsRequestParams.SearchType.NAVIGATE,
                    sub_category_name = categoryItem.categoryName,
                    searchTerm = categoryItem.dimValId,
                    isBrowsing = true,
                    sendDeliveryDetails = arguments?.getBoolean(AppConstant.Keys.ARG_SEND_DELIVERY_DETAILS,
                        false) == true
                )
            )
        }
    }


    private fun setEventsForCategoryClick(categoryItem: RootCategory) {
        if (getDeliveryType()?.deliveryType == null) {
            return
        }

        val categoryParamsParams = Bundle()
        val categoryId = categoryItem?.categoryId?.toInt()?.plus(1)
        val slotName = AppConstant.QUICK_LINK.plus(categoryId)

        categoryParamsParams?.apply {

            putString(
                FirebaseManagerAnalyticsProperties.PropertyNames.CONTENT_TYPE,
                FirebaseManagerAnalyticsProperties.PropertyValues.DASH_MENU_CLICK
            )
            putString(
                FirebaseManagerAnalyticsProperties.PropertyNames.CATEGORY_NAME,
                FirebaseManagerAnalyticsProperties.PropertyValues.DASH_CATEGORY_NAME
            )

            putString(
                FirebaseManagerAnalyticsProperties.PropertyNames.CONTENT_NAME,
                categoryItem?.categoryName
            )

            putString(
                FirebaseManagerAnalyticsProperties.PropertyNames.CONTENT_SLOT,
                slotName
            )
        }

        AnalyticsManager.logEvent(
            FirebaseManagerAnalyticsProperties.DASH_SELECT_CONTENT,
            categoryParamsParams
        )
    }

    override fun onDashLandingNavigationClicked(view: View?, item: Banner) {
        (requireActivity() as? BottomNavigationActivity)?.apply {
            pushFragment(
                ProductListingFragment.newInstance(
                    searchType = ProductsRequestParams.SearchType.NAVIGATE,
                    sub_category_name = item.displayName,
                    searchTerm = item.navigationState,
                    isBrowsing = true,
                    sendDeliveryDetails = arguments?.getBoolean(AppConstant.Keys.ARG_SEND_DELIVERY_DETAILS,
                        false) == true
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