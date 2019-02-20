package za.co.woolworths.financial.services.android.ui.fragments.shop

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
import za.co.woolworths.financial.services.android.ui.extension.requestOrders
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.OnOrdersResult
import kotlinx.android.synthetic.main.fragment_shop_my_orders.*
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.ui.adapters.OrdersAdapter
import za.co.woolworths.financial.services.android.util.SessionUtilities

class MyOrdersFragment : Fragment(), OnOrdersResult {

    private var dataList = arrayListOf<OrderItem>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shop_my_orders, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    fun initViews() {
        myOrdersList.layoutManager = LinearLayoutManager(activity)
        configureUI()
    }

    private fun configureUI() {

        if (SessionUtilities.getInstance().isUserAuthenticated()) {
            executeOrdersRequest()
        } else {
            showSignOutView()
        }
    }

    private fun showEmptyOrdersView() {
        relEmptyStateHandler.visibility = View.VISIBLE
        imgEmpyStateIcon.setImageResource(R.drawable.emptyinbox)
        txtEmptyStateTitle.text = getString(R.string.empty_order_view_title)
        txtEmptyStateDesc.text = getString(R.string.empty_order_view_desc)
        btnGoToProduct.text = getString(R.string.go_to_products)
    }

    private fun showSignOutView() {
        relEmptyStateHandler.visibility = View.VISIBLE
        imgEmpyStateIcon.setImageResource(R.drawable.emptyinbox)
        txtEmptyStateTitle.text = getString(R.string.sign_out_order_view_title)
        txtEmptyStateDesc.text = getString(R.string.sign_out_order_view_desc)
        btnGoToProduct.text = getString(R.string.sign_in)
    }

    private fun showSignInView(dataList: ArrayList<OrderItem>) {
        if (dataList.size > 0) {
            relEmptyStateHandler.visibility = View.GONE
            myOrdersList.adapter = OrdersAdapter(activity, dataList)
        } else
            showEmptyOrdersView()
    }


    private fun executeOrdersRequest() {
        loadingBar.visibility = View.VISIBLE
        requestOrders(this, activity).execute()
    }

    override fun onOrdersRequestSuccess(ordersResponse: OrdersResponse) {
        loadingBar.visibility = View.GONE
        dataList = buildDataToDisplayOrders(ordersResponse)
        showSignInView(dataList)
    }

    override fun onOrdersRequestFailure(message: String) {
        loadingBar.visibility = View.GONE
    }

    private fun buildDataToDisplayOrders(ordersResponse: OrdersResponse): ArrayList<OrderItem> {
        val dataList = arrayListOf<OrderItem>()
        ordersResponse.apply {
            when (httpCode) {
                440 -> {
                    SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE)
                }
                else -> {
                    upcomingOrders?.forEach {
                        dataList.add(OrderItem(it, OrderItem.ViewType.UPCOMING_ORDER))
                    }
                    if (pastOrders?.size!! > 0) {
                        dataList.add(OrderItem(null, OrderItem.ViewType.HEADER))
                        pastOrders?.forEach {
                            dataList.add(OrderItem(it, OrderItem.ViewType.PAST_ORDER))
                        }
                    }
                }
            }
        }
        return dataList
    }
}
