package za.co.woolworths.financial.services.android.geolocation.model


data class ConfirmLocationRequest (
    val deliveryType: String,
    val address: ConfirmLocationAddress
 )