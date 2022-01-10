package za.co.woolworths.financial.services.android.models.dto.app_config.chat

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigTradingHours(
		val day: String,
		val opens: String,
		val closes: String
) : Parcelable