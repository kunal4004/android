package za.co.woolworths.financial.services.android.models.dto.app_config.account_options

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PetInsuranceConfig(
    val minimumSupportedAppBuildNumber: Int,
    val renderMode: String,
    val petInsuranceUrl: String,
    val exitUrl: String
) : Parcelable