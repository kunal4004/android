package za.co.woolworths.financial.services.android.models.dto.app_config

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigVirtualTryOn(
        val minimumSupportedAppBuildNumber: Int?,
        var isEnabled: Boolean = false,
        val lightingTipText: String? = null
) : Parcelable