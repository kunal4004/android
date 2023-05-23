package za.co.woolworths.financial.services.android.ui.wfs.core


import android.annotation.SuppressLint
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode import za.co.woolworths.financial.services.android.models.dto.app_config.defaults.ConfigUserPropertiesForDelinquentCodes
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.model.ProductDetails
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.model.UserAccountResponse
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.AnalyticsManager
import javax.inject.Inject

interface IFirebaseAnalyticsUserProperty {
    fun handleUserPropertiesOnRetryProduct(productGroupCode: String?, account : ProductDetails?)
    fun handleUserPropertiesOnGetAccountResponseSuccess(userAccountResponse: UserAccountResponse?)
}

class FirebaseAnalyticsUserProperty @Inject constructor() : FirebaseManagerAnalyticsProperties() , IFirebaseAnalyticsUserProperty{

    companion object {
        private const val CREDIT_CARD_PRODUCT_GROUP_CODE = "CC"
        private const val STORE_CARD_PRODUCT_GROUP_CODE = "SC"
        private const val PERSONAL_LOAN_PRODUCT_GROUP_CODE = "PL"
    }

    private val accountDebitOrderActivePropertyList = Triple(
        PropertyNames.SC_DEBIT_ORDER,
        PropertyNames.CC_DEBIT_ORDER,
        PropertyNames.PL_DEBIT_ORDER
    )
    private val accountPaymentDueDatePropertyList = Triple(
        PropertyNames.SC_PAYMENT_DUE_DATE,
        PropertyNames.CC_PAYMENT_DUE_DATE,
        PropertyNames.PL_PAYMENT_DUE_DATE
    )

    private fun setUserPropertiesForCardProductOfferings(accountsMap: Map<String, ProductDetails?>) {
        val cardProductOfferings = mapOf(
            PropertyNames.PERSONAL_LOAN_PRODUCT_OFFERING to accountsMap.containsKey(PERSONAL_LOAN_PRODUCT_GROUP_CODE).toString(),
            PropertyNames.STORE_CARD_PRODUCT_OFFERING to accountsMap.containsKey(STORE_CARD_PRODUCT_GROUP_CODE).toString(),
            PropertyNames.SILVER_CREDIT_CARD_PRODUCT_OFFERING to accountsMap[CREDIT_CARD_PRODUCT_GROUP_CODE]?.accountNumberBin.equals(Utils.SILVER_CARD, ignoreCase = true).toString(),
            PropertyNames.GOLD_CREDIT_CARD_PRODUCT_OFFERING to accountsMap[CREDIT_CARD_PRODUCT_GROUP_CODE]?.accountNumberBin.equals(Utils.GOLD_CARD, ignoreCase = true).toString(),
            PropertyNames.BLACK_CREDIT_CARD_PRODUCT_OFFERING to accountsMap[CREDIT_CARD_PRODUCT_GROUP_CODE]?.accountNumberBin.equals(Utils.BLACK_CARD, ignoreCase = true).toString()
        )

        cardProductOfferings.forEach { (propertyName, propertyValue) ->
            AnalyticsManager.setUserProperty(propertyName, propertyValue)
        }
    }

    private fun setUserPropertiesDelinquencyCode(accountsMap: Map<String, ProductDetails?>) {
        val userPropertiesForDelinquentCodes = getUserPropertiesForDelinquentCodes()
        userPropertiesForDelinquentCodes.forEach { (key, value) ->
            val delinquencyCycle = accountsMap[key]?.delinquencyCycle?.toString() ?: "N/A"
            AnalyticsManager.setUserProperty(value, delinquencyCycle)
        }
    }

    private fun getUserPropertiesForDelinquentCodes(): HashMap<String, String> {
        val userProperty: ConfigUserPropertiesForDelinquentCodes? = AppConfigSingleton.firebaseUserPropertiesForDelinquentProductGroupCodes
        val notFound = "N/A"
        return hashMapOf(CREDIT_CARD_PRODUCT_GROUP_CODE to (userProperty?.cc ?: notFound) ,
            STORE_CARD_PRODUCT_GROUP_CODE to (userProperty?.sc ?: notFound),
            PERSONAL_LOAN_PRODUCT_GROUP_CODE to (userProperty?.pl ?: notFound)
        )
    }
    private fun setUserPropertiesDelinquencyCodeForProduct(productGroupCode: String?, account: ProductDetails?) {
        val userPropertiesForDelinquentCodes: HashMap<String, String> = getUserPropertiesForDelinquentCodes()
        for (key in userPropertiesForDelinquentCodes.keys) {
            if (key.equals(productGroupCode, ignoreCase = true)) {
                userPropertiesForDelinquentCodes[key]?.let {
                    AnalyticsManager.setUserProperty(it, account?.delinquencyCycle?.toString() ?: "N/A")
                }
                break
            }
        }
    }

