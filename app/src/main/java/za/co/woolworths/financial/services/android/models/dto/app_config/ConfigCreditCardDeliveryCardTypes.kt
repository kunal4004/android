package za.co.woolworths.financial.services.android.models.dto.app_config

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigCreditCardDeliveryCardTypes(
    var binNumber: String,
    var minimumSupportedAppBuildNumber: Int
) : Parcelable