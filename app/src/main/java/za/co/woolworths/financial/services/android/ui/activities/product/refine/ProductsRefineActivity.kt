package za.co.woolworths.financial.services.android.ui.activities.product.refine

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.extensions.addFragmentSafelfy
import za.co.woolworths.financial.services.android.ui.fragments.product.refine.RefinementOptionsFragment
import za.co.woolworths.financial.services.android.util.Utils

class ProductsRefineActivity : AppCompatActivity() {

    companion object {
        const val OPTIONS_FRAGMENT_TAG: String = "OptionsFragment"
        const val REFINEMENT_FRAGMENT_TAG: String = "RefinementFragment"
        const val SUB_REFINEMENT_FRAGMENT_TAG: String = "SubRefinementFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products_refine)
        Utils.updateStatusBarBackground(this)
        addRefinementOptionsFragment()
    }

    private fun addRefinementOptionsFragment() {
        addFragmentSafelfy(RefinementOptionsFragment(), OPTIONS_FRAGMENT_TAG, false, R.id.refinement_fragment_container)
    }
}