    /**
     * WOP-10669 : As a Collections manager I would like to campaign to app users
     * based on their payment due date (user property)
     * My Accounts -> Total Due > 0 && Debit order active == false -> set the {{productGroupCode}}PaymentDueDate = ‘payment due date from my accounts’
     */

    @SuppressLint("DefaultLocale")
    fun setUserPropertiesPreDelinquencyPaymentDueDate(productDetails: ProductDetails?) {
        productDetails?.apply {
                val paymentDueDate = paymentDueDate ?: "N/A"
                val debitOrderActive = debitOrder?.debitOrderActive ?: false
                val paymentDueDateKey = propertyKey(productGroupCode, accountPaymentDueDatePropertyList)

                if ((totalAmountDue ?: 0) > 0 && !debitOrderActive) {
                    AnalyticsManager.setUserProperty(paymentDueDateKey, paymentDueDate)
                }
            }
    }


    @SuppressLint("DefaultLocale")
    fun setUserPropertiesOnRetryPreDelinquencyPaymentDueDate(
        productGroupCode: String?,
        account: ProductDetails?
    ) {
        account?.apply {
            val paymentDueDate = paymentDueDate ?: "N/A"
            val debitOrderActive = debitOrder?.debitOrderActive ?: false
            val paymentDueDateKey = propertyKey(productGroupCode, accountPaymentDueDatePropertyList)

            if ((totalAmountDue ?: 0) > 0 && !debitOrderActive) {
                AnalyticsManager.setUserProperty(paymentDueDateKey, paymentDueDate)
            }
        }
    }


    /***
     * WOP-10667 - As a Collections manager I would like to identify app users with an active debit order (user property)
     */
    private fun setUserPropertiesPreDelinquencyForDebitOrder(productDetails : ProductDetails?) {
        productDetails?.apply {
                val debitOrderActive = debitOrder?.debitOrderActive?.toString() ?: "false"
                val debitOrderKey =
                    propertyKey(productGroupCode, accountDebitOrderActivePropertyList)
                AnalyticsManager.setUserProperty(debitOrderKey, debitOrderActive)
            }
    }

    private fun setUserPropertiesOnRetryPreDelinquencyDebitOrder(
        productGroupCode: String?,
        account: ProductDetails?
    ) {
        account?.apply {
            val debitOrderActive = debitOrder?.debitOrderActive?.toString() ?: "false"
            val debitOrderKey = propertyKey(productGroupCode, accountDebitOrderActivePropertyList)
            AnalyticsManager.setUserProperty(debitOrderKey, debitOrderActive)
        }
    }

    @SuppressLint("DefaultLocale")
    fun propertyKey(productGroupCode: String?, keys: Triple<String, String, String>): String {
        return when (productGroupCode?.uppercase()) {
            AccountsProductGroupCode.STORE_CARD.groupCode -> keys.first
            AccountsProductGroupCode.CREDIT_CARD.groupCode -> keys.second
            else -> keys.third
        }
    }

    override fun handleUserPropertiesOnRetryProduct(
        productGroupCode: String?,
        account: ProductDetails?
    ) {
        //set Firebase user property when retry reload specific product
        setUserPropertiesOnRetryPreDelinquencyPaymentDueDate(productGroupCode, account)
        setUserPropertiesDelinquencyCodeForProduct(productGroupCode, account)
        setUserPropertiesOnRetryPreDelinquencyDebitOrder(productGroupCode, account)
    }

    override fun handleUserPropertiesOnGetAccountResponseSuccess(userAccountResponse: UserAccountResponse?) {
        userAccountResponse?.let { response ->
            val accountList = response.accountList
            val productsList = response.products

            val productMap = productsList?.associateBy { it.productGroupCode }
            val accountMap = accountList?.associateBy { it.productGroupCode }

            val groupedProducts = productMap?.mapValues { (_, product) ->
                accountMap?.get(product.productGroupCode)?.let { account ->
                    setUserPropertiesPreDelinquencyPaymentDueDate(account)
                    setUserPropertiesPreDelinquencyForDebitOrder(account)
                    account
                }
            }

            groupedProducts?.let {
                setUserPropertiesForCardProductOfferings(it)
                setUserPropertiesDelinquencyCode(it)}
        }
    }

}