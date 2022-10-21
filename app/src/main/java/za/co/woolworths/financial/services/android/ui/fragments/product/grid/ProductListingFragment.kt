package za.co.woolworths.financial.services.android.ui.fragments.product.grid

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.text.method.LinkMovementMethod
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.JsonSyntaxException
import com.skydoves.balloon.balloon
import kotlinx.android.synthetic.main.blp_error_layout.view.*
import kotlinx.android.synthetic.main.fragment_brand_landing.view.*
import kotlinx.android.synthetic.main.grid_layout.*
import kotlinx.android.synthetic.main.no_connection_handler.*
import kotlinx.android.synthetic.main.no_connection_handler.view.*
import kotlinx.android.synthetic.main.promotional_text_plp.*
import kotlinx.android.synthetic.main.sort_and_refine_selection_layout.*
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.chanel.utils.ChanelUtils
import za.co.woolworths.financial.services.android.chanel.views.ChanelNavigationClickListener
import za.co.woolworths.financial.services.android.chanel.views.adapter.BrandLandingAdapter
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IProductListing
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.geolocation.GeoUtils
import za.co.woolworths.financial.services.android.geolocation.network.apihelper.GeoLocationApiHelper
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.geolocation.viewmodel.GeoLocationViewModelFactory
import za.co.woolworths.financial.services.android.geolocation.viewmodel.UnSellableItemsLiveData
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.BrandNavigationDetails
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.brandlandingpage.DynamicBanner
import za.co.woolworths.financial.services.android.models.dto.brandlandingpage.Navigation
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow.DISMISS_POP_WINDOW_CLICKED
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.ui.activities.WStockFinderActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.*
import za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity
import za.co.woolworths.financial.services.android.ui.adapters.ProductListingAdapter
import za.co.woolworths.financial.services.android.ui.adapters.SortOptionsAdapter
import za.co.woolworths.financial.services.android.ui.adapters.holder.ProductListingViewType
import za.co.woolworths.financial.services.android.ui.adapters.holder.RecyclerViewViewHolderItems
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.IOnConfirmDeliveryLocationActionListener
import za.co.woolworths.financial.services.android.ui.views.*
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ProductListingFindInStoreNoQuantityFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.SelectYourQuantityFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.SingleButtonDialogFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_EXPECTATION_FAILED_417
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_OK
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_SESSION_TIMEOUT_440
import za.co.woolworths.financial.services.android.util.AppConstant.Keys.Companion.EXTRA_SEND_DELIVERY_DETAILS_PARAMS
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.saveAnonymousUserLocationDetails
import za.co.woolworths.financial.services.android.util.analytics.AnalyticsManager
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager.Companion.logException
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager.Companion.setCrashlyticsString
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.*


