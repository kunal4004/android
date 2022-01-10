package za.co.woolworths.financial.services.android.models.dto.app_config

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigCreditCardActivation(
    val otpEnabledForCreditCardActivation: Boolean = false,
    val endpointAvailabilityTimes: ConfigAvailabilityTimes,
    val minimumSupportedAppBuildNumber: Int,
    var isEnabled: Boolean = false
) : Parcelable