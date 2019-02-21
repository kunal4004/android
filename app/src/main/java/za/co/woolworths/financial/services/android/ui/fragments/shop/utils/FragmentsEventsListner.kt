package za.co.woolworths.financial.services.android.ui.fragments.shop.utils

import za.co.woolworths.financial.services.android.models.dto.OrderDetailsResponse

interface FragmentsEventsListner {
    fun onOrderItemsClicked(orderDetailsResponse: OrderDetailsResponse)
    fun onItemsAddedToCart()
    fun openTaxInvoices()
}