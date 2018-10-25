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
import za.co.woolworths.financial.services.android.models.dto.RefinementSelectableItem
import za.co.woolworths.financial.services.android.models.dto.SubRefinement
import za.co.woolworths.financial.services.android.ui.adapters.SubRefinementAdapter
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.OnRefinementOptionSelected
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.BaseFragmentListner
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.ui.extension.withArgs

class SubRefinementFragment : BaseRefinementFragment(), BaseFragmentListner {
    private lateinit var listener: OnRefinementOptionSelected
    private var subRefinementAdapter: SubRefinementAdapter? = null
    private var clearRefinement: TextView? = null
    private var refinement: Refinement? = null
    private var backButton: ImageView? = null
    private var dataList = arrayListOf<RefinementSelectableItem>()
    private var pageTitle: TextView? = null

    companion object {
        private val ARG_PARAM = "refinementObject"
        fun getInstance(refinement: Refinement) = SubRefinementFragment().withArgs {
            putString(ARG_PARAM, Utils.toJson(refinement))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            refinement = Utils.jsonStringToObject(arguments.getString(ARG_PARAM), Refinement::class.java) as Refinement
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
        pageTitle?.text = refinement?.label
        clearRefinement?.text = getString(R.string.refinement_clear)
        clearRefinement?.setOnClickListener { subRefinementAdapter?.clearRefinement() }
        backButton?.setOnClickListener { onBackPressed() }
        refinementSeeResult.setOnClickListener { seeResults() }
        refinementList.layoutManager = LinearLayoutManager(activity)
        onSelectionChanged()
        loadData()
    }

    private fun loadData() {
        dataList = getSubRefinementSelectableItems(refinement!!.subRefinements)
        subRefinementAdapter = SubRefinementAdapter(activity, this, listener, dataList)
        refinementList.adapter = subRefinementAdapter
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnRefinementOptionSelected) {
            listener = context
        } else {
            throw ClassCastException(context.toString() + " must implement OnRefinementOptionSelected.")
        }
    }

    private fun getSubRefinementSelectableItems(subRefinements: ArrayList<SubRefinement>): ArrayList<RefinementSelectableItem> {
        var dataList = arrayListOf<RefinementSelectableItem>()

        subRefinements.forEach {
            dataList.add(RefinementSelectableItem(it, if (it.multiSelect) RefinementSelectableItem.ViewType.MULTI_SELECTOR else RefinementSelectableItem.ViewType.SINGLE_SELECTOR))
        }
        return dataList
    }

    override fun onBackPressed() {
        var navigationState = getNavigationState()
        if (TextUtils.isEmpty(navigationState)) listener.onBackPressedWithOutRefinement() else listener.onBackPressedWithRefinement(navigationState)
    }

    private fun seeResults() {
        listener.onSeeResults(if (TextUtils.isEmpty(getNavigationState())) refinement?.navigationState!! else getNavigationState())
    }

    private fun getNavigationState(): String {
        var navigationState = ""
        dataList.forEach {
            if (it.isSelected) {
                var item = it.item as SubRefinement
                if (TextUtils.isEmpty(navigationState))
                    navigationState = navigationState.plus(item.navigationState)
                else
                    navigationState = navigationState.plus("Z").plus(item.navigationState.substringAfterLast("Z"))

            }
        }
        return navigationState
    }

    override fun onSelectionChanged() {
        clearRefinement?.isEnabled = !TextUtils.isEmpty(getNavigationState())
    }

    override fun onPromotionToggled(count: Int, isEnabled: Boolean) {
    }
}
