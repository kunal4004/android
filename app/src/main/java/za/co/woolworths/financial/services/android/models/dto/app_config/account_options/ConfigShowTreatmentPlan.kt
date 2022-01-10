package za.co.woolworths.financial.services.android.models.dto.app_config.account_options

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigShowTreatmentPlan(
    val renderMode: String,
    val personalLoan: ConfigViewTreatmentPlan,
    val creditCard: ConfigViewTreatmentPlan,
    val storeCard: ConfigViewTreatmentPlan
) : Parcelable