open class ProductListingFragment : ProductListingExtensionFragment(), GridNavigator,
    IProductListing, View.OnClickListener, SortOptionsAdapter.OnSortOptionSelected,
    WMaterialShowcaseView.IWalkthroughActionListener,
    IOnConfirmDeliveryLocationActionListener, ChanelNavigationClickListener {

    private var state: Parcelable? = null
    private var LOGIN_REQUEST_SUBURB_CHANGE = 1419
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
    private lateinit var confirmAddressViewModel: ConfirmAddressViewModel
    private var localDeliveryType: String? = null
    private var localDeliveryTypeForHiddenChange: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.apply {
            arguments?.apply {
                mSubCategoryName = getString(SUB_CATEGORY_NAME, "")
                isUserBrowsing = getBoolean(IS_BROWSING, false)
                mSearchType =
                    ProductsRequestParams.SearchType.valueOf(getString(SEARCH_TYPE, "SEARCH"))
                mSearchTerm = getString(SEARCH_TERM, "")
                mSortOption = getString(SORT_OPTION, "")

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
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.grid_layout, container, false)
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
            mErrorHandlerView = ErrorHandlerView(this, no_connection_layout)
            mErrorHandlerView?.setMargin(no_connection_layout, 0, 0, 0, 0)

            toolbarTitleText =
                if (mSubCategoryName?.isEmpty() == true) mSearchTerm else mSubCategoryName
            updateToolbarTitle()
            setUpConfirmAddressViewModel()
            startProductRequest()
            setUniqueIds()
            addFragmentListner()
            isUnSellableItemsRemoved()
            localPlaceId = KotlinUtils.getPreferredPlaceId()
            localDeliveryType = KotlinUtils.getDeliveryType()?.deliveryType

        }

        toolbarPLPAddress?.setOnClickListener(this)
        toolbarPLPTitle?.setOnClickListener(this)
        plpSearchIcon?.setOnClickListener(this)
        plpBackIcon?.setOnClickListener(this)

        layout_error_blp?.blp_error_back_btn?.setOnClickListener {
            (activity as? BottomNavigationActivity)?.popFragment()
        }

        layout_error_blp?.btn_retry_it?.setOnClickListener {
            startProductRequest()
        }
    }

    private fun setUpConfirmAddressViewModel() {
        confirmAddressViewModel = ViewModelProvider(
            this,
            GeoLocationViewModelFactory(GeoLocationApiHelper())
        ).get(ConfirmAddressViewModel::class.java)
    }

    private fun showPromotionalBanner(response: ProductView) {
        promotionalTextBannerLayout?.visibility = VISIBLE
        val htmlDataPromotionalText = response.richText
        promotionalTextDesc?.text =
            HtmlCompat.fromHtml(htmlDataPromotionalText, HtmlCompat.FROM_HTML_MODE_LEGACY)
        promotionalTextDesc.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun addFragmentListner() {
        setFragmentResultListener(CustomBottomSheetDialogFragment.DIALOG_BUTTON_CLICK_RESULT) { _, _ ->
            // As User selects to change the delivery location. So we will call confirm place API and will change the users location.
            getUpdatedValidateResponse()
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
                                        KotlinUtils.browsingDeliveryType?.name
                                    )
                                }
                            } else
                                callConfirmPlace()
                        }
                    }
                }
            } catch (e: Exception) {
                FirebaseManager.logException(e)
                dismissProgressBar()
            } catch (e: JsonSyntaxException) {
                FirebaseManager.logException(e)
                dismissProgressBar()
            }
        }
    }

    private fun isUnSellableItemsRemoved() {
        UnSellableItemsLiveData.observe(viewLifecycleOwner) {
            isUnSellableItemsRemoved = it
            if (isUnSellableItemsRemoved == true && (activity as? BottomNavigationActivity)?.mNavController?.currentFrag is ProductListingFragment) {
                callConfirmPlace()
                UnSellableItemsLiveData.value = false
            }
        }
    }

    private fun navigateToUnsellableItemsFragment(
        unSellableCommerceItems: ArrayList<UnSellableCommerceItem>, deliveryType: String?,
    ) {
        deliveryType?.let {
            val unsellableItemsBottomSheetDialog =
                UnsellableItemsBottomSheetDialog.newInstance(unSellableCommerceItems, it)
            unsellableItemsBottomSheetDialog.show(
                requireFragmentManager(),
                UnsellableItemsBottomSheetDialog::class.java.simpleName
            )
        }
    }

    private fun callConfirmPlace() {
        // Confirm the location
        lifecycleScope.launch {
            showProgressBar()
            try {
                val confirmLocationRequest =
                    KotlinUtils.getConfirmLocationRequest(KotlinUtils.browsingDeliveryType)
                val confirmLocationResponse =
                    confirmAddressViewModel.postConfirmAddress(confirmLocationRequest)
                dismissProgressBar()
                if (confirmLocationResponse != null) {
                    when (confirmLocationResponse.httpCode) {
                        HTTP_OK -> {
                            if (SessionUtilities.getInstance().isUserAuthenticated) {
                                Utils.savePreferredDeliveryLocation(
                                    ShoppingDeliveryLocation(
                                        confirmLocationResponse.orderSummary?.fulfillmentDetails
                                    )
                                )
                                if (KotlinUtils.getAnonymousUserLocationDetails() != null)
                                    KotlinUtils.clearAnonymousUserLocationDetails()
                            } else {
                                saveAnonymousUserLocationDetails(
                                    ShoppingDeliveryLocation(
                                        confirmLocationResponse.orderSummary?.fulfillmentDetails
                                    )
                                )
                            }

                            val savedPlaceId = KotlinUtils.getDeliveryType()?.address?.placeId
                            KotlinUtils.apply {
                                this.placeId = confirmLocationRequest.address.placeId
                                isLocationSame =
                                    confirmLocationRequest.address.placeId?.equals(savedPlaceId)
                            }

                            setBrowsingData()
                            updateToolbarTitle() // update plp location.
                            onConfirmLocation() // This will again call addToCart
                        }
                    }
                }
            } catch (e: Exception) {
                FirebaseManager.logException(e)
                dismissProgressBar()
            }
        }
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
        incNoConnectionHandler?.btnRetry?.setOnClickListener(this@ProductListingFragment)
        refineProducts?.setOnClickListener(this@ProductListingFragment)
        sortProducts?.setOnClickListener(this@ProductListingFragment)
    }

    override fun onResume() {
        super.onResume()
        activity?.let { activity ->
            Utils.setScreenName(
                activity,
                FirebaseManagerAnalyticsProperties.ScreenNames.PRODUCT_SEARCH_RESULTS
            )
        }

        val arguments = HashMap<String, String>()
        arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ITEM_LIST_NAME] =
            mSubCategoryName!!
        Utils.triggerFireBaseEvents(
            FirebaseManagerAnalyticsProperties.VIEW_ITEM_LIST,
            arguments,
            activity
        )

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
                    arguments?.getBoolean(EXTRA_SEND_DELIVERY_DETAILS_PARAMS, false)
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
        toolbarPLPTitle.text =
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
                toolbarPLPAddress.text = requireContext().getString(R.string.standard_delivery)
                toolbarPLPIcon?.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_delivery_circle
                    )
                )
            }
            Delivery.CNC.type -> {
                this.deliveryType = Delivery.CNC
                toolbarPLPAddress.text = requireContext().getString(R.string.click_collect)
                toolbarPLPIcon?.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_collection_circle
                    )
                )
            }
            Delivery.DASH.type -> {
                this.deliveryType = Delivery.DASH
                toolbarPLPAddress.text = requireContext().getString(R.string.dash_delivery)
                toolbarPLPIcon?.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_dash_delivery_circle
                    )
                )
            }
            else -> {
                this.deliveryType = Delivery.STANDARD
                toolbarPLPAddress.text = requireContext().getString(R.string.standard_delivery)
                toolbarPLPIcon?.setImageDrawable(
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
        plp_relativeLayout?.visibility = VISIBLE

        if (!response.richText.isNullOrEmpty()) {
            showPromotionalBanner(response)
        }

        val productLists = response.products
        if (mProductList?.isNullOrEmpty() == true)

            mProductList = ArrayList()
        response.history?.apply {
            if (categoryDimensions?.isNullOrEmpty() == false) {
                mSubCategoryName = categoryDimensions[categoryDimensions.size - 1].label
            } else if (searchCrumbs?.isNullOrEmpty() == false) {
                searchCrumbs?.let {
                    mSubCategoryName = it[it.size - 1].terms
                }
            }
        }

        if (productLists?.isEmpty() == true) {
            sortAndRefineLayout?.visibility = GONE
            if (!listContainHeader()) {
                val headerProduct = ProductList()
                headerProduct.rowType = ProductListingViewType.HEADER
                headerProduct.numberOfItems = numItemsInTotal
                productLists.add(0, headerProduct)
            }
            bindRecyclerViewWithUI(productLists)

        } else {
            this.productView = null
            this.productView = response
            hideFooterView()
            if (!loadMoreData) {
                sortAndRefineLayout?.visibility = VISIBLE
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
                                AddItemToCart(productList.productId, productList.sku, 0),
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
    }

    private fun onChanelSuccess(response: ProductView) {
        chanel_layout?.visibility = VISIBLE
        plp_relativeLayout?.visibility = GONE
        val brandLandingAdapter = BrandLandingAdapter(
            context,
            response.dynamicBanners as List<DynamicBanner?>, this
        )
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        chanel_layout?.rv_chanel?.layoutManager = layoutManager
        chanel_layout?.rv_chanel?.setHasFixedSize(true)
        chanel_layout?.rv_chanel?.adapter = brandLandingAdapter

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
            chanel_layout?.visibility = GONE
            plp_relativeLayout?.visibility = GONE
            layout_error_blp?.visibility = VISIBLE
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
            FirebaseManager.logException(ex)
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
        OneAppService.cancelRequest(loadProductRequest)
    }

    override fun bindRecyclerViewWithUI(productLists: MutableList<ProductList>) {
        mProductList?.clear()
        mProductList = ArrayList()
        mProductList = productLists
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
                mIsComingFromBLP
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
                    mIsComingFromBLP
                )
            }
        productsRecyclerView?.apply {
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
            filterContent
        )
    }

    override fun onLoadStart(isLoadMore: Boolean) {
        setIsLoading(true)
        if (!isLoadMore) {
            incCenteredProgress?.visibility = VISIBLE
        }
    }

    override fun onLoadComplete(isLoadMore: Boolean) {
        setIsLoading(false)
        if (!isLoadMore) {
            incCenteredProgress?.visibility = GONE
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
                    setSupportActionBar(toolbarPLP)
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
        }
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
            PDP_REQUEST_CODE, OPEN_CART_REQUEST -> {
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
            BundleKeysConstants.REQUEST_CODE -> {
                updateToolbarTitle()
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
        productsRecyclerView?.visibility = View.INVISIBLE
        sortAndRefineLayout?.visibility = GONE
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
                    .setTarget(refineDownArrow)
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
        if (refineProducts?.isClickable == true)
            refineProducts?.let { refineProducts -> onClick(refineProducts) }
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
        refineProducts?.isEnabled = refinementViewState
        refineDownArrow?.isEnabled = refinementViewState
        refinementText?.isEnabled = refinementViewState
        (activity as? BottomNavigationActivity)?.apply {
            when (refinementViewState) {
                true -> unLockDrawerFragment()
                false -> lockDrawerFragment()
            }
        }

    }

    override fun openProductDetailView(productList: ProductList) {
        //firebase event select_item
        state = productsRecyclerView.layoutManager?.onSaveInstanceState()
        val selectItemParams = Bundle()
        selectItemParams.putString(
            FirebaseManagerAnalyticsProperties.PropertyNames.ITEM_LIST_NAME,
            mSubCategoryName
        )
        selectItemParams.putString(
            FirebaseManagerAnalyticsProperties.PropertyNames.ITEM_BRAND,
            productList.brandText
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
        state = productsRecyclerView.layoutManager?.onSaveInstanceState()
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
        if (incCenteredProgress?.visibility == VISIBLE) return // ensure one api runs at a time
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
        OneAppService.getInventorySkuForStore(
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
                                    AddItemToCart(
                                        addItemToCart?.productId,
                                        addItemToCart?.catalogRefId,
                                        1
                                    )
                                )
                            } else {
                                val cartItem = AddItemToCart(
                                    addItemToCart?.productId
                                        ?: "", addItemToCart?.catalogRefId
                                        ?: "", skuInventoryList[0].quantity
                                )
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
                                    FirebaseManager.logException(ex)
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
        incCenteredProgress?.visibility = VISIBLE
    }

    private fun dismissProgressBar() {
        // hide progress bar
        incCenteredProgress?.visibility = GONE
        mProductAdapter?.resetQuickShopButton()
    }

    override fun addFoodProductTypeToCart(addItemToCart: AddItemToCart?) {
        showProgressBar()
        mAddItemsToCart = mutableListOf()
        addItemToCart?.let { cartItem -> mAddItemsToCart?.add(cartItem) }
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
                                            productsRecyclerView,
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
                            KotlinUtils.showQuantityLimitErrror(
                                activity?.supportFragmentManager,
                                addItemToCartResponse.response.desc,
                                "",
                                context
                            )
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
            FirebaseManager.logException(ex)
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
            OneAppService.getLocationsItem(
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
                arguments?.getBoolean(EXTRA_SEND_DELIVERY_DETAILS_PARAMS, false)
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
        private const val SORT_OPTION = "SORT_OPTION"
        private const val BRAND_NAVIGATION_DETAILS = "BRAND_NAVIGATION_DETAILS"

        fun newInstance(
            searchType: ProductsRequestParams.SearchType?,
            sub_category_name: String?,
            searchTerm: String?,
            isBrowsing: Boolean,
            sendDeliveryDetails: Boolean?
        ) = ProductListingFragment().withArgs {
            putString(SEARCH_TYPE, searchType?.name)
            putString(SUB_CATEGORY_NAME, sub_category_name)
            putString(SEARCH_TERM, searchTerm)
            putBoolean(IS_BROWSING, isBrowsing)
            putBoolean(EXTRA_SEND_DELIVERY_DETAILS_PARAMS, sendDeliveryDetails ?: false)
        }

        fun newInstance(
            searchType: ProductsRequestParams.SearchType?,
            searchTerm: String?,
            sub_category_name: String?,
            brandNavigationDetails: BrandNavigationDetails?,
            isBrowsing: Boolean,
            sendDeliveryDetails: Boolean?
        ) = ProductListingFragment().withArgs {
            putString(SEARCH_TYPE, searchType?.name)
            putString(SEARCH_TERM, searchTerm)
            putString(SUB_CATEGORY_NAME, sub_category_name)
            putSerializable(BRAND_NAVIGATION_DETAILS, brandNavigationDetails)
            putBoolean(IS_BROWSING, isBrowsing)
            putBoolean(EXTRA_SEND_DELIVERY_DETAILS_PARAMS, sendDeliveryDetails ?: false)
        }

        fun newInstance(
            searchType: ProductsRequestParams.SearchType?,
            searchTerm: String?,
            sub_category_name: String?,
            sortOption: String,
            brandNavigationDetails: BrandNavigationDetails?,
            isBrowsing: Boolean,
            sendDeliveryDetails: Boolean?
        ) = ProductListingFragment().withArgs {
            putString(SEARCH_TYPE, searchType?.name)
            putString(SUB_CATEGORY_NAME, sub_category_name)
            putString(SEARCH_TERM, searchTerm)
            putString(SORT_OPTION, sortOption)
            putSerializable(BRAND_NAVIGATION_DETAILS, brandNavigationDetails)
            putBoolean(IS_BROWSING, isBrowsing)
            putBoolean(EXTRA_SEND_DELIVERY_DETAILS_PARAMS, sendDeliveryDetails == true)
        }
    }

    private fun setUniqueIds() {
        resources.apply {
            refineProducts?.contentDescription = getString(R.string.plp_buttonRefine)
            sortProducts?.contentDescription = getString(R.string.plp_buttonSort)
            productsRecyclerView?.contentDescription = getString(R.string.plp_productListLayout)
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
                    arguments?.getBoolean(EXTRA_SEND_DELIVERY_DETAILS_PARAMS, false)
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
                    arguments?.getBoolean(EXTRA_SEND_DELIVERY_DETAILS_PARAMS, false)
                )
            )
        }
    }
}
