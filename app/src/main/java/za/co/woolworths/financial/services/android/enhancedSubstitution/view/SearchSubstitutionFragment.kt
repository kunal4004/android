package za.co.woolworths.financial.services.android.enhancedSubstitution.view

import android.os.Build
import android.os.Bundle
import android.text.Spanned
import android.view.View
import android.view.View.GONE
import android.view.View.OnClickListener
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.awfs.coordination.R
import com.awfs.coordination.databinding.LayoutSearchSubstitutionFragmentBinding
import com.facebook.shimmer.Shimmer
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.cart.view.SubstitutionChoice
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.AddSubstitutionRequest
import za.co.woolworths.financial.services.android.enhancedSubstitution.util.listener.ProductListSelectionListener
import za.co.woolworths.financial.services.android.enhancedSubstitution.util.triggerFirebaseEventForAddSubstitution
import za.co.woolworths.financial.services.android.enhancedSubstitution.util.triggerFirebaseEventForSubstitution
import za.co.woolworths.financial.services.android.enhancedSubstitution.viewmodel.ProductSubstitutionViewModel
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.util.KeyboardUtil
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.AnalyticsManager
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

@AndroidEntryPoint
class SearchSubstitutionFragment : BaseFragmentBinding<LayoutSearchSubstitutionFragmentBinding>(
    LayoutSearchSubstitutionFragmentBinding::inflate
), ProductListSelectionListener, OnClickListener {

    private var totalItemCount: Int? = -1
    private var searchProductSubstitutionAdapter: SearchProductSubstitutionAdapter? = null
    private val productSubstitutionViewModel: ProductSubstitutionViewModel by activityViewModels()
    private var productList: ProductList? = null
    private var searchText: String? = null
    private var commerceItemId: String? = ""
    private var productId: String? = ""

    companion object {
        fun newInstance(
            commerceItemId: String?,
            productId: String?,
        ) = SearchSubstitutionFragment().withArgs {
            putString(ManageSubstitutionFragment.COMMERCE_ITEM_ID, commerceItemId)
            putString(ManageSubstitutionFragment.PRODUCT_ID, productId)
        }

        const val SELECTED_SUBSTITUTED_PRODUCT = "SELECTED_SUBSTITUTED_PRODUCT"
        const val SEARCH_SCREEN_BACK_NAVIGATION = "SEARCH_SCREEN_BACK_NAVIGATION"
        const val SUBSTITUTION_ITEM_KEY = "SUBSTITUTION_ITEM_KEY"
        const val SUBSTITUTION_ITEM_ADDED = "SUBSTITUTION_ITEM_ADDED"
        const val ERROR_SEARCH_SCREEN_BACK_NAVIGATION = "ERROR_SEARCH_SCREEN_BACK_NAVIGATION"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addFragmentListener()
        initView()
    }

    private fun addFragmentListener() {
        setFragmentResultListener(SubstitutionProcessingScreen.SUBSTITUTION_ERROR_SCREEN_BACK_NAVIGATION) { _, bundle ->
            setFragmentResult(ERROR_SEARCH_SCREEN_BACK_NAVIGATION, bundle)
            (activity as? BottomNavigationActivity)?.popFragment()
        }
    }

    private fun initView() {
        arguments?.apply {
            commerceItemId = getString(ManageSubstitutionFragment.COMMERCE_ITEM_ID, "")
            productId = getString(ManageSubstitutionFragment.PRODUCT_ID, "")
        }
        binding.apply {
            tvSearchProduct.setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    searchText = v?.text?.toString()
                    val productsRequestParams = searchText?.let { getRequestParamsBody(it) }
                    if (searchText?.length != 0) {
                        productsRequestParams?.let { getSubstituteProductList(it, productId) }
                    }
                    false
                } else {
                    false
                }
            }
            tvSearchProduct.doOnTextChanged { text, _, before, _ ->
                if (text.isNullOrEmpty() && before == 1) {
                    reloadFragment()
                }
            }

            btnConfirm.setOnClickListener(this@SearchSubstitutionFragment)
            crossIamgeView.setOnClickListener(this@SearchSubstitutionFragment)
            txtCancelSearch.setOnClickListener(this@SearchSubstitutionFragment)
            rootLayout.setOnClickListener(this@SearchSubstitutionFragment)
        }
        closeKeyBoard()
    }

    private fun getSubstituteProductList(requestParams: ProductsRequestParams, productId: String?) {
        initializeRecyclerView() // when we change the already searched text we need to show shimmer view again.
        binding.apply {
            lifecycleScope.launch {
                try {
                    productSubstitutionViewModel.getAllSearchedSubstitutions(
                        requestParams, productId
                    ).collectLatest {
                        productSubstitutionViewModel._pagingResponse.observe(
                            viewLifecycleOwner
                        ) { pagingResponse ->
                            totalItemCount = pagingResponse?.numItemsInTotal
                            triggerFirebaseEventsForSearchEvent(requestParams.searchTerm, requestParams.searchType.toString(), pagingResponse?.numItemsInTotal)
                        }
                        searchProductSubstitutionAdapter?.submitData(it)

                    }
                } catch (exception: Exception) {
                    FirebaseManager.logException(exception)
                }
            }
        }

        searchProductSubstitutionAdapter?.addLoadStateListener {
            when (it.refresh) {
                is LoadState.Loading -> {
                    showShimmerView()
                    binding.txtSubstitutionCount.visibility = GONE
                }

                is LoadState.NotLoading -> {
                    hideShimmerView()
                    binding.txtSubstitutionCount.visibility = VISIBLE
                    if (searchProductSubstitutionAdapter?.itemCount == 0) {
                        binding.txtSubstitutionCount.text = formattedProductCount(0)
                    } else {
                        binding.txtSubstitutionCount.text = totalItemCount?.let {
                            formattedProductCount(it)
                        }
                        if (totalItemCount!=0) {
                            triggerFirebaseEventForSearchResultEvent(requestParams.searchTerm)
                            triggerFirebaseEventForViewItemList(searchProductSubstitutionAdapter?.snapshot()?.items)
                        }
                    }
                }

                is LoadState.Error -> {
                    hideShimmerView()
                    val error = when {
                        it.prepend is LoadState.Error -> it.prepend as LoadState.Error
                        it.append is LoadState.Error -> it.append as LoadState.Error
                        it.refresh is LoadState.Error -> it.refresh as LoadState.Error
                        else -> null
                    }
                    FirebaseManager.logException(error?.error?.message)
                    error?.error?.message?.let { message ->
                        showErrorView(message)
                    }
                }

                else -> {
                    // Nothing to do
                }
            }
        }
    }

    private fun triggerFirebaseEventForSearchResultEvent(searchTerm: String) {
        val viewSearchBundle = Bundle()
        viewSearchBundle.putString(FirebaseManagerAnalyticsProperties.PropertyNames.SEARCH_TERM, searchTerm)
        AnalyticsManager.logEvent(FirebaseAnalytics.Event.VIEW_SEARCH_RESULTS, viewSearchBundle)
    }

    fun triggerFirebaseEventsForSearchEvent(searchTerm:String, searchType:String, totalItemCount:Int?) {
        val searchBundle = Bundle()
        if (totalItemCount != null) {
            searchBundle.putInt(FirebaseManagerAnalyticsProperties.PropertyNames.SEARCH_RESULT_COUNT, totalItemCount)
        }
        searchBundle.putString(FirebaseManagerAnalyticsProperties.PropertyNames.SEARCH_TERM, searchTerm)
        searchBundle.putString(FirebaseManagerAnalyticsProperties.PropertyNames.SEARCH_TYPE, searchType)
        AnalyticsManager.logEvent(FirebaseManagerAnalyticsProperties.SEARCH, searchBundle)
    }

    fun triggerFirebaseEventForViewItemList(productList: List<ProductList>?) {
        val productListParams = Bundle()
        productListParams.apply {

            productList?.let {
                val itemArrayEvent = arrayListOf<Bundle>()
                for (product in it) {
                    val productListItem = Bundle()
                    productListItem.apply {
                        putString(
                            FirebaseAnalytics.Param.ITEM_ID, product.productId
                        )

                        putString(
                            FirebaseAnalytics.Param.ITEM_NAME, product.productName
                        )

                        product.price?.let { it1 ->
                            putFloat(
                                FirebaseAnalytics.Param.PRICE, it1
                            )
                        }

                        putString(
                            FirebaseAnalytics.Param.ITEM_BRAND, product.brandText
                        )

                        putString(
                            FirebaseManagerAnalyticsProperties.PropertyNames.ITEM_RATING,
                            product.averageRating
                        )

                        putString(
                            FirebaseManagerAnalyticsProperties.PropertyNames.LOCATION_ID,
                            KotlinUtils.getPreferredPlaceId()
                        )

                        itemArrayEvent.add(this)
                    }
                }
                putParcelableArray(
                    FirebaseAnalytics.Param.ITEMS,
                    itemArrayEvent.toTypedArray()
                )
            }

            AnalyticsManager.logEvent(
                FirebaseManagerAnalyticsProperties.VIEW_ITEM_LIST,
                this
            )
        }
    }

    private fun formattedProductCount(count: Int): Spanned {
        val totalItemCount: String =
            "<b>" + count + "</b>".plus(
                getString(R.string.item_found)
            )
        return HtmlCompat.fromHtml(
            totalItemCount, HtmlCompat.FROM_HTML_MODE_COMPACT
        )
    }

    private fun initializeRecyclerView() {
        searchProductSubstitutionAdapter = SearchProductSubstitutionAdapter(this)
        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = searchProductSubstitutionAdapter
            addOnScrollListener(object : OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (!recyclerView.canScrollVertically(1)) {
                        binding.viewSeprator.visibility = VISIBLE
                    } else {
                        binding.viewSeprator.visibility = GONE
                    }
                }
            })
        }
    }

    private fun showShimmerView() {
        binding.shimmerLayout.visibility = VISIBLE
        val shimmer = Shimmer.AlphaHighlightBuilder().build()
        binding.shimmerLayout.setShimmer(shimmer)
        binding.shimmerLayout.startShimmer()
    }

    private fun hideShimmerView() {
        binding.shimmerLayout.setShimmer(null)
        binding.shimmerLayout.stopShimmer()
        binding.shimmerLayout.visibility = GONE
    }

    private fun showErrorView(desc: String) {
        Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.ERROR, desc)
    }

    private fun getRequestParamsBody(searchTerm: String): ProductsRequestParams {
        val productsRequestParams = ProductsRequestParams(
            searchTerm = searchTerm,
            searchType = ProductsRequestParams.SearchType.SEARCH,
            responseType = ProductsRequestParams.ResponseType.DETAIL,
            pageOffset = 0,
        )
        productsRequestParams.filterContent = false
        productsRequestParams.sendDeliveryDetailsParams = true
        return productsRequestParams
    }

    override fun clickOnProductSelection(productList: ProductList?) {
        binding.btnConfirm.isEnabled = true
        binding.btnConfirm.background =
            ResourcesCompat.getDrawable(resources, R.drawable.black_color_drawable, null)
        this.productList = productList
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnConfirm -> confirmProductSelection()
            R.id.crossIamgeView -> {
                binding.tvSearchProduct.text?.clear()
                if (!searchText.isNullOrEmpty()) {
                    reloadFragment()
                }
            }

            R.id.txtCancelSearch -> (activity as BottomNavigationActivity).popFragment()
        }
    }

    private fun reloadFragment() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            parentFragmentManager.beginTransaction().detach(this).commitNow()
            parentFragmentManager.beginTransaction().attach(this).commitNow()
        } else {
            parentFragmentManager.beginTransaction().detach(this).attach(this).commit()
        }
    }

    private fun confirmProductSelection() {
        callInventoryApi()
    }

    private fun callInventoryApi() {

        val storeId: String? = KotlinUtils.getDeliveryType()?.storeId

        if (productList?.sku == null || storeId.isNullOrEmpty()) {
            return
        }

        productList?.sku?.let { sku->
            productSubstitutionViewModel.getInventoryForKiboProducts(storeId, sku)
        }
        productSubstitutionViewModel.stockInventoryResponse.observe(viewLifecycleOwner) { skuInventoryResponse->
            skuInventoryResponse.getContentIfNotHandled()?.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        binding.progressBar.visibility = VISIBLE
                    }

                    Status.SUCCESS -> {
                        resource.data?.skuInventory?.let { inventoryList ->
                            val configQuantity: Int? =
                                AppConfigSingleton.enhanceSubstitution?.thresholdQuantityForSubstitutionProduct

                            val inventoryQuantity: Int? = inventoryList.getOrNull(0)?.quantity
                            if (inventoryQuantity != null && configQuantity != null) {
                                if (inventoryList.isNullOrEmpty() || inventoryQuantity < configQuantity) {
                                    binding.progressBar.visibility = GONE
                                    productOutOfStockErrorMessage()
                                    return@observe
                                } else {
                                    navigateToPdpScreen()
                                }
                            }
                        }
                    }

                    Status.ERROR -> {
                        binding.progressBar.visibility = GONE
                        showErrorView(getString(R.string.common_error_unfortunately_something_went_wrong))
                    }
                }
            }
        }
    }

    private fun navigateToPdpScreen() {
        if (commerceItemId?.isEmpty() == true) {
            /*navigate to pdp with selected product  object and then call add to cart api in order to add substitute there*/
            binding.progressBar.visibility = GONE
            setResultAndNavigationToPdpWithProduct(bundleOf(SUBSTITUTION_ITEM_KEY to productList))
        } else {
            callAddSubstitutionApi()
        }
    }

    private fun callAddSubstitutionApi() {
        /*add substitute api here since we have commarceId because product is already added in cart */
        val addSubstitutionRequest = AddSubstitutionRequest(
            substitutionSelection = SubstitutionChoice.USER_CHOICE.name,
            substitutionId = productList?.sku,
            commerceItemId = commerceItemId
        )
        productSubstitutionViewModel.addSubstitutionForProduct(addSubstitutionRequest)
        productSubstitutionViewModel.addSubstitutionResponse.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        binding.progressBar.visibility = VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.progressBar.visibility = GONE
                        /* if we get form exception need to show error popup*/
                        resource.data?.data?.getOrNull(0)?.formexceptions?.getOrNull(0)?.let {
                            if (it.message?.isNotEmpty() == true) {
                                showErrorScreen(SubstitutionChoice.USER_CHOICE.name)
                                return@observe
                            }
                        }
                        triggerFirebaseEventForSubstitution(selectionChoice = SubstitutionChoice.USER_CHOICE.name)
                        productList?.price?.let {
                                price ->
                            triggerFirebaseEventForAddSubstitution(itemId = productList?.productId,itemName= productList?.productName, itemPrice = price)
                        }
                        setResultAndNavigationToPdpWithProduct(
                            bundleOf(SUBSTITUTION_ITEM_KEY to productList)
                        )
                    }

                    Status.ERROR -> {
                        binding.progressBar.visibility = GONE
                        showErrorScreen(SubstitutionChoice.USER_CHOICE.name)
                    }
                }
            }
        })
    }

    fun showErrorScreen(selectionChoice: String) {
        productSubstitutionViewModel.addSubstitutionResponse.removeObservers(viewLifecycleOwner)
        (activity as? BottomNavigationActivity)?.pushFragment(
            SubstitutionProcessingScreen.newInstance(
                commerceItemId,
                productList?.sku,
                selectionChoice
            )
        )
    }

    private fun setResultAndNavigationToPdpWithProduct(bundle: Bundle) {
        /*send product details to pdp screen*/
        setFragmentResult(SEARCH_SCREEN_BACK_NAVIGATION, bundle)
        (activity as? BottomNavigationActivity)?.popFragment()
    }

    private fun productOutOfStockErrorMessage() {
        KotlinUtils.showGeneralInfoDialog(
            requireActivity().supportFragmentManager,
            getString(R.string.item_outofstock_error),
            getString(R.string.out_of_stock_dialog_title),
            getString(R.string.got_it),
            R.drawable.es_no_stock_available,
            false
        )
    }

    private fun closeKeyBoard() {
        val view = activity?.currentFocus
        if (view != null) {
            KeyboardUtil.hideSoftKeyboard(activity)
        }
    }
}