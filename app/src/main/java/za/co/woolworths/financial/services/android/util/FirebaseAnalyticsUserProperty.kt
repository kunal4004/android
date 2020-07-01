package za.co.woolworths.financial.services.android.util

import com.google.firebase.analytics.FirebaseAnalytics
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.Account

class FirebaseAnalyticsUserProperty {

    companion object {
        private const val CREDIT_CARD_PRODUCT_GROUP_CODE = "CC"
        private const val STORE_CARD_PRODUCT_GROUP_CODE = "SC"
        private const val PERSONAL_LOAN_PRODUCT_GROUP_CODE = "PL"

        fun setUserPropertiesForCardProductOfferings(accountsMap: Map<String, Account?>) {
            val firebaseInstance = FirebaseAnalytics.getInstance(WoolworthsApplication.getAppContext())
            firebaseInstance.setUserProperty(FirebaseManagerAnalyticsProperties.PropertyNames.PERSONAL_LOAN_PRODUCT_OFFERING, if (accountsMap.containsKey(PERSONAL_LOAN_PRODUCT_GROUP_CODE)) "true" else "false")
            firebaseInstance.setUserProperty(FirebaseManagerAnalyticsProperties.PropertyNames.STORE_CARD_PRODUCT_OFFERING, if (accountsMap.containsKey(STORE_CARD_PRODUCT_GROUP_CODE)) "true" else "false")
            firebaseInstance.setUserProperty(FirebaseManagerAnalyticsProperties.PropertyNames.SILVER_CREDIT_CARD_PRODUCT_OFFERING, if (accountsMap[CREDIT_CARD_PRODUCT_GROUP_CODE]?.accountNumberBin.equals(Utils.SILVER_CARD, ignoreCase = true)) "true" else "false")
            firebaseInstance.setUserProperty(FirebaseManagerAnalyticsProperties.PropertyNames.GOLD_CREDIT_CARD_PRODUCT_OFFERING, if (accountsMap[CREDIT_CARD_PRODUCT_GROUP_CODE]?.accountNumberBin.equals(Utils.GOLD_CARD, ignoreCase = true)) "true" else "false")
            firebaseInstance.setUserProperty(FirebaseManagerAnalyticsProperties.PropertyNames.BLACK_CREDIT_CARD_PRODUCT_OFFERING, if (accountsMap[CREDIT_CARD_PRODUCT_GROUP_CODE]?.accountNumberBin.equals(Utils.BLACK_CARD, ignoreCase = true)) "true" else "false")
        }
    }
}