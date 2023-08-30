package za.co.woolworths.financial.services.android.ui.fragments

import android.content.Intent
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentDrawerBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.ProductView
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.models.dto.Refinement
import za.co.woolworths.financial.services.android.models.dto.RefinementNavigation
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.extension.refineProducts
import za.co.woolworths.financial.services.android.ui.extension.replaceChildFragmentSafely
import za.co.woolworths.financial.services.android.ui.fragments.product.refine.RefinementFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.refine.RefinementNavigationFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.refine.SubRefinementFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.OnRefineProductsResult
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.OnRefinementOptionSelected
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class RefinementDrawerFragment : BaseFragmentBinding<FragmentDrawerBinding>(FragmentDrawerBinding::inflate), OnRefinementOptionSelected, OnRefineProductsResult {

    private var mDrawerToggle: ActionBarDrawerToggle? = null
    private var mDrawerLayout: DrawerLayout? = null

    private var productsResponse: ProductView? = null
    private var productsRequestParams: ProductsRequestParams? = null
    private var updatedProductsRequestParams: ProductsRequestParams? = null
    private val emptyNavigationState = ""
    private val ERROR_REQUEST_CODE = 755
    var selectedNavigationState: String? = null
    var isResetFilterSelected = false
    var isMultiSelectCategoryRefined = false

    companion object {
        const val TAG_NAVIGATION_FRAGMENT: String = "OptionsFragment"
        const val TAG_REFINEMENT_FRAGMENT: String = "RefinementFragment"
        const val TAG_SUB_REFINEMENT_FRAGMENT: String = "SubRefinementFragment"
        const val NAVIGATION_STATE = "NAVIGATION_STATE"
        const val UPDATED_NAVIGATION_STATE = "UPDATED_NAVIGATION_STATE"

    }

    public fun setUpDrawer(drawerLayout: DrawerLayout, productsResponse: ProductView, productsRequestParams: ProductsRequestParams) {
        resetRefinementData()
        this.productsResponse = productsResponse
        this.productsRequestParams = productsRequestParams
        removeAllFragments()
        activity?.apply {
            mDrawerLayout = drawerLayout

            //Remove listener if there is any before adding a new one
            mDrawerToggle?.apply {
                mDrawerLayout?.removeDrawerListener(this)
            }

            mDrawerToggle = object : ActionBarDrawerToggle(this, drawerLayout, R.string.open_drawer, R.string.close_drawer) {
                override fun onDrawerClosed(drawerView: View) {
                    super.onDrawerClosed(drawerView)
                    removeAllFragments()
                    if(isResetFilterSelected)
                        (activity as? BottomNavigationActivity)?.onResetFilter()
                    if (!selectedNavigationState.isNullOrEmpty())
                        (activity as? BottomNavigationActivity)?.onRefined(selectedNavigationState, isMultiSelectCategoryRefined)
                }

                override fun onDrawerStateChanged(newState: Int) {
                    super.onDrawerStateChanged(newState)
                    if (newState == DrawerLayout.STATE_DRAGGING && !drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                        removeAllFragments()
                        initViews()
                    }
                }
            }
            mDrawerToggle?.apply { mDrawerLayout?.addDrawerListener(this) }

            initViews()
            setUniqueIds()
        }

    }

    private fun initViews() {
        productsResponse?.let {
            replaceRefinementOptionsFragment(it)
        }
    }

    private fun replaceRefinementOptionsFragment(productsResponse: ProductView) {
        replaceChildFragmentSafely(RefinementNavigationFragment.getInstance(productsResponse, getBaseNavigationState(), getRefinedNavigationState()), TAG_NAVIGATION_FRAGMENT, false, false, R.id.fragment_container)
    }

    override fun onRefinementOptionSelected(refinementNavigation: RefinementNavigation) {
        pushFragment(RefinementFragment.getInstance(refinementNavigation, getRefinementState()), TAG_REFINEMENT_FRAGMENT)
    }

    override fun onRefinementSelected(refinement: Refinement) {
        pushFragment(SubRefinementFragment.getInstance(refinement), TAG_SUB_REFINEMENT_FRAGMENT)
    }

    fun pushFragment(fragment: Fragment, tag: String) {
        replaceChildFragmentSafely(fragment, tag, false, true, R.id.fragment_container, R.anim.slide_in_from_right, R.anim.slide_out_to_left, R.anim.slide_in_from_left, R.anim.slide_out_to_right)
    }

    override fun onBackPressedWithRefinement(navigationState: String, isMultiSelect: Boolean) {
        isMultiSelectCategoryRefined = isMultiSelect
        executeRefineProducts(navigationState)
    }

    override fun onBackPressedWithOutRefinement() {
        if (binding.progressBar.visibility == View.VISIBLE)
            return
        if (childFragmentManager.backStackEntryCount > 0) {
            childFragmentManager.popBackStack()
        } else {
            closeDownPage()
        }
    }

    private fun executeRefineProducts(refinement: String) {
        showProgressBar()
        updatedProductsRequestParams = ProductsRequestParams(productsRequestParams?.searchTerm!!, productsRequestParams?.searchType!!, productsRequestParams?.responseType!!, productsRequestParams?.pageOffset!!)
        setRefinedNavigationState(refinement)
        activity?.let { refineProducts(this, updatedProductsRequestParams!!) }
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
        Utils.displayDialog(activity, CustomPopUpWindow.MODAL_LAYOUT.ERROR, message, ERROR_REQUEST_CODE)
    }

    override fun onSeeResults(navigationState: String, isMultiSelect: Boolean) {
        isMultiSelectCategoryRefined = isMultiSelect
        if (!TextUtils.isEmpty(navigationState)) {
            setResultForProductListing(navigationState)
        } else if (!TextUtils.isEmpty(getRefinedNavigationState())) {
            setResultForProductListing(getRefinedNavigationState())
        } else if (TextUtils.isEmpty(getBaseNavigationState()) && TextUtils.isEmpty(getRefinedNavigationState())) {
            setResultForProductListing(emptyNavigationState)
        }
        activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.REFINE_EVENT_SEE_RESULT, this) }
        this.closeDownPage()
    }

    private fun setResultForProductListing(navigationState: String) {
        selectedNavigationState = navigationState
        closeDownPage()
    }

    private fun hideProgressBar() {
        binding.progressBar?.visibility = View.INVISIBLE
        activity?.apply {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    private fun showProgressBar() {
        binding.progressBar?.visibility = View.VISIBLE
        activity?.apply {
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    override fun onRefinementClear() {
        setRefinedNavigationState(emptyNavigationState)
        reloadFragment(productsResponse!!)
    }

    private fun reloadFragment(productView: ProductView) {
        //Here we are clearing back stack fragment entries
        val backStackEntry = childFragmentManager.backStackEntryCount
        if (backStackEntry > 0) {
            for (i in 0 until backStackEntry) {
                childFragmentManager.popBackStackImmediate()
            }
        }

        //Here we are removing all the fragment that are shown here
        if (childFragmentManager.fragments != null && childFragmentManager.fragments.size > 0) {
            for (i in 0 until childFragmentManager.fragments.size) {
                val mFragment = childFragmentManager.fragments[i]
                if (mFragment != null) {
                    childFragmentManager.beginTransaction().remove(mFragment).commitAllowingStateLoss()
                }
            }
        }
        replaceRefinementOptionsFragment(productView)
    }

    override fun onRefinementReset() {
        //executeRefineProducts(emptyNavigationState)
        isResetFilterSelected = true
        closeDownPage()
    }

    private fun closeDownPage() {
        activity?.runOnUiThread {
            mDrawerLayout?.closeDrawer(Gravity.RIGHT)
        }
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

    override fun onCategorySelected(refinement: Refinement, isMultiSelect: Boolean) {
        onSeeResults(refinement.navigationState, isMultiSelect)
    }

    override fun hideBackButton() {
        super.hideBackButton()
        binding.backButton.visibility = View.GONE
        binding.closeButton.visibility = View.VISIBLE
    }

    override fun hideCloseButton() {
        super.hideCloseButton()
        binding.backButton.visibility = View.VISIBLE
        binding.closeButton.visibility = View.INVISIBLE
    }

    override fun setPageTitle(title: String) {
        super.setPageTitle(title)
        binding.toolbarText.text = title
    }

    fun removeAllFragments() {
        binding.fragmentContainer?.removeAllViewsInLayout()
    }

    private fun resetRefinementData() {
        productsResponse = null
        productsRequestParams = null
        updatedProductsRequestParams = null
        selectedNavigationState = null
        isResetFilterSelected = false
        isMultiSelectCategoryRefined = false
    }

    fun setUniqueIds(){
        resources?.apply {
            binding.toolbarText?.contentDescription = getString(R.string.plp_testViewFilterResult)
            binding.closeButton?.contentDescription = getString(R.string.plp_buttonClose)
        }
    }

}