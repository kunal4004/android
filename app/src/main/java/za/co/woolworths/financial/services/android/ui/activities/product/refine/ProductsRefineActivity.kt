package za.co.woolworths.financial.services.android.ui.activities.product.refine

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.activity_products_refine.*
import za.co.woolworths.financial.services.android.ui.extensions.addFragmentSafelfy
import za.co.woolworths.financial.services.android.ui.extensions.replaceFragmentSafely
import za.co.woolworths.financial.services.android.ui.fragments.product.refine.RefinementFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.refine.RefinementOptionsFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.OnRefinementOptionSelected
import za.co.woolworths.financial.services.android.util.Utils

class ProductsRefineActivity : AppCompatActivity(), OnRefinementOptionSelected {


    companion object {
        const val OPTIONS_FRAGMENT_TAG: String = "OptionsFragment"
        const val REFINEMENT_FRAGMENT_TAG: String = "RefinementFragment"
        const val SUB_REFINEMENT_FRAGMENT_TAG: String = "SubRefinementFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products_refine)
        Utils.updateStatusBarBackground(this)
        initViews()
    }

    fun initViews() {
        btnClose.setOnClickListener { onBackPressed() }
        addRefinementOptionsFragment()

    }

    private fun addRefinementOptionsFragment() {
        addFragmentSafelfy(RefinementOptionsFragment.getInstance(), OPTIONS_FRAGMENT_TAG, false, R.id.refinement_fragment_container)
    }

    override fun onRefinementOptionSelected(position: Int) {
        pushFragment(RefinementFragment.getInstance(), REFINEMENT_FRAGMENT_TAG)
    }

    fun pushFragment(fragment: Fragment, tag: String) {
        replaceFragmentSafely(fragment, tag, false, R.id.refinement_fragment_container, R.anim.slide_in_from_right, R.anim.slide_out_to_left, R.anim.slide_in_from_left, R.anim.slide_out_to_right)
    }

    fun popFragment(fragment: Fragment, tag: String) {
        replaceFragmentSafely(fragment, tag, false, R.id.refinement_fragment_container, R.anim.slide_in_from_left, R.anim.slide_out_to_right)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
            overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
        }
    }

}
