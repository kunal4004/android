package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.SearchResultFragmentBinding
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
import za.co.woolworths.financial.services.android.ui.fragments.colorandsize.ColorAndSizeBottomSheetListener
import za.co.woolworths.financial.services.android.ui.fragments.colorandsize.ColorAndSizeFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList.Companion.openShoppingList
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_OK
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_SESSION_TIMEOUT_440
import java.util.*

class SearchResultFragment : Fragment(), SearchResultNavigator, View.OnClickListener,
    NetworkChangeListener, ColorAndSizeBottomSheetListener {
    private var mErrorHandlerView: ErrorHandlerView? = null
    private var productAdapter: SearchResultShopAdapter? = null
    private var mProductList: MutableList<ProductList>? = null
    private var mSearchText: String? = ""
    private var isLoading = false
    private var mListId: String? = null
    private var mGetProductDetail: Call<ProductDetailResponse>? = null
    private var selectedProduct: ProductList? = null
    private var mAddToListSize = 0
    private var mPostAddToList: Call<ShoppingListItemsResponse>? = null
    private var connectionBroadcast: BroadcastReceiver? = null
    private var addToListLoadFail = false
    private var numItemsInTotal = 0
    var loadMoreData = false
    private var pageOffset = 0
    private var mIsLoading = false
    var otherSkus: ArrayList<OtherSkus>? = null
    private var productIsLoading = false
    private var productRequestBody: ProductsRequestParams? = null
    private var mGetProductsRequest: Call<ProductView>? = null
    private var _binding: SearchResultFragmentBinding? = null
    private val binding get() = _binding!!

    private val globalState: WGlobalState?
        get() = WoolworthsApplication.getInstance()?.wGlobalState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.apply {
            mSearchText = getString(MY_LIST_SEARCH_TERM, "")
            mListId = getString(MY_LIST_LIST_ID, "")
        }
        Utils.updateStatusBarBackground(activity)
        setProductBody()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SearchResultFragmentBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setTranslationZ(getView()!!, 100f)
        setUpToolbar()
        binding.incNoConnectionHandler.apply {
            mErrorHandlerView = ErrorHandlerView(activity, noConnectionLayout)
            mErrorHandlerView?.setMargin(noConnectionLayout, 0, 0, 0, 0)
            btnRetry.setOnClickListener(this@SearchResultFragment)
        }
        startProductRequest()
        setUpAddToListButton()
        connectionBroadcast()
    }

    private fun setUpToolbar() {
        binding.appbar.visibility = View.VISIBLE
        binding.shoppingListTitleTextView.text = mSearchText
        binding.btnBack.setOnClickListener { activity?.onBackPressed() }

        (activity as? BottomNavigationActivity)?.apply {
            hideToolbar()
            showBackNavigationIcon(true)
            setToolbarBackgroundDrawable(R.drawable.appbar_background)
            toolbar().setNavigationOnClickListener { popFragment() }
            setTitle(mSearchText)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as? BottomNavigationActivity)?.hideBottomNavigationMenu()
    }

    private fun setUpAddToListButton() {
        if (!isAdded) return
        binding.btnCheckOut.apply {
            text = getString(R.string.add_to_list)
            setOnClickListener(this@SearchResultFragment)
        }
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
        activity?.runOnUiThread { mErrorHandlerView?.networkFailureHandler(e) }
    }

    private fun cancelColorSizeSelection() {
        selectedProduct?.let {
            val otherSkus = OtherSkus()
            otherSkus.sku = it.sku
            productAdapter?.onDeselectSKU(it, otherSkus)
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
        productAdapter = SearchResultShopAdapter(requireActivity(), mProductList, this)
        binding.productList.apply {
            val mRecyclerViewLayoutManager = LinearLayoutManager(requireContext())
            layoutManager = mRecyclerViewLayoutManager
            isNestedScrollingEnabled = false
            adapter = productAdapter
            itemAnimator = null
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    loadData(
                        mRecyclerViewLayoutManager.itemCount,
                        mRecyclerViewLayoutManager.findLastVisibleItemPosition()
                    )
                }
            })
        }
    }

    private fun loadData(totalItemCount: Int, lastVisibleItem: Int) {
        val visibleThreshold = 5
        if (!isLoading && totalItemCount <= lastVisibleItem + visibleThreshold) {
            if (productIsLoading()) return
            val total = Utils.PAGE_SIZE.plus(numItemsInTotal)
            val start = mProductList?.size ?: 0
            val end = start.plus(Utils.PAGE_SIZE)
            isLoading = total < end
            if (isLoading) {
                return
            }
            if (!listContainFooter()) {
                val footerItem = ProductList()
                footerItem.rowType = ProductListingViewType.FOOTER
                mProductList?.add(footerItem)
                productAdapter?.notifyItemInserted((mProductList?.size ?: 0) - 1)
            }
            startProductRequest()
        }
    }

    private fun listContainFooter(): Boolean {
        val list =
            mProductList?.filter { it.rowType === ProductListingViewType.FOOTER } ?: emptyList()
        return list.isNotEmpty()
    }

    private fun removeFooter() {
        mProductList?.forEachIndexed { index, productList ->
            if (productList.rowType === ProductListingViewType.FOOTER) {
                mProductList?.remove(productList)
                productAdapter?.notifyItemRemoved(index)
            }
        }
    }

    private fun listContainHeader(): Boolean {
        val list = mProductList?.filter {
            it.rowType === ProductListingViewType.HEADER
            return true
        } ?: emptyList()
        return list.isNotEmpty()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelAPIRequest()
    }

    override fun startProductRequest() {
        executeSearchProduct(activity, productRequestBody)
    }

    override fun loadMoreData(productLists: List<ProductList>) {
        val actualSize = (mProductList?.size ?: 0) + 1
        mProductList?.addAll(productLists)
        val sizeOfList = mProductList?.size ?: 0
        productAdapter?.notifyItemChanged(actualSize, sizeOfList)
        canLoadMore(numItemsInTotal, sizeOfList)
    }

    override fun setProductBody() {
        setProductRequestBody(SearchType.SEARCH, mSearchText)
    }

    override fun onLoadStart(isLoadMore: Boolean) {
        setIsLoading(true)
        if (!isLoadMore) {
            binding.pbLoadProduct.visibility = View.VISIBLE
        }
    }

    override fun onLoadComplete(isLoadMore: Boolean) {
        if (listContainFooter()) {
            removeFooter()
        }
        setIsLoading(false)
        if (!isLoadMore) {
            binding.pbLoadProduct.visibility = View.GONE
        }
    }

    override fun onClick(view: View) {
        MultiClickPreventer.preventMultiClick(view)
        when (view.id) {
            R.id.btnRetry -> if (isNetworkConnected) {
                mErrorHandlerView?.hideErrorHandler()
                startProductRequest()
            }
            R.id.btnCheckOut -> {
                cancelRequest(mGetProductDetail)
                if (productAdapter == null) return
                productAdapter?.productList?.forEach { productList ->
                    if (productList.viewIsLoading) {
                        productList.viewIsLoading = false
                        productList.itemWasChecked = false
                    }
                    productAdapter?.notifyDataSetChanged()
                }
                val addToListRequests: MutableList<AddToListRequest> = ArrayList()
                productAdapter?.productList?.forEach {
                    if (it.itemWasChecked) {
                        val addToList = AddToListRequest().apply {
                            catalogRefId = it.sku
                            quantity = "1"
                            giftListId = it.sku
                            skuID = it.sku
                        }
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
        val strProductList = Gson().toJson(productList)
        ScreenManager.openProductDetailFragment(activity, mSearchText, strProductList)
    }

    override fun onClothingTypeSelect(productList: ProductList) {
        val strProductList = Gson().toJson(productList)
        ScreenManager.openProductDetailFragment(activity, mSearchText, strProductList)
    }

    override fun minOneItemSelected(prodList: MutableList<ProductList>) {
        mProductList = prodList
        var productWasChecked = false
        prodList.forEach {
            if (it.itemWasChecked) {
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
        activity?.runOnUiThread {
            onAddToListLoad(false)
            addToListLoadFail = true
        }
    }

    override fun onAddToListLoad(isLoading: Boolean) {
        addToListLoadFail = false
        binding.pbLoadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnCheckOut.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    fun onAddToListLoadComplete() {
        binding.pbLoadingIndicator.visibility = View.GONE
        binding.btnCheckOut.visibility = View.VISIBLE
        addToListLoadFail = false
        val addedToListIntent = Bundle()
        addedToListIntent.putInt("listItems", mAddToListSize)
        (activity as? BottomNavigationActivity)?.apply {
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                // Send back the result to ShoppingListDetailFragment to update current shopping list. Delay added to show toast message.
                supportFragmentManager.setFragmentResult(
                    ADDED_TO_SHOPPING_LIST_RESULT_CODE.toString(),
                    addedToListIntent
                )
            }, AppConstant.DELAY_200_MS)
            onBackPressed()
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
            this@SearchResultFragment.selectedProduct?.let {
                val otherSkus = OtherSkus().also { otherSku ->
                    otherSku.sku = selectedProduct.sku
                }
                productAdapter?.onDeselectSKU(selectedProduct = it, otherSkus)
            }
        }
        updateAddToListCount()
    }

    private fun updateAddToListCount() {
        if (!isAdded) return

        val itemCount = mProductList?.filter { it.itemWasChecked } ?: emptyList()

        binding.btnCheckOut.text = requireContext().resources.getQuantityString(
            R.plurals.plural_add_to_list,
            itemCount.size,
            itemCount.size
        )
    }

    override fun responseFailureHandler(response: Response) {}
    override fun onSuccessResponse(product: WProduct) {
        globalState?.saveButtonClicked(ProductDetailsFragment.INDEX_SEARCH_FROM_LIST)
        selectedProduct?.let { productAdapter?.setCheckedProgressBar(it) }

        if (isNetworkConnected) {
            val colorList = getColorList(otherSkus)
            val sizeList = getSizeList(otherSkus)
            val objProduct = product.product

            // No color and no size
            val containsColorAndSize = colorList.size <= 0 && sizeList.size <= 0
            if (containsColorAndSize) {
                noSizeColorIntent(objProduct.sku)
            } else {
                openColorAndSizeBottomSheetFragment(objProduct)
            }
        }
    }

    private fun getColorList(otherSkus: ArrayList<OtherSkus>?): ArrayList<OtherSkus> {
        otherSkus?.sortWith { sku1, sku2 ->
            sku1.colour?.compareTo(other = sku2.colour ?: "", ignoreCase = true) ?: -1
        }
        val commonColorSku = ArrayList<OtherSkus>()
        otherSkus?.forEach {
            if (colourValueExist(commonColorSku, it.colour)) {
                commonColorSku.add(it)
            }
        }
        return commonColorSku
    }

    private fun getSizeList(otherSkus: ArrayList<OtherSkus>?): ArrayList<OtherSkus> {
        otherSkus?.sortWith { sku1, sku2 ->
            sku1.size?.compareTo(other = sku2.size ?: "", ignoreCase = true) ?: -1
        }
        val commonColorSku = ArrayList<OtherSkus>()
        otherSkus?.forEach {
            if (colourValueExist(commonColorSku, it.size)) {
                commonColorSku.add(it)
            }
        }
        return commonColorSku
    }

    private fun openColorAndSizeBottomSheetFragment(productItem: WProductDetail) {
        ColorAndSizeFragment.getInstance(this, productItem)
            .show(requireActivity().supportFragmentManager, tag)
    }

    private val isNetworkConnected: Boolean
        get() = NetworkManager.getInstance().isConnectedToNetwork(activity)

    private fun noSizeColorIntent(mSkuId: String?) {
        val otherSkus = OtherSkus()
        otherSkus.sku = mSkuId
        globalState?.selectedSKUId = otherSkus
        val activity: Activity? = activity
        if (activity != null) {
            when (globalState?.saveButtonClick) {
                ProductDetailsFragment.INDEX_ADD_TO_SHOPPING_LIST -> openAddToListFragment(activity)
                else -> {}
            }
        }
    }

    private fun openAddToListFragment(activity: Activity) {
        val sku = globalState?.selectedSKUId.toString()
        val item = AddToListRequest()
        item.catalogRefId = sku
        item.skuID = sku
        item.giftListId = sku
        item.quantity = "1"
        val addToListRequests = ArrayList<AddToListRequest>()
        addToListRequests.add(item)
        openShoppingList(activity, addToListRequests, "", false)
    }

    override fun onLoadDetailFailure(e: String) {
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

    override fun unknownErrorMessage(shoppingCartResponse: ShoppingListItemsResponse?) {
        onAddToListLoad(false)
        if (!isAdded) return
        val response = shoppingCartResponse?.response
        response?.desc?.let {
            Utils.displayValidationMessage(
                requireActivity(),
                CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                it
            )
        }
    }

    override fun accountExpired(shoppingCartResponse: ShoppingListItemsResponse) {
        if (shoppingCartResponse?.response != null)
            SessionUtilities.getInstance().setSessionState(
                SessionDao.SESSION_STATE.INACTIVE,
                shoppingCartResponse.response.stsParams,
                activity
            )
    }

    private fun productDetailRequest(productRequest: ProductRequest) {
        mGetProductDetail = getProductDetail(productRequest)
    }

    override fun toggleAddToListBtn(enable: Boolean) {
        if (!isAdded) return
        binding.rlCheckOut.visibility = if (enable) View.VISIBLE else View.GONE
        binding.btnCheckOut.isEnabled = enable
    }

    private fun connectionBroadcast() {
        connectionBroadcast = Utils.connectionBroadCast(activity, this)
    }

    override fun onDetach() {
        super.onDetach()
        (activity as? BottomNavigationActivity)?.showBottomNavigationMenu()
        cancelRequest(mGetProductDetail)
        cancelRequest(mPostAddToList)
    }

    private fun cancelRequest(call: Call<*>?) {
        if (call?.isCanceled == false) {
            call.cancel()
        }
    }

    override fun onPause() {
        super.onPause()
        connectionBroadcast?.let { activity?.unregisterReceiver(it) }
    }

    override fun onResume() {
        super.onResume()
        Utils.setScreenName(
            activity,
            FirebaseManagerAnalyticsProperties.ScreenNames.SHOPPING_LIST_SEARCH_RESULTS
        )
        activity?.registerReceiver(
            connectionBroadcast,
            IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        )
    }

    override fun onConnectionChanged() {
        retryConnect()
    }

    private fun retryConnect() {
        activity?.runOnUiThread {
            if (addToListLoadFail) {
                binding.btnCheckOut.performClick()
            }
        }
    }

    private fun searchProduct(
        context: Context?,
        requestParams: ProductsRequestParams?
    ): Call<ProductView> {
        onLoadStart(loadMoreData)
        setProductIsLoading(true)
        val productListCall = getProducts(requestParams!!)
        productListCall.enqueue(
            CompletionHandler(
                object : IResponseListener<ProductView> {
                    override fun onSuccess(response: ProductView?) {
                        when (response?.httpCode) {
                            HTTP_OK -> {
                                val productLists: MutableList<ProductList>? = response.products
                                if (productLists != null) {
                                    numItemsInTotal(response)
                                    calculatePageOffset()
                                    onLoadProductSuccess(productLists, loadMoreData)
                                    onLoadComplete(loadMoreData)
                                    loadMoreData = true
                                }
                            }
                            else -> if (response?.response != null) {
                                onLoadComplete(loadMoreData)
                                unhandledResponseCode(response.response)
                            }
                        }
                        setProductIsLoading(false)
                    }

                    override fun onFailure(error: Throwable?) {
                        if (error == null) return
                        activity?.runOnUiThread {
                            setProductIsLoading(false)
                            failureResponseHandler(error.message!!)
                            onLoadComplete(loadMoreData)
                        }
                    }
                }, ProductView::class.java
            )
        )
        return productListCall
    }

    private fun addToList(addToListRequest: List<AddToListRequest>?, listId: String?):
            Call<ShoppingListItemsResponse> {
        onAddToListLoad(true)
        val shoppingListItemsResponseCall =
            OneAppService.addToList(addToListRequest as MutableList<AddToListRequest>, listId!!)
        shoppingListItemsResponseCall.enqueue(
            CompletionHandler(
                object : IResponseListener<ShoppingListItemsResponse> {
                    override fun onSuccess(response: ShoppingListItemsResponse?) {
                        when (response?.httpCode) {
                            HTTP_OK -> onAddToListLoadComplete()
                            HTTP_SESSION_TIMEOUT_440 -> {
                                accountExpired(response)
                                onAddToListLoad(false)
                            }
                            else -> unknownErrorMessage(response)
                        }
                    }

                    override fun onFailure(error: Throwable?) {
                        error?.message?.let { onAddToListFailure(it) }
                    }
                }, ShoppingListItemsResponse::class.java
            )
        )
        return shoppingListItemsResponseCall
    }

    private fun setIsLoading(mIsLoading: Boolean) {
        this.mIsLoading = mIsLoading
    }

    fun isLoading(): Boolean {
        return mIsLoading
    }

    private fun setProductRequestBody(searchType: SearchType?, searchTerm: String?) {
        productRequestBody = ProductsRequestParams(
            searchTerm = searchTerm ?: "",
            searchType = searchType ?: SearchType.SEARCH,
            responseType = ProductsRequestParams.ResponseType.DETAIL,
            pageOffset = pageOffset
        )
    }

    private fun executeSearchProduct(context: Context?, lp: ProductsRequestParams?) {
        mGetProductsRequest = searchProduct(context, lp)
    }

    private fun numItemsInTotal(productView: ProductView?) {
        productView?.pagingResponse?.numItemsInTotal?.let {
            numItemsInTotal = it
        }
    }

    private fun canLoadMore(totalItem: Int, sizeOfList: Int) {
        if (sizeOfList >= totalItem) {
            loadMoreData = false
        }
    }

    private fun calculatePageOffset() {
        pageOffset += Utils.PAGE_SIZE
        productRequestBody?.pageOffset = pageOffset
    }

    private fun colourValueExist(list: ArrayList<OtherSkus>, name: String?): Boolean {
        return list.none { it.colour == name }
    }

    fun setProductIsLoading(productIsLoading: Boolean) {
        this.productIsLoading = productIsLoading
    }

    private fun productIsLoading(): Boolean {
        return productIsLoading
    }

    private fun getProductDetail(productRequest: ProductRequest): Call<ProductDetailResponse> {
        val productDetailRequest =
            productDetail(productRequest.productId, productRequest.skuId, false)
        productDetailRequest.enqueue(
            CompletionHandler(
                object : IResponseListener<ProductDetailResponse> {
                    override fun onSuccess(response: ProductDetailResponse?) {
                        val detailProduct = Utils.objectToJson(response)
                        if (response?.httpCode == 200) {
                            response.product?.let {
                                otherSkus = it.otherSkus
                            }
                            val product =
                                Utils.strToJson(detailProduct, WProduct::class.java) as WProduct
                            onSuccessResponse(product)
                        } else {
                            response?.response?.let { responseFailureHandler(it) }
                        }
                    }

                    override fun onFailure(error: Throwable?) {
                        error?.message?.let { onLoadDetailFailure(it) }
                    }
                }, ProductDetailResponse::class.java
            )
        )
        return productDetailRequest
    }

    override fun onCancelColorAndSize() {
        cancelColorSizeSelection()
    }

    override fun setSelectedSku(selectedSku: OtherSkus) {
        selectedProduct?.let {
            productAdapter?.setSelectedSku(
                it, selectedSku
            )
        }
        toggleAddToListBtn(true)
        minOneItemSelected(mProductList!!)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == COLOR_SIZE_SELECTION_REQUEST_CODE) {
            when (resultCode) {
                ConfirmColorSizeActivity.SELECTED_SHOPPING_LIST_ITEM_RESULT_CODE -> if (productAdapter != null) {
                    globalState?.selectedSKUId?.let {
                        productAdapter?.setSelectedSku(
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