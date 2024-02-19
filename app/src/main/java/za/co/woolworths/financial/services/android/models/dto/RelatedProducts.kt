package za.co.woolworths.financial.services.android.models.dto

import com.google.gson.JsonElement


data class RelatedProducts(
    var productName: String,
    var productId: String = "",
    var colourSKUsPrices: JsonElement? = null,
    var brandText: String,
    var range: String,
    var network: String,
    var matchingSet: String,
    var auxiliaryImages: JsonElement? = null
)