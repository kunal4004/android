package za.co.woolworths.financial.services.android.util

class OneAppEvents {

    class FeatureName {
        companion object {
            const val ABSA: String = "absa"
            const val WREWARDS_VIEW_VOUCHERS: String = "viewVouchers"
            const val WHATSAPP = "WhatsApp"
            const val CHAT_COLLECTIONS = "chatCollections"
            const val CHAT_CUSTOMER_SERVICES = "chatCustomerServices"
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
            const val CHAT_ACCOUNT_CC_LANDING_INITIATE_SESSION = "CC Landing InitiateSession"
            const val CHAT_PAY_OPTIONS_CC_LANDING_INITIATE_SESSION = "CC PayOptions InitiateSession"
            const val CHAT_COLLECTIONS_CC_END_SESSION = "CC EndSession"
            const val CHAT_COLLECTIONS_CC_Transactions_InitiateSession = "CC Transactions InitiateSession"


        }
    }
}