package za.co.woolworths.financial.services.android.models.dto.voc

import java.io.Serializable

data class SurveyQuestion(
    val id: Long,
    val type: String,
    val title: String? = null,
    val minValue: Int? = null,
    val maxValue: Int? = null,
    var required: Boolean?,
    val matrix: Boolean? = null,
    val column: Int? = null,
    val group: Int? = null
) : Serializable {
    enum class QuestionType(val type: String, val viewType: Int) {
        RATE_SLIDER("NUMERIC", 0),
        FREE_TEXT("FREE_TEXT", 1);

        companion object {
            fun ofType(type: String?): QuestionType? {
                if (type.isNullOrBlank()) return null
                return when (type) {
                    RATE_SLIDER.type -> RATE_SLIDER
                    FREE_TEXT.type -> FREE_TEXT
                    else -> null
                }
            }
        }
    }
}