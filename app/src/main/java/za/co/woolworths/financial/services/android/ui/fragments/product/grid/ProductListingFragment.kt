package za.co.woolworths.financial.services.android.ui.fragments.product.grid

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.view.Gravity
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.*
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.GridLayoutBinding
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.JsonSyntaxException
import com.skydoves.balloon.balloon
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.cart.view.SubstitutionChoice
import za.co.woolworths.financial.services.android.chanel.utils.ChanelUtils
import za.co.woolworths.financial.services.android.chanel.views.ChanelNavigationClickListener
import za.co.woolworths.financial.services.android.chanel.views.adapter.BrandLandingAdapter
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IProductListing
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.enhancedSubstitution.util.isEnhanceSubstitutionFeatureAvailable
import za.co.woolworths.financial.services.android.geolocation.GeoUtils
import za.co.woolworths.financial.services.android.geolocation.viewmodel.AddToCartLiveData
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmLocationResponseLiveData
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.BrandNavigationDetails
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.brandlandingpage.DynamicBanner
import za.co.woolworths.financial.services.android.models.dto.brandlandingpage.Navigation
import za.co.woolworths.financial.services.android.models.network.AppContextProviderImpl
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.NetworkConfig
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.recommendations.data.response.request.CartProducts
import za.co.woolworths.financial.services.android.recommendations.data.response.request.Event
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow.DISMISS_POP_WINDOW_CLICKED
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.ui.activities.WStockFinderActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.*
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.*
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.response.DyHomePageViewModel
import za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity
import za.co.woolworths.financial.services.android.ui.adapters.ProductListingAdapter
import za.co.woolworths.financial.services.android.ui.adapters.SortOptionsAdapter
import za.co.woolworths.financial.services.android.ui.adapters.holder.ProductListingViewType
import za.co.woolworths.financial.services.android.ui.adapters.holder.RecyclerViewViewHolderItems
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request.PrepareChangeAttributeRequestEvent
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request.Properties
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.ViewModel.DyChangeAttributeViewModel
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.IOnConfirmDeliveryLocationActionListener
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.usecase.Constants.EVENT_TYPE_CART
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.usecase.Constants.EVENT_TYPE_PAGEVIEW
import za.co.woolworths.financial.services.android.ui.views.*
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ProductListingFindInStoreNoQuantityFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.SelectYourQuantityFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.SingleButtonDialogFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_EXPECTATION_FAILED_417
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_OK
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_SESSION_TIMEOUT_440
import za.co.woolworths.financial.services.android.util.AppConstant.Keys.Companion.EXTRA_SEND_DELIVERY_DETAILS_PARAMS
import za.co.woolworths.financial.services.android.util.Utils.*
import za.co.woolworths.financial.services.android.util.analytics.AnalyticsManager
import za.co.woolworths.financial.services.android.util.analytics.FirebaseAnalyticsEventHelper
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager.Companion.logException
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager.Companion.setCrashlyticsString
import za.co.woolworths.financial.services.android.util.analytics.dto.AddToWishListFirebaseEventData
import za.co.woolworths.financial.services.android.util.analytics.dto.AnalyticProductItem
import za.co.woolworths.financial.services.android.util.analytics.dto.ScreenViewEventData
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.*
import kotlin.collections.ArrayList
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.Context

