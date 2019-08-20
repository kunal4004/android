package za.co.woolworths.financial.services.android.models.dto.chat

data class PresenceInAppChat(val tradingHours: List<TradingHours>, val minSupportedAppVersion: String, var isEnabled: Boolean = false)