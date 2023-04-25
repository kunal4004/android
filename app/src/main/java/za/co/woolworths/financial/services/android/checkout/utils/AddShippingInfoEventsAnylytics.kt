package za.co.woolworths.financial.services.android.checkout.utils

import za.co.woolworths.financial.services.android.models.dto.CommerceItem

interface AddShippingInfoEventsAnalytics {

    fun sendEventData(cartItemList: ArrayList<CommerceItem>, shippingTier: String, value: Double)
}