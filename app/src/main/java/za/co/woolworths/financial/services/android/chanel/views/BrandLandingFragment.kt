package za.co.woolworths.financial.services.android.chanel.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_brand_landing.*
import kotlinx.android.synthetic.main.fragment_brand_landing.incCenteredProgress
import kotlinx.android.synthetic.main.fragment_brand_landing.view.*
import za.co.woolworths.financial.services.android.chanel.model.ChanelResponse
import za.co.woolworths.financial.services.android.chanel.model.DynamicBanner
import za.co.woolworths.financial.services.android.chanel.services.network.ChanelApiHelper
import za.co.woolworths.financial.services.android.chanel.services.repository.ChanelRepository
import za.co.woolworths.financial.services.android.chanel.utils.FILTER_CONTENT
import za.co.woolworths.financial.services.android.chanel.utils.SEARCH_TERM
import za.co.woolworths.financial.services.android.chanel.utils.SEARCH_TYPE
import za.co.woolworths.financial.services.android.chanel.utils.setProgressIndicator
import za.co.woolworths.financial.services.android.chanel.viewmodel.ChanelViewModel
import za.co.woolworths.financial.services.android.chanel.viewmodel.ChanelViewModelFactory
import za.co.woolworths.financial.services.android.chanel.views.adapter.BrandLandingAdapter
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.service.network.ResponseStatus
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity
import za.co.woolworths.financial.services.android.ui.extension.withArgs

class BrandLandingFragment : Fragment(), NavigationClickListener {

    private lateinit var chanelViewModel: ChanelViewModel
    private var searchTerm: String? = null
    private var searchType: String? = null
    private var filterContent: Boolean = false
    private var menuActionSearch: MenuItem? = null

    companion object {
        fun newInstance(
            searchTerm: String,
            searchType: ProductsRequestParams.SearchType?,
            filterContent: Boolean,
        ) = BrandLandingFragment().withArgs {
            putString(SEARCH_TERM, searchTerm)
            putString(SEARCH_TYPE, searchType.toString().lowercase())
            putBoolean(FILTER_CONTENT, filterContent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_brand_landing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        arguments?.apply {
            searchType = getString(SEARCH_TYPE)
            searchTerm = getString(SEARCH_TERM)
            filterContent = getBoolean(FILTER_CONTENT)
        }

        (activity as? BottomNavigationActivity)?.apply {
            showToolbar()
            showBackNavigationIcon(true)
            setToolbarBackgroundDrawable(R.drawable.appbar_background)
            toolbar?.setNavigationOnClickListener { popFragment() }
            setTitle(searchTerm)
        }

        setupChanelViewModel()
        fetchChanelResponse()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.drill_down_category_menu, menu)
        menuActionSearch = menu.findItem(R.id.action_drill_search)
        menuActionSearch?.isVisible =
            (activity as? BottomNavigationActivity)?.currentFragment is BrandLandingFragment
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_drill_search -> {
                activity?.apply {
                    val openSearchActivity = Intent(this, ProductSearchActivity::class.java)
                    startActivity(openSearchActivity)
                    overridePendingTransition(0, 0)
                }
                true
            }
            else -> false
        }
    }

    private fun fetchChanelResponse() {

        chanelViewModel.getChanelResposne(
            searchTerm,
            searchType, filterContent
        ).observe(
            viewLifecycleOwner, {
                when (it.responseStatus) {
                    ResponseStatus.SUCCESS -> {
                        incCenteredProgress.setProgressIndicator(false)

                        setupChanelReyclerView(it.data)
                    }
                    ResponseStatus.LOADING -> {
                        incCenteredProgress.visibility = View.VISIBLE
                    }
                    ResponseStatus.ERROR -> {
                        incCenteredProgress.visibility = View.GONE
                        Log.e("ERROR:", it.data.toString())
                    }
                }
            }
        )
    }

    private fun setupChanelReyclerView(chanelResponse: ChanelResponse?) {
        val adapter = BrandLandingAdapter(
            context,
            chanelResponse?.dynamicBanners as List<DynamicBanner>, this
        )

        rv_chanel.layoutManager = LinearLayoutManager(requireContext())
        rv_chanel.adapter = adapter
    }

    private fun setupChanelViewModel() {
        chanelViewModel = ViewModelProvider(
            this,
            ChanelViewModelFactory(ChanelRepository(ChanelApiHelper()))
        ).get(ChanelViewModel::class.java)
    }

    override fun openProductDetailsView(productList: ProductList) {
        (activity as? BottomNavigationActivity)?.openProductDetailFragment(searchTerm, productList)
    }

    override fun openCategoryListView() {

    }

}