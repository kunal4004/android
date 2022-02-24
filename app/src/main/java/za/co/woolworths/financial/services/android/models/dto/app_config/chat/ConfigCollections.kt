package za.co.woolworths.financial.services.android.models.dto.app_config.chat

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigCollections(
	val serviceUnavailable: String,
	val offlineMessageTemplate: String,
	val emailAddress: String,
	val emailSubjectLine: String,
	val emailMessage: String,
	val tradingHours: MutableList<ConfigTradingHours>,
	var isEnabled: Boolean? = false
) : Parcelable