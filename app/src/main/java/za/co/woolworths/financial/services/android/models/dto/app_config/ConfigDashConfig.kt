package za.co.woolworths.financial.services.android.models.dto.app_config

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigDashConfig(
        val appURI: String,
        val minimumSupportedAppBuildNumber: Int?,
        var isEnabled: Boolean = false,
        val driverTip: DriverTip,
        var inAppChat: OneCartConfigInAppChat? = null,
        var inAppChatHuaweiPNData: InAppChatHuaweiPNConfig? = null
): Parcelable