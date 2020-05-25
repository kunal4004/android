package za.co.woolworths.financial.services.android.ui.fragments.shop

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_shop_department.*
import za.co.woolworths.financial.services.android.models.dto.RootCategories
import za.co.woolworths.financial.services.android.models.dto.RootCategory
import za.co.woolworths.financial.services.android.ui.adapters.DepartmentAdapter
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.no_connection_layout.*
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.fragments.click_and_collect.DeliveryOrClickAndCollectSelectorDialogFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.ProductListingFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.sub_category.SubCategoryFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.list.DepartmentExtensionFragment
import za.co.woolworths.financial.services.android.util.*

class DepartmentsFragment : DepartmentExtensionFragment(), DeliveryOrClickAndCollectSelectorDialogFragment.IDeliveryOptionSelection {

    private var rootCategoryCall: Call<RootCategories>? = null
    private var mDepartmentAdapter: DepartmentAdapter? = null
    private var isFragmentVisible: Boolean = false
    private var parentFragment: ShopFragment? = null
    private var version:String = ""
    private var DEPARTMENT_LOGIN_REQUEST = 1717


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_shop_department, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parentFragment = (activity as? BottomNavigationActivity)?.currentFragment as? ShopFragment
        setUpRecyclerView(mutableListOf())
        setListener()
        if (isFragmentVisible) {
            if (parentFragment?.getCategoryResponseData() != null) bindDepartment() else executeDepartmentRequest()
            (activity as? AppCompatActivity)?.supportFragmentManager?.beginTransaction()?.let { fragmentTransaction -> DeliveryOrClickAndCollectSelectorDialogFragment.newInstance(this).show(fragmentTransaction, DeliveryOrClickAndCollectSelectorDialogFragment::class.java.simpleName) }
        }

    }

    private fun setListener() {
        btnRetry.setOnClickListener {
            if (networkConnectionStatus()) {
                executeDepartmentRequest()
            }
        }
    }

    private fun executeDepartmentRequest() {
        if (networkConnectionStatus()) {
            noConnectionLayout(false)
            rootCategoryCall = OneAppService.getRootCategory()
            rootCategoryCall?.enqueue(CompletionHandler(object : IResponseListener<RootCategories> {
                override fun onSuccess(rootCategories: RootCategories) {
                    when (rootCategories.httpCode) {
                        200 -> {
                            version = rootCategories.response.version
                            parentFragment?.setCategoryResponseData(rootCategories)
                            bindDepartment()
                        }
                        else -> rootCategories.response?.desc?.let { showErrorDialog(it) }
                    }
                }

                override fun onFailure(error: Throwable) {
                    if (isAdded) {
                        activity?.runOnUiThread {
                            if (networkConnectionStatus())
                                noConnectionLayout(true)
                        }
                    }
                }
            },RootCategories::class.java))
        } else {
            noConnectionLayout(true)
        }
    }


    private fun bindDepartment() {
        mDepartmentAdapter?.setRootCategories(parentFragment?.getCategoryResponseData()?.rootCategories)
        mDepartmentAdapter?.notifyDataSetChanged()
    }

    private fun setUpRecyclerView(categories: MutableList<RootCategory>?) {
        mDepartmentAdapter = DepartmentAdapter(categories, ::departmentItemClicked, ::onEditDeliveryLocation) //{ rootCategory: RootCategory -> departmentItemClicked(rootCategory)}
        activity?.let {
            rclDepartment?.apply {
                layoutManager = LinearLayoutManager(it, LinearLayoutManager.VERTICAL, false)
                adapter = mDepartmentAdapter
            }
        }
    }

    private fun departmentItemClicked(rootCategory: RootCategory) {
        (activity as? BottomNavigationActivity)?.pushFragmentSlideUp(openNextFragment(rootCategory))
    }

    private fun onEditDeliveryLocation() {
        if (SessionUtilities.getInstance().isUserAuthenticated) {
            /* if (Utils.getPreferredDeliveryLocation() != null) {
                 activity?.apply { KotlinUtils.presentEditDeliveryLocationActivity(this, if (Utils.getPreferredDeliveryLocation().suburb.storePickup) DeliveryType.STORE_PICKUP else DeliveryType.DELIVERY) }
             } else*/
            activity?.apply { KotlinUtils.presentEditDeliveryLocationActivity(this) }
        } else {
            ScreenManager.presentSSOSignin(activity, DEPARTMENT_LOGIN_REQUEST)
        }
    }

    private fun openNextFragment(rootCategory: RootCategory): Fragment {
        val drillDownCategoryFragment = SubCategoryFragment()
        val bundle = Bundle()
        return when (rootCategory.hasChildren) {
            // navigate to drill down of categories
            true -> {
                bundle.putString("ROOT_CATEGORY", Utils.toJson(rootCategory))
                bundle.putString("VERSION", version)
                drillDownCategoryFragment.arguments = bundle
                return drillDownCategoryFragment
            }
            else -> ProductListingFragment.newInstance(ProductsRequestParams.SearchType.NAVIGATE, rootCategory.categoryName, rootCategory.dimValId)
        }
    }

    fun noConnectionLayout(isVisible: Boolean) {
        incConnectionLayout.visibility = if (isVisible) VISIBLE else GONE
    }

    fun networkConnectionStatus(): Boolean = activity?.let { NetworkManager.getInstance().isConnectedToNetwork(it) }
            ?: false

    override fun onDestroy() {
        super.onDestroy()
        rootCategoryCall?.apply {
            if (isCanceled)
                cancel()
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        isFragmentVisible = isVisibleToUser
    }

    fun scrollToTop() {
        if (rclDepartment != null)
            rclDepartment.scrollToPosition(0)
    }

    override fun onDeliveryOptionSelected(deliveryType: DeliveryType) {
        if (SessionUtilities.getInstance().isUserAuthenticated) {
            activity?.apply { KotlinUtils.presentEditDeliveryLocationActivity(this, deliveryType) }
        } else {
            ScreenManager.presentSSOSignin(activity, DEPARTMENT_LOGIN_REQUEST)
        }
    }

}
