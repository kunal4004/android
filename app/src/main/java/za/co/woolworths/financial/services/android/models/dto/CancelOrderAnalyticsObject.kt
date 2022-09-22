package za.co.woolworths.financial.services.android.models.dto

import java.io.Serializable

class CancelOrderAnalyticsObject(
    var itemId:String? = "",
    var itemName:String? = "",
    var price:String? = "",
    var itemBrand:String? = "",
    var itemCategory:String? = "",
    var quantity: Int,
): Serializable