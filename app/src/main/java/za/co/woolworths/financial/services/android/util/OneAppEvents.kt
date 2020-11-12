package za.co.woolworths.financial.services.android.util

class OneAppEvents {

    class FeatureName {
        companion object {
            const val ABSA: String = "absa"
            const val WREWARDS_VIEW_VOUCHERS: String = "viewVouchers"
            const val WHATSAPP = "WhatsApp"
            const val CHAT_COLLECTIONS = "chatCollections"
            const val CHAT_CUSTOMER_SERVICES = "chatCustomerServices"
            const val DASH_FEATURE_NAME = "dash"
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

            const val CHAT_OFFLINE_STORE_CARD = "Chat Offline Store Card"
            const val CHAT_OFFLINE_PERSONAL_LOAN = "Chat Offline Personal Loan"
            const val CHAT_OFFLINE_CREDIT_LOAN = "Chat Offline Credit Card"

            const val CHAT_COLLECTIONS_CC_LANDING_INITIATE_SESSION = "CC Landing InitiateSession"
            const val CHAT_COLLECTIONS_SC_LANDING_INITIATE_SESSION = "SC Landing InitiateSession"
            const val CHAT_COLLECTIONS_PL_LANDING_INITIATE_SESSION = "PL Landing InitiateSession"

            const val CHAT_PAY_OPTIONS_PL_LANDING_INITIATE_SESSION = "PL PayOptions InitiateSession"
            const val CHAT_PAY_OPTIONS_CC_LANDING_INITIATE_SESSION = "CC PayOptions InitiateSession"
            const val CHAT_PAY_OPTIONS_SC_LANDING_INITIATE_SESSION = "SC PayOptions InitiateSession"

            const val CHAT_COLLECTIONS_CC_END_SESSION = "CC EndSession"
            const val CHAT_COLLECTIONS_PL_END_SESSION = "PL EndSession"
            const val CHAT_COLLECTIONS_SC_END_SESSION = "SC EndSession"

            const val CHAT_CUSTOMER_SERVICE_CC_TRANSACTIONS_INITIATE_SESSION = "CC Transactions InitiateSession"
            const val CHAT_CUSTOMER_SERVICE_PL_TRANSACTIONS_INITIATE_SESSION = "PL Transactions InitiateSession"
            const val CHAT_CUSTOMER_SERVICE_SC_TRANSACTIONS_INITIATE_SESSION = "SC Transactions InitiateSession"

            const val CHAT_CUSTOMER_SERVICE_CC_STATEMENT_INITIATE_SESSION = "CC Statements InitiateSession"
            const val CHAT_CUSTOMER_SERVICE_PL_STATEMENT_INITIATE_SESSION = "PL Statements InitiateSession"
            const val CHAT_CUSTOMER_SERVICE_SC_STATEMENT_INITIATE_SESSION = "SC Statements InitiateSession"

            const val DASH_BANNER_SCREEN_NAME = "view"
            const val DASH_DOWNLOAD_SCREEN_NAME = "breakout"
        }
    }
}