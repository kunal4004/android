package za.co.woolworths.financial.services.android.ui.fragments.product.refine


import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_refinement.*
import za.co.woolworths.financial.services.android.models.dto.ProductView
import za.co.woolworths.financial.services.android.models.dto.RefinementNavigation
import za.co.woolworths.financial.services.android.models.dto.RefinementSelectableItem
import za.co.woolworths.financial.services.android.ui.adapters.RefinementNavigationAdapter
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.OnRefinementOptionSelected
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.BaseFragmentListner
import za.co.woolworths.financial.services.android.util.Utils


class RefinementNavigationFragment : BaseRefinementFragment() {

    private lateinit var listener: OnRefinementOptionSelected
    private var refinementNavigationAdapter: RefinementNavigationAdapter? = null
    private var clearOrRresetRefinement: TextView? = null
    private var productView: ProductView? = null
    private var backButton: ImageView? = null
    private var dataList = arrayListOf<RefinementSelectableItem>()
    private var baseNavigationState = ""
    private var updatedNavigationState = ""
    private var pageTitle: TextView? = null
    private val emptyNavigationState = ""

    companion object {
        private val ARG_PARAM = "productViewObject"
        val ON_PROMOTION: String = "On Promotion"
        val CATEGORY: String = "Category"
        val NAVIGATION_STATE = "NAVIGATION_STATE"
        val UPDATED_NAVIGATION_STATE = "UPDATED_NAVIGATION_STATE"

        fun getInstance(productView: ProductView, navigationState: String, updatedNavigationState: String) = RefinementNavigationFragment().withArgs {
            putString(ARG_PARAM, Utils.toJson(productView))
            putString(NAVIGATION_STATE, navigationState)
            putString(UPDATED_NAVIGATION_STATE, updatedNavigationState)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            productView = Utils.jsonStringToObject(it.getString(ARG_PARAM), ProductView::class.java) as ProductView
            baseNavigationState = it.getString(NAVIGATION_STATE, "")
            updatedNavigationState = it.getString(UPDATED_NAVIGATION_STATE, "")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        activity?.let {
            backButton = it.findViewById(R.id.btnClose)
            clearOrRresetRefinement = it.findViewById(R.id.resetRefinement)
            pageTitle = it.findViewById(R.id.toolbarText)
        }
        backButton?.setImageResource(R.drawable.close_24)
        pageTitle?.text = resources.getString(R.string.refine)
        backButton?.setOnClickListener { onBackPressed() }
        clearOrRresetRefinement?.setOnClickListener { if (showReset()) listener.onRefinementReset() else listener.onRefinementClear() }
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
            refinementList.adapter = refinementNavigationAdapter!!
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnRefinementOptionSelected) {
            listener = context
        } else {
            throw ClassCastException("$context must implement OnRefinementOptionSelected.")
        }
    }

    private fun getRefinementSelectableItems(navigationList: ArrayList<RefinementNavigation>): ArrayList<RefinementSelectableItem> {
        var dataList = arrayListOf<RefinementSelectableItem>()
        var isHeaderAddedForCategory: Boolean = false
        navigationList.forEach {
            if (it.displayName.contentEquals(ON_PROMOTION)) {
                dataList.add(0, RefinementSelectableItem(it, RefinementSelectableItem.ViewType.SECTION_HEADER))
                var refinementSelectableItem = RefinementSelectableItem(it, RefinementSelectableItem.ViewType.PROMOTION)
                refinementSelectableItem.isSelected = (it.refinementCrumbs != null && it.refinementCrumbs.size > 0)
                dataList.add(1, refinementSelectableItem)
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
        seeResultCount.text = getString(R.string.see_results_count_start) + count.toString() + getString(R.string.see_results_count_end)
    }

    override fun onBackPressed() {
        activity?.finish()
        activity?.overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }

    private fun seeResults() {
        listener.onSeeResults(emptyNavigationState)
    }

    private fun updateToolBarMenuText() {
        if (showReset()) {
            clearOrRresetRefinement?.text = getString(R.string.refine_reset)
            clearOrRresetRefinement?.isEnabled = !TextUtils.isEmpty(baseNavigationState)
        } else {
            clearOrRresetRefinement?.text = getString(R.string.refinement_clear)
            clearOrRresetRefinement?.isEnabled = !TextUtils.isEmpty(updatedNavigationState)
        }
    }

    private fun showReset(): Boolean {
        return (!TextUtils.isEmpty(baseNavigationState) && TextUtils.isEmpty(updatedNavigationState))
    }

}
