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
import za.co.woolworths.financial.services.android.ui.activities.voc.VoiceOfCustomerActivity
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import javax.inject.Inject

@HiltViewModel
class SurveyProcessRequestVocViewModel @Inject constructor(): ViewModel() {

    private var surveyDetails: SurveyDetails? = null
    private var surveyAnswers: HashMap<Long, SurveyAnswer>? = null

    fun configure(details: SurveyDetails?, answers: HashMap<Long, SurveyAnswer>?) {
        surveyDetails = details
        surveyAnswers = answers

        surveyDetails?.questions?.forEach { question ->
            // Pass some data required by Genex
            surveyAnswers?.get(question.id)?.apply {
                matrix = question.matrix
                column = question.column
                group = question.group

                if (question.type == SurveyQuestion.QuestionType.RATE_SLIDER.type) {
                    // Add back offset removed to draw slider
                    answerId?.let {
                        answerId = it + 1
                    }
                }

                // Set default answer value for required questions
                if (question.required == true) {
                    when (question.type) {
                        SurveyQuestion.QuestionType.RATE_SLIDER.type -> {
                            if (answerId == null) {
                                answerId = (question.maxValue ?: VoiceOfCustomerActivity.DEFAULT_VALUE_RATE_SLIDER_MAX)
                            }
                        }
                        SurveyQuestion.QuestionType.FREE_TEXT.type -> {
                            if (textAnswer == null) {
                                // Validation is already done on UI
                                textAnswer = "N/A"
                            }
                        }
                    }
                }

                if (answerId == null && textAnswer == null) {
                    surveyAnswers?.remove(question.id)
                }
            }
        }
    }

    fun performSubmitSurveyRepliesRequest(onSuccess: () -> Unit, onFailed: () -> Unit) {
        if (surveyDetails == null || surveyAnswers == null) {
            onFailed()
            return
        }
        val submitVocSurveyRepliesRequest = OneAppService.submitVocSurveyReplies(surveyDetails!!, surveyAnswers!!)
        submitVocSurveyRepliesRequest.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onFailed()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                onFailed()
                FirebaseManager.logException(t)
            }
        })
    }
}