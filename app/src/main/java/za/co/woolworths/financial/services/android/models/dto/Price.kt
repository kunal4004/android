package za.co.woolworths.financial.services.android.models.dto

data class Price(val amount: Double, val wasPrice: Double?, val rawTotalPrice: Double, val salePrice: Double, val listPrice: Double?){
    var totalDiscount:Double = 0.0
    fun getDiscountedAmount(): Double {
        if (totalDiscount == 0.0 && amount != rawTotalPrice) {
            totalDiscount = rawTotalPrice - amount
        }
        return totalDiscount
    }
}