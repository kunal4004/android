package za.co.woolworths.financial.services.android.models.dto.app_config

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigCreditView(
    var transUnionLink: String,
    var transUnionPrivacyPolicyUrl: String,
    val minimumSupportedAppBuildNumber: Int,
    var isEnabled: Boolean = false
) : Parcelable