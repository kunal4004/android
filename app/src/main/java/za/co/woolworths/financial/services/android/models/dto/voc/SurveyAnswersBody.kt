package za.co.woolworths.financial.services.android.models.dto.voc

data class SurveyAnswersBody(
        val surveyId: Long,
        val participantReplies: ArrayList<SurveyAnswer>? = null
)