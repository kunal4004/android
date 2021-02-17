package za.co.woolworths.financial.services.android.models.dto

import java.io.Serializable

data class CreditLimitIncrease(
        val eligibilityQuestions: EligibilityQuestions? = null,
        val permissions: Permissions? = null,
        val maritalStatus: List<String>? = null
) : Serializable