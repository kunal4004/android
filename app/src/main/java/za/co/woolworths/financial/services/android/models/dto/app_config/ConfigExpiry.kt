package za.co.woolworths.financial.services.android.models.dto.app_config

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ConfigExpiry(
    val expiryDate: Long?,
    val updateUrl: String?,
    val expiryMsg: String?,
    val reminderMessage: String?,
    val reminderInterval: Long?
) : Parcelable