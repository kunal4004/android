package za.co.woolworths.financial.services.android.geolocation.model.response

data class ConfirmLocationAddress (
    val placeId: String?,
    val nickname: String? = null,
    val address2: String? = null
)