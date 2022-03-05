package za.co.woolworths.financial.services.android.geolocation.model.request

data class SaveAddressLocationRequest(
    val address1: String,
    val city: String,
    val country: String,
    val formattedAddress: String,
    val latitude: Double,
    val longitude: Double,
    val placeId: String,
    val postalCode: String,
    val province: String,
    val suburb: String
)