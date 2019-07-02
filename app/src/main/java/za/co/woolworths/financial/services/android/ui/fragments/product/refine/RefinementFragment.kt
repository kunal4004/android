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
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.Refinement
import za.co.woolworths.financial.services.android.models.dto.RefinementCrumb
import za.co.woolworths.financial.services.android.models.dto.RefinementNavigation
import za.co.woolworths.financial.services.android.models.dto.RefinementSelectableItem
import za.co.woolworths.financial.services.android.ui.adapters.RefinementAdapter
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.OnRefinementOptionSelected
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.BaseFragmentListner
import za.co.woolworths.financial.services.android.util.Utils

class RefinementFragment : BaseRefinementFragment(), BaseFragmentListner {
    private lateinit var listener: OnRefinementOptionSelected
    private var refinementAdapter: RefinementAdapter? = null
    private var clearRefinement: TextView? = null
    private var refinementNavigation: RefinementNavigation? = null
    private var backButton: ImageView? = null
    private var dataList = arrayListOf<RefinementSelectableItem>()
    private var pageTitle: TextView? = null
    private var refinedNavigateState = ""

    companion object {
        private val ARG_PARAM = "refinementNavigationObject"
        private val REFINED_NAVIGATION_STATE = "refinementNavigationState"
        fun getInstance(refinementNavigation: RefinementNavigation, refinedNavigationState: String) = RefinementFragment().withArgs {
            putString(ARG_PARAM, Utils.toJson(refinementNavigation))
            putString(REFINED_NAVIGATION_STATE, refinedNavigationState)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let{
            refinementNavigation = Utils.jsonStringToObject(it.getString(ARG_PARAM), RefinementNavigation::class.java) as RefinementNavigation
            refinedNavigateState = it.getString(REFINED_NAVIGATION_STATE, "")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun onResume() {
        super.onResume()
        Utils.setScreenName(activity, FirebaseManagerAnalyticsProperties.ScreenNames.PRODUCT_SEARCH_REFINEMENT)
    }

    private fun initViews() {
        activity?.let {
            backButton = it.findViewById(R.id.btnClose)
            clearRefinement = it.findViewById(R.id.resetRefinement)
            pageTitle = it.findViewById(R.id.toolbarText)
        }
        backButton?.setImageResource(R.drawable.back24)
        activity?.let {  }
        pageTitle?.text = refinementNavigation?.displayName
        clearRefinement?.text = getString(R.string.refinement_clear)
        clearRefinement?.setOnClickListener { refinementAdapter?.clearRefinement() }
        backButton?.setOnClickListener { onBackPressed() }
        refinementSeeResult.setOnClickListener { seeResults() }
        refinementList.layoutManager = LinearLayoutManager(activity)
        loadData()
        onSelectionChanged()
    }

    private fun loadData() {
        dataList = getRefinementSelectableItems(refinementNavigation!!)
        refinementAdapter = activity?.let { RefinementAdapter(it, this, listener, dataList, refinementNavigation!!) }
        refinementList.adapter = refinementAdapter
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnRefinementOptionSelected) {
            listener = context
        } else {
            throw ClassCastException(context.toString() + " must implement OnRefinementOptionSelected.")
        }
    }

    private fun getRefinementSelectableItems(refinementNavigation: RefinementNavigation): ArrayList<RefinementSelectableItem> {
        var dataList = arrayListOf<RefinementSelectableItem>()
        if (refinementNavigation.refinementCrumbs != null && refinementNavigation.refinementCrumbs.size > 0) {
            refinementNavigation.refinementCrumbs.forEach {
                var refinementSelectableItem = RefinementSelectableItem(it, if (it.multiSelect) RefinementSelectableItem.ViewType.MULTI_SELECTOR else RefinementSelectableItem.ViewType.SINGLE_SELECTOR)
                refinementSelectableItem.isSelected = true
                dataList.add(refinementSelectableItem)
            }
        }

        if (refinementNavigation.refinements != null && refinementNavigation.refinements.size > 0) {
            refinementNavigation.refinements.forEach {
                if (it.subRefinements != null && it.subRefinements.size > 0) {
                    dataList.add(RefinementSelectableItem(it, RefinementSelectableItem.ViewType.OPTIONS))
                } else {
                    dataList.add(RefinementSelectableItem(it, if (it.multiSelect) RefinementSelectableItem.ViewType.MULTI_SELECTOR else RefinementSelectableItem.ViewType.SINGLE_SELECTOR))
                }

            }
        }
        return dataList
    }

    override fun onBackPressed() {
        var navigationState = getNavigationState()
        if (TextUtils.isEmpty(navigationState)) listener.onBackPressedWithOutRefinement() else listener.onBackPressedWithRefinement(navigationState)
    }

    private fun seeResults() {
        listener.onSeeResults(getNavigationState())
    }

    private fun getNavigationState(): String {
        dataList.forEach {
            var item = it.item
            if (it.type == RefinementSelectableItem.ViewType.MULTI_SELECTOR) {
                if (item is Refinement && it.isSelected) {
                    if (TextUtils.isEmpty(refinedNavigateState))
                        refinedNavigateState = refinedNavigateState.plus(item.navigationState)
                    else
                        refinedNavigateState = refinedNavigateState.plus("Z").plus(item.navigationState.substringAfterLast("Z"))
                } else if (item is RefinementCrumb && !it.isSelected) {
                    refinedNavigateState = refinedNavigateState.replace(getNavigationStateForRefinementCrumb(item.navigationState), "")
                }
            } else if (it.type == RefinementSelectableItem.ViewType.SINGLE_SELECTOR) {
                if (item is Refinement && it.isSelected) {
                    refinedNavigateState = item.navigationState
                } else if (item is RefinementCrumb && !it.isSelected) {
                    refinedNavigateState = refinedNavigateState.replace(getNavigationStateForRefinementCrumb(item.navigationState), "")
                }
            }
        }
        return refinedNavigateState
    }

    override fun onSelectionChanged() {
        clearRefinement?.isEnabled = isAnyRefinementSelected()
        this.updateSeeResultButtonText()
    }

    private fun isAnyRefinementSelected(): Boolean {
        dataList.forEach {
            if (it.isSelected)
                return true
        }
        return false
    }

    private fun getNavigationStateForRefinementCrumb(navigationState: String): String {
        val list = navigationState.substringAfter("Z").split("Z")
        var navigation = refinedNavigateState.substringAfter("Z")
        list.forEachIndexed { index, it ->
            if (index == 0) navigation = navigation.replace(it, "") else navigation = navigation.replace("Z".plus(it), "")
        }
        return navigation
    }

    private fun buildSeeResultButtonText(): String {
        var selectedItems = arrayListOf<String>()
        selectedItems.clear()
        dataList.forEach {
            var item = it.item
            if (item is Refinement && it.isSelected) {
                selectedItems.add(item.label)
            } else if (item is RefinementCrumb && it.isSelected) {
                selectedItems.add(item.label)
            }

        }

        return getString(R.string.refinement_see_result_button_text) + if (selectedItems.size > 0) selectedItems.joinToString(",") else refinementNavigation?.displayName
    }

    private fun updateSeeResultButtonText() {
        seeResultCount.text = buildSeeResultButtonText()
    }

}
