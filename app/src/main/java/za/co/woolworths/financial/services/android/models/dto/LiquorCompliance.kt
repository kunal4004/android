package za.co.woolworths.financial.services.android.models.dto

class LiquorCompliance(liquorOrder: Boolean, liquorImageUrl: String) {
    var liquorImageUrl: String
    var isLiquorOrder = false

    init {
        isLiquorOrder = liquorOrder
        this.liquorImageUrl = liquorImageUrl
    }
}