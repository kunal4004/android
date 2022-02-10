package za.co.woolworths.financial.services.android.models.dto.app_config

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ConfigLowStock(
    val minimumSupportedAppBuildNumber: Int?,
    var isEnabled: Boolean = false,
    val lowStockCopy: String? = null
) : Parcelable

