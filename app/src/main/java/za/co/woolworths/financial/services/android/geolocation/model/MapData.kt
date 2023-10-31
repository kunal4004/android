package za.co.woolworths.financial.services.android.geolocation.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MapData(
    val latitude: Double?,
    val longitude: Double?,
    val isAddAddress: Boolean?,
    val isComingFromCheckout:Boolean?,
    val isFromDashTab: Boolean?,
    val deliveryType: String?,
    val isFromNewFulfilmentScreen: Boolean?,
    val newDeliveryType: String?
) : Parcelable
