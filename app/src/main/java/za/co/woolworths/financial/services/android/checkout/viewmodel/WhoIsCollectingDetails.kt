package za.co.woolworths.financial.services.android.checkout.viewmodel

/**
 * Created by Kunal Uttarwar on 09/11/21.
 */
data class WhoIsCollectingDetails(
    val recipientName: String,
    val phoneNumber: String,
    val vehicleColor: String,
    val vehicleModel: String,
    val vehicleRegistration: String,
    val isMyVehicle: Boolean
)
