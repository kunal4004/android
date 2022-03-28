package za.co.woolworths.financial.services.android.models.dto

import java.io.Serializable

enum class ProductGroupCode(val value: String) {
    PL("PL"),
    SC("SC"),
    CC("CC")
}

enum class ActionText(val value: String) {
    VIEW_TREATMENT_PLAN("collectionsViewExistingPlan"),
    TAKE_UP_TREATMENT_PLAN("collectionsStartNewPlan"),
    VIEW_ELITE_PLAN("collectionsViewElitePlan"),
    START_NEW_ELITE_PLAN("collectionsStartNewElitePlan")
}

class EligibilityPlan(
    val planType: String?,
    val appGuid: String?,
    val appGuidExpiryDate: String?,
    val productGroupCode: ProductGroupCode?,
    val actionText: String?,
    val displayText: String?
) : Serializable
