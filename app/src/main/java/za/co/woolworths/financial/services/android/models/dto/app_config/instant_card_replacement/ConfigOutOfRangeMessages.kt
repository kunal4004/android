package za.co.woolworths.financial.services.android.models.dto.app_config.instant_card_replacement

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigOutOfRangeMessages(
    val inRange: String? = null,
    val outOfRange: String? = null
) : Parcelable