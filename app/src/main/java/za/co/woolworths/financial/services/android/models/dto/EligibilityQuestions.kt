package za.co.woolworths.financial.services.android.models.dto

import java.io.Serializable

data class EligibilityQuestions(
    val title: String? = null,
    val description: List<String>? = null
) : Serializable