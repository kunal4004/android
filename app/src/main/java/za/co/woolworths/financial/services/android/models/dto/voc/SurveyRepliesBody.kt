package za.co.woolworths.financial.services.android.models.dto.voc

data class SurveyRepliesBody(
        val surveyId: Long,
        val participantReplies: List<SurveyAnswer>? = null
)