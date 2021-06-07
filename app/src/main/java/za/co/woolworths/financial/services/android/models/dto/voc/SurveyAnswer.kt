package za.co.woolworths.financial.services.android.models.dto.voc

data class SurveyAnswer(
        val questionId: Long,
        var answerId: Int? = null,
        var textAnswer: String? = null
)
