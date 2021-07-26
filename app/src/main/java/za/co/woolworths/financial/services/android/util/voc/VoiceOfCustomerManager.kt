package za.co.woolworths.financial.services.android.util.voc

import android.content.Context
import android.content.Intent
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyDetails
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyDetailsResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.voc.VoiceOfCustomerActivity
import za.co.woolworths.financial.services.android.util.wenum.VocTriggerEvent

class VoiceOfCustomerManager {
    companion object {
        fun showVocSurveyIfNeeded(context: Context?, triggerEvent: VocTriggerEvent? = null) {
            // TODO VOC: check minimum support from App Config
            if (triggerEvent == null) return
            val allowedTriggerEvents = WoolworthsApplication.getCustomerFeedback().triggerEvents ?: return
            if (!allowedTriggerEvents.contains(triggerEvent.value)) return

            val getVocSurveyRequest = OneAppService.getVocSurvey(triggerEvent)
            getVocSurveyRequest.enqueue(CompletionHandler(object : IResponseListener<SurveyDetailsResponse> {
                override fun onSuccess(response: SurveyDetailsResponse?) {
                    response?.survey?.let {
                        showVocSurvey(context, triggerEvent, it)
                    }
                }

                override fun onFailure(error: Throwable?) {
                    // ignored if request fails
                }
            }, SurveyDetailsResponse::class.java))
        }

        private fun showVocSurvey(context: Context?, triggerEvent: VocTriggerEvent, survey: SurveyDetails) {
            if (survey.questions == null || survey.questions.isEmpty()) return
            context?.apply {
                Intent(this, VoiceOfCustomerActivity::class.java).apply {
                    putExtra(VoiceOfCustomerActivity.EXTRA_SURVEY_DETAILS, survey)
                    putExtra(VoiceOfCustomerActivity.EXTRA_SURVEY_TRIGGER_EVENT, triggerEvent)
                    startActivity(this)
                }
            }
        }
    }
}