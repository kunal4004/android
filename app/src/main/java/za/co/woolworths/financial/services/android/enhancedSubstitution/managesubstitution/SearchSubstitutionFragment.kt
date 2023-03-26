package za.co.woolworths.financial.services.android.enhancedSubstitution.managesubstitution

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
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
import za.co.woolworths.financial.services.android.util.KeyboardUtil
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding


class SearchSubstitutionFragment : BaseFragmentBinding<LayoutSearchSubstitutionFragmentBinding>(LayoutSearchSubstitutionFragmentBinding::inflate), ProductListSelectionListener, OnClickListener, OnEditorActionListener {

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
        hideKeyBoard(view)

        binding.tvSearchProduct?.setOnEditorActionListener(this)
        binding.btnConfirm?.setOnClickListener(this)
        binding.crossIamgeView?.setOnClickListener(this)
        binding.txtCancelSearch?.setOnClickListener(this)
        binding.rootLayout?.setOnClickListener(this)
    }

    private fun setUpViewModel() {
        productSubstitutionViewModel = ViewModelProvider(this, ProductSubstitutionViewModelFactory(ProductSubstitutionRepository(SubstitutionApiHelper()))).get(ProductSubstitutionViewModel::class.java)
    }

    private fun getSubstututeProductList(requestParams: ProductsRequestParams) {

        lifecycleScope.launch {

            productSubstitutionViewModel?.getAllSearchedSubstitutions(requestParams)?.collectLatest {
                binding.txtSubstitutionCount?.visibility = View.VISIBLE
                productSubstitutionViewModel._pagingResponse.observe(viewLifecycleOwner, {
                    val totalItemCount: String = "<b>" + it.numItemsInTotal?.toString() + "</b>".plus(getString(R.string.item_found))
                    val formattedItemCount = HtmlCompat.fromHtml(totalItemCount, HtmlCompat.FROM_HTML_MODE_COMPACT)
                    binding.txtSubstitutionCount.text = formattedItemCount
                })
                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.visibility = View.GONE
                searchProductSubstitutionAdapter?.submitData(it)
            }
        }

        searchProductSubstitutionAdapter?.addLoadStateListener {
            if (it.refresh is LoadState.Error) {
                showErrorView()
            }
        }
    }

    private fun showErrorView() {

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
        binding.btnConfirm.background = resources.getDrawable(R.drawable.black_background_with_corner_5, null)
        this.productList = productList
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnConfirm -> confirmProductSelection()
            R.id.crossIamgeView -> binding.tvSearchProduct?.text?.clear()
            R.id.txtCancelSearch -> (activity as BottomNavigationActivity)?.popFragment()
            R.id.rootLayout -> hideKeyBoard(v)
        }
    }

    fun confirmProductSelection() {
        /* call inventory api */
        callInventoryApi()
        /* call add substitution api */
    }

    private fun callInventoryApi() {
        if (productList?.sku == null) {
            return
        }
        /*todo get store id */
        val storeId = "473"
        productSubstitutionViewModel.getInventoryForSubstitution(storeId, productList?.sku!!)
        productSubstitutionViewModel.inventorySubstitution.observe(viewLifecycleOwner) {

            it.getContentIfNotHandled()?.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {

                    }
                    Status.SUCCESS -> {

                        if (resource.data?.skuInventory?.isNullOrEmpty() == true) {
                            //show OutOf stock pop up
                            return@observe
                        }

                        if (resource?.data?.skuInventory?.get(0)?.quantity == 0) {
                            // show out of stock pop up
                            return@observe
                        }
                    }
                    Status.ERROR -> {

                    }
                }
            }
        }

    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            hideKeyBoard(v)
            var productsRequestParams = getRequestParamsBody(v?.text.toString())
            if (v?.text?.length != 0) {
                binding.shimmerLayout?.visibility = View.VISIBLE
                binding.shimmerLayout.startShimmer()
                getSubstututeProductList(productsRequestParams)
            }
            return true
        }
        return false
    }

    fun hideKeyBoard(view: View?) {
        if (view !is EditText) {
            view?.setOnTouchListener { v, event ->
                KeyboardUtil.hideSoftKeyboard(activity)
                false
            }
        }
    }
}
