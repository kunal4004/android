package za.co.woolworths.financial.services.android.ui.fragments.shop

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_shop_department.*
import za.co.woolworths.financial.services.android.models.dto.RootCategories
import za.co.woolworths.financial.services.android.models.dto.RootCategory
import za.co.woolworths.financial.services.android.models.rest.product.ProductCategoryRequest
import za.co.woolworths.financial.services.android.ui.adapters.DepartmentAdapter
import za.co.woolworths.financial.services.android.util.OnEventListener
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.view.View.GONE
import android.view.View.VISIBLE
import kotlinx.android.synthetic.main.no_connection_layout.*
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.GridFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.sub_category.SubCategoryFragment
import za.co.woolworths.financial.services.android.util.NetworkManager
import za.co.woolworths.financial.services.android.util.Utils

class CategoryFragment : Fragment() {

    private var mProductDepartmentRequest: ProductCategoryRequest? = null
    private var mDepartmentAdapter: DepartmentAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_shop_department, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView(mutableListOf())
        executeDepartmentRequest()
        setListener()
    }

    private fun setListener() {
        btnRetryConnect.setOnClickListener {
            if (networkConnectionStatus()) {
                executeDepartmentRequest()
            }
        }
    }

    private fun executeDepartmentRequest() {
        if (networkConnectionStatus()) {
            noConnectionLayout(false)
            mProductDepartmentRequest = requestDepartment()
            mProductDepartmentRequest?.execute()
        } else {
            noConnectionLayout(true)
        }
    }

    private fun requestDepartment(): ProductCategoryRequest {
        return ProductCategoryRequest(object : OnEventListener<RootCategories> {
            override fun onSuccess(rootCategories: RootCategories) {
                when (rootCategories.httpCode) {
                    200 -> bindDepartment(rootCategories)
                    else -> errorMessage(rootCategories.response?.desc)
                }
            }

            override fun onFailure(e: String?) {
                activity?.apply {
                    runOnUiThread {
                        if (networkConnectionStatus())
                            noConnectionLayout(true)
                    }
                }
            }
        })
    }

    private fun errorMessage(desc: String?) {

    }

    private fun bindDepartment(rootCategories: RootCategories) {
        mDepartmentAdapter?.setRootCategories(rootCategories.rootCategories)
        rootCategories.rootCategories?.size?.let { mDepartmentAdapter?.notifyItemRangeInserted(0, it) }
    }

    private fun setUpRecyclerView(categories: MutableList<RootCategory>?) {
        mDepartmentAdapter = DepartmentAdapter(categories) { rootCategory: RootCategory -> departmentItemClicked(rootCategory) }
        activity?.let {
            rclDepartment?.apply {
                val mLayoutManager = LinearLayoutManager(it, LinearLayout.VERTICAL, false)
                //setting top and bottom space between item row
                val dividerItemDecoration = DividerItemDecoration(it, mLayoutManager.orientation)
                dividerItemDecoration.setDrawable(ContextCompat.getDrawable(it, R.drawable.department_line_divider))
                addItemDecoration(dividerItemDecoration)
                layoutManager = mLayoutManager
                adapter = mDepartmentAdapter
            }
        }
    }

    private fun departmentItemClicked(rootCategory: RootCategory) {
        (activity as? BottomNavigationActivity)?.pushFragmentSlideUp(openNextFragment(rootCategory))
    }

    private fun openNextFragment(rootCategory: RootCategory): Fragment {
        val drillDownCategoryFragment = SubCategoryFragment()
        val bundle = Bundle()
        when (rootCategory.hasChildren) {
            // navigate to drill down of categories
            true -> {
                bundle.putString("ROOT_CATEGORY", Utils.toJson(rootCategory))
                drillDownCategoryFragment.arguments = bundle
                return drillDownCategoryFragment
            }
            else -> {
                // navigate to product listing
                val gridFragment = GridFragment()
                bundle.putString("sub_category_id", rootCategory.dimValId)
                bundle.putString("sub_category_name", rootCategory.categoryName)
                gridFragment.arguments = bundle
                return gridFragment
            }
        }
    }

    fun noConnectionLayout(isVisible: Boolean) {
        incConnectionLayout.visibility = if (isVisible) VISIBLE else GONE
    }

    fun networkConnectionStatus(): Boolean = activity?.let { NetworkManager.getInstance().isConnectedToNetwork(it) }
            ?: false

    override fun onDestroy() {
        super.onDestroy()
        mProductDepartmentRequest?.let {
            if (!it.isCancelled) {
                it.cancel(true)
            }
        }
    }
}
