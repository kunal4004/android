package za.co.woolworths.financial.services.android.models.dto.app_config

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class BnplConfig(
    val minimumSupportedAppBuildNumber: Int?,
    var isBnplEnabled: Boolean = false,
    var isBnplRequiredInThisVersion: Boolean = false,
) : Parcelable