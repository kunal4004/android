package za.co.woolworths.financial.services.android.models.dto.app_config.device_security

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigDeviceSecurity(
    val personalLoan: ConfigProductSecurityDetails,
    val storeCard: ConfigProductSecurityDetails,
    val creditCard: ConfigProductSecurityDetails
) : Parcelable
