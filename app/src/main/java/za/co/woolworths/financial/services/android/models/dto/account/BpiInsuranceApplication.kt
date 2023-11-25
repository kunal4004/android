package za.co.woolworths.financial.services.android.models.dto.account

import java.io.Serializable

data class BpiInsuranceApplication(
    val status: BpiInsuranceApplicationStatusType?, // can be converted to enum
    val displayLabel: String?,
    val displayLabelColor: String?
) : Serializable

enum class BpiInsuranceApplicationStatusType(val status: String) : Serializable {
    OPTED_IN("OPTED_IN"),
    NOT_OPTED_IN("NOT_OPTED_IN"),
    COVERED("COVERED"),
    NOT_COVERED("NOT_COVERED"),
    INSURANCE_COVERED("INSURANCE_COVERED"),
    DISABLED("DISABLED")

}
