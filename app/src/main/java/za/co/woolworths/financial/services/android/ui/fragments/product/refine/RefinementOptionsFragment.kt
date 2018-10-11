package za.co.woolworths.financial.services.android.ui.fragments.product.refine


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_refinement.*
import za.co.woolworths.financial.services.android.models.dto.RefinementSelectableItem
import za.co.woolworths.financial.services.android.ui.adapters.RefinementAdapter
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.OnRefinementOptionSelected


class RefinementOptionsFragment : Fragment() {

    private lateinit var listener: OnRefinementOptionSelected
    var dataList = arrayListOf<RefinementSelectableItem>()


    companion object {
        fun getInstance(): RefinementOptionsFragment {
            return RefinementOptionsFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_refinement, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity.findViewById<ImageView>(R.id.btnClose).setImageResource(R.drawable.close_24)
        refinementList.layoutManager = LinearLayoutManager(activity)
        dataList.addAll(listOf(RefinementSelectableItem("Test",RefinementSelectableItem.ViewType.SECTION_HEADER),RefinementSelectableItem("Test",RefinementSelectableItem.ViewType.PROMOTION),RefinementSelectableItem("Test",RefinementSelectableItem.ViewType.SECTION_HEADER),RefinementSelectableItem("Test",RefinementSelectableItem.ViewType.OPTIONS)))
        refinementList.adapter = RefinementAdapter(activity, listener,dataList)
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
