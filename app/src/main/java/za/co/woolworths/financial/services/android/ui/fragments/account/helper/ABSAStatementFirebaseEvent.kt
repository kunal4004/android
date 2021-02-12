package za.co.woolworths.financial.services.android.ui.fragments.account.helper

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils

class ABSAStatementFirebaseEvent {
    companion object {
        fun tapped() {
            val arguments = HashMap<String, String>()
            arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION] = FirebaseManagerAnalyticsProperties.PropertyNames.TAPPED
            triggerEvent(arguments)
        }

        fun timeout() {
            val arguments = HashMap<String, String>()
            arguments[FirebaseManagerAnalyticsProperties.PropertyNames.FAILED] = FirebaseManagerAnalyticsProperties.PropertyNames.TIMEOUT
            triggerEvent(arguments)
        }

        fun network() {
            val arguments = HashMap<String, String>()
            arguments[FirebaseManagerAnalyticsProperties.PropertyNames.FAILED] = FirebaseManagerAnalyticsProperties.PropertyNames.NETWORK
            triggerEvent(arguments)
        }

        fun undefined() {
            val arguments = HashMap<String, String>()
            arguments[FirebaseManagerAnalyticsProperties.PropertyNames.FAILED] = FirebaseManagerAnalyticsProperties.PropertyNames.UNDEFINED
            triggerEvent(arguments)
        }

        fun pin() {
            val arguments = HashMap<String, String>()
            arguments[FirebaseManagerAnalyticsProperties.PropertyNames.FAILED] = FirebaseManagerAnalyticsProperties.PropertyNames.PIN
            triggerEvent(arguments)
        }

        fun passcode() {
            val arguments = HashMap<String, String>()
            arguments[FirebaseManagerAnalyticsProperties.PropertyNames.FAILED] = FirebaseManagerAnalyticsProperties.PropertyNames.PASSCODE
            triggerEvent(arguments)
        }

        fun success() {
            val arguments = HashMap<String, String>()
            arguments[FirebaseManagerAnalyticsProperties.PropertyNames.SUCCESSFUL] = FirebaseManagerAnalyticsProperties.PropertyNames.SUCCESSFUL
            triggerEvent(arguments)
        }

        private fun triggerEvent(arguments: HashMap<String, String>) {
            SessionUtilities.getInstance()?.jwt?.C2Id?.let { c2id -> arguments[FirebaseManagerAnalyticsProperties.PropertyNames.C2ID] = c2id }
            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.ABSA_CC_VIEW_STATEMENTS, arguments)
        }
    }
}