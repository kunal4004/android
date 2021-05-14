package za.co.woolworths.financial.services.android.ui.fragments.account.helper

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils

class FirebaseEventDetailManager {
    companion object {
        fun tapped(eventName: String) = triggerEvent(hashMapOf(FirebaseManagerAnalyticsProperties.PropertyNames.ACTION to FirebaseManagerAnalyticsProperties.PropertyNames.TAPPED), eventName)

        fun timeout(eventName: String) = triggerEvent(hashMapOf(FirebaseManagerAnalyticsProperties.PropertyNames.FAILED to FirebaseManagerAnalyticsProperties.PropertyNames.TIMEOUT), eventName)

        fun network(eventName: String) = triggerEvent(hashMapOf(FirebaseManagerAnalyticsProperties.PropertyNames.FAILED to FirebaseManagerAnalyticsProperties.PropertyNames.NETWORK), eventName)

        fun undefined(eventName: String) = triggerEvent(hashMapOf(FirebaseManagerAnalyticsProperties.PropertyNames.FAILED to FirebaseManagerAnalyticsProperties.PropertyNames.UNDEFINED), eventName)

        fun pin(eventName: String) = triggerEvent(hashMapOf(FirebaseManagerAnalyticsProperties.PropertyNames.FAILED to FirebaseManagerAnalyticsProperties.PropertyNames.PIN), eventName)

        fun passcode(eventName: String) = triggerEvent(hashMapOf(FirebaseManagerAnalyticsProperties.PropertyNames.FAILED to FirebaseManagerAnalyticsProperties.PropertyNames.PASSCODE), eventName)

        fun success(eventName: String) = triggerEvent(hashMapOf(FirebaseManagerAnalyticsProperties.PropertyNames.SUCCESSFUL to FirebaseManagerAnalyticsProperties.PropertyNames.SUCCESSFUL), eventName)

        private fun triggerEvent(arguments: HashMap<String, String>, eventName: String) {
            SessionUtilities.getInstance()?.jwt?.C2Id?.let { c2id -> arguments[FirebaseManagerAnalyticsProperties.PropertyNames.C2ID] = c2id }
            Utils.triggerFireBaseEvents(eventName, arguments)
        }
    }
}