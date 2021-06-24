package za.co.woolworths.financial.services.android.util.voc

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyDetails
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyDetailsResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.voc.VoiceOfCustomerActivity
import za.co.woolworths.financial.services.android.util.wenum.VocTriggerEvent

class VoiceOfCustomerManager {
    companion object {
        fun showVocSurveyIfNeeded(context: Context?) {
            val getVocSurveyRequest = OneAppService.getVocSurvey(VocTriggerEvent.PL_STATEMENT_CHAT)
            getVocSurveyRequest.enqueue(CompletionHandler(object : IResponseListener<SurveyDetailsResponse> {
                override fun onSuccess(response: SurveyDetailsResponse?) {
                    response?.survey?.let {
                        showVocSurvey(context, it)
                    }
                }

                override fun onFailure(error: Throwable?) {
                    // ignored if request failed
                }
            }, SurveyDetailsResponse::class.java))
        }

        private fun showVocSurvey(context: Context?, survey: SurveyDetails) {
            if (survey.questions.isEmpty()) return
            context?.apply {
                Intent(this, VoiceOfCustomerActivity::class.java).apply {
                    putExtra(VoiceOfCustomerActivity.EXTRA_SURVEY_DETAILS, survey)
                    startActivity(this)
                }
            }
        }
    }
}