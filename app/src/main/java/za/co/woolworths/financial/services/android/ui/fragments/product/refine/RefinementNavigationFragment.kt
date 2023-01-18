package za.co.woolworths.financial.services.android.ui.fragments.product.refine

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentRefinementBinding
import za.co.woolworths.financial.services.android.models.dto.ProductView
import za.co.woolworths.financial.services.android.models.dto.RefinementNavigation
import za.co.woolworths.financial.services.android.models.dto.RefinementSelectableItem
import za.co.woolworths.financial.services.android.ui.adapters.RefinementNavigationAdapter
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.RefinementDrawerFragment
import za.co.woolworths.financial.services.android.ui.fragments.RefinementDrawerFragment.Companion.NAVIGATION_STATE
import za.co.woolworths.financial.services.android.ui.fragments.RefinementDrawerFragment.Companion.UPDATED_NAVIGATION_STATE
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.OnRefinementOptionSelected
import za.co.woolworths.financial.services.android.util.Utils

class RefinementNavigationFragment : BaseRefinementFragment() {

    private lateinit var listener: OnRefinementOptionSelected
    private var refinementNavigationAdapter: RefinementNavigationAdapter? = null
    private var productView: ProductView? = null
    private var dataList = arrayListOf<RefinementSelectableItem>()
    private var baseNavigationState = ""
    private var updatedNavigationState = ""
    private val emptyNavigationState = ""

    companion object {
        private val ARG_PARAM = "productViewObject"
        val ON_PROMOTION: String = "On Promotion"
        val CATEGORY: String = "Category"

        fun getInstance(productView: ProductView, navigationState: String, updatedNavigationState: String) = RefinementNavigationFragment().withArgs {
            putString(ARG_PARAM, Utils.toJson(productView))
            putString(NAVIGATION_STATE, navigationState)
            putString(UPDATED_NAVIGATION_STATE, updatedNavigationState)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            productView = Utils.jsonStringToObject(it.getString(ARG_PARAM), ProductView::class.java) as ProductView
            baseNavigationState = it.getString(NAVIGATION_STATE, "")
            updatedNavigationState = it.getString(UPDATED_NAVIGATION_STATE, "")
        }

        try {
            listener = parentFragment as OnRefinementOptionSelected
        } catch (e: ClassCastException) {
            throw ClassCastException("Calling fragment must implement Callback interface")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.initViews()
    }

    private fun FragmentRefinementBinding.initViews() {
        listener.apply {
            setPageTitle(resources.getString(R.string.filter_results))
            hideBackButton()
        }
        closeButton?.setOnClickListener { onBackPressed() }
        clearAndResetFilter?.text = getString(R.string.reset_filter)
        clearAndResetFilter?.setOnClickListener { listener.onRefinementReset() }
        refinementSeeResult.setOnClickListener { seeResults() }
        refinementList.layoutManager = LinearLayoutManager(activity)
        updateToolBarMenuText()
        loadData()
    }

    private fun loadData() {
        if (productView!!.navigation != null && productView!!.navigation.size > 0) {
            setResultCount(productView?.pagingResponse?.numItemsInTotal)
            dataList = getRefinementSelectableItems(productView?.navigation!!)
            refinementNavigationAdapter = activity?.let { RefinementNavigationAdapter(it, listener, dataList, productView?.history!!) }
            binding.refinementList.adapter = refinementNavigationAdapter!!
        }
    }

    private fun getRefinementSelectableItems(navigationList: ArrayList<RefinementNavigation>): ArrayList<RefinementSelectableItem> {
        var dataList = arrayListOf<RefinementSelectableItem>()
        var isHeaderAddedForCategory: Boolean = false
        navigationList.forEach {
            if (it.displayName.contentEquals(ON_PROMOTION)) {
                var refinementSelectableItem = RefinementSelectableItem(it, RefinementSelectableItem.ViewType.PROMOTION)
                refinementSelectableItem.isSelected = (it.refinementCrumbs != null && it.refinementCrumbs.size > 0)
                dataList.add(0, refinementSelectableItem)
            } else if ((it.refinements != null && it.refinements.size > 0) || (it.refinementCrumbs != null && it.refinementCrumbs.size > 0)) {
                if (!isHeaderAddedForCategory) {
                    dataList.add(RefinementSelectableItem(it, RefinementSelectableItem.ViewType.SECTION_HEADER))
                    isHeaderAddedForCategory = true
                }
                dataList.add(RefinementSelectableItem(it, RefinementSelectableItem.ViewType.OPTIONS))
            }
        }
        return dataList
    }

    private fun setResultCount(count: Int?) {
        binding.seeResultCount.text = getString(R.string.see_results_count_start) + count.toString() + getString(R.string.see_results_count_end)
    }

    override fun onBackPressed() {
        listener?.onBackPressedWithOutRefinement()
    }

    private fun seeResults() {
        listener.onSeeResults(emptyNavigationState, (parentFragment as RefinementDrawerFragment).isMultiSelectCategoryRefined)
    }

    private fun updateToolBarMenuText() {
        binding.clearAndResetFilter?.text = getString(R.string.reset_filter)
        binding.clearAndResetFilter?.isEnabled = !TextUtils.isEmpty(baseNavigationState)
    }

}
