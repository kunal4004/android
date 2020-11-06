package za.co.woolworths.financial.services.android.ui.fragments.shop

import android.app.Activity.RESULT_OK
import android.content.Intent
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
import za.co.woolworths.financial.services.android.models.ValidateSelectedSuburbResponse
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.ui.activities.click_and_collect.EditDeliveryLocationActivity
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
    private var deliveryType: DeliveryType = DeliveryType.DELIVERY

    companion object{
        var DEPARTMENT_LOGIN_REQUEST = 1717
    }


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
            if (!Utils.isDeliverySelectionModalShown()) {
                showDeliveryOptionDialog()
            }
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
                override fun onSuccess(response: RootCategories?) {
                    when (response?.httpCode) {
                        200 -> {
                            version = response.response.version
                            parentFragment?.setCategoryResponseData(response)
                            bindDepartment()
                        }
                        else -> response?.response?.desc?.let { showErrorDialog(it) }
                    }
                }

                override fun onFailure(error: Throwable?) {
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
        executeValidateSuburb()
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
            activity?.apply { KotlinUtils.presentEditDeliveryLocationActivity(this, EditDeliveryLocationActivity.REQUEST_CODE) }
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

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if(!hidden){
            activity?.apply {
                executeValidateSuburb()
            }
        }
    }


    fun scrollToTop() {
        rclDepartment?.scrollToPosition(0)
    }

    override fun onDeliveryOptionSelected(deliveryType: DeliveryType) {
        if (SessionUtilities.getInstance().isUserAuthenticated) {
            activity?.apply { KotlinUtils.presentEditDeliveryLocationActivity(this, EditDeliveryLocationActivity.REQUEST_CODE, deliveryType) }
        } else {
            this.deliveryType = deliveryType
            ScreenManager.presentSSOSignin(activity, DEPARTMENT_LOGIN_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DEPARTMENT_LOGIN_REQUEST && resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()) {
            activity?.apply { KotlinUtils.presentEditDeliveryLocationActivity(this, EditDeliveryLocationActivity.REQUEST_CODE, deliveryType) }
        } else if (resultCode == RESULT_OK || resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()) {
            mDepartmentAdapter?.notifyDataSetChanged()
            executeValidateSuburb()
        }
    }


    override fun onResume() {
        super.onResume()
        activity?.apply {
            mDepartmentAdapter?.notifyDataSetChanged()
            executeValidateSuburb()
        }
    }

    private fun showDeliveryOptionDialog() {
        (activity as? AppCompatActivity)?.supportFragmentManager?.beginTransaction()?.let { fragmentTransaction -> DeliveryOrClickAndCollectSelectorDialogFragment.newInstance(this).show(fragmentTransaction, DeliveryOrClickAndCollectSelectorDialogFragment::class.java.simpleName) }
    }

    private fun executeValidateSuburb() {
        Utils.getPreferredDeliveryLocation().let {
            if (it == null) {
                mDepartmentAdapter?.hideDeliveryDates()
            } else {
                if (it.suburb.id.equals(WoolworthsApplication.getValidatedSuburbProducts()?.suburbId, true)) {
                    updateDeliveryDates()
                } else {
                    mDepartmentAdapter?.showDeliveryDatesProgress(true)
                    OneAppService.validateSelectedSuburb(it.suburb.id, it.suburb.storePickup).enqueue(CompletionHandler(object : IResponseListener<ValidateSelectedSuburbResponse> {
                        override fun onSuccess(response: ValidateSelectedSuburbResponse?) {
                            when (response?.httpCode) {
                                200 -> response.validatedSuburbProducts?.let { it1 ->
                                    it1.suburbId = it.suburb.id
                                    WoolworthsApplication.setValidatedSuburbProducts(it1)
                                    updateDeliveryDates()
                                }
                                else -> mDepartmentAdapter?.hideDeliveryDates()
                            }
                        }

                        override fun onFailure(error: Throwable?) {
                            mDepartmentAdapter?.hideDeliveryDates()
                        }
                    }, ValidateSelectedSuburbResponse::class.java))
                }
            }
        }
    }

    fun updateDeliveryDates() {
        mDepartmentAdapter?.updateDeliveryDate(WoolworthsApplication.getValidatedSuburbProducts())
    }

}
