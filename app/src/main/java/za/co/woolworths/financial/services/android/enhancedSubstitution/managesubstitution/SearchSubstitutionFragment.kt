package za.co.woolworths.financial.services.android.enhancedSubstitution.managesubstitution

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.awfs.coordination.R
import com.awfs.coordination.databinding.LayoutSearchSubstitutionFragmentBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.enhancedSubstitution.ProductListSelectionListener
import za.co.woolworths.financial.services.android.enhancedSubstitution.adapter.SearchProductSubstitutionAdapter
import za.co.woolworths.financial.services.android.enhancedSubstitution.apihelper.SubstitutionApiHelper
import za.co.woolworths.financial.services.android.enhancedSubstitution.repository.ProductSubstitutionRepository
import za.co.woolworths.financial.services.android.enhancedSubstitution.viewmodel.ProductSubstitutionViewModel
import za.co.woolworths.financial.services.android.enhancedSubstitution.viewmodel.ProductSubstitutionViewModelFactory
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.FoodProductNotAvailableForCollectionDialog
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ProductDetailsFindInStoreDialog
import za.co.woolworths.financial.services.android.util.KeyboardUtil
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding


class SearchSubstitutionFragment : BaseFragmentBinding<LayoutSearchSubstitutionFragmentBinding>(
        LayoutSearchSubstitutionFragmentBinding::inflate
) , ProductListSelectionListener, OnClickListener, FoodProductNotAvailableForCollectionDialog.IProductNotAvailableForCollectionDialogListener {

    private var searchProductSubstitutionAdapter: SearchProductSubstitutionAdapter? = null
    private lateinit var productSubstitutionViewModel: ProductSubstitutionViewModel
    private var productList: ProductList? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViewModel()
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

        binding.tvSearchProduct.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val productsRequestParams = getRequestParamsBody(v?.text.toString())
                if (v?.text?.length != 0) {
                    getSubstututeProductList(productsRequestParams)
                }
                false
            } else {
                false
            }
        }

        binding.btnConfirm?.setOnClickListener(this)
        binding.crossIamgeView?.setOnClickListener(this)
        binding.txtCancelSearch?.setOnClickListener(this)
        binding.rootLayout?.setOnClickListener(this)

        closeKeyBoard();
    }

    private fun setUpViewModel() {
        productSubstitutionViewModel = ViewModelProvider(
                this,
                ProductSubstitutionViewModelFactory(ProductSubstitutionRepository(SubstitutionApiHelper()))
        ).get(ProductSubstitutionViewModel::class.java)
    }

    private fun getSubstututeProductList(requestParams: ProductsRequestParams) {
        binding.shimmerLayout?.visibility = View.VISIBLE
        binding.shimmerLayout.startShimmer()
        lifecycleScope.launch {
            productSubstitutionViewModel?.getAllSearchedSubstitutions(
                    requestParams)?.collectLatest {
                binding.txtSubstitutionCount?.visibility = View.VISIBLE

                productSubstitutionViewModel._pagingResponse.observe(viewLifecycleOwner, {
                    val totalItemCount: String = "<b>" + it.numItemsInTotal?.toString() + "</b>" .plus(getString(R.string.item_found))
                    val formattedItemCount = HtmlCompat.fromHtml(totalItemCount, HtmlCompat.FROM_HTML_MODE_COMPACT)
                    binding.txtSubstitutionCount.text = formattedItemCount
                })
                searchProductSubstitutionAdapter?.submitData(it)
                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.visibility = View.GONE
            }
        }

        searchProductSubstitutionAdapter?.addLoadStateListener {
            if (it.refresh is LoadState.Error) {
                showErrorView()
            }
        }
    }

    private fun showErrorView() {
        /*todo error view if search api is failed*/

    }

    private fun getRequestParamsBody(searchTerm: String): ProductsRequestParams {
        /*todo need to remove hardcode values once UI is ready */
        var productsRequestParams = ProductsRequestParams(
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
            R.id.crossIamgeView -> binding.tvSearchProduct?.text?.clear()
            R.id.txtCancelSearch -> (activity as BottomNavigationActivity)?.popFragment()
        }
    }

    fun confirmProductSelection() {
        /* call inventory api */
        callInventoryApi()
        /* call add substitution api */
    }

    private fun callInventoryApi() {

        val storeId: String? =  Utils.getPreferredDeliveryLocation()?.let {
            it.fulfillmentDetails.storeId
        }

        if (productList?.sku == null || storeId == null || storeId?.isEmpty() == true) {
            return
        }

        productSubstitutionViewModel.getInventoryForSubstitution(storeId, productList?.sku!!)
        productSubstitutionViewModel.inventorySubstitution.observe(viewLifecycleOwner) {

            it.getContentIfNotHandled()?.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                    }
                    Status.SUCCESS -> {
                        if (resource.data?.skuInventory?.isNullOrEmpty() == true) {

                            productOutOfStockErrorMessage()
                            return@observe
                        }

                        if (resource?.data?.skuInventory?.get(0)?.quantity == 0) {
                            productOutOfStockErrorMessage()
                            return@observe
                        }
                        productOutOfStockErrorMessage()
                        /*add subs api*/
                        callAddSubsApi()

                    }
                    Status.ERROR -> {
                        /*todo error view if inventory api is failed*/
                    }
                }
            }
        }
    }

    fun callAddSubsApi() {
        /*todo call add subs api*/
    }

    fun productOutOfStockErrorMessage() {
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