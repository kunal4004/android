package za.co.woolworths.financial.services.android.models.dto.app_config

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigInAppReview(
    val minimumSupportedAppBuildNumber: Int,
    val triggerEvents: ArrayList<String>,
    var isEnabled: Boolean = false
) : Parcelable