package za.co.woolworths.financial.services.android.models.dto

data class EligibilityTakeUpPlanResponse(
    val hasPlan: Boolean,
    val isEligible: Boolean,
    val response: Response,
    val httpCode: String?
)
