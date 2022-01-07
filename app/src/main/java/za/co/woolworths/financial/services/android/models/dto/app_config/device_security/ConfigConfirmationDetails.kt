package za.co.woolworths.financial.services.android.models.dto.app_config.device_security

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigConfirmationDetails(
    val title: String,
    val description: String
) : Parcelable
