package za.co.woolworths.financial.services.android.ui.fragments.account.helper

import android.app.Activity
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.activities.AbsaStatementsActivity
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils

class FirebaseEventDetailManager {
    companion object {
        fun tapped(eventName: String, activity: Activity) = triggerEvent(hashMapOf(FirebaseManagerAnalyticsProperties.PropertyNames.ACTION to FirebaseManagerAnalyticsProperties.PropertyNames.TAPPED), eventName, activity)

        fun timeout(eventName: String, activity: Activity) = triggerEvent(hashMapOf(FirebaseManagerAnalyticsProperties.PropertyNames.FAILED to FirebaseManagerAnalyticsProperties.PropertyNames.TIMEOUT), eventName, activity)

        fun network(eventName: String, activity: Activity) = triggerEvent(hashMapOf(FirebaseManagerAnalyticsProperties.PropertyNames.FAILED to FirebaseManagerAnalyticsProperties.PropertyNames.NETWORK), eventName, activity)

        fun undefined(eventName: String, activity: Activity) = triggerEvent(hashMapOf(FirebaseManagerAnalyticsProperties.PropertyNames.FAILED to FirebaseManagerAnalyticsProperties.PropertyNames.UNDEFINED), eventName, activity)

        fun pin(eventName: String, activity: Activity) = triggerEvent(hashMapOf(FirebaseManagerAnalyticsProperties.PropertyNames.FAILED to FirebaseManagerAnalyticsProperties.PropertyNames.PIN), eventName, activity)

        fun passcode(eventName: String, activity: Activity) = triggerEvent(hashMapOf(FirebaseManagerAnalyticsProperties.PropertyNames.FAILED to FirebaseManagerAnalyticsProperties.PropertyNames.PASSCODE), eventName, activity)

        fun success(eventName: String, activity: Activity) = triggerEvent(hashMapOf(FirebaseManagerAnalyticsProperties.PropertyNames.SUCCESSFUL to FirebaseManagerAnalyticsProperties.PropertyNames.SUCCESSFUL), eventName, activity)

        private fun triggerEvent(arguments: HashMap<String, String>, eventName: String, activity: Activity) {
            SessionUtilities.getInstance()?.jwt?.C2Id?.let { c2id -> arguments[FirebaseManagerAnalyticsProperties.PropertyNames.C2ID] = c2id }
            Utils.triggerFireBaseEvents(eventName, arguments, activity)
        }
    }
}