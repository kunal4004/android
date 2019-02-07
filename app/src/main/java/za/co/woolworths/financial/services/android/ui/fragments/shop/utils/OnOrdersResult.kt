package za.co.woolworths.financial.services.android.ui.fragments.shop.utils

import za.co.woolworths.financial.services.android.models.dto.OrdersResponse

interface OnOrdersResult {
    fun onOrdersRequestSuccess(ordersResponse: OrdersResponse)
    fun onOrdersRequestFailure(message: String)
}