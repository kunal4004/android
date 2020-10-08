package za.co.woolworths.financial.services.android.models.dto.chat

data class PresenceInAppChat (
		val tradingHours : MutableList<TradingHours>,
		val minimumSupportedAppBuildNumber : String,
		var liveChatEnabled : LiveChatEnabled? = null,
		var isEnabled: Boolean? = false,
		val collections : Collections,
		val customerService : CustomerService
)