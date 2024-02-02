package za.co.woolworths.financial.services.android.models.dto.app_config

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigBackInStock(
    var frequency_hours: String,
    var notification_pending_count: String
) : Parcelable