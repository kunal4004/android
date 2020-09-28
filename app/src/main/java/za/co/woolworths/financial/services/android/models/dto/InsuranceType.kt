package za.co.woolworths.financial.services.android.models.dto

import java.io.Serializable

data class InsuranceType(var description: String, var covered: Boolean, var effectiveDate: String) : Serializable {
    constructor() : this("", false, "")
}