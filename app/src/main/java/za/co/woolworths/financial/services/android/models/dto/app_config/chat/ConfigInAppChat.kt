package za.co.woolworths.financial.services.android.models.dto.app_config.chat

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigInAppChat(
        val minimumSupportedAppBuildNumber: Int,
        val apiURI: String,
        val userPoolId: String,
        val userPoolWebClientId: String,
        var collections: ConfigCollections?=null,
        val customerService: ConfigCustomerService,
        var liveChatEnabled: ConfigLiveChatEnabled? = null,
        val tradingHours: MutableList<ConfigTradingHours>,
        var isEnabled: Boolean? = false
) : Parcelable