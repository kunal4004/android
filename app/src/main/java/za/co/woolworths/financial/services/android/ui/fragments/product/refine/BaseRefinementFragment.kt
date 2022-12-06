package za.co.woolworths.financial.services.android.ui.fragments.product.refine

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentRefinementBinding
import za.co.woolworths.financial.services.android.ui.fragments.RefinementDrawerFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.BaseFragmentListner
import za.co.woolworths.financial.services.android.ui.views.WTextView

open class BaseRefinementFragment : Fragment(), BaseFragmentListner {

    protected lateinit var binding: FragmentRefinementBinding
    var backButton: ImageView? = null
    var closeButton: ImageView? = null
    var pageTitle: WTextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentRefinementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onBackPressed() {
    }

    override fun onSelectionChanged() {
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (parentFragment as RefinementDrawerFragment).view?.apply {
            pageTitle = findViewById(R.id.toolbarText)
            backButton = findViewById(R.id.backButton)
            closeButton = findViewById(R.id.closeButton)
        }
    }
}