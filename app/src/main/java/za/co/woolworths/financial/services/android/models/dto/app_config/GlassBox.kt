package za.co.woolworths.financial.services.android.models.dto.app_config

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class GlassBox(
    val minimumSupportedAppBuildNumber: Int?,
    var isEnabled: Boolean = false,
    val reportUrl: String?,
    val appId: String?,
) : Parcelable
