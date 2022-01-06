package za.co.woolworths.financial.services.android.models.dto.app_config.defaults

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigUserPropertiesForDelinquentCodes(
    val sc: String,
    val cc: String,
    val pl: String
) : Parcelable