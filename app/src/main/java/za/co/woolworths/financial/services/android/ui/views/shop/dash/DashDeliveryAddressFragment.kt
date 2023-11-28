package za.co.woolworths.financial.services.android.ui.views.shop.dash

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.text.buildSpannedString
import androidx.core.view.contains
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentDashDeliveryBinding
import com.awfs.coordination.databinding.LayoutInappOrderNotificationBinding
import com.google.gson.Gson
import com.skydoves.balloon.balloon
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.cart.view.SubstitutionChoice
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IProductListing
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.enhancedSubstitution.util.isEnhanceSubstitutionFeatureAvailable
import za.co.woolworths.financial.services.android.geolocation.GeoUtils
import za.co.woolworths.financial.services.android.geolocation.viewmodel.AddToCartLiveData
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmLocationResponseLiveData
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart
import za.co.woolworths.financial.services.android.models.dto.CartSummaryResponse
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.models.dto.RootCategory
import za.co.woolworths.financial.services.android.models.dto.UnSellableCommerceItem
import za.co.woolworths.financial.services.android.models.dto.dash.LastOrderDetailsResponse
import za.co.woolworths.financial.services.android.models.dto.shop.Banner
import za.co.woolworths.financial.services.android.models.dto.shop.ProductCatalogue
import za.co.woolworths.financial.services.android.models.network.Parameter
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.onecartgetstream.OCChatActivity
import za.co.woolworths.financial.services.android.onecartgetstream.service.DashChatMessageListeningService
import za.co.woolworths.financial.services.android.receivers.DashOrderReceiver
import za.co.woolworths.financial.services.android.receivers.DashOrderReceiverListener
import za.co.woolworths.financial.services.android.recommendations.data.response.request.CommonRecommendationEvent
import za.co.woolworths.financial.services.android.recommendations.data.response.request.Recommendation
import za.co.woolworths.financial.services.android.recommendations.data.response.request.RecommendationRequest
import za.co.woolworths.financial.services.android.recommendations.presentation.adapter.viewholder.MyRecycleViewHolder
import za.co.woolworths.financial.services.android.recommendations.presentation.viewmodel.RecommendationViewModel
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.ui.activities.WStockFinderActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.INDEX_CART
import za.co.woolworths.financial.services.android.ui.adapters.SelectQuantityAdapter
import za.co.woolworths.financial.services.android.ui.adapters.holder.RecyclerViewViewHolderItems
import za.co.woolworths.financial.services.android.ui.adapters.shop.dash.DashDeliveryAdapter
import za.co.woolworths.financial.services.android.ui.adapters.shop.dash.ProductCarouselItemViewHolder
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment.Companion.newInstance
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.ProductListingFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.usecase.Constants
import za.co.woolworths.financial.services.android.ui.fragments.shop.OrderDetailsFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.OrderTrackingWebViewActivity
import za.co.woolworths.financial.services.android.ui.fragments.shop.ShopFragment
import za.co.woolworths.financial.services.android.ui.views.AddedToCartBalloonFactory
import za.co.woolworths.financial.services.android.ui.views.CustomBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.ui.views.ToastFactory
import za.co.woolworths.financial.services.android.ui.views.UnsellableItemsBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ProductListingFindInStoreNoQuantityFragment
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_EXPECTATION_FAILED_502
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.REQUEST_CODE_QUERY_INVENTORY_FOR_STORE
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.REQUEST_CODE_QUERY_STORE_FINDER
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.SET_DELIVERY_LOCATION_REQUEST_CODE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants
import za.co.woolworths.financial.services.android.util.CustomTypefaceSpan
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.GetCartSummary
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.getDeliveryType
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.UnsellableUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.AnalyticsManager
import za.co.woolworths.financial.services.android.util.analytics.FirebaseAnalyticsEventHelper
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.isFragmentAttached
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import za.co.woolworths.financial.services.android.viewmodels.shop.ShopViewModel
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.Locale

