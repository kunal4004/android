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
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
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
        arguments?.let{
            refinement = Utils.jsonStringToObject(it.getString(ARG_PARAM), Refinement::class.java) as Refinement
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun onResume() {
        super.onResume()
        Utils.setScreenName(activity, FirebaseManagerAnalyticsProperties.ScreenNames.PRODUCT_SEARCH_REFINEMENT_CATEGORY)
    }

    private fun initViews() {

        activity?.let {
            backButton = it.findViewById(R.id.btnClose)
            clearRefinement = it.findViewById(R.id.resetRefinement)
            pageTitle = it.findViewById(R.id.toolbarText)
        }
        backButton?.setImageResource(R.drawable.back24)
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
        subRefinementAdapter = activity?.let { SubRefinementAdapter(it, this, listener, dataList) }
        refinementList.adapter = subRefinementAdapter
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnRefinementOptionSelected) {
            listener = context
        } else {
            throw ClassCastException("$context must implement OnRefinementOptionSelected.")
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
        updateSeeResultButtonText()
    }

    private fun buildSeeResultButtonText(): String {
        var selectedItems = arrayListOf<String>()
        selectedItems.clear()
        dataList.forEach {
            if (it.isSelected) {
                var item = it.item as SubRefinement
                selectedItems.add(item.label)
            }
        }

        return getString(R.string.refinement_see_result_button_text) + if (selectedItems.size > 0) selectedItems.joinToString(",") else refinement?.label
    }

    private fun updateSeeResultButtonText() {
        seeResultCount.text = buildSeeResultButtonText()
    }

}
