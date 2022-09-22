package za.co.woolworths.financial.services.android.models.dto

data class EligibilityPlanResponse(
    val eligibilityPlan: EligibilityPlan?,
    val response: Response,
    val httpCode: Int
)