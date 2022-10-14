package za.co.woolworths.financial.services.android.util.analytics

import android.annotation.SuppressLint
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.account.Products
import za.co.woolworths.financial.services.android.models.dto.app_config.defaults.ConfigUserPropertiesForDelinquentCodes
import za.co.woolworths.financial.services.android.util.Utils

class FirebaseAnalyticsUserProperty : FirebaseManagerAnalyticsProperties() {

    companion object {
        private const val CREDIT_CARD_PRODUCT_GROUP_CODE = "CC"
        private const val STORE_CARD_PRODUCT_GROUP_CODE = "SC"
        private const val PERSONAL_LOAN_PRODUCT_GROUP_CODE = "PL"

        private val accountDebitOrderActivePropertyList = Triple(PropertyNames.SC_DEBIT_ORDER, PropertyNames.CC_DEBIT_ORDER, PropertyNames.PL_DEBIT_ORDER)
        private val accountPaymentDueDatePropertyList = Triple(PropertyNames.SC_PAYMENT_DUE_DATE, PropertyNames.CC_PAYMENT_DUE_DATE, PropertyNames.PL_PAYMENT_DUE_DATE)

        fun setUserPropertiesForCardProductOfferings(accountsMap: Map<String, Account?>) {
            AnalyticsManager.setUserProperty(PropertyNames.PERSONAL_LOAN_PRODUCT_OFFERING, if (accountsMap.containsKey(
                    PERSONAL_LOAN_PRODUCT_GROUP_CODE
                )) "true" else "false")
            AnalyticsManager.setUserProperty(PropertyNames.STORE_CARD_PRODUCT_OFFERING, if (accountsMap.containsKey(
                    STORE_CARD_PRODUCT_GROUP_CODE
                )) "true" else "false")
            AnalyticsManager.setUserProperty(PropertyNames.SILVER_CREDIT_CARD_PRODUCT_OFFERING, if (accountsMap[CREDIT_CARD_PRODUCT_GROUP_CODE]?.accountNumberBin.equals(
                    Utils.SILVER_CARD, ignoreCase = true)) "true" else "false")
            AnalyticsManager.setUserProperty(PropertyNames.GOLD_CREDIT_CARD_PRODUCT_OFFERING, if (accountsMap[CREDIT_CARD_PRODUCT_GROUP_CODE]?.accountNumberBin.equals(
                    Utils.GOLD_CARD, ignoreCase = true)) "true" else "false")
            AnalyticsManager.setUserProperty(PropertyNames.BLACK_CREDIT_CARD_PRODUCT_OFFERING, if (accountsMap[CREDIT_CARD_PRODUCT_GROUP_CODE]?.accountNumberBin.equals(
                    Utils.BLACK_CARD, ignoreCase = true)) "true" else "false")
        }

        fun setUserPropertiesDelinquencyCode(accountsMap: Map<String, Account?>) {
            val userPropertiesForDelinquentCodes: HashMap<String, String> = getUserPropertiesForDelinquentCodes()
            for (key in userPropertiesForDelinquentCodes.keys) {
                if (accountsMap.containsKey(key)) {
                    userPropertiesForDelinquentCodes[key]?.let {
                        AnalyticsManager.setUserProperty(it, accountsMap[key]?.delinquencyCycle?.toString()
                                ?: "N/A")
                    }
                } else {
                    userPropertiesForDelinquentCodes[key]?.let { AnalyticsManager.setUserProperty(it, "N/A") }
                }
            }
        }

        fun setUserPropertiesDelinquencyCodeForProduct(productCode: String, account: Account?) {
            val userPropertiesForDelinquentCodes: HashMap<String, String> = getUserPropertiesForDelinquentCodes()
            for (key in userPropertiesForDelinquentCodes.keys) {
                if (key.equals(productCode, ignoreCase = true)) {
                    userPropertiesForDelinquentCodes[key]?.let {
                        AnalyticsManager.setUserProperty(it, account?.delinquencyCycle?.toString()
                                ?: "N/A")
                    }
                    break
                }
            }
        }

        private fun getUserPropertiesForDelinquentCodes(): HashMap<String, String> {
            val userProperty: ConfigUserPropertiesForDelinquentCodes? = AppConfigSingleton.firebaseUserPropertiesForDelinquentProductGroupCodes
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
                    val paymentDueDateKey = propertyKey(productGroupCode, accountPaymentDueDatePropertyList)

                    if (totalAmountDue > 0 && !debitOrderActive) {
                        AnalyticsManager.setUserProperty(paymentDueDateKey, paymentDueDate)
                    }
                }
            }
        }

        @SuppressLint("DefaultLocale")
        fun setUserPropertiesOnRetryPreDelinquencyPaymentDueDate(productGroupCode: String?, account: Account?) {
            account?.apply {
                val paymentDueDate = paymentDueDate?.toString() ?: "N/A"
                val debitOrderActive = debitOrder?.debitOrderActive ?: false
                val paymentDueDateKey = propertyKey(productGroupCode, accountPaymentDueDatePropertyList)

                if (totalAmountDue > 0 && !debitOrderActive) {
                    AnalyticsManager.setUserProperty(paymentDueDateKey, paymentDueDate)
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
                    val debitOrderKey = propertyKey(productGroupCode, accountDebitOrderActivePropertyList)
                    AnalyticsManager.setUserProperty(debitOrderKey, debitOrderActive)
                }
            }
        }

        fun setUserPropertiesOnRetryPreDelinquencyDebitOrder(productGroupCode: String?, account: Account?) {
            account?.apply {
                val debitOrderActive = debitOrder?.debitOrderActive?.toString() ?: "false"
                val debitOrderKey = propertyKey(productGroupCode, accountDebitOrderActivePropertyList)
                AnalyticsManager.setUserProperty(debitOrderKey, debitOrderActive)
            }
        }

        @SuppressLint("DefaultLocale")
        fun propertyKey(productGroupCode: String?, keys: Triple<String, String, String>): String {
            return when (productGroupCode?.toUpperCase()) {
                AccountsProductGroupCode.STORE_CARD.groupCode -> keys.first
                AccountsProductGroupCode.CREDIT_CARD.groupCode -> keys.second
                else -> keys.third
            }
        }
    }
}