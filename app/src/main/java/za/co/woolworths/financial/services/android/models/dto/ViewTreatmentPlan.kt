package za.co.woolworths.financial.services.android.models.dto

data class ViewTreatmentPlan(
        val minimumSupportedAppBuildNumber: Int? = null,
        val minimumDelinquencyCycle: Int? = null,
        val maximumDelinquencyCycle: Int? = null
)