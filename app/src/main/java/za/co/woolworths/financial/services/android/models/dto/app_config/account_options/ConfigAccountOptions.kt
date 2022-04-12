package za.co.woolworths.financial.services.android.models.dto.app_config.account_options

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigAccountOptions(
    val showTreatmentPlanJourney: ConfigShowTreatmentPlan,
    val collectionsStartNewPlanJourney: ConfigShowTreatmentPlan,
    var ficaRefresh: FicaRefresh
) : Parcelable
