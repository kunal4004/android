package za.co.woolworths.financial.services.android.models.dto

data class InsuranceType(var description: String, var covered: Boolean, var effectiveDate: String) {
    constructor() : this("", false, "")
}