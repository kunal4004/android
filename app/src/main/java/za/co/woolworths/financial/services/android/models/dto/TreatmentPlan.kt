package za.co.woolworths.financial.services.android.models.dto

data class TreatmentPlan(
    val personalLoan: ViewTreatmentPlan,
    val creditCard: ViewTreatmentPlan,
    val storeCard: ViewTreatmentPlan
)