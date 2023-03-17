package za.co.woolworths.financial.services.android.ui.fragments.product.shop.usecase

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties

object Constants {
    const val EVENT_TYPE_PURCHASE = "monetate:context:Purchase"
    const val EVENT_TYPE_REC_CLICKS = "monetate:record:RecClicks"
    const val CURRENCY_VALUE: String = FirebaseManagerAnalyticsProperties.PropertyValues.CURRENCY_VALUE

    const val PRODUCT_ID_FOR_SHIPPING = "SHIPPING"
    const val PRODUCT_ID_FOR_DISCOUNT = "DISCOUNT"
    const val QUANTITY_FOR_DISCOUNT = 1
    const val QUANTITY_FOR_SHIPPING = 1
}