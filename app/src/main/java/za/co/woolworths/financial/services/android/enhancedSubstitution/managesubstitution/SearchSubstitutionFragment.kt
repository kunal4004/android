package za.co.woolworths.financial.services.android.enhancedSubstitution.managesubstitution

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.awfs.coordination.R
import com.awfs.coordination.databinding.LayoutSearchSubstitutionFragmentBinding
import com.facebook.shimmer.Shimmer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.enhancedSubstitution.ProductListSelectionListener
import za.co.woolworths.financial.services.android.enhancedSubstitution.adapter.SearchProductSubstitutionAdapter
import za.co.woolworths.financial.services.android.enhancedSubstitution.apihelper.SubstitutionApiHelper
import za.co.woolworths.financial.services.android.enhancedSubstitution.model.AddSubstitutionRequest
import za.co.woolworths.financial.services.android.enhancedSubstitution.repository.ProductSubstitutionRepository
import za.co.woolworths.financial.services.android.enhancedSubstitution.viewmodel.ProductSubstitutionViewModel
import za.co.woolworths.financial.services.android.enhancedSubstitution.viewmodel.ProductSubstitutionViewModelFactory
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment.Companion.USER_CHOICE
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.FoodProductNotAvailableForCollectionDialog
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ProductDetailsFindInStoreDialog
import za.co.woolworths.financial.services.android.util.KeyboardUtil
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding


