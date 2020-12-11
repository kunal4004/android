package za.co.woolworths.financial.services.android.models.dto.chat

data class Collections(
		val serviceUnavailable: String,
		val offlineMessageTemplate: String,
		val emailAddress: String,
		val emailSubjectLine: String,
		val emailMessage: String,
		val tradingHours: MutableList<TradingHours>,
		var isEnabled: Boolean? = false
)