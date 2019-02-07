package za.co.woolworths.financial.services.android.ui.extension

import android.content.Context
import za.co.woolworths.financial.services.android.models.dto.OrdersResponse
import za.co.woolworths.financial.services.android.models.rest.product.GetOrdersRequest
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.OnOrdersResult
import za.co.woolworths.financial.services.android.util.OnEventListener

fun requestOrders(resultListner: OnOrdersResult, context: Context): GetOrdersRequest {
    return GetOrdersRequest(context, object : OnEventListener<OrdersResponse> {
        override fun onSuccess(ordersResponse: OrdersResponse) {
           // if (ordersResponse?.httpCode == 200) {
                resultListner.onOrdersRequestSuccess(ordersResponse!!)
            /*} else {
                resultListner.onOrdersRequestFailure(ordersResponse?.response?.desc!!)
            }*/
        }

        override fun onFailure(e: String?) {
            resultListner.onOrdersRequestFailure(e!!)
        }

    })

}