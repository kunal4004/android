package za.co.woolworths.financial.services.android.util.voc

import android.content.Context
import android.content.Intent
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyDetails
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyDetailsResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.voc.VoiceOfCustomerActivity
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.wenum.VocTriggerEvent

class VoiceOfCustomerManager {
    companion object {
        var pendingTriggerEvent: VocTriggerEvent? = null

        fun showPendingSurveyIfNeeded(context: Context?) {
            pendingTriggerEvent?.let {
                showVocSurveyIfNeeded(context, it)
                pendingTriggerEvent = null
            }
        }

        fun showVocSurveyIfNeeded(context: Context?, triggerEvent: VocTriggerEvent? = null) {
            if (triggerEvent == null) return

            // Check minimum app version support
            if (!Utils.isFeatureEnabled(AppConfigSingleton.customerFeedback?.minimumSupportedAppBuildNumber)) return

            // Check for allowed trigger events
            val allowedTriggerEvents = AppConfigSingleton.customerFeedback?.triggerEvents ?: return
            if (!allowedTriggerEvents.contains(triggerEvent.value)) return

            val getVocSurveyRequest = OneAppService().getVocSurvey(triggerEvent)
            getVocSurveyRequest.enqueue(CompletionHandler(object : IResponseListener<SurveyDetailsResponse> {
                override fun onSuccess(response: SurveyDetailsResponse?) {
                    response?.survey?.let {
                        showVocSurvey(context, it)
                    }
                }

                override fun onFailure(error: Throwable?) {
                    // Ignored if request fails
                    FirebaseManager.logException(error)
                }
            }, SurveyDetailsResponse::class.java))
        }

        fun showVocSurvey(context: Context?, survey: SurveyDetails) {
            if (survey.questions == null || survey.questions.isEmpty()) return
            context?.apply {
                Intent(this, VoiceOfCustomerActivity::class.java).apply {
                    putExtra(VoiceOfCustomerActivity.EXTRA_SURVEY_DETAILS, survey)
                    startActivity(this)
                }
            }
        }
    }
}