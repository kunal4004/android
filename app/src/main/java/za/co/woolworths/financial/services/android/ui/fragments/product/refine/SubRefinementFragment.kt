package za.co.woolworths.financial.services.android.ui.fragments.product.refine


import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
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
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.RefinementOnBackPressed
import za.co.woolworths.financial.services.android.util.Utils

class SubRefinementFragment : BaseRefinementFragment(),RefinementOnBackPressed {
    private lateinit var listener: OnRefinementOptionSelected
    private var subRefinementAdapter: SubRefinementAdapter? = null
    private var clearRefinement: TextView? = null
    private var refinement: Refinement? = null
    private var backButton: ImageView? = null

    companion object {
        private val ARG_PARAM = "refinementObject"
        fun getInstance(refinement: Refinement): SubRefinementFragment {
            val fragment = SubRefinementFragment()
            val args = Bundle()
            args.putString(ARG_PARAM, Utils.toJson(refinement))
            fragment.arguments = args
            return fragment
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
        clearRefinement?.text = getString(R.string.refinement_clear)
        backButton?.setOnClickListener { onBackPressed() }
        refinementSeeResult.setOnClickListener { seeResults() }
        refinementList.layoutManager = LinearLayoutManager(activity)
        loadData()
    }

    private fun loadData() {
        setResultCount(refinement!!.subRefinements)
        subRefinementAdapter = SubRefinementAdapter(activity, listener, getSubRefinementSelectableItems(refinement!!.subRefinements))
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

    private fun setResultCount(subRefinements: ArrayList<SubRefinement>) {
        var totalCount = 0
        subRefinements.forEach {
            totalCount += it.count
        }
        setResultCount(totalCount)
    }

    private fun setResultCount(resultCount: Int) {
        seeResultCount.text = getString(R.string.see_results_count_start) + resultCount.toString() + getString(R.string.see_results_count_end)
    }

    /*private fun onBackPressed() {
        listener.onBackPressedWithOutRefinement()
    }*/
    override fun onBackPressed() {
        listener.onBackPressedWithOutRefinement()
    }

    private fun seeResults() {

    }

}
