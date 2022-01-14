package za.co.woolworths.financial.services.android.models.dto

import java.io.Serializable

enum class ProductGroupCode(val value: String) {
    PL("PL"),
    SC("SC"),
    CC("CC")
}

enum class ActionText(val value: String) {
    VIEW_TREATMENT_PLAN("viewTreatmentPlan"),
    TAKE_UP_TREATMENT_PLAN("takeUpTreatmentPlan")
}

class EligibilityPlan(
    val planType: String,
    val appGuid: String,
    val productGroupCode: ProductGroupCode,
    val actionText: String,
    val displayText: String
    ) : Serializable
