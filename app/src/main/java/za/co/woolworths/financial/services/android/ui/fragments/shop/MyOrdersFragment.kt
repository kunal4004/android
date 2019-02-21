package za.co.woolworths.financial.services.android.ui.fragments.shop

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.empty_state_template.*
import za.co.woolworths.financial.services.android.models.dto.OrderItem
import za.co.woolworths.financial.services.android.models.dto.OrdersResponse
import kotlinx.android.synthetic.main.fragment_shop_my_orders.*
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.rest.product.GetOrdersRequest
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.ui.adapters.OrdersAdapter
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.OnChildFragmentEvents
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.OnEventListener
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.SessionUtilities

class MyOrdersFragment : Fragment() {

    private var dataList = arrayListOf<OrderItem>()
    private var mErrorHandlerView: ErrorHandlerView? = null
    private var listner: OnChildFragmentEvents? = null

    companion object {
        const val ORDERS_LOGIN_REQUEST = 2025
        private val ARG_PARAM = "listner"

        fun getInstance(listner: OnChildFragmentEvents) = MyOrdersFragment().withArgs {
            putSerializable(ARG_PARAM, listner)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shop_my_orders, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            listner = arguments.getSerializable(ARG_PARAM) as OnChildFragmentEvents
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    fun initViews() {
        mErrorHandlerView = ErrorHandlerView(activity, relEmptyStateHandler, imgEmpyStateIcon, txtEmptyStateTitle, txtEmptyStateDesc, btnGoToProduct)
        myOrdersList.layoutManager = LinearLayoutManager(activity)
        btnGoToProduct.setOnClickListener { onActionClick() }
        configureUI()
    }

    fun configureUI() {

        if (SessionUtilities.getInstance().isUserAuthenticated()) {
            executeOrdersRequest()
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
                executeOrdersRequest()
            }
        }
    }

    private fun showEmptyOrdersView() {

        mErrorHandlerView?.setEmptyStateWithAction(6, R.string.start_shopping, ErrorHandlerView.ACTION_TYPE.REDIRECT)
    }

    private fun showSignOutView() {

        mErrorHandlerView?.setEmptyStateWithAction(7, R.string.sign_in, ErrorHandlerView.ACTION_TYPE.SIGN_IN)

    }

    private fun showErrorView() {
        mErrorHandlerView?.setEmptyStateWithAction(8, R.string.retry, ErrorHandlerView.ACTION_TYPE.RETRY)

    }

    private fun showSignInView(ordersResponse: OrdersResponse) {
        dataList = buildDataToDisplayOrders(ordersResponse)
        if (dataList.size > 0) {
            mErrorHandlerView?.hideEmpyState()
            myOrdersList.adapter = OrdersAdapter(activity, dataList)
        } else
            showEmptyOrdersView()
    }


    private fun executeOrdersRequest() {
        mErrorHandlerView?.hideEmpyState()
        loadingBar.visibility = View.VISIBLE
        requestOrders().execute()
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

    private fun requestOrders(): GetOrdersRequest {
        return GetOrdersRequest(context, object : OnEventListener<OrdersResponse> {
            override fun onSuccess(ordersResponse: OrdersResponse) {
                loadingBar.visibility = View.GONE
                when (ordersResponse.httpCode) {
                    0 -> {
                        showSignInView(ordersResponse)
                    }
                    440 -> {
                        showSignOutView()
                    }
                    else -> {
                        showErrorView()
                    }
                }

            }

            override fun onFailure(e: String?) {
                activity.runOnUiThread(java.lang.Runnable {
                    loadingBar.visibility = View.GONE
                    showErrorView()
                })
            }

        })

    }
}
