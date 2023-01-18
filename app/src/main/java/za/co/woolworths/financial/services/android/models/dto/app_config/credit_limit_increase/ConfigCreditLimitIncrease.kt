package za.co.woolworths.financial.services.android.models.dto.app_config.credit_limit_increase

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigCreditLimitIncrease(
        val eligibilityQuestions: ConfigEligibilityQuestions? = null,
        val permissions: ConfigPermissions? = null,
        var maritalStatus: MutableList<ConfigMaritalStatus>? = null
) : Parcelable