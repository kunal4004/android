package za.co.woolworths.financial.services.android.models.dto.app_config.balance_protection_insurance

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigBalanceProtectionInsurance (
    val minimumSupportedAppBuildNumber: Int? = null,
    val coverage: ConfigBpiCoverage? = null
) : Parcelable