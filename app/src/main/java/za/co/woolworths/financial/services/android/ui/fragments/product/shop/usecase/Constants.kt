package za.co.woolworths.financial.services.android.ui.fragments.product.shop.usecase

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties

object Constants {
    const val EVENT_TYPE_PURCHASE = "monetate:context:Purchase"
    const val EVENT_TYPE_REC_CLICKS = "monetate:record:RecClicks"
    const val EVENT_TYPE_REC_IMPRESSIONS = "monetate:record:RecImpressions"
    const val EVENT_TYPE_USER_AGENT = "monetate:context:UserAgent"
    const val Event_TYPE_IP_ADDRESS = "monetate:context:IpAddress"
    const val Event_TYPE_CUSTOM_VARIABLES = "monetate:context:CustomVariables"
    const val CURRENCY_VALUE: String = FirebaseManagerAnalyticsProperties.PropertyValues.CURRENCY_VALUE

    const val PRODUCT_ID_FOR_SHIPPING = "SHIPPING"
    const val PRODUCT_ID_FOR_DISCOUNT = "DISCOUNT"
    const val QUANTITY_FOR_DISCOUNT = 1
    const val QUANTITY_FOR_SHIPPING = 1
    const val EVENT_TYPE_PAGEVIEW = "monetate:context:PageView"
    const val EVENT_TYPE_CART = "monetate:context:Cart"
    const val EVENT_URL_ORDERDETAILS = "/orderDetails"
    const val EVENT_PAGE_TYPE = "purchase"
    const val EVENT_PAGE_TYPE_MAIN = "main"
    const val EVENT_PAGE_TYPE_SHOPPING_LIST = "shoppingList"
    const val EVENT_TYPE_PRODUCT_THUMBNAIL_VIEW = "monetate:context:ProductThumbnailView"
    const val EVENT_URL_SHOPPING_LIST = "/shoppinglist"
    const val EVENT_URL_MAIN = "/main"
    const val DELIVERY_TYPE = "DeliveryType"
    const val DELIVERY_TYPE_CNC = "CnC"
    const val DELIVERY_TYPE_STANDARD = "Standard"
    const val DELIVERY_TYPE_DASH = "onDemand"
}