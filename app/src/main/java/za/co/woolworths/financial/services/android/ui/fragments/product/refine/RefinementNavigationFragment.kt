package za.co.woolworths.financial.services.android.ui.fragments.product.refine


import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_refinement.*
import za.co.woolworths.financial.services.android.models.dto.ProductView
import za.co.woolworths.financial.services.android.models.dto.RefinementNavigation
import za.co.woolworths.financial.services.android.models.dto.RefinementSelectableItem
import za.co.woolworths.financial.services.android.ui.adapters.RefinementNavigationAdapter
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.OnRefinementOptionSelected
import za.co.woolworths.financial.services.android.util.Utils


class RefinementNavigationFragment : BaseRefinementFragment() {

    private lateinit var listener: OnRefinementOptionSelected
    private var refinementNavigationAdapter: RefinementNavigationAdapter? = null
    private var productView: ProductView? = null
    private var dataList = arrayListOf<RefinementSelectableItem>()
    private var baseNavigationState = ""
    private var updatedNavigationState = ""
    private var pageTitle: TextView? = null
    private val emptyNavigationState = ""

    companion object {
        private val ARG_PARAM = "productViewObject"
        val ON_PROMOTION: String = "On Promotion"
        val CATEGORY: String = "Category"
        val NAVIGATION_STATE = "NAVIGATION_STATE"
        val UPDATED_NAVIGATION_STATE = "UPDATED_NAVIGATION_STATE"

        fun getInstance(productView: ProductView, navigationState: String, updatedNavigationState: String) = RefinementNavigationFragment().withArgs {
            putString(ARG_PARAM, Utils.toJson(productView))
            putString(NAVIGATION_STATE, navigationState)
            putString(UPDATED_NAVIGATION_STATE, updatedNavigationState)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            productView = Utils.jsonStringToObject(it.getString(ARG_PARAM), ProductView::class.java) as ProductView
            baseNavigationState = it.getString(NAVIGATION_STATE, "")
            updatedNavigationState = it.getString(UPDATED_NAVIGATION_STATE, "")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {

        activity?.let {
            (it as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
            pageTitle = it.findViewById(R.id.toolbarText)
        }
        pageTitle?.text = resources.getString(R.string.filter_results)
        clearAndResetFilter?.text = getString(R.string.reset_filter)
        clearAndResetFilter?.setOnClickListener { if (showReset()) listener.onRefinementReset() else listener.onRefinementClear() }
        refinementSeeResult.setOnClickListener { seeResults() }
        refinementList.layoutManager = LinearLayoutManager(activity)
        updateToolBarMenuText()
        loadData()
    }

    private fun loadData() {
        if (productView!!.navigation != null && productView!!.navigation.size > 0) {
            setResultCount(productView?.pagingResponse?.numItemsInTotal)
            dataList = getRefinementSelectableItems(productView?.navigation!!)
            refinementNavigationAdapter = activity?.let { RefinementNavigationAdapter(it, listener, dataList, productView?.history!!) }
            refinementList.adapter = refinementNavigationAdapter!!
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnRefinementOptionSelected) {
            listener = context
        } else {
            throw ClassCastException("$context must implement OnRefinementOptionSelected.")
        }
    }

    private fun getRefinementSelectableItems(navigationList: ArrayList<RefinementNavigation>): ArrayList<RefinementSelectableItem> {
        var dataList = arrayListOf<RefinementSelectableItem>()
        var isHeaderAddedForCategory: Boolean = false
        navigationList.forEach {
            if (it.displayName.contentEquals(ON_PROMOTION)) {
               // dataList.add(0, RefinementSelectableItem(it, RefinementSelectableItem.ViewType.SECTION_HEADER))
                var refinementSelectableItem = RefinementSelectableItem(it, RefinementSelectableItem.ViewType.PROMOTION)
                refinementSelectableItem.isSelected = (it.refinementCrumbs != null && it.refinementCrumbs.size > 0)
                dataList.add(0, refinementSelectableItem)
            } else if ((it.refinements != null && it.refinements.size > 0) || (it.refinementCrumbs != null && it.refinementCrumbs.size > 0)) {
                if (!isHeaderAddedForCategory) {
                    dataList.add(RefinementSelectableItem(it, RefinementSelectableItem.ViewType.SECTION_HEADER))
                    isHeaderAddedForCategory = true
                }
                dataList.add(RefinementSelectableItem(it, RefinementSelectableItem.ViewType.OPTIONS))
            }
        }
        return dataList
    }

    private fun setResultCount(count: Int?) {
        seeResultCount.text = getString(R.string.see_results_count_start) + count.toString() + getString(R.string.see_results_count_end)
    }

    override fun onBackPressed() {
        activity?.finish()
        activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun seeResults() {
        listener.onSeeResults(emptyNavigationState)
    }

    private fun updateToolBarMenuText() {
        if (showReset()) {
            clearAndResetFilter?.text = getString(R.string.reset_filter)
            clearAndResetFilter?.isEnabled = !TextUtils.isEmpty(baseNavigationState)
        } else {
            clearAndResetFilter?.text = getString(R.string.clear_filter)
            clearAndResetFilter?.isEnabled = !TextUtils.isEmpty(updatedNavigationState)
        }
    }

    private fun showReset(): Boolean {
        return (!TextUtils.isEmpty(baseNavigationState) && TextUtils.isEmpty(updatedNavigationState))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.getItem(0)?.isVisible = true
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            R.id.itmIconClose -> {
                onBackPressed()
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

}
