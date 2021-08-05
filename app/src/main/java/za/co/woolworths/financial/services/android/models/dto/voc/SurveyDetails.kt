package za.co.woolworths.financial.services.android.models.dto.voc

import java.io.Serializable

data class SurveyDetails(
        val id: Long,
        val name: String? = null,
        val type: String,
        val questions: ArrayList<SurveyQuestion>? = null
): Serializable