@AndroidEntryPoint
class DashDeliveryAddressFragment : Fragment(R.layout.fragment_dash_delivery), IProductListing,
    View.OnClickListener, OnDemandNavigationListener, OnDashLandingNavigationListener,
    DashOrderReceiverListener {

    private lateinit var viewModel: ShopViewModel
    private val recommendationViewModel: RecommendationViewModel by viewModels()
    private val confirmAddressViewModel: ConfirmAddressViewModel by activityViewModels()
    private lateinit var binding: FragmentDashDeliveryBinding
    private lateinit var dashDeliveryAdapter: DashDeliveryAdapter
    private var isQuickShopClicked = false
    private var isUnSellableItemsRemoved: Boolean? = false
    private var mStoreId = ""
    private var dashOrderReceiver: DashOrderReceiver? = null
    private var inAppNotificationViewBinding: LayoutInappOrderNotificationBinding? = null
    private var isRetrievedUnreadMessagesOnLaunch: Boolean = false
    private var isLastDashOrderAvailable: Boolean = false
    private var recyclerViewViewHolderItems: ProductCarouselItemViewHolder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isFragmentAttached()) {
            dashDeliveryAdapter =
                DashDeliveryAdapter(
                    requireContext(), onDemandNavigationListener = this@DashDeliveryAddressFragment,
                    dashLandingNavigationListener = this@DashDeliveryAddressFragment, onDataUpdateListener = onDataUpdateListener, this@DashDeliveryAddressFragment,
                    activity = activity,
                    recommendationViewModel
                )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(ShopViewModel::class.java)
        binding = FragmentDashDeliveryBinding.bind(view)
        val parentFragment = (activity as? BottomNavigationActivity)?.currentFragment as? ShopFragment
        if (!isVisible || parentFragment?.getCurrentFragmentIndex() != ShopFragment.SelectedTabIndex.DASH_TAB.index || !isAdded) {
            return
        }
        initViews()
        setFragmentResultListener(CustomBottomSheetDialogFragment.DIALOG_BUTTON_CLICK_RESULT) { result, _ ->
            if(result.equals(UnsellableUtils.ADD_TO_LIST_SUCCESS_RESULT_CODE)){

            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (confirmAddressViewModel.getQuickShopButtonPressed()){
            confirmAddressViewModel.setQuickShopButtonPressed(false)
            updateMainRecyclerView()
        }
        val parentFragment = (activity as? BottomNavigationActivity)?.currentFragment as? ShopFragment
        if (!isVisible || parentFragment?.getCurrentFragmentIndex() != ShopFragment.SelectedTabIndex.DASH_TAB.index || !isAdded) {
            return
        }
        //verify if the show dash order is true
        refreshInAppNotificationToast()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden){
            refreshInAppNotificationToast()
            if (confirmAddressViewModel.getQuickShopButtonPressed()){
                confirmAddressViewModel.setQuickShopButtonPressed(false)
                updateMainRecyclerView()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dashOrderReceiver = DashOrderReceiver()
        dashOrderReceiver?.setDashOrderReceiverListener(this)
        dashOrderReceiver?.let {
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                it, IntentFilter(DashOrderReceiver.ACTION_LAST_DASH_ORDER)
            )
        }
    }

    override fun onStop() {
        dashOrderReceiver?.let {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(it)
        }
        super.onStop()
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
        setFragmentResultListener(UnsellableUtils.ADD_TO_LIST_SUCCESS_RESULT_CODE) { _, _ ->
            // Proceed with add to cart as we have moved unsellable items to List.
            addToCart(viewModel.addItemToCart.value) // This will again call addToCart
        }
        setFragmentResultListener(CustomBottomSheetDialogFragment.DIALOG_BUTTON_DISMISS_RESULT) { requestKey, bundle ->
            val resultCode =
                bundle.getString(CustomBottomSheetDialogFragment.DIALOG_BUTTON_CLICK_RESULT)
            if (resultCode == UnsellableUtils.ADD_TO_LIST_SUCCESS_RESULT_CODE) {
                // Proceed with add to cart as we have moved unsellable items to List.
                addToCart(viewModel.addItemToCart.value) // This will again call addToCart
            }
        }
    }

    private fun callValidatePlace(placeId: String?) {
        if (placeId.isNullOrEmpty())
            return
        callValidatePlaceApi()
        viewModel.getValidateLocationResponse(placeId)
    }

    private fun showSearchBar() {
        if (this.parentFragment is ShopFragment && KotlinUtils.browsingDeliveryType == Delivery.DASH)
            (this.parentFragment as ShopFragment).showSearchAndBarcodeUi() // show search bar.
    }

    private fun hideSearchBar() {
        if (this.parentFragment is ShopFragment && KotlinUtils.browsingDeliveryType == Delivery.DASH)
            (this.parentFragment as ShopFragment).hideSearchAndBarcodeUi() // hide search bar.
    }

    private fun showSetAddressScreen() {
        hideSearchBar()
        binding.layoutDashSetAddress?.root?.visibility = View.VISIBLE
        binding.layoutDashSetAddress.imgView?.setImageResource(R.drawable.img_dash_delivery)
        binding.layoutDashSetAddress.txtDashTitle?.text = context?.resources?.getString(R.string.dash_delivery_msg)
        binding.layoutDashSetAddress.txtDashSubTitle?.text = context?.resources?.getString(R.string.dash_delivery_title)
        binding.layoutDashSetAddress.btnDashSetAddress?.text = context?.resources?.getString(R.string.set_location)
        binding.layoutDashSetAddress.btnDashSetAddress?.setOnClickListener(this)
    }

    private fun showChangeLocationScreen() {
        hideSearchBar()
        binding.layoutDashSetAddress?.root?.visibility = View.VISIBLE
        binding.layoutDashSetAddress.imgView?.setImageResource(R.drawable.location_disabled)
        binding.layoutDashSetAddress.txtDashTitle?.text = context?.resources?.getString(R.string.no_location_title)
        binding.layoutDashSetAddress.txtDashSubTitle?.text = context?.resources?.getString(R.string.no_location_desc)
        binding.layoutDashSetAddress.btnDashSetAddress?.text = context?.resources?.getString(R.string.change_location)
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
                    viewModel.dashLandingDetails.value?.peekContent()?.data?.productCatalogues,
                    recommendedProducts = recommendationCatalogue()
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

    private fun fetchRecommendations() {
        val recommendationRequest = RecommendationRequest(
            events = listOf(
                Recommendation.PageView(
                    pageType = Constants.EVENT_PAGE_TYPE_MAIN,
                    eventType = Constants.EVENT_TYPE_PAGEVIEW,
                    url = Constants.EVENT_URL_MAIN
                ),
            ).plus(CommonRecommendationEvent.commonRecommendationEvents()),
            monetateId = Utils.getMonetateId()
        )
        recommendationViewModel.getRecommendationResponse(recommendationRequest)

        recommendationViewModel.recommendationResponseData.observe(viewLifecycleOwner) { actionItems ->
            if (!actionItems.isNullOrEmpty()) {
                dashDeliveryAdapter.setData(
                    onDemandCategories = viewModel.onDemandCategories.value?.peekContent()?.data?.onDemandCategories,
                    dashCategories = viewModel.dashLandingDetails.value?.peekContent()?.data?.productCatalogues,
                    recommendedProducts = recommendationCatalogue()
                )
            }
        }
    }

    private fun recommendationCatalogue(): ProductCatalogue? {
        val actions = recommendationViewModel.recommendationResponseData.value
        if (actions.isNullOrEmpty()) {
            return  null
        }
        val products = actions[0].products
        if (products.isNullOrEmpty()) {
            return  null
        }
        return ProductCatalogue(
            name = DashDeliveryAdapter.TYPE_NAME_RECOMMENDATION_SLOT,
            headerText = recommendationViewModel.recommendationTitle(),
            products = products
        )
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
                        recommendationViewModel.clearRecommendations()
                        binding.layoutDashSetAddress?.root?.visibility = View.GONE
                        resource.data?.productCatalogues?.let { productCatalog ->
                            val recommendationNeeded = productCatalog.any { catalog -> catalog.name == DashDeliveryAdapter.TYPE_NAME_RECOMMENDATION_SLOT } && SessionUtilities.getInstance().isUserAuthenticated
                            if (recommendationNeeded) {
                                fetchRecommendations()
                            }
                        }
                        if (viewModel.isOnDemandCategoriesAvailable.value == true) {
                            dashDeliveryAdapter.setData(
                                viewModel.onDemandCategories.value?.peekContent()?.data?.onDemandCategories,
                                resource.data?.productCatalogues,
                                recommendedProducts = recommendationCatalogue()
                            )
                        } else {
                            dashDeliveryAdapter.setData(
                                null,
                                resource.data?.productCatalogues,
                                recommendedProducts = recommendationCatalogue()
                            )
                        }
                        binding.progressBar.visibility = View.GONE
                    }
                    Status.ERROR -> {
                        binding.progressBar.visibility = View.GONE
                        if (isFragmentAttached()) {
                            showErrorView(context?.getString(resource.message), resource.data)
                        }
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
                                recommendedProducts = recommendationCatalogue()
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
                                recommendedProducts = recommendationCatalogue()
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
                                    if (isEnhanceSubstitutionFeatureAvailable()) {
                                        AddItemToCart(
                                            addItemToCart?.productId,
                                            addItemToCart?.catalogRefId,
                                            1,
                                            SubstitutionChoice.SHOPPER_CHOICE.name,
                                            ""
                                        )
                                    } else {
                                        AddItemToCart(
                                            addItemToCart?.productId,
                                            addItemToCart?.catalogRefId,
                                            1
                                        )
                                    }

                                )
                            }
                            else -> {
                                try {
                                    val cartItem =
                                        if (isEnhanceSubstitutionFeatureAvailable()) {
                                            AddItemToCart(
                                                addItemToCart?.productId ?: "",
                                                addItemToCart?.catalogRefId ?: "",
                                                skuInventoryList?.getOrNull(0)?.quantity ?: 0,
                                                SubstitutionChoice.SHOPPER_CHOICE.name,
                                                ""
                                            )
                                        } else {
                                            AddItemToCart(
                                                addItemToCart?.productId ?: "",
                                                addItemToCart?.catalogRefId ?: "",
                                                skuInventoryList?.getOrNull(0)?.quantity ?: 0
                                            )
                                        }

                                    showQuantitySelector(cartItem)

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
                                    if (isFragmentAttached()) {
                                        Utils.displayValidationMessage(
                                            context,
                                            CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                                            response.response.desc
                                        )
                                    }
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
                                if (isFragmentAttached()) {
                                    Utils.displayValidationMessage(
                                        requireContext(),
                                        CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                                        desc
                                    )
                                }
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
                                if (isFragmentAttached()) {
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

        viewModel.lastDashOrder.observe(viewLifecycleOwner) {
            it.peekContent()?.data?.apply {
                isLastDashOrderAvailable = true
                addInAppNotificationToast(this)
            }
        }
    }

    private fun showQuantitySelector(
        addItemToCart: AddItemToCart?,
    ) {
        recommendationViewModel.setQuickShopButtonPressed(true)
        var selectQuantityViewAdapter = SelectQuantityAdapter { selectedQuantity: Int ->
                quantityItemClicked(selectedQuantity, addItemToCart)
            }
        if (addItemToCart != null) {
            val quantityInStock = addItemToCart.quantity
            if (quantityInStock > 0) {
                // replace quickshop button image to cross button image
                context?.let {
                    recyclerViewViewHolderItems?.itemBinding?.rowLayout?.includeProductListingPriceLayout?.imQuickShopAddToCartIcon?.setImageDrawable(
                        ContextCompat.getDrawable(
                            it,
                            R.drawable.cross_button_bg
                        )
                    )
                }
            }
            recyclerViewViewHolderItems?.itemBinding?.rowLayout?.quantitySelectorView?.apply {
                visibility = View.VISIBLE
                layoutManager = context?.let { activity ->
                    LinearLayoutManager(
                        activity,
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                }
                val imageViewHeight =
                    (recyclerViewViewHolderItems?.itemBinding?.rowLayout?.imProductImage?.height
                        ?: 0) + (recyclerViewViewHolderItems?.itemBinding?.rowLayout?.tvProductName?.height
                        ?: 0)
                if (quantityInStock >= 5) {
                    layoutParams?.height = imageViewHeight
                } else {
                    layoutParams?.height = RecyclerView.LayoutParams.WRAP_CONTENT
                }
                adapter = selectQuantityViewAdapter

                val mScrollTouchListener: RecyclerView.OnItemTouchListener =
                    object : RecyclerView.OnItemTouchListener {
                        override fun onInterceptTouchEvent(
                            rv: RecyclerView,
                            e: MotionEvent,
                        ): Boolean {
                            val action = e.action
                            when (action) {
                                MotionEvent.ACTION_MOVE -> rv.parent.requestDisallowInterceptTouchEvent(
                                    true
                                )
                            }
                            return false
                        }

                        override fun onTouchEvent(
                            rv: RecyclerView,
                            e: MotionEvent,
                        ) {
                        }

                        override fun onRequestDisallowInterceptTouchEvent(
                            disallowIntercept: Boolean,
                        ) {
                        }
                    }
                addOnItemTouchListener(mScrollTouchListener)
            }
            selectQuantityViewAdapter?.setItem(quantityInStock)
        }
    }

    private fun quantityItemClicked(quantity: Int, addItemToCart: AddItemToCart?) {
        addItemToCart?.apply {
            addFoodProductTypeToCart(
                if (isEnhanceSubstitutionFeatureAvailable()) {
                    AddItemToCart(
                        productId,
                        catalogRefId,
                        quantity,
                        SubstitutionChoice.SHOPPER_CHOICE.name,
                        ""
                    )
                } else {
                    AddItemToCart(productId, catalogRefId, quantity)
                }
            )
        }
        updateMainRecyclerView()
    }

    private fun refreshInAppNotificationToast() {
        if (!SessionUtilities.getInstance().isUserAuthenticated) {
            removeNotificationToast()
            return
        }
        if (!isLastDashOrderAvailable) {
            viewModel.getLastDashOrderDetails()
            return
        }
        viewModel.lastDashOrder.value?.peekContent()?.data?.apply {
            if (showDashOrder
                && SessionUtilities.getInstance().isUserAuthenticated
                && viewModel.lastDashOrderInProgress.value == false
            ) {
                viewModel.getLastDashOrderDetails()
            }
        }
    }

    private fun removeNotificationToast() {
        // Remove view
        if (inAppNotificationViewBinding != null && binding.fragmentDashDelivery.contains(
                inAppNotificationViewBinding!!.root
            )
        )
            binding.fragmentDashDelivery.removeView(inAppNotificationViewBinding!!.root)
    }

    private fun addInAppNotificationToast(params: LastOrderDetailsResponse) {
        if (!isAdded || activity == null || view == null) {
            return
        }

        // Remove view if already added.
        removeNotificationToast()

        // user should be authenticated
        if (!SessionUtilities.getInstance().isUserAuthenticated) {
            return
        }

        // Show only when showDashOrder flag is true
        if (!params.showDashOrder) {
            return
        }

        val inflater = LayoutInflater.from(requireContext())
        inAppNotificationViewBinding =
            LayoutInappOrderNotificationBinding.inflate(inflater, binding.fragmentDashDelivery, false)
        inAppNotificationViewBinding?.root?.id = R.id.layoutInappNotification
        inAppNotificationViewBinding?.root?.layoutParams =
            ConstraintLayout.LayoutParams(
                ConstraintSet.MATCH_CONSTRAINT,
                ConstraintSet.WRAP_CONTENT
            )
        // Copy LayoutParams and add view
        val set = ConstraintSet()
        set.clone(binding.fragmentDashDelivery)
        // Align view to bottom
        // pin to the bottom of the container
        inAppNotificationViewBinding?.root?.id?.let {
            set.clear(it)
            set.constrainHeight(it, ConstraintSet.WRAP_CONTENT)
            set.constrainWidth(it, ConstraintSet.MATCH_CONSTRAINT)
            set.connect(
                it,
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM,
                requireContext().resources.getDimension(R.dimen.sixteen_dp).toInt()
            )
            set.connect(
                it,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START,
                requireContext().resources.getDimension(R.dimen.sixteen_dp).toInt()
            )
            set.connect(
                it,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END,
                requireContext().resources.getDimension(R.dimen.sixteen_dp).toInt()
            )
        }
        binding.fragmentDashDelivery.addView(inAppNotificationViewBinding!!.root)
        // Apply the changes
        set.applyTo(binding.fragmentDashDelivery)

        inAppNotificationViewBinding?.inappOrderNotificationContainer?.setOnClickListener(this)
        inAppNotificationViewBinding?.inappOrderNotificationContainer?.setTag(
            R.id.inappOrderNotificationContainer,
            params.orderId
        )

        params.orderId?.let { orderId ->
            inAppNotificationViewBinding?.inappOrderNotificationTitle?.text = buildSpannedString {
                val text = requireContext().getString(
                    R.string.inapp_order_notification_title,
                    orderId
                )
                append(text)
                val index = text.indexOf(orderId)
                val regularSpan = ResourcesCompat.getFont(requireContext(), R.font.opensans_regular)
                setSpan(
                    CustomTypefaceSpan("opensans", regularSpan),
                    index,
                    text.length,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
            }
        }
        inAppNotificationViewBinding?.inappOrderNotificationSubitle?.text =
            params.orderStatus ?: params.state
        // Chat / Driver Tracking / Location
        inAppNotificationViewBinding?.inappOrderNotificationIcon?.apply {
            setTag(R.id.inappOrderNotificationIcon, params)
            // Chat enabled STATUS == PACKING i.e. CONFIRMED
            if (params.isChatEnabled) {
                visibility = View.VISIBLE
                setImageResource(R.drawable.ic_chat_icon)
                setOnClickListener(this@DashDeliveryAddressFragment)
                if (!isRetrievedUnreadMessagesOnLaunch) {
                    isRetrievedUnreadMessagesOnLaunch = true
                    params.orderId?.let {
                        DashChatMessageListeningService.getUnreadMessageForOrder(
                            requireContext(),
                            it
                        )
                    }
                }
            }
            // Driver tracking enabled STATUS == EN-ROUTE
            else if (params.isDriverTrackingEnabled) {
                visibility = View.VISIBLE
                setImageResource(R.drawable.ic_white_location)
                setOnClickListener(this@DashDeliveryAddressFragment)
            } else {
                visibility = View.GONE
            }
        }
    }

    override fun updateUnreadMessageCount(unreadMsgCount: Int) {
        inAppNotificationViewBinding?.inAppOrderNotificationChatCount?.visibility = View.GONE
        //TODO: Later requirements for chat bubble.
        /*if (unreadMsgCount <= 0) {
            inAppNotificationViewBinding?.inAppOrderNotificationChatCount?.visibility = GONE
        } else {
            inAppNotificationViewBinding?.inAppOrderNotificationChatCount?.text =
                unreadMsgCount.toString()
            inAppNotificationViewBinding?.inAppOrderNotificationChatCount?.visibility = VISIBLE
        }*/
    }

    override fun updateLastDashOrder() {
        viewModel.getLastDashOrderDetails()
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
                                    UnsellableUtils.callConfirmPlace(
                                        (this@DashDeliveryAddressFragment),
                                        null,
                                        binding.progressBar,
                                        confirmAddressViewModel,
                                        KotlinUtils.browsingDeliveryType ?: Delivery.DASH
                                    )
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
        ConfirmLocationResponseLiveData.observe(viewLifecycleOwner) {
            isUnSellableItemsRemoved = it
            if (isUnSellableItemsRemoved == true && ((activity as? BottomNavigationActivity)?.mNavController?.currentFrag as? ShopFragment)?.getCurrentFragmentIndex() == ShopFragment.SelectedTabIndex.DASH_TAB.index) {
                ConfirmLocationResponseLiveData.value = false
                if (this.parentFragment is ShopFragment) {
                    (this.parentFragment as ShopFragment).setDeliveryView() // update main location UI.
                }
                val savedPlaceId =
                    getDeliveryType()?.address?.placeId
                KotlinUtils.apply {
                    Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId.let { responsePlaceId ->
                        this.placeId = responsePlaceId
                        isLocationPlaceIdSame = responsePlaceId.equals(savedPlaceId)
                    }
                }

                val browsingPlaceDetails =
                    WoolworthsApplication.getDashBrowsingValidatePlaceDetails()
                WoolworthsApplication.setValidatedSuburbProducts(browsingPlaceDetails)
                // set latest response to browsing data.
                WoolworthsApplication.setCncBrowsingValidatePlaceDetails(
                    browsingPlaceDetails
                )
            }
        }
        AddToCartLiveData.observe(viewLifecycleOwner) {
            if (it && isVisible) {
                // isVisible condition is necessary while searching product from dash landing to PLP.
                AddToCartLiveData.value = false
                addToCart(viewModel.addItemToCart.value) // This will again call addToCart
            }
        }
    }

    private fun navigateToUnsellableItemsFragment(
        unSellableCommerceItems: ArrayList<UnSellableCommerceItem>,
    ) {
        val unsellableItemsBottomSheetDialog =
            UnsellableItemsBottomSheetDialog.newInstance(
                unSellableCommerceItems,
                Delivery.DASH,
                binding.progressBar,
                confirmAddressViewModel,
                this@DashDeliveryAddressFragment
            )
        unsellableItemsBottomSheetDialog.show(
            parentFragmentManager,
            UnsellableItemsBottomSheetDialog::class.java.simpleName
        )
    }

    private fun setupRecyclerView() {
        binding.rvDashDelivery?.apply {
            adapter = dashDeliveryAdapter
            if (isFragmentAttached()) {
                layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            }
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
        if (!productList.recToken.isNullOrEmpty() && productList.sku.isNullOrEmpty()) {
            // This is recommendation product which does not have sku so will add it here
            productList.sku = productList.productId
        }
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
        if (resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()) {
            // Update Toast if logged in with another user
            // Use Case: If first user does not have any order, Second user should update Last order details
            viewModel.getLastDashOrderDetails()
        }
    }

    override fun setRecyclerViewHolderView(recyclerViewViewHolderItems: RecyclerViewViewHolderItems) {
        // Nothing to do.
    }

    override fun setMyRecycleViewHolder(recyclerViewHolder: MyRecycleViewHolder) {
        // Nothing to do.
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
            if (isFragmentAttached()) {
                KotlinUtils.showChangeDeliveryTypeDialog(
                    requireContext(), requireFragmentManager(),
                    KotlinUtils.browsingDeliveryType
                )
                return
            }
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
                    ErrorHandlerView(activity).showToast(context?.resources?.getString(R.string.no_connection))
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


    override fun onDashLandingNavigationClicked(
        position: Int,
        view: View?,
        item: Banner,
        headerText: String?
    ) {

        addBannerEngagementEvent(item,position,headerText)

        (requireActivity() as? BottomNavigationActivity)?.apply {
            val screenViewEventData = FirebaseAnalyticsEventHelper.Utils.getPLPScreenViewEventDataForDash(
                headerText = headerText,
                bannerDisplayName = item.displayName,
                bannerNavigationState = item.navigationState
            )
            pushFragment(
                ProductListingFragment.newInstance(
                    searchType = ProductsRequestParams.SearchType.NAVIGATE,
                    sub_category_name = item.displayName,
                    searchTerm = item.navigationState,
                    isBrowsing = true,
                    sendDeliveryDetails = arguments?.getBoolean(AppConstant.Keys.ARG_SEND_DELIVERY_DETAILS,
                        false) == true,
                    screenViewEventData = screenViewEventData
                )
            )
        }
    }

    override fun setProductCarousalItemViewHolder(viewHolder: ProductCarouselItemViewHolder) {
        this.recyclerViewViewHolderItems = viewHolder
    }

    override fun updateMainRecyclerView() {
        recommendationViewModel?.setQuickShopButtonPressed(false)
        dashDeliveryAdapter?.notifyDataSetChanged()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_dash_set_address -> {
                navigateToConfirmAddressScreen()
            }
            // In App notification click, Navigate to Order Details
            R.id.inappOrderNotificationContainer -> {
                (requireActivity() as? BottomNavigationActivity)?.apply {
                    val orderId: String? = v.getTag(R.id.inappOrderNotificationContainer) as? String
                    orderId?.let {
                        pushFragment(OrderDetailsFragment.getInstance(Parameter(it)))
                    }
                }
            }
            // In App notification Chat click, Navigate to Chat
            // Chat / Driver Tracking / Location
            R.id.inappOrderNotificationIcon -> {
                val params = v.getTag(R.id.inappOrderNotificationIcon) as? LastOrderDetailsResponse
                params?.apply {
                    // Chat
                    if (params.isChatEnabled) {
                        navigateToChat(orderId)
                    }
                    // Driver tracking
                    else if (params.isDriverTrackingEnabled) {
                        driverTrackingUrl?.let { navigateToOrderTrackingScreen(it) }
                    }
                }
            }
        }
    }

    private fun navigateToChat(orderId: String?) {
        orderId?.let {
            startActivity(OCChatActivity.newIntent(requireActivity(), it))
        }
    }

    private fun navigateToOrderTrackingScreen(url: String) {
        requireActivity().apply {
            startActivity(OrderTrackingWebViewActivity.newIntent(this, url))
            overridePendingTransition(
                R.anim.slide_from_right,
                R.anim.slide_out_to_left
            )
        }
    }

    private val onDataUpdateListener = object: OnDataUpdateListener {
        override fun onProductCatalogueUpdate(productCatalogues: ArrayList<ProductCatalogue>?) {
            if (productCatalogues.isNullOrEmpty()){
                return
            }
            if (((activity as? BottomNavigationActivity)?.mNavController?.currentFrag as? ShopFragment)?.getCurrentFragmentIndex() == ShopFragment.SelectedTabIndex.DASH_TAB.index){
                for (catalogues in productCatalogues){
                    if(DashDeliveryAdapter.TYPE_NAME_PRODUCT_CAROUSEL.lowercase() == catalogues.name?.lowercase()){
                        FirebaseAnalyticsEventHelper.viewItemList(
                            products = catalogues.products,
                            category = catalogues.headerText
                        )
                    }
                }
            }
        }
    }

    private fun addBannerEngagementEvent(
        banner: Banner,
        position: Int,
        bannerType: String?,
    ) {

        val categoryBanner = Bundle()
        categoryBanner?.apply {
            putString(
                FirebaseManagerAnalyticsProperties.PropertyNames.CONTENT_NAME,
                banner.displayName
            )
            putInt(
                FirebaseManagerAnalyticsProperties.PropertyNames.BANNER_POSITION,
                position
            )
            putString(
                FirebaseManagerAnalyticsProperties.PropertyNames.BANNER_LIST_NAME,
                bannerType
            )

        }
        AnalyticsManager.logEvent(
            FirebaseManagerAnalyticsProperties.PropertyNames.BANNER_ENGAGEMENT,
            categoryBanner
        )
    }
}
