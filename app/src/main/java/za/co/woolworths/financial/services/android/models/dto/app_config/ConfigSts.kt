package za.co.woolworths.financial.services.android.models.dto.app_config

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigSts(
    var kmsiMinimumSupportedAppVersion: String? = null
) : Parcelable