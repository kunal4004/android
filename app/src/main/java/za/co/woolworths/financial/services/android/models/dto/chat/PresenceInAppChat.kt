package za.co.woolworths.financial.services.android.models.dto.chat

import com.google.gson.annotations.SerializedName

data class PresenceInAppChat(@SerializedName("tradingHours") val tradingHours: List<TradingHours>,
                             @SerializedName("minimumSupportedAppBuildNumber") val minimumSupportedAppBuildNumber: String,
                             var isEnabled: Boolean? = false, @SerializedName("collections") val collections: Collections)
