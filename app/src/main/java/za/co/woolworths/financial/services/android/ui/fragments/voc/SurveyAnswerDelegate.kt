package za.co.woolworths.financial.services.android.ui.fragments.voc

import za.co.woolworths.financial.services.android.models.dto.voc.SurveyAnswer

interface SurveyAnswerDelegate {
    fun getRecyclerViewHeight(): Int
    fun getAnswer(questionId: Long): SurveyAnswer?
    fun isSurveyAnswersValid(): Boolean
    fun onInputRateSlider(questionId: Long, value: Int)
    fun onInputFreeText(questionId: Long, value: String)
    fun onSubmit()
    fun onOptOut()
}