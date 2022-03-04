package za.co.woolworths.financial.services.android.geolocation.model

data class QuantityLimit(
    val food: Int,
    val foodLayoutColour: String,
    val foodMaximumQuantity: Int,
    val other: Int,
    val otherLayoutColour: String,
    val otherMaximumQuantity: Int
)