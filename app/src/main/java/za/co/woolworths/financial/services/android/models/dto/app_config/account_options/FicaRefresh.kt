package za.co.woolworths.financial.services.android.models.dto.app_config.account_options

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FicaRefresh(
    val minimumSupportedAppBuildNumber: Int,
    val renderMode: String,
    val ficaRefreshUrl: String,
    val privacyPolicyUrl: String,
    val exitUrl: String
) : Parcelable