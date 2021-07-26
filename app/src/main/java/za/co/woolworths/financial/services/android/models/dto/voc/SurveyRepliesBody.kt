package za.co.woolworths.financial.services.android.models.dto.voc

import java.io.Serializable

class SurveyRepliesBody(
        val surveyId: Long,
        val participantReplies: List<SurveyAnswer>? = null
) : Serializable