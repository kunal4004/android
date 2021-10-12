package za.co.woolworths.financial.services.android.models.dto
enum class ProductGroupCode(val value: String) {PL("PL"), SC("SC"), CC("CC")}
class ProductTakeUpPlan(
    val productGroupCode: ProductGroupCode,
    val hasPlan: Boolean,
    val isEligible: Boolean
    )
