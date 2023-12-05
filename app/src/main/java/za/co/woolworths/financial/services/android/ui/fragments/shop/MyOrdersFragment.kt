package za.co.woolworths.financial.services.android.ui.fragments.shop

import android.os.Bundle
import android.view.View
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentShopMyOrdersBinding
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IPresentOrderDetailInterface
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.Order
import za.co.woolworths.financial.services.android.models.dto.OrderItem
import za.co.woolworths.financial.services.android.models.dto.OrdersResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.adapters.OrdersAdapter
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.OnChildFragmentEvents
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ErrorMessageDialogFragment
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.QueryBadgeCounter
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class MyOrdersFragment : BaseFragmentBinding<FragmentShopMyOrdersBinding>(FragmentShopMyOrdersBinding::inflate), OrderHistoryErrorDialogFragment.IOrderHistoryErrorDialogDismiss, IPresentOrderDetailInterface {

    private var dataList = arrayListOf<OrderItem>()
    private var mErrorHandlerView: ErrorHandlerView? = null
    private var listner: OnChildFragmentEvents? = null
    private var isFragmentVisible: Boolean = false
    private var requestOrders: Call<OrdersResponse>? = null
    private var parentFragment: ShopFragment? = null

    companion object {
        const val ORDERS_LOGIN_REQUEST = 2025
        private const val ARG_PARAM = "listener"

        fun getInstance(listener: OnChildFragmentEvents) = MyOrdersFragment().withArgs {
            putSerializable(ARG_PARAM, listener)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            listner = it.getSerializable(ARG_PARAM) as OnChildFragmentEvents
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isFragmentVisible)
            initViews()
    }

    fun initViews() {
        binding.apply {
            parentFragment = (activity as BottomNavigationActivity).currentFragment as ShopFragment
            // TODO SYNTHETIC: views below cannot be found on layout. TBC if this class is even still being used, since there's no usage of it
//            mErrorHandlerView = ErrorHandlerView(
//                activity,
//                emptyStateTemplate.relEmptyStateHandler,
//                emptyStateTemplate.imgEmpyStateIcon,
//                emptyStateTemplate.txtEmptyStateTitle,
//                emptyStateTemplate.txtEmptyStateDesc,
//                emptyStateTemplate.btnGoToProduct
//            )
//            myOrdersList?.layoutManager = LinearLayoutManager(activity)
//            emptyStateTemplate.btnGoToProduct.setOnClickListener { onActionClick() }

            swipeToRefresh?.setOnRefreshListener {
                swipeToRefresh?.isRefreshing = true
                executeOrdersRequest(true)
            }
            configureUI(false)
        }
    }

    fun configureUI(isNewSession: Boolean) {
        if (SessionUtilities.getInstance().isUserAuthenticated) {
            val orderResponse = parentFragment?.getOrdersResponseData()
            if (orderResponse != null && !isNewSession && !parentFragment?.isDifferentUser()!!)
                showSignInView(orderResponse)
            else {
                parentFragment?.clearCachedData()
                executeOrdersRequest(false)
            }
        } else {
            showSignOutView()
        }
    }

    private fun onActionClick() {
        when (mErrorHandlerView?.actionType) {
            ErrorHandlerView.ACTION_TYPE.SIGN_IN -> {
                ScreenManager.presentSSOSignin(activity, ORDERS_LOGIN_REQUEST)
            }
            ErrorHandlerView.ACTION_TYPE.REDIRECT -> {
                listner?.onStartShopping()
            }
            ErrorHandlerView.ACTION_TYPE.RETRY -> {
                executeOrdersRequest(false)
            }
            else -> {}
        }
    }

    private fun showEmptyOrdersView() {
        binding.apply {
            myOrdersList.visibility = View.GONE
            swipeToRefresh.isEnabled = true
            mErrorHandlerView?.setEmptyStateWithAction(
                6,
                R.string.start_shopping,
                ErrorHandlerView.ACTION_TYPE.REDIRECT
            )
        }
    }

    private fun showSignOutView() {
        binding.apply {
            myOrdersList.visibility = View.GONE
            swipeToRefresh.isEnabled = false
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

    private fun showSignInView(ordersResponse: OrdersResponse) {
        binding.apply {
            dataList = buildDataToDisplayOrders(ordersResponse)
                if (dataList.size > 0) {
                mErrorHandlerView?.hideEmpyState()
                myOrdersList?.adapter = activity?.let { OrdersAdapter(it, this@MyOrdersFragment, dataList) }
                myOrdersList?.visibility = View.VISIBLE
                swipeToRefresh?.isEnabled = true
            } else {
                showEmptyOrdersView()
            }
        }
    }

    private fun executeOrdersRequest(isPullToRefresh: Boolean) {
        activity?.runOnUiThread {
            mErrorHandlerView?.hideEmpyState()
            if (!isPullToRefresh) showLoading()
            requestOrders = OneAppService().getOrders().apply {
                enqueue(CompletionHandler(object : IResponseListener<OrdersResponse> {
                    override fun onSuccess(ordersResponse: OrdersResponse?) {
                        if (isAdded) {
                            if (isPullToRefresh) binding.swipeToRefresh?.isRefreshing = false
                            parentFragment?.setOrdersResponseData(ordersResponse)
                            updateUI()
                        }
                    }

                    override fun onFailure(error: Throwable?) {
                        if (isAdded) {
                            activity?.apply {
                                runOnUiThread {
                                    binding.loadingBar?.visibility = View.GONE
                                    showErrorView()
                                }
                            }
                        }
                    }
                }, OrdersResponse::class.java))
            }
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

    fun updateUI() {
        binding.apply {
            // Proceed with onPostExecute block code if UI exist
            if (myOrdersList == null) return
            val ordersResponse = parentFragment?.getOrdersResponseData()
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
                    showErrorDialog(
                        ordersResponse.response?.desc ?: bindString(R.string.general_error_desc)
                    )
                }
                else -> {
                    showErrorView()
                }
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
        binding.apply {
            if (myOrdersList != null)
                myOrdersList.scrollToPosition(0)
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        isFragmentVisible = isVisibleToUser
        if (!isVisibleToUser && requestOrders != null)
            cancelRequest()
    }

    fun cancelRequest() {
        requestOrders?.apply {
            if (!isCanceled)
                cancel()
        }
    }

    fun showErrorDialog(errorMessage: String) {
        try {
            val messageError =  ErrorMessageDialogFragment.newInstance(errorMessage, bindString(R.string.ok))
            activity?.supportFragmentManager?.let { supportManager -> messageError.show(supportManager, ErrorMessageDialogFragment::class.java.simpleName) }
        }catch (ex: IllegalStateException){
            FirebaseManager.logException(ex)
        }
    }

    override fun onErrorDialogDismiss() {
        //parentFragment?.switchToDepartmentTab() //browsing tabs have been removed from the Shop page so can not change it, also TBC if this class is even still being used, since there's no usage of it
    }

    override fun presentOrderDetailsPage(item: Order) {
        item?.let {
            (requireActivity() as? BottomNavigationActivity)?.pushFragment(
                    OrderDetailsFragment.getInstance(it.orderId)
            )
        }
    }
}
