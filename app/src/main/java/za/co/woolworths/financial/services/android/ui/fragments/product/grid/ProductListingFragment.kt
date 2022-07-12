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
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.google.firebase.analytics.FirebaseAnalytics
import com.skydoves.balloon.balloon
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.blp_error_layout.view.*
import kotlinx.android.synthetic.main.fragment_brand_landing.*
import kotlinx.android.synthetic.main.fragment_brand_landing.view.*
import kotlinx.android.synthetic.main.grid_layout.*
import kotlinx.android.synthetic.main.grid_layout.incCenteredProgress
import kotlinx.android.synthetic.main.grid_layout.incNoConnectionHandler
import kotlinx.android.synthetic.main.grid_layout.sortAndRefineLayout
import kotlinx.android.synthetic.main.grid_layout.vtoTryItOnBanner
import kotlinx.android.synthetic.main.no_connection_handler.*
import kotlinx.android.synthetic.main.no_connection_handler.view.*
import kotlinx.android.synthetic.main.search_result_fragment.*
import kotlinx.android.synthetic.main.sort_and_refine_selection_layout.*
import kotlinx.android.synthetic.main.try_it_on_banner.*
import za.co.woolworths.financial.services.android.chanel.utils.ChanelUtils
import za.co.woolworths.financial.services.android.chanel.views.ChanelNavigationClickListener
import za.co.woolworths.financial.services.android.chanel.views.adapter.BrandLandingAdapter
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IProductListing
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.geolocation.GeoUtils
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
import za.co.woolworths.financial.services.android.ui.fragments.click_and_collect.DeliveryOrClickAndCollectSelectorDialogFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.IOnConfirmDeliveryLocationActionListener
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.dialog.ConfirmDeliveryLocationFragment
import za.co.woolworths.financial.services.android.ui.views.AddedToCartBalloonFactory
import za.co.woolworths.financial.services.android.ui.views.ToastFactory
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ProductListingFindInStoreNoQuantityFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.SelectYourQuantityFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.SingleButtonDialogFragment
import za.co.woolworths.financial.services.android.ui.vto.di.qualifier.OpenTermAndLighting
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.VtoBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.vto.utils.VirtualTryOnUtil
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_EXPECTATION_FAILED_417
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_OK
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_SESSION_TIMEOUT_440
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.VTO
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.REQUEST_CODE
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@AndroidEntryPoint
open class ProductListingFragment : ProductListingExtensionFragment(), GridNavigator,
    IProductListing, View.OnClickListener, SortOptionsAdapter.OnSortOptionSelected,
    WMaterialShowcaseView.IWalkthroughActionListener,
    DeliveryOrClickAndCollectSelectorDialogFragment.IDeliveryOptionSelection,
    IOnConfirmDeliveryLocationActionListener, ChanelNavigationClickListener {

    private var state: Parcelable? = null
    private val BUNDLE_RECYCLER_LAYOUT = "ProductListingFragment.SearchProduct"
    private var EDIT_LOCATION_LOGIN_REQUEST = 1919
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
    private var menuActionSearch: MenuItem? = null
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
    private var mIsComingFromBLP: Boolean = false
    private var liquorDialog: Dialog? = null

    @OpenTermAndLighting
    @Inject
    lateinit var vtoBottomSheetDialog: VtoBottomSheetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        activity?.apply {
            arguments?.apply {
                mSubCategoryName = getString(SUB_CATEGORY_NAME, "")
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
            isReloadNeeded = true
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
            showToolbar()
            showBackNavigationIcon(true)
            showBottomNavigationMenu()
            setToolbarBackgroundDrawable(R.drawable.appbar_background)

            toolbar?.setNavigationOnClickListener { popFragment() }

            mErrorHandlerView = ErrorHandlerView(this, no_connection_layout)
            mErrorHandlerView?.setMargin(no_connection_layout, 0, 0, 0, 0)

            toolbarTitleText =
                if (mSubCategoryName?.isEmpty() == true) mSearchTerm else mSubCategoryName
            setTitle()
            startProductRequest()
            setUniqueIds()
            localPlaceId = KotlinUtils.getPreferredPlaceId()
            imgInfo?.setOnClickListener {
                vtoBottomSheetDialog.showBottomSheetDialog(
                    this@ProductListingFragment,
                    requireActivity(),
                    true
                )
            }
        }

        layout_error_blp?.blp_error_back_btn?.setOnClickListener {
            (activity as? BottomNavigationActivity)?.popFragment()
        }

        layout_error_blp?.btn_retry_it?.setOnClickListener {
            startProductRequest()
        }
    }

    private fun showVtoBanner() {
        if (!mSubCategoryName.isNullOrEmpty() && mSubCategoryName.equals(VTO) && VirtualTryOnUtil.isVtoConfigAvailable()) {
            vtoTryItOnBanner.visibility = View.VISIBLE
        }
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
        arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ITEM_LIST_NAME] = mSubCategoryName!!
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.VIEW_ITEM_LIST,arguments, activity)

        if (activity is BottomNavigationActivity && (activity as BottomNavigationActivity).currentFragment is ProductListingFragment) {
            val currentPlaceId = KotlinUtils.getPreferredPlaceId()
            if (currentPlaceId == null && !(currentPlaceId?.equals(localPlaceId))!!) {
                localPlaceId = currentPlaceId
                isReloadNeeded = false
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
                    )
                )
            )
        }
    }

    private fun updateRequestForReload() {
        if (localProductBody.isNotEmpty()) {
            val list: HashMap<String, Any> =
                (localProductBody.get(localProductBody.lastIndex) as HashMap<String, Any>)
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

    fun setTitle() {
        if ((activity as? BottomNavigationActivity)?.currentFragment !is ProductListingFragment) {
            return
        }

        (activity as? BottomNavigationActivity)?.setTitle(toolbarTitleText)
    }

    override fun onLoadProductSuccess(response: ProductView, loadMoreData: Boolean) {

        if (response.isBanners) {
            if (!response.dynamicBanners.isNullOrEmpty()) {
                onChanelSuccess(response)
            }
            return
        }
        plp_relativeLayout?.visibility = View.VISIBLE
        showVtoBanner()
        val productLists = response.products
        if (mProductList?.isNullOrEmpty() == true)

            mProductList = ArrayList()
        response.history?.apply {
            if (!categoryDimensions?.isNullOrEmpty()) {
                mSubCategoryName = categoryDimensions.get(categoryDimensions.size - 1).label
            } else if (searchCrumbs?.isNullOrEmpty() == false) {
                searchCrumbs?.let {
                    mSubCategoryName = it.get(it.size - 1).terms
                }
            }
        }

        if (productLists?.isEmpty() == true) {
            sortAndRefineLayout?.visibility = GONE
            vtoTryItOnBanner?.visibility = GONE
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
                if (!Utils.isDeliverySelectionModalShown()) {
                    showDeliveryOptionDialog()
                }

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

        toolbarTitleText = response?.pageHeading ?: mSearchTerm
        setTitle()
    }

    override fun showLiquorDialog() {

        liquorDialog = activity?.let { activity -> Dialog(activity) }
        liquorDialog?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            val view = layoutInflater.inflate(R.layout.liquor_info_dialog, null)
            val desc = view.findViewById<TextView>(R.id.desc)
            val close = view.findViewById<Button>(R.id.close)
            val setSuburb = view.findViewById<TextView>(R.id.setSuburb)
            desc?.text = AppConfigSingleton.liquor?.message ?: ""
            close?.setOnClickListener { dismiss() }
            setSuburb?.setOnClickListener {
                dismiss()
                if (!SessionUtilities.getInstance().isUserAuthenticated) {
                    ScreenManager.presentSSOSignin(activity, LOGIN_REQUEST_SUBURB_CHANGE)
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
            setTitle()
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
            if(state!=null) {
                layoutManager?.onRestoreInstanceState(state)
                state=null
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
            //and therefor we can most likely expect a IndexOutOfBoundsExeption
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.drill_down_category_menu, menu)
        menuActionSearch = menu.findItem(R.id.action_drill_search)
        menuActionSearch?.isVisible =
            (activity as? BottomNavigationActivity)?.currentFragment is ProductListingFragment
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_drill_search -> {
                activity?.apply {
                    val openSearchActivity = Intent(this, ProductSearchActivity::class.java)
                    startActivity(openSearchActivity)
                    overridePendingTransition(0, 0)
                }
                val arguments = HashMap<String, String>()
                arguments[FirebaseManagerAnalyticsProperties.PropertyNames.SEARCH_TERM] = mSearchTerm.toString()
                arguments[FirebaseManagerAnalyticsProperties.PropertyNames.SEARCH_TYPE] = mSearchType.toString()
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SEARCH, arguments, activity)
                true
            }
            else -> false
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

                else -> return
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        setHasOptionsMenu(true)
        (activity as? BottomNavigationActivity)?.apply {
            when (hidden) {
                true -> lockDrawerFragment()
                else -> {
                    showToolbar()
                    showBottomNavigationMenu()
                    showBackNavigationIcon(true)
                    setToolbarBackgroundDrawable(R.drawable.appbar_background)
                    setTitle()

                    if (!localProductBody.isEmpty() && isBackPressed) {
                        localProductBody.removeLast()
                        isBackPressed = false
                    }
                    if (isReloadNeeded) {
                        updateRequestForReload()
                        reloadProductsWithSortAndFilter()
                    }
                    isReloadNeeded = true
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
                    if (Utils.getPreferredDeliveryLocation() != null)
                        mSelectedProductList?.let { productList ->
                            mFulfilmentTypeId?.let {
                                queryInventoryForStore(
                                    it,
                                    mAddItemToCart,
                                    productList
                                )
                            }
                        }
                    else
                        requestCartSummary()
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
            else -> return
        }
    }

    private fun reloadProductsWithSortAndFilter() {
        productsRecyclerView?.visibility = View.INVISIBLE
        sortAndRefineLayout?.visibility = View.GONE
        vtoTryItOnBanner?.visibility = GONE
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

    override fun onPromptDismiss() {

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
        val mFirebaseAnalytics = FirebaseManager.getInstance().getAnalytics()
        val selectItemParams = Bundle()
        selectItemParams.putString(FirebaseManagerAnalyticsProperties.PropertyNames.ITEM_LIST_NAME, mSubCategoryName)
        selectItemParams.putString(FirebaseManagerAnalyticsProperties.PropertyNames.ITEM_BRAND, productList.brandText)
        for (products in 0..(mProductList?.size ?: 0)) {
            val selectItem = Bundle()
            selectItem.putString(FirebaseAnalytics.Param.ITEM_ID, productList.productId)
            selectItem.putString(FirebaseAnalytics.Param.ITEM_NAME, productList.productName)
            selectItem.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, mSubCategoryName)
            selectItem.putString(FirebaseAnalytics.Param.ITEM_VARIANT, productList.productVariants)
            selectItem.putString(FirebaseAnalytics.Param.PRICE, productList.price.toString())
            selectItemParams.putParcelableArray(FirebaseAnalytics.Param.ITEMS, arrayOf(selectItem))
        }
        mFirebaseAnalytics.logEvent(FirebaseManagerAnalyticsProperties.SELECT_ITEM_EVENT, selectItemParams)

        val title = if (mSearchTerm?.isNotEmpty() == true) mSearchTerm else mSubCategoryName
        (activity as? BottomNavigationActivity)?.openProductDetailFragment(
            title,
            productList,
            mBannerLabel,
            mBannerImage
        )
    }

    fun openProductDetailView(
        productList: ProductList,
        bannerLabel: String?,
        bannerImage: String?
    ) {
        state = productsRecyclerView.layoutManager?.onSaveInstanceState()
        val title = if (mSearchTerm?.isNotEmpty() == true) mSearchTerm else mSubCategoryName
        (activity as? BottomNavigationActivity)?.openProductDetailFragment(
            title,
            productList,
            bannerLabel,
            bannerImage
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
            ScreenManager.presentSSOSignin(activity, QUERY_INVENTORY_FOR_STORE_REQUEST_CODE)
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

        showProgressBar()
        OneAppService.getInventorySkuForStore(
            mStoreId, addItemToCart?.catalogRefId
                ?: ""
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
                            if (KotlinUtils.isDeliveryOptionClickAndCollect() && addItemToCartResponse.data[0]?.productCountMap?.quantityLimit?.foodLayoutColour != null) {
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
                )
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
        private var isReloadNeeded = true
        private var localProductBody: ArrayList<Any> = ArrayList()
        private var localPlaceId: String? = null
        private var isBackPressed: Boolean = false

        /*const val REFINEMENT_DATA = "REFINEMENT_DATA"*/
        const val PRODUCTS_REQUEST_PARAMS = "PRODUCTS_REQUEST_PARAMS"
        private const val SUB_CATEGORY_NAME = "SUB_CATEGORY_NAME"

        const val REFINE_REQUEST_CODE = 77
        private const val QUERY_INVENTORY_FOR_STORE_REQUEST_CODE = 3343
        private const val QUERY_LOCATION_ITEM_REQUEST_CODE = 3344
        const val SET_DELIVERY_LOCATION_REQUEST_CODE = 180

        private const val SEARCH_TYPE = "SEARCH_TYPE"
        private const val SEARCH_TERM = "SEARCH_TERM"
        private const val SORT_OPTION = "SORT_OPTION"
        private const val BRAND_NAVIGATION_DETAILS = "BRAND_NAVIGATION_DETAILS"

        fun newInstance(
            searchType: ProductsRequestParams.SearchType?,
            sub_category_name: String?,
            searchTerm: String?,
        ) = ProductListingFragment().withArgs {
            putString(SEARCH_TYPE, searchType?.name)
            putString(SUB_CATEGORY_NAME, sub_category_name)
            putString(SEARCH_TERM, searchTerm)
        }

        fun newInstance(
            searchType: ProductsRequestParams.SearchType?,
            searchTerm: String?,
            sub_category_name: String?,
            brandNavigationDetails: BrandNavigationDetails?
        ) = ProductListingFragment().withArgs {
            putString(SEARCH_TYPE, searchType?.name)
            putString(SEARCH_TERM, searchTerm)
            putString(SUB_CATEGORY_NAME, sub_category_name)
            putSerializable(BRAND_NAVIGATION_DETAILS, brandNavigationDetails)
        }

        fun newInstance(
            searchType: ProductsRequestParams.SearchType?,
            searchTerm: String?,
            sub_category_name: String?,
            sortOption: String,
            brandNavigationDetails: BrandNavigationDetails?
        ) = ProductListingFragment().withArgs {
            putString(SEARCH_TYPE, searchType?.name)
            putString(SUB_CATEGORY_NAME, sub_category_name)
            putString(SEARCH_TERM, searchTerm)
            putString(SORT_OPTION, sortOption)
            putSerializable(BRAND_NAVIGATION_DETAILS, brandNavigationDetails)
        }
    }

    private fun setUniqueIds() {
        resources.apply {
            refineProducts?.contentDescription = getString(R.string.plp_buttonRefine)
            sortProducts?.contentDescription = getString(R.string.plp_buttonSort)
            productsRecyclerView?.contentDescription = getString(R.string.plp_productListLayout)
        }
    }

    private fun showDeliveryOptionDialog() {
        lifecycleScope.launchWhenResumed {
            (activity as? AppCompatActivity)?.supportFragmentManager?.beginTransaction()
                ?.let { fragmentTransaction ->
                    DeliveryOrClickAndCollectSelectorDialogFragment.newInstance(this@ProductListingFragment)
                        .show(
                            fragmentTransaction,
                            DeliveryOrClickAndCollectSelectorDialogFragment::class.java.simpleName
                        )
                }
        }
    }

    override fun onDeliveryOptionSelected(deliveryType: Delivery) {
        activity?.apply {
            KotlinUtils.presentEditDeliveryGeoLocationActivity(
                this,
                REQUEST_CODE,
                deliveryType,
                Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId
            )
        }
    }


    private fun requestCartSummary() {
        showProgressBar()
        GetCartSummary().getCartSummary(object : IResponseListener<CartSummaryResponse> {
            override fun onSuccess(response: CartSummaryResponse?) {
                dismissProgressBar()

                when (response?.httpCode) {
                    HTTP_OK -> {
                        confirmDeliveryLocation()
                    }
                }
            }

            override fun onFailure(error: Throwable?) {
                dismissProgressBar()
            }

        })
    }

    fun confirmDeliveryLocation() {
        this.childFragmentManager.apply {
            ConfirmDeliveryLocationFragment.newInstance().let {
                it.isCancelable = false
                it.show(this, ConfirmDeliveryLocationFragment::class.java.simpleName)
            }
        }
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
        bannerImage: String?
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
                    brandNavigationDetails
                )
            )
        }
    }

    override fun clickCategoryListViewCell(
        navigation: Navigation?,
        bannerImage: String?,
        bannerLabel: String?,
        isComingFromBLP: Boolean
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
                    brandNavigationDetails
                )
            )
        }
    }
}