@AndroidEntryPoint
open class ProductListingFragment : ProductListingExtensionFragment(GridLayoutBinding::inflate),
    GridNavigator,
    IProductListing, View.OnClickListener, SortOptionsAdapter.OnSortOptionSelected,
    WMaterialShowcaseView.IWalkthroughActionListener,
    IOnConfirmDeliveryLocationActionListener, ChanelNavigationClickListener,
    ProductListingAdapter.OnTapIcon {

    private lateinit var mAddToListProduct: ProductList
    private var state: Parcelable? = null
    private var LOGIN_REQUEST_SUBURB_CHANGE = 1419
    private val SSO_REQUEST_ADD_TO_SHOPPING_LIST = 1420
    private var lastVisibleItem: Int = 0
    internal var totalItemCount: Int = 0

    var toolbarTitleText: String? = ""
    private var mSearchTerm: String = ""
    private var mNavigationState: String = ""
    private var mSubCategoryName: String = ""
    private var mFulfilmentTypeId: String = ""
    private var mStoreId: String = ""
    private var mSortOption: String = ""
    private var oneTimeInventoryErrorDialogDisplay: Boolean = false
    private var filterContent: Boolean = false

    private var mSearchType: ProductsRequestParams.SearchType? = null
    private var mAddItemsToCart: MutableList<AddItemToCart>? = null
    private var mErrorHandlerView: ErrorHandlerView? = null
    private var mProductAdapter: ProductListingAdapter? = null
    private var mProductList: MutableList<ProductList>? = null
    private var productView: ProductView? = null
    private var sortOptionDialog: Dialog? = null
    private var mAddItemToCart: AddItemToCart? = null
    private var mSelectedProductList: ProductList? = null
    private var mBannerLabel: String? = null
    private var mBannerImage: String? = null
    private var isUserBrowsing: Boolean = false
    private var mIsComingFromBLP: Boolean = false
    private var liquorDialog: Dialog? = null
    private var deliveryType: Delivery? = null
    private var placeId: String? = null
    private var isUnSellableItemsRemoved: Boolean? = false
    private val confirmAddressViewModel: ConfirmAddressViewModel by activityViewModels()
    private var localDeliveryType: String? = null
    private var localDeliveryTypeForHiddenChange: String? = null
    private var mPromotionalCopy: String? = null
    private var isChanelPage = false
    private val dyChoosevariationViewModel: DyHomePageViewModel by viewModels()
    private var breadCrumbList: ArrayList<String> = ArrayList()
    private var breadCrumb: ArrayList<BreadCrumb> = ArrayList()
    private var dyServerId: String? = null
    private var dySessionId: String? = null
    private var config: NetworkConfig? = null
    private var PLP_SCREEN_LOCATION: String? = "PLP Screen"
    private val dyReportEventViewModel: DyChangeAttributeViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var isSearchByKeywordNavigation = false
        activity?.apply {
            arguments?.apply {
                mSubCategoryName = getString(SUB_CATEGORY_NAME, "")
                isUserBrowsing = getBoolean(IS_BROWSING, false)
                mSearchType =
                    ProductsRequestParams.SearchType.valueOf(getString(SEARCH_TYPE, "SEARCH"))
                mSearchTerm = getString(SEARCH_TERM, "")
                mSortOption = getString(SORT_OPTION, "")
                isChanelPage = getBoolean(IS_CHANEL_PAGE, false)
                isSearchByKeywordNavigation = getBoolean(BUNDLE_NAVIGATION_FROM_SEARCH_BY_KEYWORD, false)

                (getSerializable(BRAND_NAVIGATION_DETAILS) as? BrandNavigationDetails)?.let { brandNavigationDetails ->
                    mNavigationState = brandNavigationDetails.navigationState ?: ""
                    mBannerLabel = brandNavigationDetails.bannerLabel ?: ""
                    mBannerImage = brandNavigationDetails.bannerImage ?: ""
                    mIsComingFromBLP = brandNavigationDetails.isComingFromBLP
                    filterContent = brandNavigationDetails.filterContent
                }
            }
            val localBody: HashMap<String, Any> = HashMap()
            localBody.apply {
                put("subCategory", mSubCategoryName!!)
                put("searchType", mSearchType!!)
                put("searchTerm", mSearchTerm!!)
                put("navigationState", mNavigationState!!)
                put("sortOption", mSortOption)
                put("filterContent", filterContent)
            }
            localProductBody.add(localBody)
            setProductBody()
            isBackPressed = false
            callViewSearchResultEvent(isSearchByKeywordNavigation, mSearchTerm)
        }
        config = NetworkConfig(AppContextProviderImpl())
        if (Utils.getSessionDaoDyServerId(SessionDao.KEY.DY_SERVER_ID) != null)
            dyServerId = Utils.getSessionDaoDyServerId(SessionDao.KEY.DY_SERVER_ID)
        if (Utils.getSessionDaoDySessionId(SessionDao.KEY.DY_SESSION_ID) != null)
            dySessionId = Utils.getSessionDaoDySessionId(SessionDao.KEY.DY_SESSION_ID)
    }

    private fun callViewSearchResultEvent(isSearchByKeywordNavigation: Boolean?, searchTerm: String?) {
        if (isSearchByKeywordNavigation == true) {
            FirebaseAnalyticsEventHelper.viewSearchResult(searchTerm)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigator = this
        (activity as? BottomNavigationActivity)?.apply {

            hideToolbar()
            setSupportActionBar(findViewById(R.id.toolbarPLP))
            showBackNavigationIcon(false)
            supportActionBar?.apply {
                setHomeButtonEnabled(false)
                setDisplayShowHomeEnabled(false)
            }
            showBottomNavigationMenu()
            localDeliveryTypeForHiddenChange = Delivery.STANDARD.name
            mErrorHandlerView =
                ErrorHandlerView(this, binding.incNoConnectionHandler.noConnectionLayout)
            mErrorHandlerView?.setMargin(
                binding.incNoConnectionHandler.noConnectionLayout,
                0,
                0,
                0,
                0
            )

            toolbarTitleText =
                if (mSubCategoryName?.isEmpty() == true) mSearchTerm else mSubCategoryName
            updateToolbarTitle()
            startProductRequest()
            setUniqueIds()
            addFragmentListner()
            isUnSellableItemsRemoved()
            localPlaceId = KotlinUtils.getPreferredPlaceId()
            localDeliveryType = KotlinUtils.getDeliveryType()?.deliveryType

        }

        binding.apply {
            toolbarPLPAddress.setOnClickListener(this@ProductListingFragment)
            toolbarPLPTitle.setOnClickListener(this@ProductListingFragment)
            plpSearchIcon.setOnClickListener(this@ProductListingFragment)
            plpBackIcon.setOnClickListener(this@ProductListingFragment)
        }

        binding.layoutErrorBlp.blpErrorBackBtn.setOnClickListener {
            (activity as? BottomNavigationActivity)?.popFragment()
        }

        binding.layoutErrorBlp.blpErrorBackBtn.setOnClickListener {
            startProductRequest()
        }
    }

    private fun prepareCategoryDynamicYieldPageView(
        productLists: ArrayList<ProductList>,
        breadCrumbList: ArrayList<String>,
        category_dyType: String
    ) {
        val user = User(dyServerId,dyServerId)
        val session = Session(dySessionId)
        val device = Device(IPAddress, config?.getDeviceModel())
        val skuIds: ArrayList<String>? = ArrayList()
       for (other in productLists) {
          if (other.sku != null) {
              var skuData = other.sku
              skuIds?.add(skuData!!)
          }
       }
        val page = Page(breadCrumbList, PLP_SCREEN_LOCATION, category_dyType)
        var pageAttributes: PageAttributes = if (breadCrumbList.isNotEmpty()) {
            PageAttributes(breadCrumbList)
        } else {
            breadCrumbList.add(mSubCategoryName)
            PageAttributes(breadCrumbList)
        }

        val context = Context(device, page, DY_CHANNEL,null)
        val options = Options(true)
        val homePageRequestEvent = HomePageRequestEvent(user, session, context, options)
        dyChoosevariationViewModel.createDyRequest(homePageRequestEvent)
    }

    private fun addFragmentListner() {
        setFragmentResultListener(CustomBottomSheetDialogFragment.DIALOG_BUTTON_CLICK_RESULT) { _, _ ->
            // As User selects to change the delivery location. So we will call confirm place API and will change the users location.
            getUpdatedValidateResponse()
        }
        setFragmentResultListener(UnsellableUtils.ADD_TO_LIST_SUCCESS_RESULT_CODE) { _, _ ->
            // Proceed with add to cart as we have moved unsellable items to List.
            onConfirmLocation()
        }
        setFragmentResultListener(CustomBottomSheetDialogFragment.DIALOG_BUTTON_DISMISS_RESULT) { requestKey, bundle ->
            val resultCode =
                bundle.getString(CustomBottomSheetDialogFragment.DIALOG_BUTTON_CLICK_RESULT)
            if (resultCode == UnsellableUtils.ADD_TO_LIST_SUCCESS_RESULT_CODE) {
                // Proceed with add to cart as we have moved unsellable items to List.
                onConfirmLocation()
            }
        }
    }

    private fun getUpdatedValidateResponse() {
        val placeId = when (KotlinUtils.browsingDeliveryType) {
            Delivery.STANDARD ->
                WoolworthsApplication.getValidatePlaceDetails()?.placeDetails?.placeId
                    ?: KotlinUtils.getPreferredPlaceId()
            Delivery.CNC ->
                if (WoolworthsApplication.getCncBrowsingValidatePlaceDetails() != null)
                    WoolworthsApplication.getCncBrowsingValidatePlaceDetails()?.placeDetails?.placeId
                else WoolworthsApplication.getValidatePlaceDetails()?.placeDetails?.placeId
            Delivery.DASH ->
                if (WoolworthsApplication.getDashBrowsingValidatePlaceDetails() != null)
                    WoolworthsApplication.getDashBrowsingValidatePlaceDetails()?.placeDetails?.placeId
                else WoolworthsApplication.getValidatePlaceDetails()?.placeDetails?.placeId
            else ->
                WoolworthsApplication.getValidatePlaceDetails()?.placeDetails?.placeId
        }

        showProgressBar()
        lifecycleScope.launch {
            try {
                val validateLocationResponse =
                    placeId?.let { confirmAddressViewModel.getValidateLocation(it) }
                dismissProgressBar()
                if (validateLocationResponse != null) {
                    when (validateLocationResponse?.httpCode) {
                        HTTP_OK -> {
                            val unsellableList =
                                KotlinUtils.getUnsellableList(
                                    validateLocationResponse.validatePlace,
                                    KotlinUtils.browsingDeliveryType
                                )
                            if (unsellableList?.isNullOrEmpty() == false && isUnSellableItemsRemoved == false) {
                                // show unsellable items
                                unsellableList?.let {
                                    navigateToUnsellableItemsFragment(
                                        it as ArrayList<UnSellableCommerceItem>,
                                        KotlinUtils.browsingDeliveryType
                                            ?: KotlinUtils.getPreferredDeliveryType()
                                            ?: Delivery.STANDARD
                                    )
                                }
                                val placeId = validateLocationResponse?.validatePlace?.placeDetails?.placeId
                                if(placeId != null) {
                                    val store = GeoUtils.getStoreDetails(
                                            placeId,
                                            validateLocationResponse?.validatePlace?.stores
                                    )
                                }
                            } else
                                UnsellableUtils.callConfirmPlace(
                                    this@ProductListingFragment,
                                    null,
                                    binding.incCenteredProgress.progressCreditLimit,
                                    confirmAddressViewModel,
                                    KotlinUtils.browsingDeliveryType
                                        ?: KotlinUtils.getPreferredDeliveryType()
                                        ?: Delivery.STANDARD
                                )
                        }
                    }
                }
            } catch (e: Exception) {
                logException(e)
                dismissProgressBar()
            } catch (e: JsonSyntaxException) {
                logException(e)
                dismissProgressBar()
            }
        }
    }

    private fun isUnSellableItemsRemoved() {
        ConfirmLocationResponseLiveData.observe(viewLifecycleOwner) {
            isUnSellableItemsRemoved = it
            if (isUnSellableItemsRemoved == true && (activity as? BottomNavigationActivity)?.mNavController?.currentFrag is ProductListingFragment) {
                ConfirmLocationResponseLiveData.value = false
                setBrowsingData()
                updateToolbarTitle() // update plp location.
            }
        }
        AddToCartLiveData.observe(viewLifecycleOwner) {
            if (it) {
                AddToCartLiveData.value = false
                onConfirmLocation() // This will again call addToCart
            }
        }
    }

    private fun navigateToUnsellableItemsFragment(
        unSellableCommerceItems: ArrayList<UnSellableCommerceItem>, deliveryType: Delivery,
    ) {
            val unsellableItemsBottomSheetDialog =
                UnsellableItemsBottomSheetDialog.newInstance(unSellableCommerceItems, deliveryType, binding.incCenteredProgress.progressCreditLimit, confirmAddressViewModel, this)
            unsellableItemsBottomSheetDialog.show(
                requireFragmentManager(),
                UnsellableItemsBottomSheetDialog::class.java.simpleName
            )
    }

    private fun setBrowsingData() {
        val browsingPlaceDetails = when (KotlinUtils.browsingDeliveryType) {
            Delivery.STANDARD -> WoolworthsApplication.getValidatePlaceDetails()
            Delivery.CNC -> WoolworthsApplication.getCncBrowsingValidatePlaceDetails()
            Delivery.DASH -> WoolworthsApplication.getDashBrowsingValidatePlaceDetails()
            else -> WoolworthsApplication.getValidatePlaceDetails()
        }
        WoolworthsApplication.setValidatedSuburbProducts(
            browsingPlaceDetails
        )
        // set latest response to browsing data.
        WoolworthsApplication.setCncBrowsingValidatePlaceDetails(
            browsingPlaceDetails
        )
        WoolworthsApplication.setDashBrowsingValidatePlaceDetails(
            browsingPlaceDetails
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.incNoConnectionHandler.btnRetry.setOnClickListener(this@ProductListingFragment)
        binding.sortAndRefineLayout.apply {
            refineProducts.setOnClickListener(this@ProductListingFragment)
            sortProducts.setOnClickListener(this@ProductListingFragment)
        }
    }

    private fun getScreenViewEventData(): ScreenViewEventData? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(BUNDLE_SCREEN_VIEW_EVENT_DATA, ScreenViewEventData::class.java)
        } else {
            arguments?.getParcelable(BUNDLE_SCREEN_VIEW_EVENT_DATA)
        }
    }

    override fun onResume() {
        super.onResume()
        FirebaseAnalyticsEventHelper.viewScreenEventForPLP(activity = activity, screenViewEventData = getScreenViewEventData())
        requestInAppReview(FirebaseManagerAnalyticsProperties.VIEW_ITEM_LIST, activity)

        if (activity is BottomNavigationActivity
            && (activity as BottomNavigationActivity).currentFragment is ProductListingFragment
        ) {
            val currentPlaceId = KotlinUtils.getPreferredPlaceId()
            if (currentPlaceId != null && !localPlaceId.isNullOrEmpty() && !(localPlaceId.let {
                    it.equals(currentPlaceId)
                })) {
                localPlaceId = currentPlaceId
                updateRequestForReload()
                pushFragment()
            } else if (!localDeliveryType.isNullOrEmpty() && deliveryType != null && !(localDeliveryType.let {
                    it.equals(deliveryType?.type)
                })) {
                localDeliveryType = deliveryType?.type
                updateRequestForReload()
                pushFragment()
            }
        }
    }

    private fun pushFragment() {
        (activity as? BottomNavigationActivity)?.apply {
            popFragmentNoAnim()
            pushFragment(
                newInstance(
                    mSearchType,
                    mSearchTerm,
                    mSubCategoryName,
                    productRequestBody.sortOption,
                    BrandNavigationDetails(
                        brandText = (arguments?.getSerializable(BRAND_NAVIGATION_DETAILS) as? BrandNavigationDetails)?.brandText,
                        navigationState = mNavigationState
                    ),
                    isUserBrowsing,
                    arguments?.getBoolean(EXTRA_SEND_DELIVERY_DETAILS_PARAMS, false),
                    isChanelPage
                )
            )
        }
    }

    private fun updateRequestForReload() {
        if (localProductBody.isNotEmpty()) {
            val list: HashMap<String, Any> =
                (localProductBody[localProductBody.lastIndex] as HashMap<String, Any>)
            mSubCategoryName = list["subCategory"] as? String ?: ""
            mSearchType = list["searchType"] as? ProductsRequestParams.SearchType
            mSearchTerm = list["searchTerm"] as? String ?: ""
            mNavigationState = list["navigationState"] as? String ?: ""
            mSortOption = list["sortOption"] as String
            filterContent = list["filterContent"] as Boolean
            setProductBody()
        }
        updateProductRequestBodyForRefinement(mNavigationState)
    }

    fun updateToolbarTitle() {
        if (!isAdded || !isVisible) {
            return
        }
        binding.toolbarPLPTitle.text =
            if (mSubCategoryName?.isEmpty() == true) mSearchTerm else mSubCategoryName

        // set delivery type and icon
        if (SessionUtilities.getInstance().isUserAuthenticated) {
            Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.let {
                updateToolbarDeliveryAddress(it.deliveryType, it.address?.placeId)
            }
        } else {
            KotlinUtils.getAnonymousUserLocationDetails()?.fulfillmentDetails?.let {
                updateToolbarDeliveryAddress(it.deliveryType, it.address?.placeId)
            }
        }
    }

    private fun updateToolbarDeliveryAddress(deliveryType: String?, placeId: String?) {
        this.placeId = placeId
        when (deliveryType) {
            Delivery.STANDARD.type -> {
                this.deliveryType = Delivery.STANDARD
                binding.toolbarPLPAddress.text =
                    requireContext().getString(R.string.standard_delivery)
                binding.toolbarPLPIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_delivery_circle
                    )
                )
            }
            Delivery.CNC.type -> {
                this.deliveryType = Delivery.CNC
                binding.toolbarPLPAddress.text = requireContext().getString(R.string.click_collect)
                binding.toolbarPLPIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_collection_circle
                    )
                )
            }
            Delivery.DASH.type -> {
                this.deliveryType = Delivery.DASH
                binding.toolbarPLPAddress.text = requireContext().getString(R.string.dash_delivery)
                binding.toolbarPLPIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_dash_delivery_circle
                    )
                )
            }
            else -> {
                this.deliveryType = Delivery.STANDARD
                binding.toolbarPLPAddress.text =
                    requireContext().getString(R.string.standard_delivery)
                binding.toolbarPLPIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_delivery_circle
                    )
                )
            }
        }
    }

    override fun onLoadProductSuccess(response: ProductView, loadMoreData: Boolean) {

        if (response.isBanners) {
            if (!response.dynamicBanners.isNullOrEmpty()) {
                onChanelSuccess(response)
            }
            return
        }
        binding.plpRelativeLayout.visibility = VISIBLE

        val productLists = response.products
        if (mProductList?.isNullOrEmpty() == true)

            mProductList = ArrayList()
        response.history?.apply {
            if (categoryDimensions?.isNullOrEmpty() == false) {
                mSubCategoryName = categoryDimensions[categoryDimensions.size - 1].label
                breadCrumb = categoryDimensions[categoryDimensions.size - 1].breadCrumbs
            } else if (searchCrumbs?.isNullOrEmpty() == false) {
                searchCrumbs?.let {
                    mSubCategoryName = it[it.size - 1].terms
                }
            }
        }

        breadCrumb.forEach {breadCrumb->
            breadCrumbList.add(breadCrumb.label)
        }

        if (productLists?.isEmpty() == true) {
            binding.sortAndRefineLayout.root.visibility = GONE
            if (!listContainHeader()) {
                val headerProduct = ProductList()
                headerProduct.rowType = ProductListingViewType.HEADER
                headerProduct.numberOfItems = numItemsInTotal
                productLists.add(0, headerProduct)
            }
            bindRecyclerViewWithUI(productLists)

        } else {
            viewItemListAnalytics(products = productLists, category = mSubCategoryName)
            this.productView = null
            this.productView = response
            hideFooterView()
            if (!loadMoreData) {
                binding.sortAndRefineLayout.root.visibility = VISIBLE
                (activity as? BottomNavigationActivity)?.setUpDrawerFragment(
                    productView,
                    productRequestBody
                )
                setRefinementViewState(productView?.navigation?.let { nav ->
                    getRefinementViewState(
                        nav
                    )
                }
                    ?: false)
                bindRecyclerViewWithUI(productLists)
                showFeatureWalkThrough()
                getCategoryNameAndSetTitle()

                if (AppConfigSingleton.isProductItemForLiquorInventoryPending) {
                    AppConfigSingleton.productItemForLiquorInventory?.let { productList ->
                        AppConfigSingleton.quickShopDefaultValues?.foodFulfilmentTypeId?.let {
                            dismissProgressBar()
                            queryInventoryForStore(
                                it,
                                if (isEnhanceSubstitutionFeatureAvailable()) {
                                    AddItemToCart(productList.productId, productList.sku, 0, SubstitutionChoice.SHOPPER_CHOICE.name, "")
                                } else {
                                    AddItemToCart(productList.productId, productList.sku, 0)
                                },
                                productList
                            )
                        }

                        AppConfigSingleton.isProductItemForLiquorInventoryPending = false
                        AppConfigSingleton.productItemForLiquorInventory = null
                    }
                }
            } else {
                loadMoreData(productLists)
            }
        }
        mProductAdapter?.notifyDataSetChanged()
        AppConfigSingleton.dynamicYieldConfig?.apply {
            if (isDynamicYieldEnabled == true) {
               val categoryDyType = if (mSearchType?.value.equals("search"))
                    "OTHER"
                else
                    "CATEGORY"
                prepareCategoryDynamicYieldPageView(response.products,breadCrumbList,categoryDyType)
            }
        }
    }

    private fun viewItemListAnalytics(products: List<ProductList>, category: String?) {
        FirebaseAnalyticsEventHelper.viewItemList(products = products, category = category)
    }

    private fun onChanelSuccess(response: ProductView) {
        isChanelPage = true
        binding.chanelLayout.root.visibility = VISIBLE
        binding.plpRelativeLayout.visibility = GONE
        val brandLandingAdapter = BrandLandingAdapter(
            context,
            response.dynamicBanners as List<DynamicBanner?>, this
        )
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.chanelLayout.rvChanel.apply {
            this.layoutManager = layoutManager
            setHasFixedSize(true)
            adapter = brandLandingAdapter
        }


        mSearchTerm = response.pageHeading ?: mSearchTerm
        updateToolbarTitle()
    }

    override fun showLiquorDialog() {

        liquorDialog = activity?.let { activity -> Dialog(activity) }
        liquorDialog?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            val view = layoutInflater.inflate(R.layout.liquor_info_dialog, null)
            val close = view.findViewById<Button>(R.id.close)
            val setSuburb = view.findViewById<TextView>(R.id.setSuburb)
            close?.setOnClickListener { dismiss() }
            setSuburb?.setOnClickListener {
                dismiss()
                if (!SessionUtilities.getInstance().isUserAuthenticated) {
                    ScreenManager.presentSSOSigninActivity(
                        activity,
                        LOGIN_REQUEST_SUBURB_CHANGE,
                        isUserBrowsing
                    )
                } else {
                    activity?.apply {
                        KotlinUtils.presentEditDeliveryGeoLocationActivity(
                            this,
                            LOGIN_REQUEST_SUBURB_CHANGE,
                            KotlinUtils.getPreferredDeliveryType(),
                            Utils.getPreferredDeliveryLocation().fulfillmentDetails?.address?.placeId
                        )
                    }
                }
            }
            setContentView(view)
            window?.apply {
                setLayout(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
                setBackgroundDrawableResource(R.color.transparent)
                setGravity(Gravity.CENTER)
            }

            setTitle(null)
            setCancelable(true)
            show()
        }
    }

    private fun getCategoryNameAndSetTitle() {
        if (!mSubCategoryName.isNullOrEmpty()) {
            toolbarTitleText = mSubCategoryName
            updateToolbarTitle()
        }
    }

    override fun unhandledResponseCode(response: Response) {
        val activity = activity ?: return
        if (response.desc == null) return
        hideFooterView()

        if (ChanelUtils.isCategoryPresentInConfig(mSearchTerm) || ChanelUtils.isCategoryPresentInConfig(
                mSubCategoryName
            ) || mIsComingFromBLP
        ) {
            (activity as? BottomNavigationActivity)?.apply {
                hideBottomNavigationMenu()
                Handler().postDelayed({ hideToolbar() }, AppConstant.DELAY_300_MS)
            }
            binding.chanelLayout.root.visibility = GONE
            binding.plpRelativeLayout.visibility = GONE
            binding.layoutErrorBlp.root.visibility = VISIBLE
            return
        }
        val fragmentTransaction: FragmentTransaction? =
            (activity as? AppCompatActivity)?.supportFragmentManager?.beginTransaction()
        // check if sortOptionDialog is being displayed
        (activity as? AppCompatActivity)?.let { if (hasOpenedDialogs(it)) return }

        // show sortOptionDialog
        try {
            val singleButtonDialogFragment = SingleButtonDialogFragment.newInstance(response.desc)
            fragmentTransaction?.let {
                singleButtonDialogFragment.show(
                    fragmentTransaction,
                    SingleButtonDialogFragment::class.java.simpleName
                )
                it.commitAllowingStateLoss()
            }
        } catch (ex: IllegalStateException) {
            logException(ex)
        }
    }

    private fun hasOpenedDialogs(activity: AppCompatActivity?): Boolean {
        activity?.supportFragmentManager?.fragments?.apply {
            for (fragment in this)
                if (fragment is DialogFragment) return true
        }
        return false
    }

    private fun hideFooterView() {
        if (listContainFooter())
            removeFooter()
    }

    override fun failureResponseHandler(e: String) {
        activity?.runOnUiThread { mErrorHandlerView?.networkFailureHandler(e) }
    }

    override fun cancelAPIRequest() {
        OneAppService().cancelRequest(loadProductRequest)
    }

    override fun bindRecyclerViewWithUI(productLists: MutableList<ProductList>) {
        /* Commenting showing pop up since it is not implemented in IOS for oct 2023 release */
       /* if(!AppInstanceObject.get().featureWalkThrough.plp_add_to_list) {
            PLPAddToListInfoBottomSheetDialog().show(
                parentFragmentManager,
                AppConstant.TAG_ADD_TO_LIST_PLP
            )
        }*/
        mProductList?.clear()
        mProductList = ArrayList()
        mProductList = productLists
        mPromotionalCopy = productView?.richText ?: ""
        if (!listContainHeader()) {
            val headerProduct = ProductList()
            headerProduct.rowType = ProductListingViewType.HEADER
            headerProduct.numberOfItems = numItemsInTotal
            mProductList?.add(0, headerProduct)
        }

        mProductAdapter = activity?.let {
            ProductListingAdapter(
                this,
                mProductList,
                it,
                mBannerLabel,
                mBannerImage,
                mIsComingFromBLP,
                mPromotionalCopy,
                this@ProductListingFragment
            )
        }
        val mRecyclerViewLayoutManager: GridLayoutManager?
        mRecyclerViewLayoutManager = GridLayoutManager(activity, 2)
        // Set up a GridLayoutManager to change the SpanSize of the header and footer
        mRecyclerViewLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if (position > mProductList!!.size - 1) {
                    //this is a fail safe to prevent ever getting
                    //the IndexOutOfBoundsException
                    return 1
                }

                var isHeader = false
                var isFooter = false

                //header should have span size of 2, and regular item should have span size of 1
                mProductList?.get(position)?.apply {
                    isHeader = rowType === ProductListingViewType.HEADER
                    isFooter = rowType === ProductListingViewType.FOOTER
                }

                return if (isHeader || isFooter) 2 else 1
            }
        }
        mProductAdapter = null
        mProductAdapter =
            activity?.let {
                ProductListingAdapter(
                    this@ProductListingFragment,
                    mProductList,
                    it,
                    mBannerLabel,
                    mBannerImage,
                    mIsComingFromBLP,
                    mPromotionalCopy,
                    this@ProductListingFragment
                )
            }
        binding.productsRecyclerView.apply {
            if (visibility == View.INVISIBLE)
                visibility = VISIBLE
            layoutManager = mRecyclerViewLayoutManager
            if (state != null) {
                layoutManager?.onRestoreInstanceState(state)
                state = null
            }
            adapter = mProductAdapter
            clearOnScrollListeners()
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    totalItemCount = mRecyclerViewLayoutManager?.itemCount ?: 0
                    lastVisibleItem = mRecyclerViewLayoutManager?.findLastVisibleItemPosition() ?: 0

                    // Detect scrolling up
                    if (dy > 0)
                        loadData()

                    // No search recommendation
                    if (lastVisibleItem == 0)
                        showRecommendedProducts()
                }
            })

            //for some reason, when we change the visibility
            //before setting the updated Adapter, the adapter still remembers
            //the results from the previous listed data. This of course may be different in sizes
            //and therefore we can most likely expect a IndexOutOfBoundsException
            if (visibility == View.INVISIBLE)
                visibility = VISIBLE
        }
    }

    private fun showRecommendedProducts() {
        val bundle = Bundle()
        val cartLinesValue: MutableList<CartProducts> = arrayListOf()

        bundle.putParcelable(
            BundleKeysConstants.RECOMMENDATIONS_EVENT_DATA, Event(eventType = EVENT_TYPE_PAGEVIEW, url = "/searchSortAndFilterV2", pageType = "emptySearch", null, null, null)
        )
        bundle.putParcelable(
            BundleKeysConstants.RECOMMENDATIONS_EVENT_DATA_TYPE, Event(eventType = EVENT_TYPE_CART, null, null, null, null, cartLinesValue
            )
        )
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.navHostRecommendation) as NavHostFragment
        val navController = navHostFragment?.navController
        val navGraph = navController?.navInflater?.inflate(R.navigation.nav_recommendation_graph)

        navGraph?.startDestination = R.id.recommendationFragment
        navGraph?.let {
            navController?.graph = it
        }
        navGraph?.let {
            navController?.setGraph(
                it, bundleOf("bundle" to bundle)
            )
        }
    }

    private fun loadData() {
        val visibleThreshold = 5
        if (!isLoading && totalItemCount <= lastVisibleItem + visibleThreshold) {
            if (productIsLoading()) return
            val total = numItemsInTotal + Utils.PAGE_SIZE
            val start = mProductList?.size ?: 0
            val end = start + Utils.PAGE_SIZE
            isLoading = total < end
            if (isLoading) return
            if (!listContainFooter()) {
                val footerItem = ProductList()
                footerItem.rowType = ProductListingViewType.FOOTER
                mProductList?.add(footerItem)
                mProductAdapter?.notifyItemInserted(mProductList!!.size - 1)
            }
            startProductRequest()
        }
    }

    private fun listContainFooter(): Boolean {
        try {
            mProductList?.let { mProductList ->
                for (pl in mProductList) {
                    if (pl.rowType === ProductListingViewType.FOOTER) {
                        return true
                    }
                }
            }
        } catch (ignored: Exception) {
        }

        return false
    }

    private fun removeFooter() {
        for ((index, pl) in mProductList!!.withIndex()) {
            if (pl.rowType === ProductListingViewType.FOOTER) {
                mProductList!!.remove(pl)
                mProductAdapter!!.notifyItemRemoved(index)
                return
            }
        }
    }

    private fun listContainHeader(): Boolean {
        if (mProductList != null) {
            for (pl in mProductList!!) {
                if (pl.rowType === ProductListingViewType.HEADER) {
                    return true
                }
            }
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelAPIRequest()
    }

    override fun onDetach() {
        super.onDetach()
        (activity as? BottomNavigationActivity)?.apply {
            walkThroughPromtView?.removeFromWindow()
            lockDrawerFragment()
        }
    }

    override fun startProductRequest() {
        activity?.let { activity ->
            executeLoadProduct(activity, productRequestBody)
        }
    }

    override fun loadMoreData(productLists: List<ProductList>) {
        val actualSize = mProductList?.size ?: 0 + 1
        mProductList?.addAll(productLists)
        val sizeOfList = mProductList?.size ?: 0
        try {
            hideFooterView()
        } catch (ex: Exception) {

        }

        mProductAdapter?.notifyItemChanged(actualSize, sizeOfList)



        canLoadMore(numItemsInTotal, sizeOfList)
    }

    override fun setProductBody() {
        setProductRequestBody(
            mSearchType,
            mSearchTerm,
            mNavigationState,
            mSortOption,
            filterContent,
            isChanelPage
        )
    }

    override fun onLoadStart(isLoadMore: Boolean) {
        setIsLoading(true)
        if (!isLoadMore) {
            binding.incCenteredProgress.root.visibility = VISIBLE
        }
    }

    override fun onLoadComplete(isLoadMore: Boolean) {
        setIsLoading(false)
        if (!isLoadMore) {
            binding.incCenteredProgress.root.visibility = GONE
        }


    }


    override fun onClick(view: View) {
        KotlinUtils.avoidDoubleClicks(view)
        activity?.let { activity ->
            when (view.id) {
                R.id.btnRetry -> {
                    when (NetworkManager.getInstance().isConnectedToNetwork(activity)) {
                        true -> {
                            mErrorHandlerView?.hideErrorHandler()
                            startProductRequest()
                        }
                        else -> return
                    }
                }
                R.id.refineProducts -> {
                    Utils.triggerFireBaseEvents(
                        FirebaseManagerAnalyticsProperties.REFINE_EVENT_APPEARED,
                        activity
                    )
                    /*val intent = Intent(activity, ProductsRefineActivity::class.java)
                    intent.putExtra(REFINEMENT_DATA, Utils.toJson(productView))
                    intent.putExtra(PRODUCTS_REQUEST_PARAMS, Utils.toJson(productRequestBody))
                    activity?.startActivityForResult(intent, REFINE_REQUEST_CODE)
                    activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)*/
                    (activity as BottomNavigationActivity).let {
                        it.setUpDrawerFragment(productView, productRequestBody)
                        it.openDrawerFragment()
                    }
                }
                R.id.sortProducts -> {
                    Utils.triggerFireBaseEvents(
                        FirebaseManagerAnalyticsProperties.SORTBY_EVENT_APPEARED,
                        activity
                    )
                    productView?.sortOptions?.let { sortOption -> this.showShortOptions(sortOption) }
                }
                R.id.toolbarPLPAddress, R.id.toolbarPLPTitle -> {
                    presentEditDeliveryActivity()
                }

                R.id.plpSearchIcon -> {
                    requireActivity().apply {
                        val openSearchActivity =
                            Intent(this, ProductSearchActivity::class.java).also {
                                it.putExtra(
                                    EXTRA_SEND_DELIVERY_DETAILS_PARAMS,
                                    arguments?.getBoolean(
                                        EXTRA_SEND_DELIVERY_DETAILS_PARAMS, false
                                    )
                                )
                            }

                        startActivity(openSearchActivity)
                        overridePendingTransition(0, 0)
                    }
                    val arguments = HashMap<String, String>()
                    arguments[FirebaseManagerAnalyticsProperties.PropertyNames.SEARCH_TERM] =
                        mSearchTerm.toString()
                    arguments[FirebaseManagerAnalyticsProperties.PropertyNames.SEARCH_TYPE] =
                        mSearchType.toString()
                    Utils.triggerFireBaseEvents(
                        FirebaseManagerAnalyticsProperties.SEARCH,
                        arguments,
                        activity
                    )
                }

                R.id.plpBackIcon -> {
                    (activity as? BottomNavigationActivity)?.popFragment()
                }

                else -> return
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        (activity as? BottomNavigationActivity)?.apply {
            when (hidden) {
                true -> lockDrawerFragment()
                else -> {
                    setSupportActionBar(binding.toolbarPLP)
                    showBottomNavigationMenu()
                    supportActionBar?.apply {
                        showBackNavigationIcon(false)
                        setDisplayShowHomeEnabled(false)
                    }
                    updateToolbarTitle()

                    if (localProductBody.isNotEmpty() && isBackPressed) {
                        localProductBody.removeLast()
                        isBackPressed = false
                    }

                    localDeliveryTypeForHiddenChange = KotlinUtils.getDeliveryType()?.deliveryType

                    if (activity is BottomNavigationActivity && (activity as BottomNavigationActivity).currentFragment is ProductListingFragment) {
                        val currentPlaceId = KotlinUtils.getPreferredPlaceId()
                        if (currentPlaceId != null && !localPlaceId.isNullOrEmpty() && !(localPlaceId.let {
                                it.equals(currentPlaceId)
                            })
                        ) {
                            localPlaceId = currentPlaceId
                            updateRequestForReload()
                            pushFragment()
                        } else if (!localDeliveryType.isNullOrEmpty() && !localDeliveryType.let {
                                it.equals(localDeliveryTypeForHiddenChange)
                            }) {
                            localDeliveryTypeForHiddenChange = localDeliveryType
                            updateRequestForReload()
                            pushFragment()
                        }
                    }
                    if (productView?.navigation?.isNullOrEmpty() != true)
                        unLockDrawerFragment()
                }
            }

            invalidateOptionsMenu()
        }
    }

    fun onBackPressed() {
        isBackPressed = true
    }

    override fun onSortOptionSelected(sortOption: SortOption) {
        var sortBy: String? = null
        if (sortOptionDialog != null && sortOptionDialog?.isShowing == true) {
            sortOptionDialog?.dismiss()
            val arguments = HashMap<String, String>()
            arguments[FirebaseManagerAnalyticsProperties.PropertyNames.SORT_OPTION_NAME] =
                sortOption.label
            activity?.apply {
                Utils.triggerFireBaseEvents(
                    FirebaseManagerAnalyticsProperties.SORTBY_EVENT_APPLIED,
                    arguments, this
                )
            }
            updateProductRequestBodyForSort(sortOption.sortOption)
            reloadProductsWithSortAndFilter()
            AppConfigSingleton.dynamicYieldConfig?.apply {
                if (isDynamicYieldEnabled == true) {
                    sortBy = sortOption.label
                    if (sortBy.equals(SORT_BY)) {
                        sortBy = ""
                        prepareSortByRequestEvent(sortBy)
                    }else
                        prepareSortByRequestEvent(sortBy)
                }
            }
        }
    }

    private fun prepareSortByRequestEvent(sortBy: String?) {
        val user = User(dyServerId,dyServerId)
        val session = Session(dySessionId)
        val device = Device(IPAddress, config?.getDeviceModel())
        val context = Context(device,null,DY_CHANNEL)
        val sortOrder = if (sortBy.equals(PRICE_HIGH_LOW) || sortBy.equals(NAME_Z_A)) {
            DESC
        } else if (sortBy.equals(PRICE_LOW_HIGH) || sortBy.equals(NAME_A_Z)) {
            ASC
        } else
            ""
        val properties = Properties(null,null,SORT_BY_DY_TYPE,null,null,null,null,null,null,null,null,null,sortBy,sortOrder)
        val eventsDyChangeAttribute = Event(null,null,null,null,null,null,null,null,null,null,null,null,SORT_ITEMS_EVENT_NAME,properties)
        val events = ArrayList<Event>()
        events.add(eventsDyChangeAttribute);
        val prepareDySortByRequestEvent = PrepareChangeAttributeRequestEvent(
            context,
            events,
            session,
            user
        )
        dyReportEventViewModel.createDyChangeAttributeRequest(prepareDySortByRequestEvent)
    }

    @SuppressLint("InflateParams")
    private fun showShortOptions(sortOptions: ArrayList<SortOption>) {
        sortOptionDialog = activity?.let { activity -> Dialog(activity) }
        sortOptionDialog?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            val view = layoutInflater.inflate(R.layout.sort_options_view, null)
            val rcvSortOptions = view.findViewById<RecyclerView>(R.id.sortOptionsList)
            rcvSortOptions?.layoutManager =
                activity?.let { activity -> LinearLayoutManager(activity) }
            rcvSortOptions?.adapter = activity?.let { activity ->
                SortOptionsAdapter(
                    activity,
                    sortOptions,
                    this@ProductListingFragment
                )
            }
            setContentView(view)
            window?.apply {
                setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
                setBackgroundDrawableResource(R.color.transparent)
                setGravity(Gravity.TOP)
            }

            setTitle(null)
            setCancelable(true)
            show()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            QUERY_INVENTORY_FOR_STORE_REQUEST_CODE, SET_DELIVERY_LOCATION_REQUEST_CODE -> {
                if (resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue() || resultCode == RESULT_OK) {
                    // check if user has any location.
                    if (Utils.getPreferredDeliveryLocation() != null) {
                        //Continue with addToCart Flow.
                        setBrowsingData()
                        updateToolbarTitle() // update plp location.
                        onConfirmLocation() // This will again call addToCart
                    } else {
                        // request cart summary to get the user's location.
                        requestCartSummary()
                    }
                }
            }
            QUERY_LOCATION_ITEM_REQUEST_CODE -> {
                if (resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()) {
                    queryStoreFinderProductByFusedLocation(null)
                }
            }
            SSOActivity.SSOActivityResult.LAUNCH.rawValue() -> {
                if (resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()) {
                    addFoodProductTypeToCart(mAddItemsToCart?.get(0))
                }
            }
            OPEN_CART_REQUEST -> {
                if (resultCode == Activity.RESULT_CANCELED || resultCode == DISMISS_POP_WINDOW_CLICKED) {
                    val currentPlaceId = KotlinUtils.getPreferredPlaceId()
                    if (currentPlaceId == null) {
                        //Fresh install with no location selection.
                        return
                    } else if (currentPlaceId != localPlaceId)
                        isBackPressed =
                            true // if PDP closes or cart fragment closed with location change.
                }
            }
            LOGIN_REQUEST_SUBURB_CHANGE -> {
                if (resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()) {
                    activity?.apply {
                        KotlinUtils.presentEditDeliveryGeoLocationActivity(
                            this,
                            LOGIN_REQUEST_SUBURB_CHANGE,
                            KotlinUtils.getPreferredDeliveryType(),
                            Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId
                        )
                    }
                } else if (resultCode == RESULT_OK) {
                    AppConfigSingleton.isProductItemForLiquorInventoryPending = true
                }
            }
            SSO_REQUEST_ADD_TO_SHOPPING_LIST ->{
                addItemToShoppingList(mAddToListProduct)
                activity?.apply { ScreenManager.presentBiometricWalkthrough(this) }
            }
            BundleKeysConstants.REQUEST_CODE -> {
                updateToolbarTitle()
                KotlinUtils.getPreferredDeliveryType()?.let {
                    UnsellableUtils.callConfirmPlace(this@ProductListingFragment, null, binding.incCenteredProgress.progressCreditLimit, confirmAddressViewModel,
                        it
                    )
                }
            }
            else -> return
        }
    }

    private fun presentEditDeliveryActivity() {
        KotlinUtils.presentEditDeliveryGeoLocationActivity(
            requireActivity(),
            BundleKeysConstants.REQUEST_CODE,
            deliveryType,
            placeId
        )
    }

    private fun reloadProductsWithSortAndFilter() {
        binding.apply {
            productsRecyclerView.visibility = View.INVISIBLE
            sortAndRefineLayout.root.visibility = GONE
        }
        startProductRequest()
    }

    private fun showFeatureWalkThrough() {
        if (!isAdded || !AppInstanceObject.get().featureWalkThrough.showTutorials || AppInstanceObject.get().featureWalkThrough.refineProducts)
            return
        (activity as? BottomNavigationActivity)?.let {
            // Prevent dialog to display in other section when fragment is not visible
            if (it.currentFragment !is ProductListingFragment) return
            FirebaseManager.setCrashlyticsString(
                bindString(R.string.crashlytics_materialshowcase_key),
                this.javaClass.canonicalName
            )
            it.walkThroughPromtView =
                WMaterialShowcaseView.Builder(it, WMaterialShowcaseView.Feature.REFINE)
                    .setTarget(binding.sortAndRefineLayout.refineDownArrow)
                    .setTitle(R.string.walkthrough_refine_title)
                    .setDescription(R.string.walkthrough_refine_desc)
                    .setActionText(R.string.walkthrough_refine_action)
                    .setImage(R.drawable.tips_tricks_ic_refine)
                    .setShapePadding(48)
                    .setAction(this@ProductListingFragment)
                    .setAsNewFeature()
                    .setArrowPosition(WMaterialShowcaseView.Arrow.TOP_RIGHT)
                    .setMaskColour(ContextCompat.getColor(it, R.color.semi_transparent_black))
                    .build()
            it.walkThroughPromtView.show(it)
        }
    }

    override fun onWalkthroughActionButtonClick(feature: WMaterialShowcaseView.Feature) {
        binding.sortAndRefineLayout.apply {
            if (refineProducts?.isClickable == true)
                refineProducts?.let { refineProducts -> onClick(refineProducts) }
        }
    }

    override fun onPromptDismiss(feature: WMaterialShowcaseView.Feature) {

    }

    private fun getRefinementViewState(navigationList: ArrayList<RefinementNavigation>): Boolean {
        if (navigationList.size == 0)
            return false
        for ((displayName, _, refinementCrumbs, refinements) in navigationList) {
            if (displayName.equals("On Promotion", ignoreCase = true))
                return true
            else if (refinements.size > 0 || refinementCrumbs.size > 0)
                return true
        }

        return false
    }

    private fun setRefinementViewState(refinementViewState: Boolean) {
        binding.sortAndRefineLayout.apply {
            refineProducts.isEnabled = refinementViewState
            refineDownArrow.isEnabled = refinementViewState
            refinementText.isEnabled = refinementViewState
        }
        (activity as? BottomNavigationActivity)?.apply {
            when (refinementViewState) {
                true -> unLockDrawerFragment()
                false -> lockDrawerFragment()
            }
        }

    }

    override fun openProductDetailView(productList: ProductList) {
        //firebase event select_item
        state = binding.productsRecyclerView.layoutManager?.onSaveInstanceState()
        val selectItemParams = Bundle()
        selectItemParams.putString(
            FirebaseManagerAnalyticsProperties.PropertyNames.ITEM_LIST_NAME,
            mSubCategoryName
        )
        selectItemParams.putString(
            FirebaseManagerAnalyticsProperties.PropertyNames.ITEM_RATING,
            productList.averageRating
        )
        for (products in 0..(mProductList?.size ?: 0)) {
            val selectItem = Bundle()
            selectItem.putString(FirebaseAnalytics.Param.ITEM_ID, productList.productId)
            selectItem.putString(FirebaseAnalytics.Param.ITEM_NAME, productList.productName)
            selectItem.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, mSubCategoryName)
            selectItem.putString(FirebaseAnalytics.Param.ITEM_BRAND, productList.brandText)
            selectItem.putString(FirebaseAnalytics.Param.ITEM_VARIANT, productList.productVariants)
            productList.price?.let {
                selectItem.putDouble(FirebaseAnalytics.Param.PRICE, it.toDouble())
            }
            selectItemParams.putParcelableArray(FirebaseAnalytics.Param.ITEMS, arrayOf(selectItem))
        }
        AnalyticsManager.logEvent(
            FirebaseManagerAnalyticsProperties.SELECT_ITEM_EVENT,
            selectItemParams
        )

        val title = if (mSearchTerm?.isNotEmpty() == true) mSearchTerm else mSubCategoryName
        (activity as? BottomNavigationActivity)?.openProductDetailFragment(
            title,
            productList,
            mBannerLabel,
            mBannerImage,
            isUserBrowsing
        )
    }

    fun openProductDetailView(
        productList: ProductList,
        bannerLabel: String?,
        bannerImage: String?,
    ) {
        state = binding.productsRecyclerView.layoutManager?.onSaveInstanceState()
        val title = if (mSearchTerm?.isNotEmpty() == true) mSearchTerm else mSubCategoryName
        (activity as? BottomNavigationActivity)?.openProductDetailFragment(
            title,
            productList,
            bannerLabel,
            bannerImage,
            isUserBrowsing
        )
    }


    override fun queryInventoryForStore(
        fulfilmentTypeId: String,
        addItemToCart: AddItemToCart?,
        productList: ProductList,
    ) {
        this.mFulfilmentTypeId = fulfilmentTypeId
        if (binding.incCenteredProgress.root.visibility == VISIBLE) return // ensure one api runs at a time
        this.mStoreId =
            fulfilmentTypeId.let { it1 -> RecyclerViewViewHolderItems.getFulFillmentStoreId(it1) }
                ?: ""
        this.mAddItemToCart = addItemToCart
        this.mSelectedProductList = productList
        val activity = activity ?: return

        if (!SessionUtilities.getInstance().isUserAuthenticated) {
            ScreenManager.presentSSOSigninActivity(
                activity,
                QUERY_INVENTORY_FOR_STORE_REQUEST_CODE,
                isUserBrowsing
            )
            return
        }

        if (productList.isLiquor == true && !KotlinUtils.isCurrentSuburbDeliversLiquor() && !KotlinUtils.isLiquorModalShown()) {
            KotlinUtils.setLiquorModalShown()
            showLiquorDialog()
            AppConfigSingleton.productItemForLiquorInventory = productList
            return
        }

        if (KotlinUtils.getDeliveryType() == null ){
            presentEditDeliveryActivity()
            return
        }

        // Now first check for if delivery location and browsing location is same.
        // if same no issues. If not then show changing delivery location popup.
        if (!KotlinUtils.getDeliveryType()?.deliveryType.equals(KotlinUtils.browsingDeliveryType?.type) && isUserBrowsing) {
            KotlinUtils.showChangeDeliveryTypeDialog(
                requireContext(), requireFragmentManager(),
                KotlinUtils.browsingDeliveryType
            )
            return
        }


        if (mStoreId.isEmpty()) {
            addItemToCart?.catalogRefId?.let { skuId -> productOutOfStockErrorMessage(skuId) }
            return
        }

        showProgressBar()
        OneAppService().getInventorySkuForStore(
            mStoreId, addItemToCart?.catalogRefId
                ?: "", isUserBrowsing
        ).enqueue(CompletionHandler(object : IResponseListener<SkusInventoryForStoreResponse> {
            override fun onSuccess(skusInventoryForStoreResponse: SkusInventoryForStoreResponse?) {
                if (!isAdded) return
                dismissProgressBar()
                oneTimeInventoryErrorDialogDisplay = false
                with(activity.supportFragmentManager.beginTransaction()) {
                    when (skusInventoryForStoreResponse?.httpCode) {
                        HTTP_OK -> {
                            val skuInventoryList = skusInventoryForStoreResponse.skuInventory
                            if (skuInventoryList.size == 0 || skuInventoryList[0].quantity == 0) {
                                addItemToCart?.catalogRefId?.let { skuId ->
                                    // TODO: Remove non-fatal exception below once APP2-65 is closed
                                    setCrashlyticsString(
                                        FirebaseManagerAnalyticsProperties.CrashlyticsKeys.PRODUCT_ID,
                                        mSelectedProductList?.productId
                                    )
                                    setCrashlyticsString(
                                        FirebaseManagerAnalyticsProperties.CrashlyticsKeys.PRODUCT_NAME,
                                        mSelectedProductList?.productName
                                    )
                                    setCrashlyticsString(
                                        FirebaseManagerAnalyticsProperties.CrashlyticsKeys.DELIVERY_LOCATION,
                                        KotlinUtils.getPreferredDeliveryAddressOrStoreName()
                                    )
                                    setCrashlyticsString(
                                        FirebaseManagerAnalyticsProperties.CrashlyticsKeys.PRODUCT_SKU,
                                        mSelectedProductList?.sku
                                    )
                                    setCrashlyticsString(
                                        FirebaseManagerAnalyticsProperties.CrashlyticsKeys.FULFILLMENT_ID,
                                        mFulfilmentTypeId
                                    )
                                    setCrashlyticsString(
                                        FirebaseManagerAnalyticsProperties.CrashlyticsKeys.STORE_ID,
                                        mStoreId
                                    )
                                    setCrashlyticsString(
                                        FirebaseManagerAnalyticsProperties.CrashlyticsKeys.DELIVERY_TYPE,
                                        KotlinUtils.getPreferredDeliveryType().toString()
                                    )
                                    setCrashlyticsString(
                                        FirebaseManagerAnalyticsProperties.CrashlyticsKeys.IS_USER_AUTHENTICATED,
                                        SessionUtilities.getInstance().isUserAuthenticated.toString()
                                    )
                                    Utils.getLastSavedLocation()?.let {
                                        setCrashlyticsString(
                                            FirebaseManagerAnalyticsProperties.CrashlyticsKeys.LAST_KNOWN_LOCATION,
                                            "${it.latitude}, ${it.longitude}"
                                        )
                                    }
                                    logException(Exception(FirebaseManagerAnalyticsProperties.CrashlyticsExceptionName.PRODUCT_LIST_FIND_IN_STORE))

                                    productOutOfStockErrorMessage(
                                        skuId
                                    )
                                }
                            } else if (skuInventoryList[0].quantity == 1) {
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
                            } else {
                                val cartItem =
                                    if (isEnhanceSubstitutionFeatureAvailable()) {
                                        AddItemToCart(
                                            addItemToCart?.productId
                                                ?: "", addItemToCart?.catalogRefId
                                                ?: "", skuInventoryList[0].quantity,
                                            SubstitutionChoice.SHOPPER_CHOICE.name,
                                            ""
                                        )
                                    } else {
                                        AddItemToCart(
                                            addItemToCart?.productId
                                                ?: "", addItemToCart?.catalogRefId
                                                ?: "", skuInventoryList[0].quantity
                                        )
                                    }

                                try {
                                    val selectYourQuantityFragment =
                                        SelectYourQuantityFragment.newInstance(
                                            cartItem,
                                            this@ProductListingFragment
                                        )
                                    selectYourQuantityFragment.show(
                                        this,
                                        SelectYourQuantityFragment::class.java.simpleName
                                    )
                                } catch (ex: IllegalStateException) {
                                    logException(ex)
                                }
                            }
                        }

                        else -> {
                            if (!oneTimeInventoryErrorDialogDisplay) {
                                oneTimeInventoryErrorDialogDisplay = true
                                skusInventoryForStoreResponse?.response?.desc?.let { desc ->
                                    Utils.displayValidationMessage(
                                        activity,
                                        CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                                        desc
                                    )
                                }
                            } else return
                        }
                    }
                }
            }

            override fun onFailure(error: Throwable?) {
                if (!isAdded) return
                activity.runOnUiThread {
                    dismissProgressBar()
                    error?.let { onFailureHandler(it) }
                }
            }
        }, SkusInventoryForStoreResponse::class.java))
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

    private fun showProgressBar() {
        // Show progress bar
        binding.incCenteredProgress.root.visibility = VISIBLE
    }

    private fun dismissProgressBar() {
        // hide progress bar
        binding.incCenteredProgress.root.visibility = GONE
        mProductAdapter?.resetQuickShopButton()
    }

    override fun addFoodProductTypeToCart(addItemToCart: AddItemToCart?) {
        showProgressBar()
        mAddItemsToCart = mutableListOf()
        addItemToCart?.let {
                cartItem -> mAddItemsToCart?.add(cartItem)
        }
        PostItemToCart().make(mAddItemsToCart
            ?: mutableListOf(), object : IResponseListener<AddItemToCartResponse> {
            override fun onSuccess(addItemToCartResponse: AddItemToCartResponse?) {
                if (!isAdded) return
                activity?.apply {
                    dismissProgressBar()
                    when (addItemToCartResponse?.httpCode) {
                        HTTP_OK -> {
                            // Preferred Delivery Location has been reset on server
                            // As such, we give the user the ability to set their location again
                            val addToCartList = addItemToCartResponse.data
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
                                        addItemToCartResponse.response.desc = formException.message
                                        Utils.displayValidationMessage(
                                            this,
                                            CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                                            addItemToCartResponse.response.desc
                                        )
                                    }
                                    return
                                }
                            }
                            if ((KotlinUtils.isDeliveryOptionClickAndCollect() || KotlinUtils.isDeliveryOptionDash())
                                && addItemToCartResponse.data[0]?.productCountMap?.quantityLimit?.foodLayoutColour != null
                            ) {
                                addItemToCartResponse.data[0]?.productCountMap?.let {
                                    addItemToCart?.quantity?.let { it1 ->
                                        ToastFactory.showItemsLimitToastOnAddToCart(
                                            binding.productsRecyclerView,
                                            it,
                                            this,
                                            it1
                                        )
                                    }
                                }
                            } else {
                                val addToCartBalloon by balloon<AddedToCartBalloonFactory>()
                                val bottomView =
                                    (activity as? BottomNavigationActivity)?.bottomNavigationById
                                val buttonView: Button =
                                    addToCartBalloon.getContentView().findViewById(R.id.btnView)
                                val tvAddedItem: TextView = addToCartBalloon.getContentView()
                                    .findViewById(R.id.tvAddedItem)
                                val quantityAdded = addItemToCart?.quantity?.toString()
                                val quantityDesc =
                                    "$quantityAdded ITEM${if ((addItemToCart?.quantity ?: 0) >= 1) "" else "s"}"
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
                                Handler().postDelayed({
                                    addToCartBalloon.dismiss()
                                }, 3000)
                            }
                        }

                        HTTP_EXPECTATION_FAILED_417 -> resources?.let {
                            activity?.apply {
                                KotlinUtils.presentEditDeliveryGeoLocationActivity(
                                    this,
                                    SET_DELIVERY_LOCATION_REQUEST_CODE,
                                    KotlinUtils.getPreferredDeliveryType(),
                                    Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId
                                )
                            }
                        }
                        HTTP_SESSION_TIMEOUT_440 -> {
                            SessionUtilities.getInstance()
                                .setSessionState(SessionDao.SESSION_STATE.INACTIVE)
                            ScreenManager.presentSSOSignin(this)
                        }

                        AppConstant.HTTP_EXPECTATION_FAILED_502 -> {
                            addItemToCartResponse.response.desc?.let {
                                KotlinUtils.showQuantityLimitErrror(
                                    activity?.supportFragmentManager,
                                    it,
                                    "",
                                    context
                                )
                            }
                        }

                        else -> addItemToCartResponse?.response?.desc?.let { desc ->
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
                if (!isAdded) return
                activity?.runOnUiThread { dismissProgressBar() }
            }
        })
    }

    private fun productOutOfStockErrorMessage(skuId: String) {
        try {
            activity?.supportFragmentManager?.beginTransaction()?.apply {
                val productListingFindInStoreNoQuantityFragment =
                    ProductListingFindInStoreNoQuantityFragment.newInstance(
                        skuId,
                        this@ProductListingFragment
                    )
                productListingFindInStoreNoQuantityFragment.show(
                    this,
                    ProductListingFindInStoreNoQuantityFragment::class.java.simpleName
                )
            }
        } catch (ex: IllegalStateException) {
            logException(ex)
        }
    }

    private fun openCartActivity() {
        (activity as? BottomNavigationActivity)?.apply {
            bottomNavigationById?.currentItem = INDEX_CART
        }
    }

    override fun queryStoreFinderProductByFusedLocation(location: Location?) {
        showProgressBar()
        val globalState = WoolworthsApplication.getInstance().wGlobalState
        with(globalState) {
            OneAppService().getLocationsItem(
                mSelectedProductList?.sku
                    ?: "", startRadius.toString(), endRadius.toString()
            ).enqueue(CompletionHandler(object : IResponseListener<LocationResponse> {
                override fun onSuccess(locationResponse: LocationResponse?) {
                    if (!isAdded) return
                    dismissProgressBar()
                    locationResponse?.apply {
                        when (httpCode) {
                            HTTP_OK -> {
                                if (Locations != null && Locations.size > 0) {
                                    WoolworthsApplication.getInstance()?.wGlobalState?.storeDetailsArrayList =
                                        Locations
                                    val openStoreFinder = Intent(
                                        WoolworthsApplication.getAppContext(),
                                        WStockFinderActivity::class.java
                                    )
                                    openStoreFinder.putExtra(
                                        "PRODUCT_NAME",
                                        mSelectedProductList?.productName
                                    )
                                    openStoreFinder.putExtra("CONTACT_INFO", "")
                                    activity?.startActivity(openStoreFinder)
                                    activity?.overridePendingTransition(
                                        R.anim.slide_up_anim,
                                        R.anim.stay
                                    )
                                } else {
                                    activity?.let { activity ->
                                        Utils.displayValidationMessage(
                                            activity,
                                            CustomPopUpWindow.MODAL_LAYOUT.NO_STOCK,
                                            ""
                                        )
                                    }
                                }
                            }
                            HTTP_SESSION_TIMEOUT_440 -> {
                                SessionUtilities.getInstance()
                                    .setSessionState(SessionDao.SESSION_STATE.INACTIVE)
                                activity.let {
                                    ScreenManager.presentSSOSignin(
                                        it,
                                        QUERY_LOCATION_ITEM_REQUEST_CODE
                                    )
                                }
                            }
                            else -> response?.desc?.let { desc ->
                                Utils.displayValidationMessage(
                                    WoolworthsApplication.getAppContext(),
                                    CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                                    desc
                                )
                            }
                        }
                    }


                }

                override fun onFailure(error: Throwable?) {
                    activity?.runOnUiThread {
                        dismissProgressBar()
                        error?.let { onFailureHandler(it) }
                    }
                }
            }, LocationResponse::class.java))
        }
    }

    override fun openChangeFulfillmentScreen() {
        presentEditDeliveryActivity()
    }

    fun onRefined(navigationState: String, isMultiSelectCategoryRefined: Boolean) {
        if (isMultiSelectCategoryRefined)
            updateProductRequestBodyForRefinement(navigationState)

        (activity as? BottomNavigationActivity)?.pushFragment(
            newInstance(
                mSearchType,
                mSearchTerm,
                mSubCategoryName,
                productRequestBody.sortOption,
                BrandNavigationDetails(
                    brandText = (arguments?.getSerializable(BRAND_NAVIGATION_DETAILS) as? BrandNavigationDetails)?.brandText,
                    navigationState = navigationState
                ),
                isUserBrowsing,
                arguments?.getBoolean(EXTRA_SEND_DELIVERY_DETAILS_PARAMS, false),
                isChanelPage
            )
        )
    }

    fun onResetFilter() {
        val pushedFragmentCount =
            (activity as? BottomNavigationActivity)?.supportFragmentManager?.fragments?.filter {
                it.tag.toString().contains("ProductListingFragment", true)
            }?.size
                ?: 1
        if (pushedFragmentCount > 1)
            (activity as? BottomNavigationActivity)?.popFragment()
        else {
            updateProductRequestBodyForRefinement("")
            reloadProductsWithSortAndFilter()
        }
    }

    companion object {
        private var localProductBody: ArrayList<Any> = ArrayList()
        private var localPlaceId: String? = null
        private var isBackPressed: Boolean = false

        private const val SUB_CATEGORY_NAME = "SUB_CATEGORY_NAME"
        private const val QUERY_INVENTORY_FOR_STORE_REQUEST_CODE = 3343
        private const val QUERY_LOCATION_ITEM_REQUEST_CODE = 3344
        const val SET_DELIVERY_LOCATION_REQUEST_CODE = 180

        private const val SEARCH_TYPE = "SEARCH_TYPE"
        private const val SEARCH_TERM = "SEARCH_TERM"
        const val IS_BROWSING = "is_browsing"
        const val BUNDLE_NAVIGATION_FROM_SEARCH_BY_KEYWORD = "isNavigationFromSearchByKeyword"
        const val BUNDLE_SCREEN_VIEW_EVENT_DATA = "BUNDLE_SCREEN_VIEW_EVENT_DATA"
        private const val SORT_OPTION = "SORT_OPTION"
        private const val IS_CHANEL_PAGE = "IS_CHANEL_PAGE"
        private const val BRAND_NAVIGATION_DETAILS = "BRAND_NAVIGATION_DETAILS"

        @JvmOverloads
        fun newInstance(
            searchType: ProductsRequestParams.SearchType?,
            sub_category_name: String?,
            searchTerm: String?,
            isBrowsing: Boolean,
            sendDeliveryDetails: Boolean?,
            isNavigationFromSearchByKeyword: Boolean = false,
            screenViewEventData: ScreenViewEventData? = null
        ) = ProductListingFragment().withArgs {
            putString(SEARCH_TYPE, searchType?.name)
            putString(SUB_CATEGORY_NAME, sub_category_name)
            putString(SEARCH_TERM, searchTerm)
            putBoolean(IS_BROWSING, isBrowsing)
            putBoolean(EXTRA_SEND_DELIVERY_DETAILS_PARAMS, sendDeliveryDetails ?: false)
            putBoolean(BUNDLE_NAVIGATION_FROM_SEARCH_BY_KEYWORD, isNavigationFromSearchByKeyword)
            putParcelable(BUNDLE_SCREEN_VIEW_EVENT_DATA, screenViewEventData)
        }

        fun newInstance(
            searchType: ProductsRequestParams.SearchType?,
            searchTerm: String?,
            sub_category_name: String?,
            brandNavigationDetails: BrandNavigationDetails?,
            isBrowsing: Boolean,
            sendDeliveryDetails: Boolean?,
            isChanelPage: Boolean,
            screenViewEventData: ScreenViewEventData? = null
        ) = ProductListingFragment().withArgs {
            putString(SEARCH_TYPE, searchType?.name)
            putString(SEARCH_TERM, searchTerm)
            putString(SUB_CATEGORY_NAME, sub_category_name)
            putSerializable(BRAND_NAVIGATION_DETAILS, brandNavigationDetails)
            putBoolean(IS_BROWSING, isBrowsing)
            putBoolean(EXTRA_SEND_DELIVERY_DETAILS_PARAMS, sendDeliveryDetails ?: false)
            putBoolean(IS_CHANEL_PAGE, isChanelPage)
            putParcelable(BUNDLE_SCREEN_VIEW_EVENT_DATA, screenViewEventData)
        }

        fun newInstance(
            searchType: ProductsRequestParams.SearchType?,
            searchTerm: String?,
            sub_category_name: String?,
            sortOption: String,
            brandNavigationDetails: BrandNavigationDetails?,
            isBrowsing: Boolean,
            sendDeliveryDetails: Boolean?,
            isChanelPage: Boolean
        ) = ProductListingFragment().withArgs {
            putString(SEARCH_TYPE, searchType?.name)
            putString(SUB_CATEGORY_NAME, sub_category_name)
            putString(SEARCH_TERM, searchTerm)
            putString(SORT_OPTION, sortOption)
            putSerializable(BRAND_NAVIGATION_DETAILS, brandNavigationDetails)
            putBoolean(IS_BROWSING, isBrowsing)
            putBoolean(EXTRA_SEND_DELIVERY_DETAILS_PARAMS, sendDeliveryDetails == true)
            putBoolean(IS_CHANEL_PAGE, isChanelPage)
        }
    }

    private fun setUniqueIds() {
        resources.apply {
            binding.sortAndRefineLayout.apply {
                refineProducts.contentDescription = getString(R.string.plp_buttonRefine)
                sortProducts.contentDescription = getString(R.string.plp_buttonSort)
            }
            binding.productsRecyclerView.contentDescription =
                getString(R.string.plp_productListLayout)
        }
    }

    private fun requestCartSummary() {
        showProgressBar()
        GetCartSummary().getCartSummary(object : IResponseListener<CartSummaryResponse> {
            override fun onSuccess(response: CartSummaryResponse?) {
                dismissProgressBar()

                when (response?.httpCode) {
                    HTTP_OK -> {
                        // If user have location then call Confirm Place API else go to geoLocation Flow.
                        if (Utils.getPreferredDeliveryLocation() != null) {
                            updateToolbarTitle() // update plp location.
                            onConfirmLocation() // This will again call addToCart
                        } else
                            onSetNewLocation()
                    }
                }
            }

            override fun onFailure(error: Throwable?) {
                dismissProgressBar()
            }
        })
    }

    override fun onConfirmLocation() {
        mSelectedProductList?.let { productList ->
            mFulfilmentTypeId?.let {
                queryInventoryForStore(
                    it,
                    mAddItemToCart,
                    productList
                )
            }
        }
    }

    override fun onSetNewLocation() {
        activity?.apply {
            KotlinUtils.presentEditDeliveryGeoLocationActivity(
                this,
                SET_DELIVERY_LOCATION_REQUEST_CODE,
                KotlinUtils.getPreferredDeliveryType(),
                GeoUtils.getPlaceId()
            )
        }
    }

    override fun openProductDetailsView(
        productList: ProductList?,
        bannerLabel: String?,
        bannerImage: String?,
    ) {
        // From Chanel Horizontal Category click
        productList?.let { openProductDetailView(it, bannerLabel, bannerImage) }
    }

    override fun openBrandLandingPage() {
        (activity as? BottomNavigationActivity)?.apply {
            Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.BRAND_LANDING_PAGE_LOGO_IMAGE,
                hashMapOf(
                    FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                            FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_BRAND_LANDING_PAGE_LOGO_IMAGE
                ),
                activity
            )
            val brandNavigationDetails = BrandNavigationDetails()
            brandNavigationDetails.brandText =
                (arguments?.getSerializable(BRAND_NAVIGATION_DETAILS) as? BrandNavigationDetails)?.brandText
            pushFragment(
                newInstance(
                    ProductsRequestParams.SearchType.NAVIGATE,
                    searchTerm = brandNavigationDetails.brandText,
                    "",
                    brandNavigationDetails,
                    isUserBrowsing,
                    arguments?.getBoolean(EXTRA_SEND_DELIVERY_DETAILS_PARAMS, false),
                    isChanelPage
                )
            )
        }
    }

    override fun clickCategoryListViewCell(
        navigation: Navigation?,
        bannerImage: String?,
        bannerLabel: String?,
        isComingFromBLP: Boolean,
    ) {
        // From Chanel Vertical Category click
        (activity as? BottomNavigationActivity)?.apply {
            val isBrandLandingPage =
                (arguments?.getSerializable(BRAND_NAVIGATION_DETAILS) as? BrandNavigationDetails)
                    ?.isBrandLandingPage ?: false

            Utils.triggerFireBaseEvents(
                if (isBrandLandingPage)
                    FirebaseManagerAnalyticsProperties.BRAND_LANDING_PAGE_CATEGORY
                else
                    FirebaseManagerAnalyticsProperties.BRAND_LANDING_PAGE_SUB_CATEGORY,
                hashMapOf(
                    FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                            if (isBrandLandingPage)
                                FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_BRAND_LANDING_PAGE_CATEGORY
                            else
                                FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_BRAND_LANDING_PAGE_SUB_CATEGORY
                ),
                activity
            )

            val brandNavigationDetails = BrandNavigationDetails(
                brandText = (arguments?.getSerializable(BRAND_NAVIGATION_DETAILS) as? BrandNavigationDetails)?.brandText,
                displayName = navigation?.displayName,
                navigationState = navigation?.navigationState,
                bannerImage = bannerImage,
                bannerLabel = bannerLabel,
                isComingFromBLP = isComingFromBLP,
                filterContent = navigation?.filterContent ?: false
            )
            pushFragment(
                newInstance(
                    ProductsRequestParams.SearchType.NAVIGATE,
                    searchTerm = navigation?.displayName,
                    "",
                    brandNavigationDetails,
                    isUserBrowsing,
                    arguments?.getBoolean(EXTRA_SEND_DELIVERY_DETAILS_PARAMS, false),
                    isChanelPage
                )
            )
        }
    }

    override fun onAddToListClicked(productList: ProductList) {
        addItemToShoppingList(productList)
    }

    private fun addItemToShoppingList(productList: ProductList) {
        activity?.apply {
            Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.SHOPADDTOLIST,
                this
            )
        }
        mAddToListProduct = productList
        if (!SessionUtilities.getInstance().isUserAuthenticated) {
            ScreenManager.presentSSOSigninActivity(
                activity,
                SSO_REQUEST_ADD_TO_SHOPPING_LIST,
                isUserBrowsing
            )
        } else {
            val woolworthsApplication = WoolworthsApplication.getInstance()
            if (woolworthsApplication != null) {
                woolworthsApplication.wGlobalState.selectedSKUId = null
            }
            Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.MYCARTADDTOLIST,
                activity
            )
            val addToListRequests = ArrayList<AddToListRequest>()
            val listItem = AddToListRequest().apply {
                catalogRefId = productList.sku
                skuID = productList.sku
                giftListId = productList.sku
                quantity = "1"
            }
            addToListRequests.add(listItem)
            val analyticProductItemList = ArrayList<AnalyticProductItem>()
            val analyticProductItem = AnalyticProductItem(
                itemId = productList.productId,
                itemName = productList.productName,
                itemBrand = productList.brandText,
                itemVariant = productList.productVariants,
                category = mSubCategoryName,
                quantity = 1,
                price = productList.price?.toDouble(),
                affiliation = FirebaseManagerAnalyticsProperties.PropertyValues.AFFILIATION_VALUE,
                index = FirebaseManagerAnalyticsProperties.PropertyValues.INDEX_VALUE.toInt()
            )
            analyticProductItemList.add(analyticProductItem)
            val addToWishListEventData =
                AddToWishListFirebaseEventData(products = analyticProductItemList)
            KotlinUtils.openAddToListPopup(
                requireActivity(),
                requireActivity().supportFragmentManager,
                addToListRequests,
                eventData = addToWishListEventData
            )
        }
    }
}
