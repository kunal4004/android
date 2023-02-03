package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.Gson
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams.SearchType
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.OneAppService.getProducts
import za.co.woolworths.financial.services.android.models.network.OneAppService.productDetail
import za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity
import za.co.woolworths.financial.services.android.ui.activities.ConfirmColorSizeActivity
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.adapters.SearchResultShopAdapter
import za.co.woolworths.financial.services.android.ui.adapters.holder.ProductListingViewType
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList.Companion.openShoppingList
import za.co.woolworths.financial.services.android.ui.views.WButton
import za.co.woolworths.financial.services.android.util.*
import java.util.*

class SearchResultFragment : Fragment(), SearchResultNavigator, View.OnClickListener,
    NetworkChangeListener {
    private var mErrorHandlerView: ErrorHandlerView? = null
    private var productAdapter: SearchResultShopAdapter? = null
    private var mProductList: MutableList<ProductList>? = null
    private var mProgressLimitStart: ProgressBar? = null
    private var mRecyclerViewLayoutManager: LinearLayoutManager? = null
    private var mSearchText: String? = null
    private var totalItemCount = 0
    private var lastVisibleItem = 0
    private var isLoading = false
    private var mListId: String? = null
    private var mGetProductDetail: Call<ProductDetailResponse>? = null
    var selectedProduct: ProductList? = null
    private var mAddToListSize = 0
    private var mPostAddToList: Call<ShoppingListItemsResponse>? = null
    private var connectionBroadcast: BroadcastReceiver? = null
    private var addToListLoadFail = false
    var numItemsInTotal = 0
        private set
    var loadMoreData = false
    private var pageOffset = 0
    private var mIsLoading = false
    var otherSkus: ArrayList<OtherSkus>? = null
    private var productIsLoading = false
    var productRequestBody: ProductsRequestParams? = null
        private set
    private var mGetProductsRequest: Call<ProductView>? = null
    private var btnCheckOut: WButton? = null
    private var relNoConnectionLayout: RelativeLayout? = null
    private var btnRetry: WButton? = null
    private var rlAddToList: RelativeLayout? = null
    private var pbLoadingIndicator: ProgressBar? = null
    private var rclProductList: RecyclerView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        val bundle = this.arguments
        if (bundle != null) {
            mSearchText = bundle.getString(MY_LIST_SEARCH_TERM)
            mListId = bundle.getString(MY_LIST_LIST_ID, "")
        } else {
            mSearchText = ""
        }
        Utils.updateStatusBarBackground(activity)
        setProductBody()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.search_result_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setTranslationZ(getView()!!, 100f)
        initUI(view)
        setUpToolbar(view)
        mErrorHandlerView = ErrorHandlerView(activity, relNoConnectionLayout)
        mErrorHandlerView!!.setMargin(relNoConnectionLayout, 0, 0, 0, 0)
        startProductRequest()
        btnRetry!!.setOnClickListener(this)
        setUpAddToListButton()
        connectionBroadcast()
    }

    private fun setUpToolbar(view: View) {
        val appbar = view.findViewById<AppBarLayout>(R.id.appbar)
        val shoppingListTitleTextView = view.findViewById<TextView>(R.id.shoppingListTitleTextView)
        shoppingListTitleTextView.text = mSearchText
        if (activity is BottomNavigationActivity) {
            appbar.visibility = View.VISIBLE
            val activity = activity as BottomNavigationActivity?
            activity!!.hideToolbar()
            activity.showBackNavigationIcon(true)
            activity.setToolbarBackgroundDrawable(R.drawable.appbar_background)
            val backButton = view.findViewById<ImageView>(R.id.btnBack)
            backButton.setOnClickListener { v: View? -> activity.onBackPressed() }
            activity.toolbar().setNavigationOnClickListener { v: View? -> activity.popFragment() }
            activity.setTitle(mSearchText)
        }
    }

    private fun initUI(view: View) {
        mProgressLimitStart = view.findViewById(R.id.pbLoadProduct)
        btnCheckOut = view.findViewById(R.id.btnCheckOut)
        relNoConnectionLayout = view.findViewById(R.id.no_connection_layout)
        btnRetry = view.findViewById(R.id.btnRetry)
        rlAddToList = view.findViewById(R.id.rlCheckOut)
        pbLoadingIndicator = view.findViewById(R.id.pbLoadingIndicator)
        rclProductList = view.findViewById(R.id.productList)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val activity: Activity? = activity
        if (activity is BottomNavigationActivity) {
            activity.hideBottomNavigationMenu()
        }
    }

    private val globalState: WGlobalState?
        private get() {
            val woolworthsApplication = WoolworthsApplication.getInstance()
            return woolworthsApplication?.wGlobalState
        }

    private fun setUpAddToListButton() {
        btnCheckOut!!.setOnClickListener(this)
        btnCheckOut!!.text = getString(R.string.add_to_list)
        toggleAddToListBtn(false)
    }

    override fun onLoadProductSuccess(
        productLists: MutableList<ProductList>,
        loadMoreData: Boolean
    ) {
        if (productLists != null) {
            if (!loadMoreData) {
                bindRecyclerViewWithUI(productLists)
            } else {
                loadMoreData(productLists)
            }
        }
    }

    override fun unhandledResponseCode(response: Response) {}
    override fun failureResponseHandler(e: String) {
        val activity: Activity? = activity
        activity?.runOnUiThread { mErrorHandlerView!!.networkFailureHandler(e) }
    }

    private fun cancelColorSizeSelection() {
        try {
            val otherSkus = OtherSkus()
            otherSkus.sku = selectedProduct!!.sku
            if (productAdapter != null) productAdapter!!.onDeselectSKU(selectedProduct!!, otherSkus)
        } catch (ex: NullPointerException) {
        }
    }

    override fun cancelAPIRequest() {
        cancelRequest(mGetProductsRequest)
    }

    override fun bindRecyclerViewWithUI(productList: MutableList<ProductList>) {
        mProductList = productList
        if (!listContainHeader()) {
            val headerProduct = ProductList()
            headerProduct.rowType = ProductListingViewType.HEADER
            headerProduct.numberOfItems = numItemsInTotal
            productList.add(0, headerProduct)
        }
        productAdapter = SearchResultShopAdapter(activity!!, mProductList, this)
        mRecyclerViewLayoutManager = LinearLayoutManager(activity)
        rclProductList!!.layoutManager = mRecyclerViewLayoutManager
        rclProductList!!.isNestedScrollingEnabled = false
        rclProductList!!.adapter = productAdapter
        rclProductList!!.itemAnimator = null
        rclProductList!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                totalItemCount = mRecyclerViewLayoutManager!!.itemCount
                lastVisibleItem = mRecyclerViewLayoutManager!!.findLastVisibleItemPosition()
                loadData()
            }
        })
    }

    private fun loadData() {
        val visibleThreshold = 5
        if (!isLoading && totalItemCount <= lastVisibleItem + visibleThreshold) {
            if (productIsLoading()) return
            val Total = numItemsInTotal + Utils.PAGE_SIZE
            val start = mProductList!!.size
            val end = start + Utils.PAGE_SIZE
            isLoading = Total < end
            if (isLoading) {
                return
            }
            if (!listContainFooter()) {
                val footerItem = ProductList()
                footerItem.rowType = ProductListingViewType.FOOTER
                mProductList!!.add(footerItem)
                productAdapter!!.notifyItemInserted(mProductList!!.size - 1)
            }
            startProductRequest()
        }
    }

    private fun listContainFooter(): Boolean {
        if (mProductList == null) return false
        for ((_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, rowType) in mProductList!!) {
            if (rowType === ProductListingViewType.FOOTER) {
                return true
            }
        }
        return false
    }

    private fun removeFooter() {
        var index = 0
        for (pl in mProductList!!) {
            if (pl.rowType === ProductListingViewType.FOOTER) {
                mProductList!!.remove(pl)
                productAdapter!!.notifyItemRemoved(index)
                return
            }
            index++
        }
    }

    private fun listContainHeader(): Boolean {
        for ((_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, rowType) in mProductList!!) {
            if (rowType === ProductListingViewType.HEADER) {
                return true
            }
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelAPIRequest()
    }

    override fun startProductRequest() {
        executeSearchProduct(activity, productRequestBody)
    }

    override fun loadMoreData(productLists: List<ProductList>) {
        val actualSize = mProductList!!.size + 1
        mProductList!!.addAll(productLists)
        val sizeOfList = mProductList!!.size
        productAdapter!!.notifyItemChanged(actualSize, sizeOfList)
        canLoadMore(numItemsInTotal, sizeOfList)
    }

    override fun setProductBody() {
        setProductRequestBody(SearchType.SEARCH, mSearchText)
    }

    override fun onLoadStart(isLoadMore: Boolean) {
        setIsLoading(true)
        if (!isLoadMore) {
            mProgressLimitStart!!.visibility = View.VISIBLE
        }
    }

    override fun onLoadComplete(isLoadMore: Boolean) {
        if (listContainFooter()) {
            removeFooter()
        }
        setIsLoading(false)
        if (!isLoadMore) {
            mProgressLimitStart!!.visibility = View.GONE
        }
    }

    override fun onClick(view: View) {
        MultiClickPreventer.preventMultiClick(view)
        when (view.id) {
            R.id.btnRetry -> if (isNetworkConnected) {
                mErrorHandlerView!!.hideErrorHandler()
                startProductRequest()
            }
            R.id.btnCheckOut -> {
                cancelRequest(mGetProductDetail)
                if (productAdapter == null) return
                for (productList in productAdapter!!.productList!!) {
                    if (productList.viewIsLoading) {
                        productList.viewIsLoading = false
                        productList.itemWasChecked = false
                    }
                    productAdapter!!.notifyDataSetChanged()
                }
                val addToListRequests: MutableList<AddToListRequest> = ArrayList()
                for ((_, _, _, _, _, sku, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, itemWasChecked) in productAdapter!!.productList!!) {
                    if (itemWasChecked) {
                        val addToList = AddToListRequest()
                        addToList.catalogRefId = sku
                        addToList.quantity = "1"
                        addToList.giftListId = sku
                        addToList.skuID = sku
                        addToListRequests.add(addToList)
                    }
                }
                mAddToListSize = addToListRequests.size
                postAddToList(addToListRequests)
            }
            else -> {}
        }
    }

    private fun postAddToList(addToListRequests: List<AddToListRequest>) {
        mPostAddToList = addToList(addToListRequests, mListId)
    }

    override fun onFoodTypeSelect(productList: ProductList) {
        val gson = Gson()
        val strProductList = gson.toJson(productList)
        ScreenManager.openProductDetailFragment(activity, mSearchText, strProductList)
    }

    override fun onClothingTypeSelect(productList: ProductList) {
        val gson = Gson()
        val strProductList = gson.toJson(productList)
        ScreenManager.openProductDetailFragment(activity, mSearchText, strProductList)
    }

    override fun minOneItemSelected(prodList: MutableList<ProductList>) {
        mProductList = prodList
        var productWasChecked = false
        for ((_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, itemWasChecked) in prodList) {
            if (itemWasChecked) {
                productWasChecked = true
                toggleAddToListBtn(true)
            }
        }
        // hide checkbox when no item selected
        if (!productWasChecked) {
            toggleAddToListBtn(false)
        }
    }

    override fun onAddToListFailure(e: String) {
        Log.e("onAddToListFailure", e)
        val activity: Activity? = activity
        activity?.runOnUiThread {
            onAddToListLoad(false)
            addToListLoadFail = true
        }
    }

    override fun onAddToListLoad(isLoading: Boolean) {
        addToListLoadFail = false
        pbLoadingIndicator!!.visibility =
            if (isLoading) View.VISIBLE else View.GONE
        btnCheckOut!!.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    fun onAddToListLoadComplete() {
        addToListLoadFail = false
        pbLoadingIndicator!!.visibility = View.GONE
        btnCheckOut!!.visibility = View.VISIBLE
        val addedToListIntent = Bundle()
        addedToListIntent.putInt("listItems", mAddToListSize)
        val activity: Activity? = activity
        if (activity != null) {
            if (activity is BottomNavigationActivity) {
                val handler = Handler()
                handler.postDelayed({
                    // Send back the result to ShoppingListDetailFragment to update current shopping list. Delay added to show toast message.
                    activity.supportFragmentManager.setFragmentResult(
                        ADDED_TO_SHOPPING_LIST_RESULT_CODE.toString(),
                        addedToListIntent
                    )
                }, AppConstant.DELAY_200_MS)
                activity.onBackPressed()
            }
        }
    }

    override fun onCheckedItem(
        productLists: MutableList<ProductList>,
        selectedProduct: ProductList,
        viewIsLoading: Boolean
    ) {
        this.selectedProduct = selectedProduct
        mProductList = productLists
        if (viewIsLoading) {
            val productRequest =
                ProductRequest(selectedProduct.productId, selectedProduct.sku, false)
            productDetailRequest(productRequest)
        } else {
            if (productAdapter != null) {
                val otherSkus = OtherSkus()
                otherSkus.sku = selectedProduct.sku
                productAdapter!!.onDeselectSKU(this.selectedProduct!!, otherSkus)
            }
        }
        updateAddToListCount()
    }

    private fun updateAddToListCount() {
        if (activity == null) {
            return
        }
        var count = 0
        if (mProductList == null) {
            val addToCartText =
                activity!!.resources.getQuantityString(R.plurals.plural_add_to_list, count, count)
            btnCheckOut!!.text = addToCartText
            return
        }
        for ((_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, itemWasChecked) in mProductList!!) {
            if (itemWasChecked) count++
        }
        val addToCartText =
            activity!!.resources.getQuantityString(R.plurals.plural_add_to_list, count, count)
        btnCheckOut!!.text = addToCartText
    }

    override fun responseFailureHandler(response: Response) {}
    override fun onSuccessResponse(product: WProduct) {
        Objects.requireNonNull(globalState)?.saveButtonClicked(ProductDetailsFragment.INDEX_SEARCH_FROM_LIST)
        productAdapter!!.setCheckedProgressBar(selectedProduct!!)
        if (isNetworkConnected) {
            val otherSkuList = otherSkus
            val colorList = colorList
            val sizeList = sizeList
            val objProduct = product.product
            val colorSize = colorList.size
            val productContainColor = colorSize > 1
            val onlyOneColor = colorSize == 1
            val sizeSize = sizeList.size
            val productContainSize = sizeSize > 1
            val onlyOneSize = sizeSize == 1
            if (productContainColor) { // contains one or more color
                //show picker dialog
                twoOrMoreColorIntent(otherSkuList, colorList, objProduct)
            } else {
                if (onlyOneColor) { // contains one color only
                    val color = if (colorList[0] != null) colorList[0]!!.colour else ""
                    // contains more than one size
                    intentSizeList(
                        color,
                        colorList[0],
                        otherSkuList,
                        objProduct
                    ) // open size intent with color as filter
                } else {  // no color found
                    if (productContainSize) {
                        if (onlyOneSize) {
                            noSizeColorIntent(if (TextUtils.isEmpty(sizeList[0]!!.sku)) objProduct.sku else sizeList[0]!!.sku)
                        } else {
                            twoOrMoreSizeIntent(otherSkuList, colorList, objProduct)
                        }
                    } else {
                        // no size found
                        noSizeColorIntent(objProduct.sku)
                    }
                }
            }
        }
    }

    private val isNetworkConnected: Boolean
        private get() = NetworkManager.getInstance().isConnectedToNetwork(
            activity
        )

    fun twoOrMoreSizeIntent(
        colour: String?,
        otherSkuList: ArrayList<OtherSkus>?,
        colorList: ArrayList<OtherSkus>?,
        objProduct: WProductDetail
    ) {
        Objects.requireNonNull(globalState)?.colourSKUArrayList = colorList
        val mIntent = Intent(activity, ConfirmColorSizeActivity::class.java)
        mIntent.putExtra("SELECTED_COLOUR", colour)
        mIntent.putExtra("OTHERSKU", Utils.toJson(otherSkuList))
        mIntent.putExtra("PRODUCT_HAS_COLOR", false)
        mIntent.putExtra("PRODUCT_HAS_SIZE", true)
        mIntent.putExtra(ConfirmColorSizeActivity.SELECT_PAGE, "")
        mIntent.putExtra("PRODUCT_NAME", objProduct.productName)
        activity!!.startActivityForResult(mIntent, COLOR_SIZE_SELECTION_REQUEST_CODE)
        activity!!.overridePendingTransition(0, 0)
    }

    private fun intentSizeList(
        color: String?,
        otherSku: OtherSkus?,
        otherSkuList: ArrayList<OtherSkus>?,
        objProduct: WProductDetail
    ) {
        val sizeList = commonSizeList(otherSku)
        val sizeListSize = sizeList.size
        if (sizeListSize > 0) {
            if (sizeListSize == 1) {
                // one size only
                val otherSkus = sizeList[0]
                productAdapter!!.setSelectedSku(selectedProduct!!, otherSkus)
                noSizeColorIntent(otherSkus.sku)
                minOneItemSelected(mProductList!!)
            } else {
                // size > 1
                twoOrMoreSizeIntent(color, otherSkuList, sizeList, objProduct)
            }
        } else {
            // no size
            noSizeColorIntent(otherSku!!.sku)
        }
    }

    fun noSizeColorIntent(mSkuId: String?) {
        val otherSkus = OtherSkus()
        otherSkus.sku = mSkuId
        Objects.requireNonNull(globalState)?.selectedSKUId = otherSkus
        val activity: Activity? = activity
        if (activity != null) {
            when (globalState!!.saveButtonClick) {
                ProductDetailsFragment.INDEX_ADD_TO_SHOPPING_LIST -> openAddToListFragment(activity)
                else -> {}
            }
        }
    }

    private fun openAddToListFragment(activity: Activity) {
        val sku = Objects.requireNonNull(globalState)?.selectedSKUId.toString()
        val item = AddToListRequest()
        item.catalogRefId = sku
        item.skuID = sku
        item.giftListId = sku
        item.quantity = "1"
        val addToListRequests = ArrayList<AddToListRequest>()
        addToListRequests.add(item)
        openShoppingList(activity, addToListRequests, "", false)
    }

    private fun twoOrMoreColorIntent(
        otherSkuList: ArrayList<OtherSkus>?,
        colorList: ArrayList<OtherSkus?>,
        objProduct: WProductDetail
    ) {
        Objects.requireNonNull(globalState)?.colourSKUArrayList = colorList
        val mIntent = Intent(activity, ConfirmColorSizeActivity::class.java)
        mIntent.putExtra("COLOR_LIST", Utils.toJson(colorList))
        mIntent.putExtra("OTHERSKU", Utils.toJson(otherSkuList))
        mIntent.putExtra("PRODUCT_HAS_COLOR", true)
        mIntent.putExtra("PRODUCT_HAS_SIZE", true)
        mIntent.putExtra(ConfirmColorSizeActivity.SELECT_PAGE, "")
        mIntent.putExtra("PRODUCT_NAME", objProduct.productName)
        activity!!.startActivityForResult(mIntent, COLOR_SIZE_SELECTION_REQUEST_CODE)
        activity!!.overridePendingTransition(0, 0)
    }

    fun twoOrMoreSizeIntent(
        otherSkuList: ArrayList<OtherSkus>?,
        colorList: ArrayList<OtherSkus?>?,
        objProduct: WProductDetail
    ) {
        Objects.requireNonNull(globalState)?.colourSKUArrayList = colorList
        val mIntent = Intent(activity, ConfirmColorSizeActivity::class.java)
        mIntent.putExtra("COLOR_LIST", Utils.toJson(colorList))
        mIntent.putExtra("OTHERSKU", Utils.toJson(otherSkuList))
        mIntent.putExtra("PRODUCT_HAS_COLOR", false)
        mIntent.putExtra("PRODUCT_HAS_SIZE", true)
        mIntent.putExtra(ConfirmColorSizeActivity.SELECT_PAGE, "")
        mIntent.putExtra("PRODUCT_NAME", objProduct.productName)
        activity!!.startActivityForResult(mIntent, COLOR_SIZE_SELECTION_REQUEST_CODE)
        activity!!.overridePendingTransition(0, 0)
    }

    override fun onLoadDetailFailure(e: String) {
        val activity: Activity? = activity
        activity?.runOnUiThread { cancelColorSizeSelection() }
    }

    override fun onFoodTypeChecked(
        productLists: MutableList<ProductList>,
        selectedProduct: ProductList
    ) {
        mProductList = productLists
        toggleAddToListBtn(true)
        updateAddToListCount()
    }

    override fun unknownErrorMessage(shoppingCartResponse: ShoppingListItemsResponse) {
        onAddToListLoad(false)
        val activity: Activity? = activity
        if (activity != null) if (shoppingCartResponse != null) {
            val response = shoppingCartResponse.response
            if (response.desc != null) {
                Utils.displayValidationMessage(
                    activity,
                    CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                    response.desc
                )
            }
        }
    }

    override fun accountExpired(shoppingCartResponse: ShoppingListItemsResponse) {
        if (shoppingCartResponse != null && shoppingCartResponse.response != null) SessionUtilities.getInstance()
            .setSessionState(
                SessionDao.SESSION_STATE.INACTIVE,
                shoppingCartResponse.response.stsParams,
                activity
            )
    }

    private fun productDetailRequest(productRequest: ProductRequest) {
        mGetProductDetail = getProductDetail(productRequest)
    }

    override fun toggleAddToListBtn(enable: Boolean) {
        rlAddToList!!.visibility = if (enable) View.VISIBLE else View.GONE
        btnCheckOut!!.isEnabled = enable
    }

    private fun connectionBroadcast() {
        connectionBroadcast = Utils.connectionBroadCast(
            activity, this
        )
    }

    override fun onDetach() {
        super.onDetach()
        val activity: Activity? = activity
        if (activity is BottomNavigationActivity) {
            activity.showBottomNavigationMenu()
        }
        cancelRequest(mGetProductDetail)
        cancelRequest(mPostAddToList)
    }

    private fun cancelRequest(call: Call<*>?) {
        if (call != null && !call.isCanceled) {
            call.cancel()
        }
    }

    override fun onPause() {
        super.onPause()
        val activity: Activity? = activity
        activity?.unregisterReceiver(connectionBroadcast)
    }

    override fun onResume() {
        super.onResume()
        Utils.setScreenName(
            activity,
            FirebaseManagerAnalyticsProperties.ScreenNames.SHOPPING_LIST_SEARCH_RESULTS
        )
        val activity: Activity? = activity
        activity?.registerReceiver(
            connectionBroadcast,
            IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        )
    }

    override fun onConnectionChanged() {
        retryConnect()
    }

    private fun retryConnect() {
        val activity: Activity? = activity
        activity?.runOnUiThread {
            if (addToListLoadFail) {
                btnCheckOut!!.performClick()
            }
        }
    }

    fun searchProduct(context: Context?, requestParams: ProductsRequestParams?): Call<ProductView> {
        onLoadStart(loadMoreData)
        setProductIsLoading(true)
        val productListCall = getProducts(requestParams!!)
        productListCall.enqueue(
            CompletionHandler<ProductView>(
                object : IResponseListener<ProductView> {
                    override fun onSuccess(productView: ProductView?) {
                        when (productView!!.httpCode) {
                            200 -> {
                                val productLists: MutableList<ProductList>? = productView.products
                                if (productLists != null) {
                                    numItemsInTotal(productView)
                                    calculatePageOffset()
                                    onLoadProductSuccess(productLists, loadMoreData)
                                    onLoadComplete(loadMoreData)
                                    loadMoreData = true
                                }
                            }
                            else -> if (productView.response != null) {
                                onLoadComplete(loadMoreData)
                                unhandledResponseCode(productView.response)
                            }
                        }
                        setProductIsLoading(false)
                    }

                    override fun onFailure(error: Throwable?) {
                        if (error == null) return
                        if (context != null) {
                            val activity = context as Activity
                            activity.runOnUiThread(object : Runnable {
                                override fun run() {
                                    setProductIsLoading(false)
                                    failureResponseHandler(error.message!!)
                                    onLoadComplete(loadMoreData)
                                }
                            })
                        }
                    }
                }, ProductView::class.java
            )
        )
        return productListCall
    }

    fun addToList(
        addToListRequest: List<AddToListRequest>?,
        listId: String?
    ): Call<ShoppingListItemsResponse> {
        onAddToListLoad(true)
        val shoppingListItemsResponseCall = OneAppService.addToList(addToListRequest as MutableList<AddToListRequest>, listId!!)
        shoppingListItemsResponseCall.enqueue(
            CompletionHandler<ShoppingListItemsResponse>(
                object : IResponseListener<ShoppingListItemsResponse> {
                    override fun onSuccess(shoppingListItemsResponse: ShoppingListItemsResponse?) {
                        when (shoppingListItemsResponse!!.httpCode) {
                            200 -> onAddToListLoadComplete()
                            440 -> {
                                accountExpired(shoppingListItemsResponse)
                                onAddToListLoad(false)
                            }
                            else -> unknownErrorMessage(shoppingListItemsResponse)
                        }
                    }

                    override fun onFailure(error: Throwable?) {
                        if (error == null) return
                        onAddToListFailure(error.message!!)
                    }
                }, ShoppingListItemsResponse::class.java
            )
        )
        return shoppingListItemsResponseCall
    }

    fun setIsLoading(mIsLoading: Boolean) {
        this.mIsLoading = mIsLoading
    }

    fun isLoading(): Boolean {
        return mIsLoading
    }

    fun setProductRequestBody(searchType: SearchType?, searchTerm: String?) {
        productRequestBody = ProductsRequestParams(
            searchTerm!!,
            searchType!!,
            ProductsRequestParams.ResponseType.DETAIL,
            pageOffset
        )
    }

    fun executeSearchProduct(context: Context?, lp: ProductsRequestParams?) {
        mGetProductsRequest = searchProduct(context, lp)
    }

    private fun numItemsInTotal(productView: ProductView?) {
        val pagingResponse = productView!!.pagingResponse
        if (pagingResponse.numItemsInTotal != null) {
            numItemsInTotal = pagingResponse.numItemsInTotal
        }
    }

    fun canLoadMore(totalItem: Int, sizeOfList: Int) {
        if (sizeOfList >= totalItem) {
            loadMoreData = false
        }
    }

    private fun calculatePageOffset() {
        pageOffset = pageOffset + Utils.PAGE_SIZE
        productRequestBody!!.pageOffset = pageOffset
    }

    val colorList: ArrayList<OtherSkus?>
        get() {
            Collections.sort(otherSkus) { (_, _, _, _, _, _, colour), (_, _, _, _, _, _, colour1) ->
                colour!!.compareTo(
                    colour1!!, ignoreCase = true
                )
            }
            val commonColorSku = ArrayList<OtherSkus?>()
            for (sku in otherSkus!!) {
                if (colourValueExist(commonColorSku, sku.colour)) {
                    commonColorSku.add(sku)
                }
            }
            return commonColorSku
        }
    val sizeList: ArrayList<OtherSkus?>
        get() {
            Collections.sort(otherSkus) { (_, _, _, _, _, size), (_, _, _, _, _, size1) ->
                size!!.compareTo(
                    size1!!, ignoreCase = true
                )
            }
            val commonColorSku = ArrayList<OtherSkus?>()
            for (sku in otherSkus!!) {
                if (colourValueExist(commonColorSku, sku.size)) {
                    commonColorSku.add(sku)
                }
            }
            return commonColorSku
        }

    fun colourValueExist(list: ArrayList<OtherSkus?>, name: String?): Boolean {
        for (item in list) {
            if (item!!.colour == name) {
                return false
            }
        }
        return true
    }

    @Throws(NullPointerException::class)
    fun commonSizeList(otherSku: OtherSkus?): ArrayList<OtherSkus> {
        val commonSizeList = ArrayList<OtherSkus>()
        // filter by colour
        val sizeList = ArrayList<OtherSkus>()
        for (sku in otherSkus!!) {
            if (sku.colour != null) {
                if (sku.colour.equals(otherSku!!.colour, ignoreCase = true)) {
                    sizeList.add(sku)
                }
            }
        }

        //remove duplicates
        for (os in sizeList) {
            if (!sizeValueExist(commonSizeList, os.size)) {
                commonSizeList.add(os)
            }
        }
        return commonSizeList
    }

    private fun sizeValueExist(list: ArrayList<OtherSkus>, name: String?): Boolean {
        for ((_, _, _, _, _, size) in list) {
            if (size == name) {
                return true
            }
        }
        return false
    }

    fun setProductIsLoading(productIsLoading: Boolean) {
        this.productIsLoading = productIsLoading
    }

    fun productIsLoading(): Boolean {
        return productIsLoading
    }

    fun getProductDetail(productRequest: ProductRequest): Call<ProductDetailResponse> {
        val productDetailRequest =
            productDetail(productRequest.productId, productRequest.skuId, false)
        productDetailRequest.enqueue(
            CompletionHandler<ProductDetailResponse>(
                object : IResponseListener<ProductDetailResponse> {
                    override fun onSuccess(productDetailResponse: ProductDetailResponse?) {
                        val detailProduct = Utils.objectToJson(productDetailResponse)
                        if (productDetailResponse!!.httpCode == 200) {
                            if (productDetailResponse.product != null) {
                                otherSkus = productDetailResponse.product.otherSkus
                            }
                            val product =
                                Utils.strToJson(detailProduct, WProduct::class.java) as WProduct
                            onSuccessResponse(product)
                        } else {
                            if (productDetailResponse.response != null) {
                                responseFailureHandler(productDetailResponse.response)
                            }
                        }
                    }

                    override fun onFailure(error: Throwable?) {
                        if (error == null) return
                        onLoadDetailFailure(error.message!!)
                    }
                }, ProductDetailResponse::class.java
            )
        )
        return productDetailRequest
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == COLOR_SIZE_SELECTION_REQUEST_CODE) {
            when (resultCode) {
                ConfirmColorSizeActivity.SELECTED_SHOPPING_LIST_ITEM_RESULT_CODE -> if (productAdapter != null) {
                    Objects.requireNonNull(
                        globalState
                    )?.selectedSKUId?.let {
                        productAdapter!!.setSelectedSku(
                            selectedProduct!!, it
                        )
                    }
                    toggleAddToListBtn(true)
                    minOneItemSelected(mProductList!!)
                }
                ConfirmColorSizeActivity.CLOSE_ICON_TAPPED_RESULT_CODE -> cancelColorSizeSelection()
                else -> {}
            }
        }
        if (requestCode == BottomNavigationActivity.PDP_REQUEST_CODE && resultCode == AddToShoppingListActivity.ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE) {
            activity!!.setResult(
                AddToShoppingListActivity.ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE,
                data
            )
            activity!!.onBackPressed()
        }
    }

    companion object {
        private const val COLOR_SIZE_SELECTION_REQUEST_CODE = 3012
        const val ADDED_TO_SHOPPING_LIST_RESULT_CODE = 1312
        const val SHOPPING_LIST_SEARCH_RESULT_REQUEST_CODE = 2012
        const val PRODUCT_DETAILS_FROM_MY_LIST_SEARCH = 7657
        const val MY_LIST_LIST_NAME = "listName"
        const val MY_LIST_LIST_ID = "listId"
        const val MY_LIST_SEARCH_TERM = "searchTerm"
    }
}