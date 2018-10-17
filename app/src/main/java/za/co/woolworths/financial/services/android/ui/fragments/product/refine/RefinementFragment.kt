package za.co.woolworths.financial.services.android.ui.fragments.product.refine


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_refinement.*
import za.co.woolworths.financial.services.android.models.dto.Refinement
import za.co.woolworths.financial.services.android.models.dto.RefinementNavigation
import za.co.woolworths.financial.services.android.models.dto.RefinementSelectableItem
import za.co.woolworths.financial.services.android.ui.adapters.holder.RefinementAdapter
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.OnRefinementOptionSelected
import za.co.woolworths.financial.services.android.util.Utils

class RefinementFragment : Fragment() {
    private lateinit var listener: OnRefinementOptionSelected
    private var refinementAdapter: RefinementAdapter? = null
    private var clearRefinement: TextView? = null
    private var refinementNavigation: RefinementNavigation? = null

    companion object {
        private val ARG_PARAM = "refinementNavigationObject"
        fun getInstance(refinementNavigation: RefinementNavigation): RefinementFragment {
            val fragment = RefinementFragment()
            val args = Bundle()
            args.putString(ARG_PARAM, Utils.toJson(refinementNavigation))
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            refinementNavigation = Utils.jsonStringToObject(arguments.getString(ARG_PARAM), RefinementNavigation::class.java) as RefinementNavigation
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_refinement, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        activity.findViewById<ImageView>(R.id.btnClose).setImageResource(R.drawable.back24)
        clearRefinement = activity.findViewById(R.id.resetRefinement)
        clearRefinement?.text = getString(R.string.refinement_clear)
        refinementList.layoutManager = LinearLayoutManager(activity)
        loadData()
    }

    private fun loadData() {
        refinementAdapter = RefinementAdapter(activity, listener, getRefinementSelectableItems(refinementNavigation!!), refinementNavigation!!)
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
                dataList.add(RefinementSelectableItem(it, if (it.multiSelect) RefinementSelectableItem.ViewType.MULTI_SELECTOR else RefinementSelectableItem.ViewType.SINGLE_SELECTOR))
            }
        }
        return dataList
    }

}
