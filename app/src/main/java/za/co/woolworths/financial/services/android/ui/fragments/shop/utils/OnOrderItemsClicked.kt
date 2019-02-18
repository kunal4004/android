package za.co.woolworths.financial.services.android.ui.fragments.shop.utils

import za.co.woolworths.financial.services.android.models.dto.OrderDetailsResponse

interface OnOrderItemsClicked {
    fun onOrderItemsClicked(orderDetailsResponse: OrderDetailsResponse)
}