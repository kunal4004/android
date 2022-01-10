package za.co.woolworths.financial.services.android.models.dto.app_config

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigCreditCardDelivery(
    var callCenterNumber: String,
    var deliveryTrackingUrl: String,
    var formattedCardDeliveryFee: String,
    val cardTypes: MutableList<ConfigCreditCardDeliveryCardTypes>
) : Parcelable