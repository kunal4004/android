package za.co.woolworths.financial.services.android.ui.fragments.product.refine


import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
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
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.OnRefinementOptionSelected
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.RefinementOnBackPressed
import za.co.woolworths.financial.services.android.util.Utils


class RefinementNavigationFragment : BaseRefinementFragment(), RefinementOnBackPressed, RefinementNavigationAdapter.OnPromotionSelectionChanged {

    private lateinit var listener: OnRefinementOptionSelected
    private var refinementNavigationAdapter: RefinementNavigationAdapter? = null
    private var clearOrRresetRefinement: TextView? = null
    private var productView: ProductView? = null
    private var backButton: ImageView? = null
    private var dataList = arrayListOf<RefinementSelectableItem>()
    private var isResetEnabled = false

    companion object {
        private val ARG_PARAM = "productViewObject"
        val ON_PROMOTION: String = "On Promotion"
        val CATEGORY: String = "Category"

        fun getInstance(productView: ProductView, isResetEnabled: Boolean): RefinementNavigationFragment {
            val fragment = RefinementNavigationFragment()
            val args = Bundle()
            args.putString(ARG_PARAM, Utils.toJson(productView))
            args.putBoolean("isResetEnabled", isResetEnabled)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            productView = Utils.jsonStringToObject(arguments.getString(ARG_PARAM), ProductView::class.java) as ProductView
            isResetEnabled = arguments.getBoolean("isResetEnabled")
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        backButton = activity.findViewById(R.id.btnClose)
        backButton?.setImageResource(R.drawable.close_24)
        clearOrRresetRefinement = activity.findViewById(R.id.resetRefinement)
        backButton?.setOnClickListener { onBackPressed() }
        clearOrRresetRefinement?.setOnClickListener { if (TextUtils.isEmpty(getNavigationState()) && isResetEnabled) listener.onRefinementReset() else listener.onRefinementClear() }
        refinementSeeResult.setOnClickListener { seeResults() }
        refinementList.layoutManager = LinearLayoutManager(activity)
        updateToolBarMenuText()
        loadData()
    }

    private fun loadData() {
        if (productView!!.navigation != null && productView!!.navigation.size > 0) {
            setResultCount(productView!!.navigation)
            dataList = getRefinementSelectableItems(productView?.navigation!!)
            refinementNavigationAdapter = RefinementNavigationAdapter(activity, this, listener, dataList, productView?.history!!)
            refinementList.adapter = refinementNavigationAdapter!!
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnRefinementOptionSelected) {
            listener = context
        } else {
            throw ClassCastException(context.toString() + " must implement OnRefinementOptionSelected.")
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
            } else {
                if (!isHeaderAddedForCategory) {
                    dataList.add(RefinementSelectableItem(it, RefinementSelectableItem.ViewType.SECTION_HEADER))
                    isHeaderAddedForCategory = true
                }
                dataList.add(RefinementSelectableItem(it, RefinementSelectableItem.ViewType.OPTIONS))
            }
        }
        return dataList
    }

    private fun setResultCount(navigationList: ArrayList<RefinementNavigation>) {
        var totalCount = 0
        navigationList.forEach {
            it.refinements.forEach {
                totalCount += it.count
            }
        }
        setResultCount(totalCount)
    }

    private fun setResultCount(resultCount: Int) {
        seeResultCount.text = getString(R.string.see_results_count_start) + resultCount.toString() + getString(R.string.see_results_count_end)
    }

    override fun onBackPressed() {
        this.seeResults()
    }

    private fun seeResults() {
        listener.onSeeResults(getNavigationState())
    }

    private fun getNavigationState(): String {
        var navigationState = ""
        dataList.forEach {
            if (it.type == RefinementSelectableItem.ViewType.PROMOTION) {
                it.item as RefinementNavigation
                if (it.isSelected != (it.item.refinementCrumbs != null && it.item.refinementCrumbs.size > 0)) {
                    navigationState = it.item.refinements[0].navigationState
                    return navigationState
                }
            }
        }
        return navigationState
    }

    private fun updateToolBarMenuText() {
        if (TextUtils.isEmpty(getNavigationState()) && isResetEnabled) {
            clearOrRresetRefinement?.text = getString(R.string.refine_reset)
        } else {
            clearOrRresetRefinement?.text = getString(R.string.refinement_clear)
        }
    }

    override fun onPromotionSelectionChanged() {
        updateToolBarMenuText()
    }

}
