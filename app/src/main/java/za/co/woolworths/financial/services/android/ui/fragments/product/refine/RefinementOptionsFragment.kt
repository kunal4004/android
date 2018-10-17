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
import za.co.woolworths.financial.services.android.models.dto.ProductView
import za.co.woolworths.financial.services.android.models.dto.RefinementNavigation
import za.co.woolworths.financial.services.android.models.dto.RefinementSelectableItem
import za.co.woolworths.financial.services.android.ui.adapters.RefinementNavigationAdapter
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.OnRefinementOptionSelected
import za.co.woolworths.financial.services.android.util.Utils


class RefinementOptionsFragment : Fragment() {

    private lateinit var listener: OnRefinementOptionSelected
    private var refinementNavigationAdapter: RefinementNavigationAdapter? = null
    private var resetRefinement: TextView? = null
    private var productView: ProductView? = null

    companion object {
        private val ARG_PARAM = "productViewObject"
        val ON_PROMOTION: String = "On Promotion"
        val CATEGORY: String = "Category"

        fun getInstance(productView: ProductView): RefinementOptionsFragment {
            val fragment = RefinementOptionsFragment()
            val args = Bundle()
            args.putString(ARG_PARAM, Utils.toJson(productView))
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            productView = Utils.jsonStringToObject(arguments.getString(ARG_PARAM), ProductView::class.java) as ProductView
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
        activity.findViewById<ImageView>(R.id.btnClose).setImageResource(R.drawable.close_24)
        resetRefinement = activity.findViewById(R.id.resetRefinement)
        resetRefinement?.text = getString(R.string.refine_reset)
        refinementList.layoutManager = LinearLayoutManager(activity)
        loadData()
    }

    private fun loadData() {
        refinementNavigationAdapter = RefinementNavigationAdapter(activity, listener, getRefinementSelectableItems(productView?.navigation!!), productView?.history!!)
        refinementList.adapter = refinementNavigationAdapter!!
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
                dataList.add(1, RefinementSelectableItem(it, RefinementSelectableItem.ViewType.PROMOTION))
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

}
