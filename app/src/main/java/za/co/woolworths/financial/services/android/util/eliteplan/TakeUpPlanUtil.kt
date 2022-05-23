package za.co.woolworths.financial.services.android.util.eliteplan

import android.app.Activity
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.util.Utils

class TakeUpPlanUtil {
    companion object {
        fun takeUpPlanActionAndEventPair(applyNowState: ApplyNowState?):Pair<String,String>{
            when (applyNowState) {
                ApplyNowState.STORE_CARD -> {
                    return Pair(
                        FirebaseManagerAnalyticsProperties.TAKE_UP_TREATMENT_PLAN_SC_ACTION,
                        FirebaseManagerAnalyticsProperties.TAKE_UP_TREATMENT_PLAN_SC
                    )
                }
                ApplyNowState.PERSONAL_LOAN -> {
                    return Pair(
                        FirebaseManagerAnalyticsProperties.TAKE_UP_TREATMENT_PLAN_PL_ACTION,
                        FirebaseManagerAnalyticsProperties.TAKE_UP_TREATMENT_PLAN_PL
                    )
                }
                else -> {
                    return Pair(
                        FirebaseManagerAnalyticsProperties.TAKE_UP_TREATMENT_PLAN_CC_ACTION,
                        FirebaseManagerAnalyticsProperties.TAKE_UP_TREATMENT_PLAN_CC
                    )
                }
            }

        }
        fun takeUpPlanEventLog(applyNowState: ApplyNowState?, activity: Activity) {
            val (action, eventName) = takeUpPlanActionAndEventPair(
                applyNowState
            )
            val arguments = HashMap<String, String>()
            arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION] = action
            Utils.triggerFireBaseEvents(
                eventName,
                arguments,
                activity
            )
        }
    }
}