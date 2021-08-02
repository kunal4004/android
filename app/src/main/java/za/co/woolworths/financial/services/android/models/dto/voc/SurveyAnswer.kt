package za.co.woolworths.financial.services.android.models.dto.voc

import java.io.Serializable

class SurveyAnswer(
        val questionId: Long,
        var answerId: Int? = null,
        var textAnswer: String? = null,
        var matrix: Boolean? = null,
        var column: Int? = null,
        var group: Int? = null
) : Serializable
