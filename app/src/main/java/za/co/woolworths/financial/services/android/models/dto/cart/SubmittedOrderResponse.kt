package za.co.woolworths.financial.services.android.models.dto.cart

import za.co.woolworths.financial.services.android.models.dto.Response

class SubmittedOrderResponse {
    var basketId: String = ""
    var userInfo: String = ""
    var groupSubTotal: GroupSubTotal? = null
    var wfsCardDetails: WfsCardDetails? = null
    var errorMessage: String = ""
    var orderSummary: OrderSummary? = null
    var message: String = ""
    var modifiedItems: String = ""
    var discountDetails: String = ""
    var deliveryDetails: DeliveryDetails? = null
    var firstOrderInProfile: Boolean? = null
    var items: OrderItems? = null
    var httpCode: Int = 0
    var response: Response? = null
}