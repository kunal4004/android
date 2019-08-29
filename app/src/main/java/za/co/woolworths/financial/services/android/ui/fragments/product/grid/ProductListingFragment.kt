package za.co.woolworths.financial.services.android.ui.fragments.product.grid

import android.app.Activity
import android.app.Dialog

import androidx.lifecycle.ViewModelProviders

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import com.awfs.coordination.BR

import com.awfs.coordination.R
import com.awfs.coordination.databinding.GridLayoutBinding
import com.crashlytics.android.Crashlytics
import com.skydoves.balloon.balloon
import java.util.ArrayList
import java.util.HashMap

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IProductListing
import za.co.woolworths.financial.services.android.contracts.RequestListener
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart
import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.dto.ProductView
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.models.dto.RefinementNavigation
import za.co.woolworths.financial.services.android.models.dto.Response
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse
import za.co.woolworths.financial.services.android.models.dto.SortOption
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.CartActivity
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.OPEN_CART_REQUEST
import za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity
import za.co.woolworths.financial.services.android.ui.activities.product.refine.ProductsRefineActivity
import za.co.woolworths.financial.services.android.ui.adapters.ProductListingAdapter
import za.co.woolworths.financial.services.android.ui.adapters.SortOptionsAdapter
import za.co.woolworths.financial.services.android.ui.adapters.holder.ProductListingViewType
import za.co.woolworths.financial.services.android.ui.base.BaseFragment
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.ProductListingProgressBar
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ProductListingFindInStoreNoQuantityFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.SelectYourQuantityFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.SingleButtonDialogFragment
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.PostItemToCart
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils

import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragmentNew.SET_DELIVERY_LOCATION_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.views.AddedToCartBalloonFactory

open class ProductListingFragment : BaseFragment<GridLayoutBinding, GridViewModel>(), GridNavigator, IProductListing, View.OnClickListener, SortOptionsAdapter.OnSortOptionSelected, WMaterialShowcaseView.IWalkthroughActionListener {

    private var mAddItemsToCart: MutableList<AddItemToCart>? = null
    private var mGridViewModel: GridViewModel? = null
    private var mErrorHandlerView: ErrorHandlerView? = null
    private var mSubCategoryId: String? = null
    private var mSubCategoryName: String? = null
    private var mSearchProduct: String? = null
    private var mProductAdapter: ProductListingAdapter? = null
    private var mProductList: MutableList<ProductList>? = null
    private var mRecyclerViewLayoutManager: GridLayoutManager? = null
    private var lastVisibleItem: Int = 0
    internal var totalItemCount: Int = 0
    private var isLoading: Boolean = false
    private var productView: ProductView? = null
    private var sortOptionDialog: Dialog? = null
    private val mProgressListingProgressBar = ProductListingProgressBar()
    private var mStoreId: String? = null
    private var mAddItemToCart: AddItemToCart? = null
    override fun getViewModel(): GridViewModel? {
        return mGridViewModel
    }

    override fun getBindingVariable(): Int = BR.viewModel


    override fun getLayoutId(): Int = R.layout.grid_layout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        activity?.apply {
            mGridViewModel = ViewModelProviders.of(this).get(GridViewModel::class.java)
            mGridViewModel?.navigator = this@ProductListingFragment

            arguments?.apply {
                mSubCategoryId = getString(SUB_CATEGORY_ID, "")
                mSubCategoryName = getString(SUB_CATEGORY_NAME, "")
                mSearchProduct = getString(SEARCH_PRODUCT_TERMS, "")
            }
            setProductBody()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showToolbar()
        showBackNavigationIcon(true)
        setToolbarBackgroundDrawable(R.drawable.appbar_background)


        (activity as? BottomNavigationActivity)?.toolbar?.setNavigationOnClickListener { popFragment() }

        val relNoConnectionLayout = viewDataBinding.incNoConnectionHandler.noConnectionLayout

        activity?.let {
            mErrorHandlerView = ErrorHandlerView(it, relNoConnectionLayout)
            mErrorHandlerView?.setMargin(relNoConnectionLayout, 0, 0, 0, 0)
        }

        setTitle()
        startProductRequest()
        with(viewDataBinding) {
            incNoConnectionHandler.btnRetry.setOnClickListener(this@ProductListingFragment)
            sortAndRefineLayout.refineProducts.setOnClickListener(this@ProductListingFragment)
            sortAndRefineLayout.sortProducts.setOnClickListener(this@ProductListingFragment)
        }

    }

    override fun onResume() {
        super.onResume()
        activity.let { activity -> Utils.setScreenName(activity, FirebaseManagerAnalyticsProperties.ScreenNames.PRODUCT_SEARCH_RESULTS) }
    }

