package za.co.woolworths.financial.services.android.ui.fragments.product.grid

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import android.util.Log
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction

import com.awfs.coordination.R
import com.crashlytics.android.Crashlytics
import com.skydoves.balloon.balloon
import kotlinx.android.synthetic.main.grid_layout.*
import kotlinx.android.synthetic.main.no_connection_handler.*
import kotlinx.android.synthetic.main.no_connection_handler.view.*
import kotlinx.android.synthetic.main.sort_and_refine_selection_layout.*

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IProductListing
import za.co.woolworths.financial.services.android.contracts.RequestListener
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.CartActivity
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.ui.activities.WStockFinderActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.OPEN_CART_REQUEST
import za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity
import za.co.woolworths.financial.services.android.ui.activities.product.refine.ProductsRefineActivity
import za.co.woolworths.financial.services.android.ui.adapters.ProductListingAdapter
import za.co.woolworths.financial.services.android.ui.adapters.SortOptionsAdapter
import za.co.woolworths.financial.services.android.ui.adapters.holder.ProductListingViewType
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView

import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragmentNew.SET_DELIVERY_LOCATION_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.views.AddedToCartBalloonFactory
import za.co.woolworths.financial.services.android.ui.views.actionsheet.*
import za.co.woolworths.financial.services.android.util.*
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.*

open class ProductListingFragment : ProductListingExtensionFragment(), GridNavigator, IProductListing, View.OnClickListener, SortOptionsAdapter.OnSortOptionSelected, WMaterialShowcaseView.IWalkthroughActionListener {

