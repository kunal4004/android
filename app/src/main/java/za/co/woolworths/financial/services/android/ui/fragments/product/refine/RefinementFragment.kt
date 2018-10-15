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
import za.co.woolworths.financial.services.android.models.dto.RefinementSelectableItem
import za.co.woolworths.financial.services.android.ui.adapters.RefinementAdapter
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.OnRefinementOptionSelected

class RefinementFragment : Fragment() {
    private lateinit var listener: OnRefinementOptionSelected
    private var dataList = arrayListOf<RefinementSelectableItem>()
    private var refinementAdapter: RefinementAdapter? = null
    private var clearRefinement: TextView? = null

    companion object {
        fun getInstance(): RefinementFragment {
            return RefinementFragment()
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
        dataList.clear()
        refinementAdapter?.notifyDataSetChanged()
        dataList.addAll(listOf(RefinementSelectableItem("Test", RefinementSelectableItem.ViewType.OPTIONS), RefinementSelectableItem("Test", RefinementSelectableItem.ViewType.SINGLE_SELECTOR), RefinementSelectableItem("Test", RefinementSelectableItem.ViewType.SINGLE_SELECTOR), RefinementSelectableItem("Test", RefinementSelectableItem.ViewType.MULTI_SELECTOR), RefinementSelectableItem("Test", RefinementSelectableItem.ViewType.SINGLE_SELECTOR)))
        refinementAdapter = RefinementAdapter(activity, listener, dataList)
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

}
