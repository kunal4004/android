package za.co.woolworths.financial.services.android.geolocation.network.model

/**
 * Created by Kunal Uttarwar on 04/05/22.
 */
data class ConfirmAddressStoreLocator(
    val latitude: Double?,
    val longitude: Double?,
    val isAddAddress: Boolean?,
    val deliveryType: String?,
) {

}
