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
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.extension.refineProducts
import za.co.woolworths.financial.services.android.ui.extension.replaceFragmentSafely
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
    private var updatedProductsRequestParams: ProductsRequestParams? = null
    private val emptyNavigationState = ""
    private val ERROR_REQUEST_CODE = 755

    companion object {
        const val TAG_NAVIGATION_FRAGMENT: String = "OptionsFragment"
        const val TAG_REFINEMENT_FRAGMENT: String = "RefinementFragment"
        const val TAG_SUB_REFINEMENT_FRAGMENT: String = "SubRefinementFragment"
        const val NAVIGATION_STATE = "NAVIGATION_STATE"
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
        replaceFragmentSafely(RefinementNavigationFragment.getInstance(productsResponse, getBaseNavigationState(), getRefinedNavigationState()), TAG_NAVIGATION_FRAGMENT, false, false, R.id.refinement_fragment_container)
    }

    override fun onRefinementOptionSelected(refinementNavigation: RefinementNavigation) {
        pushFragment(RefinementFragment.getInstance(refinementNavigation, getRefinementState()), TAG_REFINEMENT_FRAGMENT)
    }

    override fun onRefinementSelected(refinement: Refinement) {
        pushFragment(SubRefinementFragment.getInstance(refinement), TAG_SUB_REFINEMENT_FRAGMENT)
    }

    fun pushFragment(fragment: Fragment, tag: String) {
        replaceFragmentSafely(fragment, tag, false, true, R.id.refinement_fragment_container, R.anim.slide_in_from_right, R.anim.slide_out_to_left, R.anim.slide_in_from_left, R.anim.slide_out_to_right)
    }

    override fun onBackPressed() {
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.REFINE_EVENT_BACK_BUTTON)
        var currentFragment: Fragment = supportFragmentManager.findFragmentById(R.id.refinement_fragment_container)!!
        if (currentFragment != null && currentFragment is BaseRefinementFragment)
            currentFragment.onBackPressed()

    }

    override fun onBackPressedWithRefinement(navigationState: String) {
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
        showProgressBar()
        updatedProductsRequestParams = ProductsRequestParams(productsRequestParams?.searchTerm!!, productsRequestParams?.searchType!!, productsRequestParams?.responseType!!, productsRequestParams?.pageOffset!!)
        setRefinedNavigationState(refinement)
        refineProducts(this, updatedProductsRequestParams!!)
    }

    override fun onProductRefineSuccess(productView: ProductView, navigationState: String) {
        hideProgressBar()
        if (productView.navigation == null || productView.navigation.size == 0) {
            setResultForProductListing(navigationState)
        } else {
            if (TextUtils.isEmpty(navigationState)) {
                setBaseNavigationState(navigationState)
                setRefinedNavigationState(navigationState)
                this.productsResponse = productView
            }
            reloadFragment(productView)
        }

    }

    override fun onProductRefineFailure(message: String) {
        hideProgressBar()
        Utils.displayDialog(this, CustomPopUpWindow.MODAL_LAYOUT.ERROR, message, ERROR_REQUEST_CODE)
    }

    override fun onSeeResults(navigationState: String) {
        if (!TextUtils.isEmpty(navigationState)) {
            setResultForProductListing(navigationState)
        } else if (!TextUtils.isEmpty(getRefinedNavigationState())) {
            setResultForProductListing(getRefinedNavigationState())
        } else if (TextUtils.isEmpty(getBaseNavigationState()) && TextUtils.isEmpty(getRefinedNavigationState())) {
            setResultForProductListing(emptyNavigationState)
        }
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.REFINE_EVENT_SEE_RESULT)
        this.closeDownPage()
    }

    private fun setResultForProductListing(navigationState: String) {
        intent = Intent()
        intent.putExtra(NAVIGATION_STATE, navigationState)
        setResult(Activity.RESULT_OK, intent)
        this.closeDownPage()
        return
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

    override fun onRefinementClear() {
        setRefinedNavigationState(emptyNavigationState)
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

    override fun onRefinementReset() {
        executeRefineProducts(emptyNavigationState)
    }

    private fun closeDownPage() {
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ERROR_REQUEST_CODE -> closeDownPage()
        }
    }

    private fun getBaseNavigationState(): String {
        return productsRequestParams?.refinement!!
    }

    private fun getRefinedNavigationState(): String {
        return if (updatedProductsRequestParams != null) updatedProductsRequestParams?.refinement!! else emptyNavigationState
    }

    private fun setBaseNavigationState(navigationState: String) {
        this.productsRequestParams?.refinement = navigationState
    }

    private fun setRefinedNavigationState(navigationState: String) {
        this.updatedProductsRequestParams?.refinement = navigationState
    }

    // will return BaseNavigationState if RefinedNavigationState is empty
    private fun getRefinementState(): String {
        return if (TextUtils.isEmpty(getRefinedNavigationState())) getBaseNavigationState() else getRefinedNavigationState()
    }
}
