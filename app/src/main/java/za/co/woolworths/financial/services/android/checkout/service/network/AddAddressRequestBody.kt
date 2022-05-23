package za.co.woolworths.financial.services.android.checkout.service.network

/**
 * Created by Kunal Uttarwar on 13/06/21.
 */
data class AddAddressRequestBody(
    val nickname: String,
    val recipientName: String,
    val address1: String,
    val address2: String,
    val postalCode: String,
    val primaryContactNo: String,
    val secondaryContactNo: String,
    val region: String,
    val suburbId: String,
    val city: String,
    val suburb: String,
    val specialInstructions: String,
    val defaultAddress: Boolean,
    val latitude: String?,
    val longitude: String?,
    val placesId: String,
    val addressType: String,
    val isGeolocation: Boolean,
)