    private var oneTimeInventoryErrorDialogDisplay: Boolean = false
    private var mAddItemsToCart: MutableList<AddItemToCart>? = null
    private var mErrorHandlerView: ErrorHandlerView? = null
    private var mSubCategoryName: String? = null
    private var mProductAdapter: ProductListingAdapter? = null
    private var mProductList: MutableList<ProductList>? = null
    private var mRecyclerViewLayoutManager: GridLayoutManager? = null
    private var lastVisibleItem: Int = 0
    internal var totalItemCount: Int = 0
    private var productView: ProductView? = null
    private var sortOptionDialog: Dialog? = null
    private var mStoreId: String? = null
    private var mAddItemToCart: AddItemToCart? = null
    private var mSelectedProductList: ProductList? = null
    private var mSearchType: ProductsRequestParams.SearchType? = null
    private var mSearchTerm: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        activity?.apply {
            arguments?.apply {
                mSubCategoryName = getString(SUB_CATEGORY_NAME, "")
                mSearchType = ProductsRequestParams.SearchType.valueOf(getString(SEARCH_TYPE, "SEARCH"))
                mSearchTerm = getString(SEARCH_TERM, "")
            }
            setProductBody()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.grid_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigator = this
        (activity as? BottomNavigationActivity)?.apply {
            showToolbar()
            showBackNavigationIcon(true)
            setToolbarBackgroundDrawable(R.drawable.appbar_background)

            toolbar?.setNavigationOnClickListener { popFragment() }

            mErrorHandlerView = ErrorHandlerView(this, no_connection_layout)
            mErrorHandlerView?.setMargin(no_connection_layout, 0, 0, 0, 0)

            setTitle()
            startProductRequest()
            incNoConnectionHandler?.btnRetry?.setOnClickListener(this@ProductListingFragment)
            refineProducts?.setOnClickListener(this@ProductListingFragment)
            sortProducts?.setOnClickListener(this@ProductListingFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.let { activity -> Utils.setScreenName(activity, FirebaseManagerAnalyticsProperties.ScreenNames.PRODUCT_SEARCH_RESULTS) }
    }

    private fun setTitle() = (activity as? BottomNavigationActivity)?.setTitle(if (mSubCategoryName?.isEmpty() == true) mSearchTerm else mSubCategoryName)

    override fun onLoadProductSuccess(response: ProductView, loadMoreData: Boolean) {
        val productLists = response.products

        if (mProductList?.isNullOrEmpty() == true)
            mProductList = ArrayList()

        if (productLists?.isEmpty() == true) {
            sortAndRefineLayout?.visibility = GONE
            if (!listContainHeader()) {
                val headerProduct = ProductList()
                headerProduct.rowType = ProductListingViewType.HEADER
                headerProduct.numberOfItems = numItemsInTotal
                productLists.add(0, headerProduct)
            }
            bindRecyclerViewWithUI(productLists)
        } else if (productLists.size == 1) {
            (activity as? BottomNavigationActivity)?.apply {
                popFragmentNoAnim()
                openProductDetailFragment(mSubCategoryName, productLists[0])
            }

        } else {
            this.productView = response
            hideFooterView()
            if (!loadMoreData) {
                sortAndRefineLayout?.visibility = View.VISIBLE
                setRefinementViewState(productView?.navigation?.let { nav -> getRefinementViewState(nav) }
                        ?: false)
                bindRecyclerViewWithUI(productLists)
                showFeatureWalkThrough()
            } else {
                loadMoreData(productLists)
            }
        }
    }

    override fun unhandledResponseCode(response: Response) {
        val activity = activity ?: return
        if (response.desc == null) return
        hideFooterView()
        val fragmentTransaction: FragmentTransaction? = (activity as? AppCompatActivity)?.supportFragmentManager?.beginTransaction()
        // check if sortOptionDialog is being displayed
        (activity as? AppCompatActivity)?.let { if (hasOpenedDialogs(it)) return }

        // show sortOptionDialog
        val singleButtonDialogFragment = SingleButtonDialogFragment.newInstance(response.desc)
        fragmentTransaction?.let { singleButtonDialogFragment.show(fragmentTransaction, SingleButtonDialogFragment::class.java.simpleName) }
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
        this.mProductList = productLists
        if (!listContainHeader()) {
            val headerProduct = ProductList()
            headerProduct.rowType = ProductListingViewType.HEADER
            headerProduct.numberOfItems = numItemsInTotal
            mProductList?.add(0, headerProduct)
        }

        mProductAdapter = ProductListingAdapter(this, mProductList)

        activity?.let { activity -> mRecyclerViewLayoutManager = GridLayoutManager(activity, 2) }
        // Set up a GridLayoutManager to change the SpanSize of the header and footer
        mRecyclerViewLayoutManager?.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
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

        productList?.apply {
            if (visibility == View.INVISIBLE)
                visibility = View.VISIBLE

            layoutManager = mRecyclerViewLayoutManager
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
                visibility = View.VISIBLE
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
        (activity as? BottomNavigationActivity)?.walkThroughPromtView?.removeFromWindow()
    }

    override fun startProductRequest() {
        activity?.let { activity ->
            if (mSearchTerm?.isEmpty() == true) {
                executeLoadProduct(activity, productRequestBody)
            } else {
                executeLoadProduct(activity, productRequestBody)
            }
        }
    }

    override fun loadMoreData(productLists: List<ProductList>) {
        val actualSize = mProductList?.size ?: 0 + 1
        mProductList?.addAll(productLists)
        val sizeOfList = mProductList?.size ?: 0
        try {
            hideFooterView()
        } catch (ex: Exception) {
            Log.e("containFooter", ex.message)
        }

        mProductAdapter?.notifyItemChanged(actualSize, sizeOfList)
        canLoadMore(numItemsInTotal, sizeOfList)
    }

    override fun setProductBody() {
        setProductRequestBody(mSearchType, mSearchTerm)
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
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.drill_down_category_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_drill_search -> {
                activity?.apply {
                    val openSearchActivity = Intent(this, ProductSearchActivity::class.java)
                    startActivity(openSearchActivity)
                    overridePendingTransition(0, 0)
                }
                true
            }
            else -> false
        }
    }

    override fun onClick(view: View) {
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
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.REFINE_EVENT_APPEARED)
                    val intent = Intent(activity, ProductsRefineActivity::class.java)
                    intent.putExtra(REFINEMENT_DATA, Utils.toJson(productView))
                    intent.putExtra(PRODUCTS_REQUEST_PARAMS, Utils.toJson(productRequestBody))
                    activity.startActivityForResult(intent, REFINE_REQUEST_CODE)
                    activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
                }
                R.id.sortProducts -> {
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SORTBY_EVENT_APPEARED)
                    productView?.sortOptions?.let { sortOption -> this.showShortOptions(sortOption) }
                }
                else -> return
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            (activity as? BottomNavigationActivity)?.apply {
                showToolbar()
                showBackNavigationIcon(true)
                setToolbarBackgroundDrawable(R.drawable.appbar_background)
                setTitle()
            }
        }
    }

    override fun onSortOptionSelected(sortOption: SortOption) {
        if (sortOptionDialog != null && sortOptionDialog?.isShowing == true) {
            sortOptionDialog?.dismiss()
            val arguments = HashMap<String, String>()
            arguments[FirebaseManagerAnalyticsProperties.PropertyNames.SORT_OPTION_NAME] = sortOption.label
            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SORTBY_EVENT_APPLIED, arguments)
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
            rcvSortOptions?.layoutManager = activity?.let { activity -> LinearLayoutManager(activity) }
            rcvSortOptions?.adapter = activity?.let { activity -> SortOptionsAdapter(activity, sortOptions, this@ProductListingFragment) }
            setContentView(view)
            window?.apply {
                setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
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
            QUERY_INVENTORY_FOR_STORE_REQUEST_CODE -> {
                if (resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue())
                    mStoreId?.let { storeId -> mSelectedProductList?.let { productList -> queryInventoryForStore(storeId, mAddItemToCart, productList) } }
            }
            QUERY_LOCATION_ITEM_REQUEST_CODE -> {
                if (resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()) {
                    queryStoreFinderProductByFusedLocation(null)
                }
            }
            REFINE_REQUEST_CODE -> {
                if (resultCode == RESULT_OK) {
                    val navigationState = data?.getStringExtra(ProductsRefineActivity.NAVIGATION_STATE)
                    updateProductRequestBodyForRefinement(navigationState)
                    reloadProductsWithSortAndFilter()
                }
            }

            SSOActivity.SSOActivityResult.LAUNCH.rawValue(), SET_DELIVERY_LOCATION_REQUEST_CODE -> {
                if (resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue() || resultCode == RESULT_OK) {
                    addFoodProductTypeToCart(mAddItemsToCart?.get(0))
                }
            }

            else -> return
        }
    }

    private fun reloadProductsWithSortAndFilter() {
        productList?.visibility = View.INVISIBLE
        sortAndRefineLayout?.visibility = View.GONE
        startProductRequest()
    }

    private fun showFeatureWalkThrough() {
        if (!isAdded || !AppInstanceObject.get().featureWalkThrough.showTutorials || AppInstanceObject.get().featureWalkThrough.refineProducts)
            return
        (activity as? BottomNavigationActivity)?.apply {
            Crashlytics.setString(getString(R.string.crashlytics_materialshowcase_key), this.javaClass.canonicalName)
            walkThroughPromtView = WMaterialShowcaseView.Builder(this, WMaterialShowcaseView.Feature.REFINE)
                    .setTarget(refineDownArrow)
                    .setTitle(R.string.walkthrough_refine_title)
                    .setDescription(R.string.walkthrough_refine_desc)
                    .setActionText(R.string.walkthrough_refine_action)
                    .setImage(R.drawable.tips_tricks_ic_refine)
                    .setShapePadding(48)
                    .setAction(this@ProductListingFragment)
                    .setAsNewFeature()
                    .setArrowPosition(WMaterialShowcaseView.Arrow.TOP_RIGHT)
                    .setMaskColour(ContextCompat.getColor(this, R.color.semi_transparent_black)).build()
            walkThroughPromtView.show(this)
        }
    }

    override fun onWalkthroughActionButtonClick() {
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
    }

    override fun openProductDetailView(productList: ProductList) {
        mSubCategoryName = if (mSearchTerm?.isNotEmpty() == true) mSearchTerm else mSubCategoryName
        (activity as? BottomNavigationActivity)?.openProductDetailFragment(mSubCategoryName, productList)

    }

    override fun queryInventoryForStore(storeId: String, addItemToCart: AddItemToCart?, productList: ProductList) {
        this.mStoreId = storeId
        this.mAddItemToCart = addItemToCart
        this.mSelectedProductList = productList
        val activity = activity ?: return

        if (!SessionUtilities.getInstance().isUserAuthenticated) {
            ScreenManager.presentSSOSignin(activity, QUERY_INVENTORY_FOR_STORE_REQUEST_CODE)
            return
        }

        if (storeId.isEmpty()) {
            val quickShopDefaultValues = WoolworthsApplication.getQuickShopDefaultValues()
            val userSelectedDeliveryLocation = Utils.getPreferredDeliveryLocation()
            val deliveryLocationName = if (userSelectedDeliveryLocation != null) userSelectedDeliveryLocation.suburb?.name
                    ?: "" else quickShopDefaultValues?.suburb?.name ?: ""
            val message = "Unfortunately this item is unavailable in $deliveryLocationName. Try changing your delivery location and try again."
            Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.ERROR_TITLE_DESC, getString(R.string.product_unavailable), message)
            return
        }

        showProgressBar()
        OneAppService.getInventorySkuForStore(storeId, addItemToCart?.catalogRefId
                ?: "").enqueue(CompletionHandler(object : RequestListener<SkusInventoryForStoreResponse> {
            override fun onSuccess(skusInventoryForStoreResponse: SkusInventoryForStoreResponse) {
                if (!isAdded) return
                dismissProgressBar()
                oneTimeInventoryErrorDialogDisplay = false
                with(activity.supportFragmentManager.beginTransaction()) {
                    when (skusInventoryForStoreResponse.httpCode) {
                        200 -> {
                            val skuInventoryList = skusInventoryForStoreResponse.skuInventory
                            if (skuInventoryList.size == 0 || skuInventoryList[0].quantity == 0) {
                                addItemToCart?.catalogRefId?.let { skuId -> productOutOfStockErrorMessage(skuId) }
                            } else if (skuInventoryList[0].quantity == 1) {
                                addFoodProductTypeToCart(AddItemToCart(addItemToCart?.productId, addItemToCart?.catalogRefId, 1))
                            } else {
                                val cartItem = AddItemToCart(addItemToCart?.productId
                                        ?: "", addItemToCart?.catalogRefId
                                        ?: "", skuInventoryList[0].quantity)
                                val selectYourQuantityFragment = SelectYourQuantityFragment.newInstance(cartItem, this@ProductListingFragment)
                                selectYourQuantityFragment.show(this, SelectYourQuantityFragment::class.java.simpleName)
                            }
                        }

                        else -> {
                            if (!oneTimeInventoryErrorDialogDisplay) {
                                oneTimeInventoryErrorDialogDisplay = true
                                skusInventoryForStoreResponse.response?.desc?.let { desc -> Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.ERROR, desc) }
                            } else return
                        }
                    }
                }
            }

            override fun onFailure(error: Throwable) {
                if (!isAdded) return
                activity.runOnUiThread {
                    dismissProgressBar()
                    onFailureHandler(error)
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
                ?: mutableListOf(), object : RequestListener<AddItemToCartResponse> {
            override fun onSuccess(addItemToCartResponse: AddItemToCartResponse) {
                if (!isAdded) return
                activity?.apply {
                    dismissProgressBar()
                    when (addItemToCartResponse.httpCode) {
                        200 -> {
                            // Preferred Delivery Location has been reset on server
                            // As such, we give the user the ability to set their location again
                            val addToCartList = addItemToCartResponse.data
                            if (addToCartList != null && addToCartList.size > 0 && addToCartList[0].formexceptions != null) {
                                val formException = addToCartList[0].formexceptions[0]
                                if (formException != null) {
                                    if (formException.message.toLowerCase(Locale.getDefault()).contains("unfortunately this product is now out of stock, please try again tomorrow")) {
                                        addItemToCart?.catalogRefId?.let { catalogRefId -> productOutOfStockErrorMessage(catalogRefId) }
                                    } else {
                                        addItemToCartResponse.response.desc = formException.message
                                        Utils.displayValidationMessage(this, CustomPopUpWindow.MODAL_LAYOUT.ERROR, addItemToCartResponse.response.desc)
                                    }
                                    return
                                }
                            }

                            val addToCartBalloon by balloon(AddedToCartBalloonFactory::class)
                            val bottomView = (activity as? BottomNavigationActivity)?.bottomNavigationById
                            val buttonView: Button = addToCartBalloon.getContentView().findViewById(R.id.btnView)
                            val tvAddedItem: TextView = addToCartBalloon.getContentView().findViewById(R.id.tvAddedItem)
                            val quantityAdded = addItemToCart?.quantity?.toString()
                            val quantityDesc = "$quantityAdded ITEM${if (addItemToCart?.quantity == 0) "" else "s"}"
                            tvAddedItem.text = quantityDesc

                            buttonView.setOnClickListener {
                                openCartActivity()
                                addToCartBalloon.dismiss()
                            }

                            bottomView?.let { bottomNavigationView -> addToCartBalloon.showAlignBottom(bottomNavigationView, 0, 16) }
                            Handler().postDelayed({
                                addToCartBalloon.dismiss()
                            }, 3000)
                        }

                        417 -> resources?.let { resources ->
                            val errorMessage = addItemToCartResponse.response?.desc?.let { desc -> ErrorMessageDialogFragment.newInstance(desc, resources.getString(R.string.set_delivery_location_button)) }
                            activity?.supportFragmentManager?.let { supportManager -> errorMessage?.show(supportManager, ErrorMessageDialogFragment::class.java.simpleName) }
                        }
                        440 -> {
                            SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE)
                            ScreenManager.presentSSOSignin(this)
                        }

                        else -> addItemToCartResponse.response?.desc?.let { desc -> Utils.displayValidationMessage(this, CustomPopUpWindow.MODAL_LAYOUT.ERROR, desc) }
                    }
                }
            }

            override fun onFailure(error: Throwable) {
                if (!isAdded) return
                activity?.runOnUiThread { dismissProgressBar() }
            }
        })
    }

    private fun productOutOfStockErrorMessage(skuId: String) {
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            val productListingFindInStoreNoQuantityFragment = ProductListingFindInStoreNoQuantityFragment.newInstance(skuId, this@ProductListingFragment)
            productListingFindInStoreNoQuantityFragment.show(this, SelectYourQuantityFragment::class.java.simpleName)
        }
    }

    private fun openCartActivity() {
        activity?.apply {
            startActivityForResult(Intent(this, CartActivity::class.java), OPEN_CART_REQUEST)
            overridePendingTransition(R.anim.anim_accelerate_in, R.anim.stay)
        }
    }

    override fun queryStoreFinderProductByFusedLocation(location: Location?) {
        showProgressBar()
        val globalState = WoolworthsApplication.getInstance().wGlobalState
        with(globalState) {
            OneAppService.getLocationsItem(mSelectedProductList?.sku
                    ?: "", startRadius.toString(), endRadius.toString()).enqueue(CompletionHandler(object : RequestListener<LocationResponse> {
                override fun onSuccess(locationResponse: LocationResponse) {
                    if (!isAdded) return
                    dismissProgressBar()
                    activity?.apply {
                        with(locationResponse) {
                            when (httpCode) {
                                200 -> {
                                    if (Locations != null && Locations.size > 0) {
                                        WoolworthsApplication.getInstance()?.wGlobalState?.storeDetailsArrayList = Locations
                                        val openStoreFinder = Intent(this@apply, WStockFinderActivity::class.java)
                                        openStoreFinder.putExtra("PRODUCT_NAME", mSelectedProductList?.productName)
                                        openStoreFinder.putExtra("CONTACT_INFO", "")
                                        startActivity(openStoreFinder)
                                        overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
                                    } else {
                                        activity?.let { activity -> Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.NO_STOCK, "") }
                                    }
                                }
                                440 -> {
                                    SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE)
                                    ScreenManager.presentSSOSignin(this@apply, QUERY_LOCATION_ITEM_REQUEST_CODE)
                                }
                                else -> response?.desc?.let { desc -> Utils.displayValidationMessage(this@apply, CustomPopUpWindow.MODAL_LAYOUT.ERROR, desc) }
                            }
                        }
                    }
                }

                override fun onFailure(error: Throwable) {
                    activity?.runOnUiThread {
                        dismissProgressBar()
                        onFailureHandler(error)
                    }
                }
            }, LocationResponse::class.java))
        }
    }

    companion object {
        const val REFINEMENT_DATA = "REFINEMENT_DATA"
        const val PRODUCTS_REQUEST_PARAMS = "PRODUCTS_REQUEST_PARAMS"
        private const val SUB_CATEGORY_NAME = "SUB_CATEGORY_NAME"

        const val REFINE_REQUEST_CODE = 77
        private const val QUERY_INVENTORY_FOR_STORE_REQUEST_CODE = 3343
        private const val QUERY_LOCATION_ITEM_REQUEST_CODE = 3344

        private const val SEARCH_TYPE = "SEARCH_TYPE"
        private const val SEARCH_TERM = "SEARCH_TERM"

        fun newInstance(searchType: ProductsRequestParams.SearchType?, sub_category_name: String?, searchTerm: String?) = ProductListingFragment().withArgs {
            putString(SEARCH_TYPE, searchType?.name)
            putString(SUB_CATEGORY_NAME, sub_category_name)
            putString(SEARCH_TERM, searchTerm)
        }
    }
}
