package za.co.woolworths.financial.services.android.models.dto.cart

import za.co.woolworths.financial.services.android.models.dto.StoreDetails
import za.co.woolworths.financial.services.android.models.dto.Suburb
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.DiscountDetails

class OrderSummary {
    var totalItemsCount: Int = 0
    var groupSubTotal: String = ""
    var giftCardAuthorizedAmount: Int = 0
    var orderId: String = ""
    var basketTotal: Double = 0.00
    var shippingAdjusted: Boolean? = null
    var wrewardsDiscount: Int = 0
    var store: StoreDetails? = null
    var submittedDate: String = ""
    var completedDate: String = ""
    var orderCancellable: Boolean? = null
    var total: Double = 0.00
    var discountDetails: DiscountDetails? = null
    var suburb: Suburb? = null
    var state: String = ""
    var estimatedDelivery: Int = 0
    var deliveryStatus: Any? = null
    var totalOrderCount: Int = 0
    var savedAmount: Int = 0
    var deliveryDates: Any? = null
}