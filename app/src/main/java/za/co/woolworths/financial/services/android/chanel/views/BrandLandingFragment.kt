package za.co.woolworths.financial.services.android.chanel.views

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.chanel.services.network.ChanelApiHelper
import za.co.woolworths.financial.services.android.chanel.services.repository.ChanelRepository
import za.co.woolworths.financial.services.android.chanel.utils.ChanelUtils
import za.co.woolworths.financial.services.android.chanel.viewmodel.ChanelViewModel
import za.co.woolworths.financial.services.android.chanel.viewmodel.ChanelViewModelFactory
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.service.network.ResponseStatus
import za.co.woolworths.financial.services.android.ui.extension.withArgs

class BrandLandingFragment: Fragment() {

    private lateinit var chanelViewModel: ChanelViewModel
    private var searchTerm: String? = null
    private var searchType: String? = null
    private var filterContent: Boolean = false

    companion object {
        fun newInstance(
            searchTerm: String,
            searchType: ProductsRequestParams.SearchType?,
            filterContent: Boolean,
        ) = BrandLandingFragment().withArgs {
            putString(ChanelUtils.SEARCH_TERM, searchTerm)
            putString(ChanelUtils.SEARCH_TYPE, searchType.toString().lowercase())
            putBoolean(ChanelUtils.FILTER_CONTENT, filterContent)
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_brand_landing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.apply {
            searchType = getString(ChanelUtils.SEARCH_TYPE, "")
            searchTerm = getString(ChanelUtils.SEARCH_TERM, "")
            filterContent = getBoolean(ChanelUtils.FILTER_CONTENT, false)
        }
        setupChanelViewModel()
        fetchData()
    }

    private fun fetchData() {
        chanelViewModel.getChanelResposne(searchTerm,
            searchType, filterContent).observe(
            viewLifecycleOwner, {
                when (it.responseStatus) {
                    ResponseStatus.SUCCESS -> {
                       Log.e("SUCCESS:", it.data.toString())
                    }
                    ResponseStatus.LOADING -> {
                        Log.e("LOADING:", "CALLED")

                    }
                    ResponseStatus.ERROR -> {
                        Log.e("ERROR:", it.data.toString())
                    }
                }
            }
        )
    }

    private fun setupChanelViewModel() {
        chanelViewModel = ViewModelProvider(
            this,
            ChanelViewModelFactory(ChanelRepository(ChanelApiHelper()))
        ).get(ChanelViewModel::class.java)
    }

}