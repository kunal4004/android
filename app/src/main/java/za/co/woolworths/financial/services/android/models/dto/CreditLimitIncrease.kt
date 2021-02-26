package za.co.woolworths.financial.services.android.models.dto

import java.io.Serializable

data class CreditLimitIncrease(
        val eligibilityQuestions: EligibilityQuestions? = null,
        val permissions: Permissions? = null,
        var maritalStatus: ArrayList<MaritalStatus>? = null
) : Serializable