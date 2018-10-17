package za.co.woolworths.financial.services.android.ui.activities.product.refine

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.activity_products_refine.*
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.ui.extensions.addFragmentSafelfy
import za.co.woolworths.financial.services.android.ui.extensions.replaceFragmentSafely
import za.co.woolworths.financial.services.android.ui.fragments.product.refine.RefinementFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.refine.RefinementOptionsFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.OnRefinementOptionSelected
import za.co.woolworths.financial.services.android.util.Utils

class ProductsRefineActivity : AppCompatActivity(), OnRefinementOptionSelected {

    private var productsResponse: ProductView? = null

    companion object {
        const val OPTIONS_FRAGMENT_TAG: String = "OptionsFragment"
        const val REFINEMENT_FRAGMENT_TAG: String = "RefinementFragment"
        const val SUB_REFINEMENT_FRAGMENT_TAG: String = "SubRefinementFragment"
        fun getAllLabelsFromRefinementCrumbs(refinementCrumbs: ArrayList<RefinementCrumb>): String {
            var result = ""
            refinementCrumbs.forEach {
                result.plus(it.label)
                result.plus(",")
            }
            return result
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products_refine)
        Utils.updateStatusBarBackground(this)
        productsResponse = Utils.jsonStringToObject(intent.getStringExtra("REFINEMENT_DATA"), ProductView::class.java) as ProductView?
        initViews()
    }

    fun initViews() {
        btnClose.setOnClickListener { onBackPressed() }
        addRefinementOptionsFragment()

    }

    private fun addRefinementOptionsFragment() {
        addFragmentSafelfy(RefinementOptionsFragment.getInstance(productsResponse!!), OPTIONS_FRAGMENT_TAG, false, R.id.refinement_fragment_container)
    }

    override fun onRefinementOptionSelected(refinementNavigation: RefinementNavigation) {
        pushFragment(RefinementFragment.getInstance(refinementNavigation), REFINEMENT_FRAGMENT_TAG)
    }

    override fun onRefinementSelected(refinement: Refinement) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSubRefinementSelected(subRefinement: SubRefinement) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
