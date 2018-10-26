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

    companion object {
        private val ARG_PARAM = "refinementNavigationObject"
        fun getInstance(refinementNavigation: RefinementNavigation) = RefinementFragment().withArgs {
            putString(ARG_PARAM, Utils.toJson(refinementNavigation))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            refinementNavigation = Utils.jsonStringToObject(arguments.getString(ARG_PARAM), RefinementNavigation::class.java) as RefinementNavigation
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        backButton = activity.findViewById(R.id.btnClose)
        backButton?.setImageResource(R.drawable.back24)
        clearRefinement = activity.findViewById(R.id.resetRefinement)
        pageTitle = activity.findViewById(R.id.toolbarText)
        pageTitle?.text = refinementNavigation?.displayName
        clearRefinement?.text = getString(R.string.refinement_clear)
        clearRefinement?.setOnClickListener { refinementAdapter?.clearRefinement() }
        backButton?.setOnClickListener { onBackPressed() }
        refinementSeeResult.setOnClickListener { seeResults() }
        refinementList.layoutManager = LinearLayoutManager(activity)
        onSelectionChanged()
        loadData()
    }

    private fun loadData() {
        dataList = getRefinementSelectableItems(refinementNavigation!!)
        refinementAdapter = RefinementAdapter(activity, this, listener, dataList, refinementNavigation!!)
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
        if (refinementNavigation.refinements != null && refinementNavigation.refinements.size > 0) {
            refinementNavigation.refinements.forEach {

                if (it.subRefinements != null && it.subRefinements.size > 0) {
                    dataList.add(RefinementSelectableItem(it, RefinementSelectableItem.ViewType.OPTIONS))
                } else {
                    dataList.add(RefinementSelectableItem(it, if (it.multiSelect) RefinementSelectableItem.ViewType.MULTI_SELECTOR else RefinementSelectableItem.ViewType.SINGLE_SELECTOR))
                }

            }
        } else if (refinementNavigation.refinementCrumbs != null && refinementNavigation.refinementCrumbs.size > 0) {
            refinementNavigation.refinementCrumbs.forEach {
                var refinementSelectableItem = RefinementSelectableItem(it, if (it.multiSelect) RefinementSelectableItem.ViewType.MULTI_SELECTOR else RefinementSelectableItem.ViewType.SINGLE_SELECTOR)
                refinementSelectableItem.isSelected = true
                dataList.add(refinementSelectableItem)
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
        var navigationState = ""
        dataList.forEach {
            if (it.type == RefinementSelectableItem.ViewType.SINGLE_SELECTOR || it.type == RefinementSelectableItem.ViewType.MULTI_SELECTOR) {
                var item = it.item
                if (item is Refinement && it.isSelected) {
                    if (TextUtils.isEmpty(navigationState))
                        navigationState = navigationState.plus(item.navigationState)
                    else
                        navigationState = navigationState.plus("Z").plus(item.navigationState.substringAfterLast("Z"))
                } else if (item is RefinementCrumb && !it.isSelected) {
                    if (TextUtils.isEmpty(navigationState))
                        navigationState = navigationState.plus(item.navigationState)
                    else
                        navigationState = navigationState.plus("Z").plus(item.navigationState.substringAfterLast("Z"))
                }
            }
        }
        return navigationState
    }

    override fun onSelectionChanged() {
        clearRefinement?.isEnabled = !TextUtils.isEmpty(getNavigationState())
    }

}
