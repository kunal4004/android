package za.co.woolworths.financial.services.android.util

import com.google.firebase.analytics.FirebaseAnalytics
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.UserPropertiesForDelinquentCodes

class FirebaseAnalyticsUserProperty {

    companion object {
        private const val CREDIT_CARD_PRODUCT_GROUP_CODE = "CC"
        private const val STORE_CARD_PRODUCT_GROUP_CODE = "SC"
        private const val PERSONAL_LOAN_PRODUCT_GROUP_CODE = "PL"
        val firebaseInstance = FirebaseAnalytics.getInstance(WoolworthsApplication.getAppContext())

        @JvmStatic
        fun setUserPropertiesForCardProductOfferings(accountsMap: Map<String, Account?>) {
            firebaseInstance.setUserProperty(FirebaseManagerAnalyticsProperties.PropertyNames.PERSONAL_LOAN_PRODUCT_OFFERING, if (accountsMap.containsKey(PERSONAL_LOAN_PRODUCT_GROUP_CODE)) "true" else "false")
            firebaseInstance.setUserProperty(FirebaseManagerAnalyticsProperties.PropertyNames.STORE_CARD_PRODUCT_OFFERING, if (accountsMap.containsKey(STORE_CARD_PRODUCT_GROUP_CODE)) "true" else "false")
            firebaseInstance.setUserProperty(FirebaseManagerAnalyticsProperties.PropertyNames.SILVER_CREDIT_CARD_PRODUCT_OFFERING, if (accountsMap[CREDIT_CARD_PRODUCT_GROUP_CODE]?.accountNumberBin.equals(Utils.SILVER_CARD, ignoreCase = true)) "true" else "false")
            firebaseInstance.setUserProperty(FirebaseManagerAnalyticsProperties.PropertyNames.GOLD_CREDIT_CARD_PRODUCT_OFFERING, if (accountsMap[CREDIT_CARD_PRODUCT_GROUP_CODE]?.accountNumberBin.equals(Utils.GOLD_CARD, ignoreCase = true)) "true" else "false")
            firebaseInstance.setUserProperty(FirebaseManagerAnalyticsProperties.PropertyNames.BLACK_CREDIT_CARD_PRODUCT_OFFERING, if (accountsMap[CREDIT_CARD_PRODUCT_GROUP_CODE]?.accountNumberBin.equals(Utils.BLACK_CARD, ignoreCase = true)) "true" else "false")
        }

        @JvmStatic
        fun setUserPropertiesDelinquencyCode(accountsMap: Map<String, Account?>) {
            val userPropertiesForDelinquentCodes: HashMap<String, String> = getUserPropertiesForDelinquentCodes()
            for (key in userPropertiesForDelinquentCodes.keys) {
                if (accountsMap.containsKey(key)) {
                    userPropertiesForDelinquentCodes[key]?.let { firebaseInstance.setUserProperty(it, accountsMap[key]?.delinquencyCycle.toString() ?: "N/A") }
                } else {
                    userPropertiesForDelinquentCodes[key]?.let { firebaseInstance.setUserProperty(it, "N/A" ) }
                }
            }
        }

        @JvmStatic
        fun setUserPropertiesDelinquencyCodeForProduct(productCode: String, account: Account?) {

            val userPropertiesForDelinquentCodes: HashMap<String, String> = getUserPropertiesForDelinquentCodes()
            for (key in userPropertiesForDelinquentCodes.keys) {
                if (key.equals(productCode, ignoreCase = true)) {
                    userPropertiesForDelinquentCodes[key]?.let { firebaseInstance.setUserProperty(it, account?.delinquencyCycle.toString() ?: "N/A") }
                    break
                }
            }
        }

        private fun getUserPropertiesForDelinquentCodes(): HashMap<String, String> {
            val userProperty: UserPropertiesForDelinquentCodes? = WoolworthsApplication.getFirebaseUserPropertiesForDelinquentProductGroupCodes()
            val userPropertiesForDelinquentCodes: HashMap<String, String> = HashMap()
            if (userProperty?.cc != null) {
                userPropertiesForDelinquentCodes[CREDIT_CARD_PRODUCT_GROUP_CODE] = userProperty.cc
            } else {
                userPropertiesForDelinquentCodes[CREDIT_CARD_PRODUCT_GROUP_CODE] = "N/A"
            }
            if (userProperty?.sc != null) {
                userPropertiesForDelinquentCodes[STORE_CARD_PRODUCT_GROUP_CODE] = userProperty.sc
            } else {
                userPropertiesForDelinquentCodes[STORE_CARD_PRODUCT_GROUP_CODE] = "N/A"
            }
            if (userProperty?.pl != null) {
                userPropertiesForDelinquentCodes[PERSONAL_LOAN_PRODUCT_GROUP_CODE] = userProperty.pl
            } else {
                userPropertiesForDelinquentCodes[PERSONAL_LOAN_PRODUCT_GROUP_CODE] = "N/A"
            }
            return userPropertiesForDelinquentCodes
        }
    }
}