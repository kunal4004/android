package za.co.woolworths.financial.services.android.ui.activities.product.refine

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.activity_products_refine.*
import za.co.woolworths.financial.services.android.models.dto.*
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
        const val TAG_NAVIGATION_FRAGMENT: String = "OptionsFragment"
        const val TAG_REFINEMENT_FRAGMENT: String = "RefinementFragment"
        const val TAG_SUB_REFINEMENT_FRAGMENT: String = "SubRefinementFragment"
        const val NAVIGATION_STATE = "NAVIGATION_STATE"
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
        replaceRefinementOptionsFragment(productsResponse!!)

    }

    private fun replaceRefinementOptionsFragment(productsResponse: ProductView) {
        replaceFragmentSafely(RefinementNavigationFragment.getInstance(productsResponse), TAG_NAVIGATION_FRAGMENT, false, false, R.id.refinement_fragment_container)
    }

    override fun onRefinementOptionSelected(refinementNavigation: RefinementNavigation) {
        pushFragment(RefinementFragment.getInstance(refinementNavigation), TAG_REFINEMENT_FRAGMENT)
    }

    override fun onRefinementSelected(refinement: Refinement) {
        pushFragment(SubRefinementFragment.getInstance(refinement), TAG_SUB_REFINEMENT_FRAGMENT)
    }

    override fun onSubRefinementSelected(subRefinement: SubRefinement) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    fun pushFragment(fragment: Fragment, tag: String) {
        replaceFragmentSafely(fragment, tag, false, true, R.id.refinement_fragment_container, R.anim.slide_in_from_right, R.anim.slide_out_to_left, R.anim.slide_in_from_left, R.anim.slide_out_to_right)
    }

    fun popFragment(fragment: Fragment, tag: String) {
        replaceFragmentSafely(fragment, tag, false, true, R.id.refinement_fragment_container, R.anim.slide_in_from_left, R.anim.slide_out_to_right)
    }

    override fun onBackPressed() {
        var currentFragment: Fragment = supportFragmentManager.findFragmentById(R.id.refinement_fragment_container)!!
        if (currentFragment != null && currentFragment is BaseRefinementFragment)
            currentFragment.onBackPressed()

    }

    override fun onBackPressedWithRefinement(navigationState: String) {
        showProgressBar()
        executeRefineProducts(navigationState)
    }

    override fun onBackPressedWithOutRefinement() {
        if (progressBar.visibility == View.VISIBLE)
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
        reloadFragment(productView)
        hideProgressBar()
    }

    override fun onProductRefineFailure(message: String) {
        hideProgressBar()
    }

    override fun onSeeResults(navigationState: String) {
        if (!TextUtils.isEmpty(navigationState)) {
            intent = Intent()
            intent.putExtra(NAVIGATION_STATE, navigationState)
            setResult(Activity.RESULT_OK, intent)
        }
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }

    private fun hideProgressBar() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        runOnUiThread {
            progressBar.visibility = View.INVISIBLE
        }
    }

    private fun showProgressBar() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        runOnUiThread {
            progressBar.visibility = View.VISIBLE
        }
    }

    private fun refreshActivity(productView: ProductView) {
        val intent = intent
        intent.putExtra(REFINEMENT_DATA, Utils.toJson(productView))
        intent.putExtra(PRODUCTS_REQUEST_PARAMS, Utils.toJson(productsRequestParams))
        overridePendingTransition(0, 0)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        finish()
        overridePendingTransition(0, 0)
        startActivity(intent)
    }

    override fun onResetClicked() {
        reloadFragment(productsResponse!!)
    }

    private fun reloadFragment(productView: ProductView) {
        //Here we are clearing back stack fragment entries
        val backStackEntry = supportFragmentManager.backStackEntryCount
        if (backStackEntry > 0) {
            for (i in 0 until backStackEntry) {
                supportFragmentManager.popBackStackImmediate()
            }
        }

        //Here we are removing all the fragment that are shown here
        if (supportFragmentManager.fragments != null && supportFragmentManager.fragments.size > 0) {
            for (i in 0 until supportFragmentManager.fragments.size) {
                val mFragment = supportFragmentManager.fragments[i]
                if (mFragment != null) {
                    supportFragmentManager.beginTransaction().remove(mFragment).commit()
                }
            }
        }
        replaceRefinementOptionsFragment(productView)
    }

}
