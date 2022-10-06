package za.co.woolworths.financial.services.android.ui.fragments.voc.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyAnswer
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyDetails
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyQuestion
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import javax.inject.Inject

@HiltViewModel
class SurveyVocViewModel @Inject constructor(): ViewModel() {

    private var surveyDetails: SurveyDetails? = null
    private val surveyAnswers = HashMap<Long, SurveyAnswer>()

    fun configure(details: SurveyDetails?) {
        surveyDetails = details
    }

    fun getAllowedQuestions(): List<SurveyQuestion> {
        val allQuestions = surveyDetails?.questions ?: ArrayList()
        // Array to be updated as new question types are implemented.
        // This is just in case the survey contains question types that have not been implemented yet in this version.
        val allowedQuestionTypes = arrayOf(
            SurveyQuestion.QuestionType.RATE_SLIDER.type,
            SurveyQuestion.QuestionType.FREE_TEXT.type
        )
        return allQuestions.filter { item -> allowedQuestionTypes.contains(item.type) }
    }

    fun getAnswers(): HashMap<Long, SurveyAnswer> = surveyAnswers

    fun getAnswer(questionId: Long): SurveyAnswer? {
        var answer = surveyAnswers[questionId]
        if (answer == null) {
            val question = surveyDetails?.questions?.firstOrNull { it.id == questionId } ?: return null
            // Set default answer
            answer = when (question.type) {
                SurveyQuestion.QuestionType.RATE_SLIDER.type -> {
                    SurveyAnswer(
                        questionId = question.id,
                        answerId = question.maxValue
                    )
                }
                else -> {
                    SurveyAnswer(
                        questionId = question.id
                    )
                }
            }
            surveyAnswers[questionId] = answer
        }
        return answer
    }

    fun setAnswer(questionId: Long, value: Int) {
        getAnswer(questionId)?.answerId = value
    }

    fun setAnswer(questionId: Long, value: String) {
        getAnswer(questionId)?.textAnswer = value
    }

    fun isSurveyAnswersValid(): Boolean {
        val questions = surveyDetails?.questions ?: run { return false }
        for (question: SurveyQuestion in questions) {
            if (question.required == true) {
                val answer = getAnswer(question.id) ?: run { return false }
                when (question.type) {
                    SurveyQuestion.QuestionType.RATE_SLIDER.type -> {
                        if (answer.answerId == null) return false
                    }
                    else -> {
                        if (answer.textAnswer.isNullOrBlank()) return false
                    }
                }
            }
        }
        return true
    }

    fun performOptOutRequest() {
        val optOutVocSurveyRequest = OneAppService.optOutVocSurvey()
        optOutVocSurveyRequest.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                // Response not needed
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                // Ignored if request fails
                FirebaseManager.logException(t)
            }
        })
    }
}