package za.co.woolworths.financial.services.android.models.dto.cart

class PriceInfo {
    var discounted: Boolean? = null
    var amount: Double = 0.00
    var rawTotalPrice: Double = 0.00
    var simplePromo: Int = 0
    var salePrice: Int = 0
    var itemSavings: Double = 0.00
    var wrewardsDiscount: Int = 0
    var staffDiscount: Int = 0
    var voucherDiscount: Int = 0
    var promoPrice: Double = 0.00
    var onSale: Boolean? = null
    var totalDiscount: Int = 0
    var otherDiscount: Int = 0
    var listPrice: Double = 0.00
    var simplePromotion: Boolean? = null
    var promoCodeDiscount: Int = 0
}