package za.co.woolworths.financial.services.android.ui.fragments.shop

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ShopFragmentBinding
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IPresentOrderDetailInterface
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.Order
import za.co.woolworths.financial.services.android.models.dto.OrderItem
import za.co.woolworths.financial.services.android.models.dto.OrdersResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity.Companion.RESULT_CODE_MY_ACCOUNT_FRAGMENT
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator
import za.co.woolworths.financial.services.android.ui.adapters.OrdersAdapter
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ErrorMessageDialogFragment
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.QueryBadgeCounter
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class MyOrdersAccountFragment : BaseFragmentBinding<ShopFragmentBinding>(ShopFragmentBinding::inflate), IPresentOrderDetailInterface {

    private var mErrorHandlerView: ErrorHandlerView? = null
    private var requestOrders: Call<OrdersResponse>? = null
    private var dataList = arrayListOf<OrderItem>()
    private var mBottomNavigator: BottomNavigator? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BottomNavigationActivity)
            mBottomNavigator = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (activity is MyAccountActivity){
            setHasOptionsMenu(true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        initView()
    }

    private fun initView() {
        if (activity is MyAccountActivity) {
            (activity as MyAccountActivity)?.binding?.accountToolbarTitle?.text = bindString(R.string.order_history)
            (activity as? MyAccountActivity)?.supportActionBar?.show()
        }

        binding.apply {
            includeEmptyStateTemplate.apply {
                mErrorHandlerView = ErrorHandlerView(
                    activity,
                    relEmptyStateHandler,
                    imgEmpyStateIcon,
                    txtEmptyStateTitle,
                    txtEmptyStateDesc,
                    btnGoToProduct
                )
                btnGoToProduct?.setOnClickListener { onActionClick() }
            }
            myOrdersList?.layoutManager = LinearLayoutManager(activity)
            configureUI()
            swipeToRefresh?.setOnRefreshListener {
                swipeToRefresh?.isRefreshing = true
                executeOrdersRequest(true)
            }
        }
    }

    private fun onActionClick() {
        when (mErrorHandlerView?.actionType) {
            ErrorHandlerView.ACTION_TYPE.SIGN_IN -> {
                ScreenManager.presentSSOSignin(activity, MyOrdersFragment.ORDERS_LOGIN_REQUEST)
            }

            ErrorHandlerView.ACTION_TYPE.RETRY -> {
                executeOrdersRequest(false)
            }
            else -> {
                if (activity is BottomNavigationActivity) {
                    (activity as? BottomNavigationActivity)?.navigateToDepartmentFragment()
                }

                if (activity is MyAccountActivity) {
                    activity?.apply {
                        setResult(RESULT_CODE_MY_ACCOUNT_FRAGMENT)
                        finish()
                    }
                }
            }
        }
    }

    private fun executeOrdersRequest(isPullToRefresh: Boolean) {
        binding.apply {
            mErrorHandlerView?.hideEmpyState()
            if (!isPullToRefresh) showLoading()
            requestOrders = OneAppService.getOrders().apply {
                enqueue(CompletionHandler(object : IResponseListener<OrdersResponse> {
                    override fun onSuccess(response: OrdersResponse?) {
                        if (isAdded) {
                            if (isPullToRefresh) swipeToRefresh?.isRefreshing = false
                            updateUI(response)
                        }
                    }

                    override fun onFailure(error: Throwable?) {
                        if (isAdded) {
                            activity?.apply {
                                runOnUiThread {
                                    loadingBar?.visibility = View.GONE
                                    showErrorView()
                                }
                            }
                        }
                    }
                }, OrdersResponse::class.java))
            }
        }
    }

    private fun showLoading() {
        binding.apply {
            loadingBar?.visibility = View.VISIBLE
            myOrdersList?.visibility = View.GONE
        }
    }

    fun scrollToTop() {
        binding.myOrdersList?.scrollToPosition(0)
    }

    fun updateUI(ordersResponse: OrdersResponse?) {
        binding.apply {
            // Proceed with onPostExecute block code if UI exist
            loadingBar?.visibility = View.GONE
            when (ordersResponse?.httpCode) {
                0 -> {
                    showSignInView(ordersResponse)
                }
                440 -> {
                    SessionUtilities.getInstance()
                        .setSessionState(SessionDao.SESSION_STATE.INACTIVE)
                    showSignOutView()
                    QueryBadgeCounter.instance.clearBadge()
                }
                502 -> {
                    try {
                        val errorMessage = ErrorMessageDialogFragment.newInstance(
                            ordersResponse.response?.desc
                                ?: bindString(R.string.general_error_desc), bindString(R.string.ok)
                        )
                        activity?.supportFragmentManager?.let { supportManager ->
                            errorMessage.show(
                                supportManager,
                                ErrorMessageDialogFragment::class.java.simpleName
                            )
                        }
                    } catch (ex: IllegalStateException) {
                        FirebaseManager.logException(ex)
                    }
                }
                else -> {
                    showErrorView()
                }
            }
        }
    }

    private fun showSignInView(ordersResponse: OrdersResponse) {
        binding.apply {
            dataList = buildDataToDisplayOrders(ordersResponse)
            if (dataList.size > 0) {
                mErrorHandlerView?.hideEmpyState()
                myOrdersList.adapter = activity?.let { OrdersAdapter(it, this@MyOrdersAccountFragment, dataList) }
                myOrdersList.visibility = View.VISIBLE
                swipeToRefresh.isEnabled = true
            } else {
                showEmptyOrdersView()
            }
        }
    }

    private fun showSignOutView() {
        binding.apply {
            myOrdersList?.visibility = View.GONE
            swipeToRefresh?.isEnabled = false
            mErrorHandlerView?.setEmptyStateWithAction(
                7,
                R.string.sign_in,
                ErrorHandlerView.ACTION_TYPE.SIGN_IN
            )
        }

    }

    private fun showErrorView() {
        binding.apply {
            myOrdersList?.visibility = View.GONE
            swipeToRefresh?.isEnabled = false
            mErrorHandlerView?.setEmptyStateWithAction(
                8,
                R.string.retry,
                ErrorHandlerView.ACTION_TYPE.RETRY
            )
        }

    }

    private fun buildDataToDisplayOrders(ordersResponse: OrdersResponse): ArrayList<OrderItem> {
        val dataList = arrayListOf<OrderItem>()
        ordersResponse.upcomingOrders?.forEach {
            dataList.add(OrderItem(it, OrderItem.ViewType.UPCOMING_ORDER))
        }
        if (ordersResponse.pastOrders != null && ordersResponse.pastOrders?.size!! > 0) {
            dataList.add(OrderItem(null, OrderItem.ViewType.HEADER))
            ordersResponse.pastOrders?.forEach {
                dataList.add(OrderItem(it, OrderItem.ViewType.PAST_ORDER))
            }
        }
        return dataList
    }

    private fun showEmptyOrdersView() {
        binding.apply {
            myOrdersList?.visibility = View.GONE
            swipeToRefresh?.isEnabled = true
            mErrorHandlerView?.setEmptyStateWithAction(
                6,
                R.string.start_shopping,
                ErrorHandlerView.ACTION_TYPE.REDIRECT
            )
        }
    }

    private fun configureUI() {
        if (SessionUtilities.getInstance().isUserAuthenticated) {
            executeOrdersRequest(false)
        } else {
            showSignOutView()
        }
    }

    private fun setupToolbar() {
        mBottomNavigator?.apply {
            setTitle(bindString(R.string.order_history))
            displayToolbar()
            showBackNavigationIcon(true)
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden)
            setupToolbar()
    }

    override fun presentOrderDetailsPage(order: Order) {
        requireActivity().apply {
            order?.let {
                (this as? BottomNavigationActivity)?.pushFragment(
                    OrderDetailsFragment.getInstance(it.orderId, true)
                )
            }
        }
    }
}