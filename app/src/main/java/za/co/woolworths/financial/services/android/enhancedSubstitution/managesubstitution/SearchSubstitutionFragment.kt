package za.co.woolworths.financial.services.android.enhancedSubstitution.managesubstitution

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.databinding.LayoutSearchSubstitutionFragmentBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.enhancedSubstitution.adapter.SearchProductSubstitutionAdapter
import za.co.woolworths.financial.services.android.enhancedSubstitution.apihelper.SubstitutionApiHelper
import za.co.woolworths.financial.services.android.enhancedSubstitution.repository.ProductSubstitutionRepository
import za.co.woolworths.financial.services.android.enhancedSubstitution.viewmodel.ProductSubstitutionViewModel
import za.co.woolworths.financial.services.android.enhancedSubstitution.viewmodel.ProductSubstitutionViewModelFactory
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
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
        }

       binding.tvSearchProduct?.setOnEditorActionListener { v, actionId, event ->
           if (actionId == EditorInfo.IME_ACTION_DONE) {
               var productsRequestParams = getRequestParamsBody(v.text.toString())
               getSubstututeProductList(productsRequestParams)
               true
           }
            false
       }

       binding.txtCancelSearch?.setOnClickListener {
           binding.tvSearchProduct?.text?.clear()
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
                binding.txtSubstitutionCount?.text = searchProductSubstitutionAdapter?.itemCount.toString().plus(" ITEMS FOUND")
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

