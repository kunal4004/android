package za.co.woolworths.financial.services.android.util

class OneAppEvents {

    class FeatureName {
        companion object {
            const val ABSA: String = "absa"
            const val WREWARDS_VIEW_VOUCHERS: String = "viewVouchers"
            const val WHATSAPP = "WhatsApp"
        }
    }

    class AppScreen {
        companion object {
            const val ABSA_GET_STATEMENT: String = "getStatement"
            const val ABSA_GET_ALL_STATEMENTS: String = "getAllStatements"
            const val ABSA_REGISTRATION_SUCCESS: String = "registrationSuccess"
            const val ABSA_VIEW_STATEMENT: String = "viewStatement"
            const val ABSA_SHARE_STATEMENT: String = "shareStatement"

            const val WREWARDS: String = "wRewards"
            const val CONTACT_US = "Contact Us"
        }
    }
}