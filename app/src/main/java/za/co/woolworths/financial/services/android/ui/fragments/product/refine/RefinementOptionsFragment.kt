package za.co.woolworths.financial.services.android.ui.fragments.product.refine


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_refinement.*
import za.co.woolworths.financial.services.android.ui.activities.product.refine.ProductsRefineActivity
import za.co.woolworths.financial.services.android.ui.adapters.RefinementAdapter
import za.co.woolworths.financial.services.android.ui.extensions.replaceFragmentSafely
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.RefinementSelection


class RefinementOptionsFragment : Fragment(), RefinementSelection {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_refinement, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refinementList.layoutManager = LinearLayoutManager(activity)
        refinementList.adapter = RefinementAdapter(activity, this)
    }

    override fun onRefinementOptionSelected(position: Int) {
        replaceRefinementFragment()
    }

    private fun replaceRefinementFragment() {
       // replaceFragmentSafely(RefinementFragment(), ProductsRefineActivity.REFINEMENT_FRAGMENT_TAG, false, (view!!.parent as ViewGroup).id)
    }

}
