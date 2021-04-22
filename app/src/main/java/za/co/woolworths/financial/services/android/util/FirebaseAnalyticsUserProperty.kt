package za.co.woolworths.financial.services.android.util

import android.annotation.SuppressLint
import com.google.firebase.analytics.FirebaseAnalytics
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.UserPropertiesForDelinquentCodes
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.account.Products

class FirebaseAnalyticsUserProperty : FirebaseManagerAnalyticsProperties() {

    companion object {
        private const val CREDIT_CARD_PRODUCT_GROUP_CODE = "CC"
        private const val STORE_CARD_PRODUCT_GROUP_CODE = "SC"
        private const val PERSONAL_LOAN_PRODUCT_GROUP_CODE = "PL"
        private const val SC_PAYMENT_DUE_DATE = PropertyNames.SC_PAYMENT_DUE_DATE
        private const val CC_PAYMENT_DUE_DATE = PropertyNames.CC_PAYMENT_DUE_DATE
        private const val PL_PAYMENT_DUE_DATE = PropertyNames.PL_PAYMENT_DUE_DATE
        private const val SC_DEBIT_ORDER = PropertyNames.SC_DEBIT_ORDER
        private const val CC_DEBIT_ORDER = PropertyNames.CC_DEBIT_ORDER
        private const val PL_DEBIT_ORDER = PropertyNames.PL_DEBIT_ORDER
        private val firebaseInstance = FirebaseAnalytics.getInstance(WoolworthsApplication.getAppContext())

        fun setUserPropertiesForCardProductOfferings(accountsMap: Map<String, Account?>) {
            firebaseInstance.setUserProperty(PropertyNames.PERSONAL_LOAN_PRODUCT_OFFERING, if (accountsMap.containsKey(PERSONAL_LOAN_PRODUCT_GROUP_CODE)) "true" else "false")
            firebaseInstance.setUserProperty(PropertyNames.STORE_CARD_PRODUCT_OFFERING, if (accountsMap.containsKey(STORE_CARD_PRODUCT_GROUP_CODE)) "true" else "false")
            firebaseInstance.setUserProperty(PropertyNames.SILVER_CREDIT_CARD_PRODUCT_OFFERING, if (accountsMap[CREDIT_CARD_PRODUCT_GROUP_CODE]?.accountNumberBin.equals(Utils.SILVER_CARD, ignoreCase = true)) "true" else "false")
            firebaseInstance.setUserProperty(PropertyNames.GOLD_CREDIT_CARD_PRODUCT_OFFERING, if (accountsMap[CREDIT_CARD_PRODUCT_GROUP_CODE]?.accountNumberBin.equals(Utils.GOLD_CARD, ignoreCase = true)) "true" else "false")
            firebaseInstance.setUserProperty(PropertyNames.BLACK_CREDIT_CARD_PRODUCT_OFFERING, if (accountsMap[CREDIT_CARD_PRODUCT_GROUP_CODE]?.accountNumberBin.equals(Utils.BLACK_CARD, ignoreCase = true)) "true" else "false")
        }

        fun setUserPropertiesDelinquencyCode(accountsMap: Map<String, Account?>) {
            val userPropertiesForDelinquentCodes: HashMap<String, String> = getUserPropertiesForDelinquentCodes()
            for (key in userPropertiesForDelinquentCodes.keys) {
                if (accountsMap.containsKey(key)) {
                    userPropertiesForDelinquentCodes[key]?.let {
                        firebaseInstance.setUserProperty(it, accountsMap[key]?.delinquencyCycle?.toString()
                                ?: "N/A")
                    }
                } else {
                    userPropertiesForDelinquentCodes[key]?.let { firebaseInstance.setUserProperty(it, "N/A") }
                }
            }
        }

        fun setUserPropertiesDelinquencyCodeForProduct(productCode: String, account: Account?) {

            val userPropertiesForDelinquentCodes: HashMap<String, String> = getUserPropertiesForDelinquentCodes()
            for (key in userPropertiesForDelinquentCodes.keys) {
                if (key.equals(productCode, ignoreCase = true)) {
                    userPropertiesForDelinquentCodes[key]?.let {
                        firebaseInstance.setUserProperty(it, account?.delinquencyCycle?.toString()
                                ?: "N/A")
                    }
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

        /**
         * WOP-10669 : As a Collections manager I would like to campaign to app users
         * based on their payment due date (user property)
         * My Accounts -> Total Due > 0 && Debit order active == false -> set the {{productGroupCode}}PaymentDueDate = ‘payment due date from my accounts’
         */

        @SuppressLint("DefaultLocale")
        fun setUserPropertiesPreDelinquencyPaymentDueDate(accountsMap: HashMap<Products, Account?>?) {
            accountsMap?.forEach { (_, account) ->
                account?.apply {
                    val paymentDueDate = paymentDueDate?.toString() ?: "N/A"
                    val debitOrderActive = debitOrder?.debitOrderActive ?: false
                    val paymentDueDateKey = propertyKey(productGroupCode, listOf(SC_PAYMENT_DUE_DATE, CC_PAYMENT_DUE_DATE, PL_PAYMENT_DUE_DATE))

                    if (totalAmountDue > 0 && !debitOrderActive) {
                        firebaseInstance.setUserProperty(paymentDueDateKey, paymentDueDate)
                    }
                }
            }
        }

        @SuppressLint("DefaultLocale")
        fun setUserPropertiesOnRetryPreDelinquencyPaymentDueDate(productCode: String?, account: Account?) {
            account?.apply {
                val paymentDueDate = paymentDueDate?.toString() ?: "N/A"
                val debitOrderActive = debitOrder?.debitOrderActive ?: false
                val paymentDueDateKey = propertyKey(productCode, listOf(SC_PAYMENT_DUE_DATE, CC_PAYMENT_DUE_DATE, PL_PAYMENT_DUE_DATE))

                if (totalAmountDue > 0 && !debitOrderActive) {
                    firebaseInstance.setUserProperty(paymentDueDateKey, paymentDueDate)
                }
            }
        }


        /***
         * WOP-10667 - As a Collections manager I would like to identify app users with an active debit order (user property)
         */
        fun setUserPropertiesPreDelinquencyForDebitOrder(accountsMap: HashMap<Products, Account?>?) {
            accountsMap?.forEach { (_, account) ->
                account?.apply {
                    val debitOrderActive = debitOrder?.debitOrderActive?.toString() ?: "false"
                    val debitOrderKey = propertyKey(productGroupCode, listOf(SC_DEBIT_ORDER, CC_DEBIT_ORDER, PL_DEBIT_ORDER))
                    firebaseInstance.setUserProperty(debitOrderKey, debitOrderActive)
                }
            }
        }

        fun setUserPropertiesOnRetryPreDelinquencyDebitOrder(productCode: String?, account: Account?) {
            account?.apply {
                val debitOrderActive = debitOrder?.debitOrderActive?.toString() ?: "false"
                val debitOrderKey = propertyKey(productCode, listOf(SC_DEBIT_ORDER, CC_DEBIT_ORDER, PL_DEBIT_ORDER))
                firebaseInstance.setUserProperty(debitOrderKey, debitOrderActive)
            }
        }

        @SuppressLint("DefaultLocale")
        fun propertyKey(productGroupCode: String?, mutableList: List<String> = listOf()): String {
            return when (productGroupCode?.toUpperCase()) {
                AccountsProductGroupCode.STORE_CARD.groupCode -> mutableList[0]
                AccountsProductGroupCode.CREDIT_CARD.groupCode -> mutableList[1]
                else -> mutableList[2]
            }
        }
    }
}