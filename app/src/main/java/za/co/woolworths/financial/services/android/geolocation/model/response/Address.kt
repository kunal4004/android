package za.co.woolworths.financial.services.android.geolocation.model.response

data class Address(
    val address1: String,
    val id: String,
    val latitude: String,
    val longitude: String,
    val placeId: String
)