package za.co.woolworths.financial.services.android.models.dto.app_config.credit_limit_increase

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigEligibilityQuestions(
    val title: String? = null,
    val description: List<String>? = null
) : Parcelable
