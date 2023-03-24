package za.co.woolworths.financial.services.android.enhancedSubstitution.managesubstitution

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.awfs.coordination.R
import com.awfs.coordination.databinding.LayoutSearchSubstitutionFragmentBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.enhancedSubstitution.adapter.SearchProductSubstitutionAdapter
import za.co.woolworths.financial.services.android.enhancedSubstitution.apihelper.SubstitutionApiHelper
import za.co.woolworths.financial.services.android.enhancedSubstitution.repository.ProductSubstitutionRepository
import za.co.woolworths.financial.services.android.enhancedSubstitution.viewmodel.ProductSubstitutionViewModel
import za.co.woolworths.financial.services.android.enhancedSubstitution.viewmodel.ProductSubstitutionViewModelFactory
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.util.KeyboardUtil
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding


class SearchSubstitutionFragment : BaseFragmentBinding<LayoutSearchSubstitutionFragmentBinding>(
        LayoutSearchSubstitutionFragmentBinding::inflate
) {

    private var searchProductSubstitutionAdapter: SearchProductSubstitutionAdapter? = null
    private lateinit var productSubstitutionViewModel: ProductSubstitutionViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViewModel()
        searchProductSubstitutionAdapter = SearchProductSubstitutionAdapter()

        binding.recyclerView?.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = searchProductSubstitutionAdapter
            addOnScrollListener(object : OnScrollListener(){
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (!recyclerView.canScrollVertically(1) ) {
                       binding.viewSeprator?.visibility = View.VISIBLE
                    } else {
                        binding.viewSeprator?.visibility = View.GONE
                    }
                }
            })
        }

        if (view !is EditText) {
            view.setOnTouchListener { v, event ->
                KeyboardUtil.hideSoftKeyboard(activity)
                false
            }
        }

       binding.tvSearchProduct?.setOnEditorActionListener { v, actionId, event ->
           if (actionId == EditorInfo.IME_ACTION_DONE) {
               var productsRequestParams = getRequestParamsBody(v.text.toString())
               if (v.text.length != 0) {
                   getSubstututeProductList(productsRequestParams)
               }
               true
           }
            false
       }

       binding.txtCancelSearch?.setOnClickListener {
           (activity as BottomNavigationActivity)?.popFragment()
       }

    }

    private fun setUpViewModel() {
        productSubstitutionViewModel = ViewModelProvider(
                this,
                ProductSubstitutionViewModelFactory(ProductSubstitutionRepository(SubstitutionApiHelper()))
        ).get(ProductSubstitutionViewModel::class.java)
    }

    private fun getSubstututeProductList(requestParams: ProductsRequestParams) =
        lifecycleScope.launch {
            productSubstitutionViewModel?.getAllSearchedSubstitutions(
                    requestParams)?.collectLatest {
                /*todo handle failure cases as well*/

                val itemCount =  productSubstitutionViewModel._pagingResponse?.value?.numItemsInTotal?.toString()
                itemCount.plus(resources.getString(R.string.item_found))
                binding.txtSubstitutionCount?.visibility = View.VISIBLE
                binding.txtSubstitutionCount?.text = itemCount
                searchProductSubstitutionAdapter?.submitData(it)
            }
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
}

