package za.co.woolworths.financial.services.android.models.dto.app_config.account_options

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigViewTreatmentPlan(
    val minimumSupportedAppBuildNumber: Int? = null,
    val minimumDelinquencyCycle: Int? = null,
    val maximumDelinquencyCycle: Int? = null,
    val collectionsUrl: String? = null,
    val collectionsDynamicUrl: String? = null,
    val exitUrl: String? = null
) : Parcelable