package za.co.woolworths.financial.services.android.ui.fragments.shop

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.OrderItem
import za.co.woolworths.financial.services.android.models.dto.OrdersResponse
import za.co.woolworths.financial.services.android.ui.extension.requestOrders
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.OnOrdersResult
import kotlinx.android.synthetic.main.fragment_shop_my_orders.*
import za.co.woolworths.financial.services.android.ui.adapters.OrdersAdapter

class MyOrdersFragment : Fragment(), OnOrdersResult {

    private var dataList = arrayListOf<OrderItem>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shop_my_orders, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureUI()

    }

    private fun configureUI() {
        myOrdersList.layoutManager = LinearLayoutManager(activity)
        executeOrdersRequest()
    }

    private fun executeOrdersRequest() {
        requestOrders(this, activity).execute()
    }

    override fun onOrdersRequestSuccess(ordersResponse: OrdersResponse) {
        dataList = buildDataToDisplayOrders(ordersResponse)
        myOrdersList.adapter = OrdersAdapter(activity, dataList)
    }

    override fun onOrdersRequestFailure(message: String) {
    }

    private fun buildDataToDisplayOrders(ordersResponse: OrdersResponse): ArrayList<OrderItem> {
        var dataList = arrayListOf<OrderItem>()
        ordersResponse.upcomingOrders.forEach {
            dataList.add(OrderItem(it, OrderItem.ViewType.UPCOMING_ORDER))
        }
        if (ordersResponse.pastOrders.size > 0) {
            dataList.add(OrderItem(null, OrderItem.ViewType.HEADER))
            ordersResponse.pastOrders.forEach {
                dataList.add(OrderItem(it, OrderItem.ViewType.PAST_ORDER))
            }
        }

        return dataList
    }
}
