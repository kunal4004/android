package za.co.woolworths.financial.services.android.ui.fragments.shop

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.crashlytics.android.Crashlytics
import kotlinx.android.synthetic.main.empty_state_template.*
import kotlinx.android.synthetic.main.shop_fragment.*
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.OrderItem
import za.co.woolworths.financial.services.android.models.dto.OrdersResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator
import za.co.woolworths.financial.services.android.ui.adapters.OrdersAdapter
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ErrorMessageDialogFragment
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.QueryBadgeCounter
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.SessionUtilities
import java.lang.IllegalStateException

class MyOrdersAccountFragment : Fragment() {

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
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.shop_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        initView()
    }

    private fun initView() {
        mErrorHandlerView = ErrorHandlerView(activity, relEmptyStateHandler, imgEmpyStateIcon, txtEmptyStateTitle, txtEmptyStateDesc, btnGoToProduct)
        myOrdersList?.layoutManager = LinearLayoutManager(activity)
        btnGoToProduct?.setOnClickListener { onActionClick() }
        configureUI()
        swipeToRefresh?.setOnRefreshListener {
            swipeToRefresh?.isRefreshing = true
            executeOrdersRequest(true)
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
            }
        }
    }

    private fun executeOrdersRequest(isPullToRefresh: Boolean) {
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

    private fun showLoading() {
        loadingBar?.visibility = View.VISIBLE
        myOrdersList?.visibility = View.GONE
    }

    fun scrollToTop() {
        myOrdersList?.scrollToPosition(0)
    }


    fun updateUI(ordersResponse: OrdersResponse?) {
        // Proceed with onPostExecute block code if UI exist
        loadingBar?.visibility = View.GONE
        when (ordersResponse?.httpCode) {
            0 -> {
                showSignInView(ordersResponse)
            }
            440 -> {
                SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE)
                showSignOutView()
                QueryBadgeCounter.instance.clearBadge()
            }
            502 -> {
                try {
                    val errorMessage = ErrorMessageDialogFragment.newInstance(ordersResponse.response?.desc
                                    ?: bindString(R.string.general_error_desc), bindString(R.string.ok))
                    activity?.supportFragmentManager?.let { supportManager -> errorMessage.show(supportManager, ErrorMessageDialogFragment::class.java.simpleName) }
                } catch (ex: IllegalStateException) {
                    Crashlytics.logException(ex)
                }
            }
            else -> {
                showErrorView()
            }
        }
    }

    private fun showSignInView(ordersResponse: OrdersResponse) {
        dataList = buildDataToDisplayOrders(ordersResponse)
        if (dataList.size > 0) {
            mErrorHandlerView?.hideEmpyState()
            myOrdersList.adapter = activity?.let { OrdersAdapter(it, dataList) }
            myOrdersList.visibility = View.VISIBLE
            swipeToRefresh.isEnabled = true
        } else
            showEmptyOrdersView()
    }

    private fun showSignOutView() {
        myOrdersList?.visibility = View.GONE
        swipeToRefresh?.isEnabled = false
        mErrorHandlerView?.setEmptyStateWithAction(7, R.string.sign_in, ErrorHandlerView.ACTION_TYPE.SIGN_IN)

    }

    private fun showErrorView() {
        myOrdersList?.visibility = View.GONE
        swipeToRefresh?.isEnabled = false
        mErrorHandlerView?.setEmptyStateWithAction(8, R.string.retry, ErrorHandlerView.ACTION_TYPE.RETRY)

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
        myOrdersList?.visibility = View.GONE
        swipeToRefresh?.isEnabled = true
        mErrorHandlerView?.setEmptyStateWithAction(6, R.string.start_shopping, ErrorHandlerView.ACTION_TYPE.REDIRECT)
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
}