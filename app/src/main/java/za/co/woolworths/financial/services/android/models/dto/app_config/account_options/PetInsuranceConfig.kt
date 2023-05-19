package za.co.woolworths.financial.services.android.models.dto.app_config.account_options

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PetInsuranceConfig(
    val minimumSupportedAppBuildNumber: Int,
    val renderMode: String,
    val petInsuranceUrl: String,
    val exitUrl: String,
    val defaultCopyPetPending : DefaultCopyPetPending
) : Parcelable

@Parcelize
data class DefaultCopyPetPending(val title: String?, val subtitle : String?, val action : String?): Parcelable