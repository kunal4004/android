package za.co.woolworths.financial.services.android.models.dto.voc

import za.co.woolworths.financial.services.android.models.dto.Response

data class SurveyDetailsResponse(
        val survey: SurveyDetails? = null,
        val response: Response,
        val httpCode: String?
)