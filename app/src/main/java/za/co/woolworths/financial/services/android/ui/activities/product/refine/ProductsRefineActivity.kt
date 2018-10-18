package za.co.woolworths.financial.services.android.ui.activities.product.refine

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.activity_products_refine.*
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.ui.extensions.addFragmentSafelfy
import za.co.woolworths.financial.services.android.ui.extensions.refineProducts
import za.co.woolworths.financial.services.android.ui.extensions.replaceFragmentSafely
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.GridFragment.PRODUCTS_REQUEST_PARAMS
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.GridFragment.REFINEMENT_DATA
import za.co.woolworths.financial.services.android.ui.fragments.product.refine.BaseRefinementFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.refine.RefinementFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.refine.RefinementNavigationFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.refine.SubRefinementFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.OnRefineProductsResult
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.OnRefinementOptionSelected
import za.co.woolworths.financial.services.android.util.Utils

class ProductsRefineActivity : AppCompatActivity(), OnRefinementOptionSelected, OnRefineProductsResult {

    private var productsResponse: ProductView? = null
    private var productsRequestParams: ProductsRequestParams? = null

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
        productsResponse = Utils.jsonStringToObject(intent.getStringExtra(REFINEMENT_DATA), ProductView::class.java) as ProductView?
        productsRequestParams = Utils.jsonStringToObject(intent.getStringExtra(PRODUCTS_REQUEST_PARAMS), ProductsRequestParams::class.java) as ProductsRequestParams?
        initViews()
    }

    private fun initViews() {
        addRefinementOptionsFragment()

    }

    private fun addRefinementOptionsFragment() {
        addFragmentSafelfy(RefinementNavigationFragment.getInstance(productsResponse!!), OPTIONS_FRAGMENT_TAG, false, R.id.refinement_fragment_container)
    }

    override fun onRefinementOptionSelected(refinementNavigation: RefinementNavigation) {
        pushFragment(RefinementFragment.getInstance(refinementNavigation), REFINEMENT_FRAGMENT_TAG)
    }

    override fun onRefinementSelected(refinement: Refinement) {
        pushFragment(SubRefinementFragment.getInstance(refinement), SUB_REFINEMENT_FRAGMENT_TAG)
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
        var currentFragment: Fragment = supportFragmentManager.findFragmentById(R.id.refinement_fragment_container)!!
        if (currentFragment != null && currentFragment is BaseRefinementFragment)
            currentFragment.onBackPressed()

    }

    override fun onBackPressedWithRefinement(navigationState: String) {

    }

    override fun onBackPressedWithOutRefinement() {
        if (loadingBar.visibility == View.VISIBLE)
            return
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
            overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
        }
    }

    private fun executeRefineProducts(refinement: String) {
        productsRequestParams?.refinement = refinement
        refineProducts(this, productsRequestParams!!).execute()
    }

    override fun onProductRefineSuccess(productView: ProductView) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProductRefineFailure(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSeeResultClicked(navigationState: String) {
        intent = Intent()
        intent.putExtra("", "")
        setResult(Activity.RESULT_OK, intent)
    }

    fun hideProgressBar() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        if (Build.VERSION.SDK_INT >= 21) {
            window.navigationBarColor = resources.getColor(R.color.transparent)
            window.statusBarColor = resources.getColor(R.color.transparent)
        }
        loadingBar.visibility = View.GONE
    }

    fun showProgressBar() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        if (Build.VERSION.SDK_INT >= 21) {
            window.navigationBarColor = resources.getColor(R.color.semi_transparent_black)
            window.statusBarColor = resources.getColor(R.color.semi_transparent_black)
        }
        loadingBar.visibility = View.VISIBLE
    }

}