    private fun setTitle() = setTitle(if (mSearchProduct?.isEmpty() == true) mSubCategoryName else mSearchProduct)

    override fun onLoadProductSuccess(response: ProductView, loadMoreData: Boolean) {
        val productLists = response.products

        if (mProductList.isNullOrEmpty())
            mProductList = ArrayList()

        if (productLists.isEmpty()) {
            viewDataBinding?.sortAndRefineLayout?.parentLayout?.visibility = View.GONE
            if (!listContainHeader()) {
                val headerProduct = ProductList()
                headerProduct.rowType = ProductListingViewType.HEADER
                headerProduct.numberOfItems = viewModel?.numItemsInTotal
                productLists.add(0, headerProduct)
            }
            bindRecyclerViewWithUI(productLists)
        } else if (productLists.size == 1) {
            bottomNavigator?.popFragmentNoAnim()
            bottomNavigator?.openProductDetailFragment(mSubCategoryName, productLists[0])

        } else {
            this.productView = response
            hideFooterView()
            if (!loadMoreData) {
                viewDataBinding?.sortAndRefineLayout?.parentLayout?.visibility = View.VISIBLE
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
        mGridViewModel?.cancelRequest(mGridViewModel?.loadProductRequest)
    }

    override fun bindRecyclerViewWithUI(productList: MutableList<ProductList>) {
        this.mProductList = productList
        if (!listContainHeader()) {
            val headerProduct = ProductList()
            headerProduct.rowType = ProductListingViewType.HEADER
            headerProduct.numberOfItems = viewModel!!.numItemsInTotal
            mProductList?.add(0, headerProduct)
        }

        mProductAdapter = ProductListingAdapter(this, mProductList)

        mRecyclerViewLayoutManager = GridLayoutManager(activity, 2)
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

        viewDataBinding?.productList?.apply {
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
            if (viewModel!!.productIsLoading()) return
            val total = viewModel!!.numItemsInTotal + Utils.PAGE_SIZE
            val start = mProductList!!.size
            val end = start + Utils.PAGE_SIZE
            isLoading = total < end
            if (isLoading) return
            if (!listContainFooter()) {
                val footerItem = ProductList()
                footerItem.rowType = ProductListingViewType.FOOTER
                mProductList!!.add(footerItem)
                mProductAdapter!!.notifyItemInserted(mProductList!!.size - 1)
            }
            startProductRequest()
        }
    }

    private fun listContainFooter(): Boolean {
        try {
            for (pl in mProductList!!) {
                if (pl.rowType === ProductListingViewType.FOOTER) {
                    return true
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
        if (bottomNavigationActivity != null && bottomNavigationActivity!!.walkThroughPromtView != null) {
            bottomNavigationActivity!!.walkThroughPromtView.removeFromWindow()
        }
    }

    override fun startProductRequest() {
        if (isEmpty(mSearchProduct)) {
            viewModel!!.executeLoadProduct(activity, viewModel!!.productRequestBody)
        } else {
            viewModel!!.executeLoadProduct(activity, viewModel!!.productRequestBody)
        }
    }

    override fun loadMoreData(productLists: List<ProductList>) {
        val actualSize = mProductList!!.size + 1
        mProductList!!.addAll(productLists)
        val sizeOfList = mProductList!!.size
        try {
            hideFooterView()
        } catch (ex: Exception) {
            Log.e("containFooter", ex.message)
        }

        mProductAdapter!!.notifyItemChanged(actualSize, sizeOfList)
        viewModel!!.canLoadMore(viewModel!!.numItemsInTotal, sizeOfList)
    }

    override fun setProductBody() {
        if (isEmpty(mSearchProduct)) {
            viewModel?.setProductRequestBody(ProductsRequestParams.SearchType.NAVIGATE, mSubCategoryId)
        } else {
            viewModel?.setProductRequestBody(ProductsRequestParams.SearchType.SEARCH, mSearchProduct)
        }
    }

    override fun onLoadStart(isLoadMore: Boolean) {
        viewModel!!.setIsLoading(true)
        if (!isLoadMore) {
            showProgressBar()
        }
    }

    override fun onLoadComplete(isLoadMore: Boolean) {
        viewModel!!.setIsLoading(false)
        if (!isLoadMore) {
            dismissProgressBar()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.drill_down_category_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_drill_search -> {
                val openSearchActivity = Intent(baseActivity, ProductSearchActivity::class.java)
                startActivity(openSearchActivity)
                baseActivity.overridePendingTransition(0, 0)
                return true
            }
            else -> {
            }
        }
        return false
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnRetry -> if (isNetworkConnected) {
                mErrorHandlerView!!.hideErrorHandler()
                startProductRequest()
            }
            R.id.refineProducts -> {
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.REFINE_EVENT_APPEARED)
                val intent = Intent(activity, ProductsRefineActivity::class.java)
                intent.putExtra(REFINEMENT_DATA, Utils.toJson(productView))
                intent.putExtra(PRODUCTS_REQUEST_PARAMS, Utils.toJson(viewModel?.productRequestBody))
                startActivityForResult(intent, REFINE_REQUEST_CODE)
                activity?.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
            }
            R.id.sortProducts -> {
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SORTBY_EVENT_APPEARED)
                this.showShortOptions(productView!!.sortOptions)
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            showToolbar()
            showBackNavigationIcon(true)
            setToolbarBackgroundDrawable(R.drawable.appbar_background)
            setTitle()
        }
    }

    override fun onSortOptionSelected(sortOption: SortOption) {
        if (sortOptionDialog != null && sortOptionDialog?.isShowing == true) {
            sortOptionDialog?.dismiss()
            val arguments = HashMap<String, String>()
            arguments[FirebaseManagerAnalyticsProperties.PropertyNames.SORT_OPTION_NAME] = sortOption.label
            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SORTBY_EVENT_APPLIED, arguments)
            viewModel?.updateProductRequestBodyForSort(sortOption.sortOption)
            reloadProductsWithSortAndFilter()
        }
    }

    private fun showShortOptions(sortOptions: ArrayList<SortOption>) {
        sortOptionDialog = activity?.let { activity -> Dialog(activity) }
        sortOptionDialog?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            val view = layoutInflater.inflate(R.layout.sort_options_view, null)
            val rcvSortOptions = view.findViewById<RecyclerView>(R.id.sortOptionsList)
            rcvSortOptions.layoutManager = activity?.let { activity -> LinearLayoutManager(activity) }
            rcvSortOptions.adapter = activity?.let { activity -> SortOptionsAdapter(activity, sortOptions, this@ProductListingFragment) }
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
            QUERY_INVENTORY_FOR_STORE_REQUEST_CODE -> if (resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue())
                mStoreId?.let { storeId -> queryInventoryForStore(storeId, mAddItemToCart) }

            REFINE_REQUEST_CODE -> if (resultCode == Activity.RESULT_OK) {
                val navigationState = data?.getStringExtra(ProductsRefineActivity.NAVIGATION_STATE)
                viewModel?.updateProductRequestBodyForRefinement(navigationState)
                reloadProductsWithSortAndFilter()
            }

            SSOActivity.SSOActivityResult.LAUNCH.rawValue() -> {
                if (resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()) {
                    addFoodProductTypeToCart(mAddItemsToCart?.get(0))
                }
            }

            else -> return
        }
    }

    private fun reloadProductsWithSortAndFilter() {
        viewDataBinding.productList.visibility = View.INVISIBLE
        viewDataBinding.sortAndRefineLayout.parentLayout.visibility = View.GONE
        startProductRequest()
    }

    private fun showFeatureWalkThrough() {
        if (!isAdded || !AppInstanceObject.get().featureWalkThrough.showTutorials || AppInstanceObject.get().featureWalkThrough.refineProducts)
            return
        activity?.apply {
            bottomNavigationActivity?.let { nav ->
                Crashlytics.setString(getString(R.string.crashlytics_materialshowcase_key), this.javaClass.canonicalName)
                nav.walkThroughPromtView = WMaterialShowcaseView.Builder(this, WMaterialShowcaseView.Feature.REFINE)
                        .setTarget(viewDataBinding.sortAndRefineLayout.refineDownArrow)
                        .setTitle(R.string.walkthrough_refine_title)
                        .setDescription(R.string.walkthrough_refine_desc)
                        .setActionText(R.string.walkthrough_refine_action)
                        .setImage(R.drawable.tips_tricks_ic_refine)
                        .setShapePadding(48)
                        .setAction(this@ProductListingFragment)
                        .setAsNewFeature()
                        .setArrowPosition(WMaterialShowcaseView.Arrow.TOP_RIGHT)
                        .setMaskColour(ContextCompat.getColor(this, R.color.semi_transparent_black)).build()
                nav.walkThroughPromtView.show(this)
            }
        }
    }

    override fun onWalkthroughActionButtonClick() {
        if (viewDataBinding.sortAndRefineLayout.refineProducts.isClickable)
            onClick(viewDataBinding.sortAndRefineLayout.refineProducts)
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
        with(viewDataBinding.sortAndRefineLayout) {
            refineProducts.isEnabled = refinementViewState
            refineDownArrow.isEnabled = refinementViewState
            refinementText.isEnabled = refinementViewState
        }
    }

    override fun openProductDetailView(productList: ProductList) {
        mSubCategoryName = if (mSearchProduct?.isNotEmpty() == true) mSearchProduct else mSubCategoryName
        bottomNavigator.openProductDetailFragment(mSubCategoryName, productList)
    }

    override fun queryInventoryForStore(storeId: String, addItemToCart: AddItemToCart?) {
        mStoreId = storeId
        mAddItemToCart = addItemToCart
        val activity = activity ?: return
        if (!SessionUtilities.getInstance().isUserAuthenticated) {
            ScreenManager.presentSSOSignin(activity, QUERY_INVENTORY_FOR_STORE_REQUEST_CODE)
            return
        }

        showProgressBar()
        OneAppService.getInventorySkuForStore(storeId, addItemToCart?.catalogRefId
                ?: "").enqueue(CompletionHandler(object : RequestListener<SkusInventoryForStoreResponse> {
            override fun onSuccess(skusInventoryForStoreResponse: SkusInventoryForStoreResponse) {
                if (!isAdded) return
                dismissProgressBar()
                with(activity.supportFragmentManager.beginTransaction()) {
                    when (skusInventoryForStoreResponse.httpCode) {
                        200 -> {
                            val skuInventoryList = skusInventoryForStoreResponse.skuInventory
                            if (skuInventoryList.size == 0 || skuInventoryList[0].quantity == 0) {
                                productOutOfStockErrorMessage()
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


                        else -> return
                    }
                }
            }

            override fun onFailure(error: Throwable) {
                dismissProgressBar()
            }
        }, SkusInventoryForStoreResponse::class.java))
    }

    private fun showProgressBar() {
        // Show progress bar
        activity?.let { activity -> mProgressListingProgressBar.show(activity) }
    }

    private fun dismissProgressBar() {
        // hide progress bar
        mProgressListingProgressBar.dialog.dismiss()
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
                                    if (formException.message.toLowerCase().contains("unfortunately this product is now out of stock, please try again tomorrow")) {
                                        productOutOfStockErrorMessage()
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
                            },3000)
                        }

                        417 -> Utils.displayValidationMessageForResult(this@ProductListingFragment,
                                this,
                                CustomPopUpWindow.MODAL_LAYOUT.ERROR, null,
                                addItemToCartResponse.response.desc, resources.getString(R.string.set_delivery_location_button), SET_DELIVERY_LOCATION_REQUEST_CODE)

                        440 -> {
                            SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE)
                            ScreenManager.presentSSOSignin(this)
                        }

                        else -> Utils.displayValidationMessage(this, CustomPopUpWindow.MODAL_LAYOUT.ERROR, addItemToCartResponse.response.desc)
                    }
                }
            }

            override fun onFailure(error: Throwable) {
                activity?.runOnUiThread { dismissProgressBar() }
            }
        })
    }

    private fun productOutOfStockErrorMessage() {
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            val productListingFindInStoreNoQuantityFragment = ProductListingFindInStoreNoQuantityFragment.newInstance()
            productListingFindInStoreNoQuantityFragment.show(this, SelectYourQuantityFragment::class.java.simpleName)
        }
    }

    private fun openCartActivity() {
        activity?.apply {
            startActivityForResult(Intent(this, CartActivity::class.java), OPEN_CART_REQUEST)
            overridePendingTransition(R.anim.anim_accelerate_in, R.anim.stay)
        }
    }

    companion object {
        const val REFINEMENT_DATA = "REFINEMENT_DATA"
        const val PRODUCTS_REQUEST_PARAMS = "PRODUCTS_REQUEST_PARAMS"
        private const val SUB_CATEGORY_ID = "SUB_CATEGORY_ID"
        private const val SUB_CATEGORY_NAME = "SUB_CATEGORY_NAME"
        private const val SEARCH_PRODUCT_TERMS = "SEARCH_PRODUCT_TERMS"

        const val REFINE_REQUEST_CODE = 77
        private const val QUERY_INVENTORY_FOR_STORE_REQUEST_CODE = 3343

        fun newInstance(sub_category_id: String?, sub_category_name: String?, search_product_term: String?) = ProductListingFragment().withArgs {
            putString(SUB_CATEGORY_ID, sub_category_id)
            putString(SUB_CATEGORY_NAME, sub_category_name)
            putString(SEARCH_PRODUCT_TERMS, search_product_term)
        }
    }

}
