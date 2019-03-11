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
import android.view.View.GONE
import android.view.View.VISIBLE
import kotlinx.android.synthetic.main.no_connection_layout.*
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.GridFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.sub_category.SubCategoryFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.list.DepartmentExtensionFragment
import za.co.woolworths.financial.services.android.util.NetworkManager
import za.co.woolworths.financial.services.android.util.Utils

class CategoryFragment : DepartmentExtensionFragment() {

    private var mProductDepartmentRequest: ProductCategoryRequest? = null
    private var mDepartmentAdapter: DepartmentAdapter? = null
    private var isFragmentVisible: Boolean = false
    private var parentFragment: ShopFragment? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_shop_department, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parentFragment = (activity as BottomNavigationActivity).currentFragment as? ShopFragment
        setUpRecyclerView(mutableListOf())
        setListener()
        if (isFragmentVisible)
            if (parentFragment?.getCategoryResponseData() != null) bindDepartment() else executeDepartmentRequest()

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
                    200 -> {
                        parentFragment?.setCategoryResponseData(rootCategories)
                        bindDepartment()
                    }
                    else -> rootCategories.response?.desc?.let { showErrorDialog(it) }
                }
            }

            override fun onFailure(e: String?) {
                if(isAdded){
                   activity.runOnUiThread {
                       if (networkConnectionStatus())
                           noConnectionLayout(true)
                   }
                }
            }
        })
    }

    private fun bindDepartment() {
        mDepartmentAdapter?.setRootCategories(parentFragment?.getCategoryResponseData()!!.rootCategories)
        mDepartmentAdapter?.notifyDataSetChanged()
    }

    private fun setUpRecyclerView(categories: MutableList<RootCategory>?) {
        mDepartmentAdapter = DepartmentAdapter(categories) { rootCategory: RootCategory -> departmentItemClicked(rootCategory) }
        activity?.let {
            rclDepartment?.apply {
                layoutManager = LinearLayoutManager(it, LinearLayout.VERTICAL, false)
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
        cancelRequest(mProductDepartmentRequest)

    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        isFragmentVisible = isVisibleToUser
    }

    fun scrollToTop() {
        if (rclDepartment != null)
            rclDepartment.scrollToPosition(0)
    }

}