class SearchSubstitutionFragment : BaseFragmentBinding<LayoutSearchSubstitutionFragmentBinding>(
        LayoutSearchSubstitutionFragmentBinding::inflate
), ProductListSelectionListener, OnClickListener,
        FoodProductNotAvailableForCollectionDialog.IProductNotAvailableForCollectionDialogListener {

    private var searchProductSubstitutionAdapter: SearchProductSubstitutionAdapter? = null
    private lateinit var productSubstitutionViewModel: ProductSubstitutionViewModel
    private var productList: ProductList? = null
    private var searchText: String? = null
    private var commerceItemId: String? = ""

    companion object {

        fun newInstance(
            commerceItemId: String?,
        ) = SearchSubstitutionFragment().withArgs {
            putString(ManageSubstitutionFragment.COMMERCE_ITEM_ID, commerceItemId)
        }

        const val SELECTED_SUBSTITUTED_PRODUCT = "SELECTED_SUBSTITUTED_PRODUCT"
        const val SUBSTITUTION_ITEM_KEY = "SUBSTITUTION_ITEM_KEY"
        const val SUBSTITUTION_ITEM_ADDED = "SUBSTITUTION_ITEM_ADDED"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViewModel()
        initView()
    }

    private fun initView() {
        arguments?.apply {
            commerceItemId = getString(ManageSubstitutionFragment.COMMERCE_ITEM_ID, "")
        }
        binding.apply {
            tvSearchProduct.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    searchText = v?.text?.toString()
                    val productsRequestParams = searchText?.let { getRequestParamsBody(it) }
                    if (searchText?.length != 0) {
                        productsRequestParams?.let { getSubstituteProductList(it) }
                    }
                    false
                } else {
                    false
                }
            }
            tvSearchProduct.doOnTextChanged { text, start, before, count ->
                if (text.isNullOrEmpty() && before == 1) {
                    reloadFragment()
                }
            }

            btnConfirm?.setOnClickListener(this@SearchSubstitutionFragment)
            crossIamgeView?.setOnClickListener(this@SearchSubstitutionFragment)
            txtCancelSearch?.setOnClickListener(this@SearchSubstitutionFragment)
            rootLayout?.setOnClickListener(this@SearchSubstitutionFragment)
        }

        closeKeyBoard()
    }

    private fun setUpViewModel() {
        productSubstitutionViewModel = ViewModelProvider(
            this,
            ProductSubstitutionViewModelFactory(ProductSubstitutionRepository(SubstitutionApiHelper()))
        )[ProductSubstitutionViewModel::class.java]
    }

    private fun getSubstituteProductList(requestParams: ProductsRequestParams) {
        initializeRecyclerView() // when we change the already searched text we need to show shimmer view again.
        binding.apply {
            lifecycleScope.launch {
                productSubstitutionViewModel?.getAllSearchedSubstitutions(
                    requestParams
                )?.collectLatest {
                    productSubstitutionViewModel._pagingResponse.observe(
                        viewLifecycleOwner
                    ) { pagingResponse ->
                        val totalItemCount: String =
                            "<b>" + pagingResponse.numItemsInTotal?.toString() + "</b>".plus(
                                getString(R.string.item_found)
                            )
                        val formattedItemCount =
                            HtmlCompat.fromHtml(totalItemCount, HtmlCompat.FROM_HTML_MODE_COMPACT)
                        txtSubstitutionCount.text = formattedItemCount
                    }
                    searchProductSubstitutionAdapter?.submitData(it)
                }
            }
        }

        searchProductSubstitutionAdapter?.addLoadStateListener {
            when (it.refresh) {
                is LoadState.Loading -> {
                    showShimmerView()
                    binding.txtSubstitutionCount?.visibility = View.GONE
                }
                is LoadState.NotLoading -> {
                    hideShimmerView()
                    binding.txtSubstitutionCount?.visibility = View.VISIBLE
                }
                is LoadState.Error -> {
                    hideShimmerView()
                    showErrorView(getString(R.string.common_error_unfortunately_something_went_wrong))

                }
                else -> {
                    // Nothing to do
                }
            }
        }
    }

    private fun initializeRecyclerView() {
        searchProductSubstitutionAdapter = SearchProductSubstitutionAdapter(this)
        binding.recyclerView?.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = searchProductSubstitutionAdapter
            addOnScrollListener(object : OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (!recyclerView.canScrollVertically(1)) {
                        binding.viewSeprator?.visibility = View.VISIBLE
                    } else {
                        binding.viewSeprator?.visibility = View.GONE
                    }
                }
            })
        }
    }

    private fun showShimmerView() {
        binding.shimmerLayout?.visibility = View.VISIBLE
        val shimmer = Shimmer.AlphaHighlightBuilder().build()
        binding.shimmerLayout.setShimmer(shimmer)
        binding.shimmerLayout.startShimmer()
    }

    private fun hideShimmerView() {
        binding.shimmerLayout.setShimmer(null)
        binding.shimmerLayout.stopShimmer()
        binding.shimmerLayout.visibility = View.GONE
    }

    private fun showErrorView(desc: String) {
        /*todo show new error view if search api is failed*/
        Utils.displayValidationMessage(
                activity,
                CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                desc
        )
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
        binding.btnConfirm.background = resources.getDrawable(R.drawable.black_color_drawable, null)
        this.productList = productList
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnConfirm -> confirmProductSelection()
            R.id.crossIamgeView -> {
                binding.tvSearchProduct?.text?.clear()
                if (!searchText.isNullOrEmpty()) {
                    reloadFragment()
                }
            }
            R.id.txtCancelSearch -> (activity as BottomNavigationActivity)?.popFragment()
        }
    }

    private fun reloadFragment() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fragmentManager?.beginTransaction()?.detach(this)?.commitNow()
            fragmentManager?.beginTransaction()?.attach(this)?.commitNow()
        } else {
            fragmentManager?.beginTransaction()?.detach(this)?.attach(this)?.commit()
        }
    }

    private fun confirmProductSelection() {
        /* call inventory api */
        callInventoryApi()
    }

    private fun callInventoryApi() {
        val storeId: String? = Utils.getPreferredDeliveryLocation()?.let {
            it.fulfillmentDetails.storeId
        }

        if (productList?.sku == null || storeId?.isNullOrEmpty() == true) {
            return
        }


        productSubstitutionViewModel.getInventoryForSubstitution(storeId, productList?.sku!!)
        productSubstitutionViewModel.inventorySubstitution.observe(viewLifecycleOwner) {

            it.getContentIfNotHandled()?.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        binding.progressBar?.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        resource?.data?.skuInventory?.let { inventoryList ->
                            val configQuantity: Int? = AppConfigSingleton.enhanceSubstitution?.thresholdQuantityForSubstitutionProduct
                            val inventoryQuantity: Int? = inventoryList?.getOrNull(0)?.quantity
                            if (inventoryQuantity != null && configQuantity != null) {
                                if (inventoryList?.isNullOrEmpty() == true || inventoryQuantity < configQuantity) {
                                    binding.progressBar?.visibility = View.GONE
                                    productOutOfStockErrorMessage()
                                    return@observe
                                }
                            }
                            navigateToPdpScreen()
                        }
                    }
                    Status.ERROR -> {
                        binding.progressBar?.visibility = View.GONE
                        /*todo error view if inventory api is failed*/
                        showErrorView(getString(R.string.common_error_unfortunately_something_went_wrong))
                    }
                }
            }
        }
    }

    private fun navigateToPdpScreen() {

        if (commerceItemId?.isEmpty() == true) {
            /*navigate to pdp with selected product  object and then call add to cart api in order to add substitute there*/
            binding.progressBar?.visibility = View.GONE
            setResultAndNaviagationToPdpWithProduct(SELECTED_SUBSTITUTED_PRODUCT, bundleOf(SUBSTITUTION_ITEM_KEY to productList))
        } else {
            /*add subsitute api here since we have commarceId because product is already added in cart */
            val addSubstitutionRequest = AddSubstitutionRequest(
                    substitutionSelection = USER_CHOICE,
                    substitutionId = productList?.sku,
                    commerceItemId = commerceItemId
            )
            productSubstitutionViewModel?.addSubstitutionForProduct(addSubstitutionRequest)
            productSubstitutionViewModel?.addSubstitutionResponse?.observe(viewLifecycleOwner, {
                it.getContentIfNotHandled()?.let { resource ->
                    when (resource.status) {
                        Status.LOADING -> {
                            binding.progressBar?.visibility = View.VISIBLE
                        }
                        Status.SUCCESS -> {
                            binding.progressBar?.visibility = View.GONE

                            /* if we get form exception need to show error popup*/
                            resource?.data?.data?.getOrNull(0)?.formExceptions?.getOrNull(0)?.let {
                               if (it.message?.isNotEmpty() == true) {
                                   showErrorView(it.message)
                               }
                                return@observe
                            }

                            /* navigate to pdp and call getSubs. api*/
                            setResultAndNaviagationToPdpWithProduct(SELECTED_SUBSTITUTED_PRODUCT, bundleOf(SUBSTITUTION_ITEM_ADDED to true))
                        }
                        Status.ERROR -> {
                            binding.progressBar?.visibility = View.GONE
                            /*todo show error view if add subs api is failed */
                            showErrorView(getString(R.string.common_error_unfortunately_something_went_wrong))
                        }
                    }
                }
            })
        }
    }

    private fun setResultAndNaviagationToPdpWithProduct(requestKey: String, bundle: Bundle) {
        /*send product details to pdp screen*/
        setFragmentResult(requestKey, bundle)
        (activity as? BottomNavigationActivity)?.popFragment()
        (activity as? BottomNavigationActivity)?.popFragment()
    }

    private fun productOutOfStockErrorMessage() {
        try {
            activity?.supportFragmentManager?.beginTransaction()?.apply {
                val productDetailsFindInStoreDialog =
                        FoodProductNotAvailableForCollectionDialog.newInstance(
                        )
                productDetailsFindInStoreDialog.show(
                        this,
                        ProductDetailsFindInStoreDialog::class.java.simpleName
                )
            }
        } catch (ex: IllegalStateException) {
            FirebaseManager.logException(ex)
        }
    }

    fun hideKeyBoard(view: View?) {
        if (view !is EditText) {
            view?.setOnTouchListener { v, event ->
                KeyboardUtil.hideSoftKeyboard(activity)
                false
            }
        }
    }

    private fun closeKeyBoard() {
        val view = activity?.currentFocus
        if (view != null) {
            KeyboardUtil.hideSoftKeyboard(activity)
        }
    }

    override fun onChangeDeliveryOption() {

    }

    override fun onFindInStore() {

    }

}