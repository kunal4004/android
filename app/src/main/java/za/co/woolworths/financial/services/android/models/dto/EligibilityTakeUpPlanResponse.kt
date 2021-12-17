package za.co.woolworths.financial.services.android.models.dto

data class EligibilityTakeUpPlanResponse(
    val eligibilityPlans: ArrayList<ProductTakeUpPlan>,
    val integrationJwt: String,
    val response: Response,
    val httpCode: Int
)
