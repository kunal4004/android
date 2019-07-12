package za.co.woolworths.financial.services.android.ui.fragments.product.refine

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.BaseFragmentListner

open class BaseRefinementFragment : Fragment(), BaseFragmentListner {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_refinement, container, false)
    }

    override fun onBackPressed() {
    }

    override fun onSelectionChanged() {
    }
}