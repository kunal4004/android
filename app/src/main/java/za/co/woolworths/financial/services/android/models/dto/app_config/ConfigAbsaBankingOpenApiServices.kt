package za.co.woolworths.financial.services.android.models.dto.app_config

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigAbsaBankingOpenApiServices(
    var isEnabled: Boolean? = false,
    val baseURL: String,
    val appPublicKey: String,
    val contentEncryptionPublicKey: String,
    val minimumSupportedAppBuildNumber: Int
) : Parcelable