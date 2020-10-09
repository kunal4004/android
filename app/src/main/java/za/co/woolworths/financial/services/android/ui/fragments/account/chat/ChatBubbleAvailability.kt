package za.co.woolworths.financial.services.android.ui.fragments.account.chat

import android.app.Activity
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.AbsaStatementsActivity
import za.co.woolworths.financial.services.android.ui.activities.StatementActivity
import za.co.woolworths.financial.services.android.ui.activities.WTransactionsActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.util.Utils
import java.util.*

class ChatBubbleAvailability(private var accountList: List<Account>? = null, private val activity: Activity) {

    companion object {
        const val STORE_CARD_PRODUCT_GROUP_CODE = "sc"
        const val PERSONAL_LOAN_PRODUCT_GROUP_CODE = "pl"
        const val CREDIT_CARD_PRODUCT_GROUP_CODE = "cc"
    }

    // config.presenceInAppChat.minimumSupportedAppBuildNumber >= currentAppBuildNumber
    private val isPresenceInAppChatEnabled: Boolean
        get() = WoolworthsApplication.getPresenceInAppChat().isEnabled ?: false

    /**
     * In Accounts Landing: Loop through all the Products returned with the Accounts Response.
     * Show the Chat FAB when any at least one Product has
     * "productOfferingGoodStanding": false && "productOfferingStatus": "ACTIVE"
     */

    fun isChatVisibleForAccountLanding(): Boolean {
        if (!isPresenceInAppChatEnabled) return false

        accountList?.forEach { account ->
            if (!account.productOfferingGoodStanding && account.productOfferingStatus == Utils.ACCOUNT_ACTIVE)
                return true
        }

        return false
    }

    /**
     * In Product/Account Detail: When the Product/Account being viewed has
     * "productOfferingGoodStanding": false && "productOfferingStatus": "ACTIVE"
     */

    fun isChatVisibleForAccountDetailProduct(applyNowState: ApplyNowState): Boolean {
        // Applicable for payment option section
        if (!isPresenceInAppChatEnabled) return false

        val productGroupCode = when (applyNowState) {
            ApplyNowState.STORE_CARD -> STORE_CARD_PRODUCT_GROUP_CODE
            ApplyNowState.BLACK_CREDIT_CARD, ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.SILVER_CREDIT_CARD -> CREDIT_CARD_PRODUCT_GROUP_CODE
            ApplyNowState.PERSONAL_LOAN -> PERSONAL_LOAN_PRODUCT_GROUP_CODE
            else -> ""
        }

        var productGroupCodeAccount: Account? = null
        accountList?.forEach { account ->
            if (account.productGroupCode.toLowerCase(Locale.getDefault()) == productGroupCode) {
                productGroupCodeAccount = account
                return@forEach
            }
        }

        return productGroupCodeAccount?.productOfferingGoodStanding != true && (productGroupCodeAccount?.productOfferingStatus == Utils.ACCOUNT_ACTIVE)
    }

    fun getAccountForProductLandingPage(applyNowState: ApplyNowState): Account? {
        // Applicable for payment option section
        if (!isPresenceInAppChatEnabled) return null

        val productGroupCode = when (applyNowState) {
            ApplyNowState.STORE_CARD -> STORE_CARD_PRODUCT_GROUP_CODE
            ApplyNowState.BLACK_CREDIT_CARD, ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.SILVER_CREDIT_CARD -> CREDIT_CARD_PRODUCT_GROUP_CODE
            ApplyNowState.PERSONAL_LOAN -> PERSONAL_LOAN_PRODUCT_GROUP_CODE
            else -> ""
        }

        var productGroupCodeAccount: Account? = null
        accountList?.forEach { account ->
            if (account.productGroupCode.toLowerCase(Locale.getDefault()) == productGroupCode) {
                productGroupCodeAccount = account
                return@forEach
            }
        }

        return productGroupCodeAccount
    }

    fun getAccountInProductLandingPage(): Account? {

        accountList?.forEach { account ->
            if (!account.productOfferingGoodStanding && account.productOfferingStatus == Utils.ACCOUNT_ACTIVE)
                return account
        }

        return null
    }

    /**
     *  Show the Chat Tip associated with the Floating Action Button (each time when parent screen is shown) in:
     */
    fun shouldPresentChatTooltip(applyNowState: ApplyNowState): Boolean {
        val inAppChatTipAcknowledgements = AppInstanceObject.get().inAppChatTipAcknowledgements
        with(inAppChatTipAcknowledgements) {
            return when (applyNowState) {
                ApplyNowState.ACCOUNT_LANDING -> isChatVisibleForAccountLanding() && !accountsLanding
                ApplyNowState.PERSONAL_LOAN -> isChatVisibleForAccountDetailProduct(applyNowState) && when (getActivityName()) {
                    AccountSignedInActivity::class.java.simpleName -> !personalLoan.landing
                    PayMyAccountActivity::class.java.simpleName -> !personalLoan.paymentOptions
                    WTransactionsActivity::class.java.simpleName -> !personalLoan.transactions
                    AbsaStatementsActivity::class.java.simpleName, StatementActivity::class.java.simpleName -> !personalLoan.statements
                    else -> false
                }

                ApplyNowState.STORE_CARD -> isChatVisibleForAccountDetailProduct(applyNowState) &&
                        when (getActivityName()) {
                            AccountSignedInActivity::class.java.simpleName -> !storeCard.landing
                            PayMyAccountActivity::class.java.simpleName -> !storeCard.paymentOptions
                            WTransactionsActivity::class.java.simpleName -> !storeCard.transactions
                            AbsaStatementsActivity::class.java.simpleName, StatementActivity::class.java.simpleName -> !storeCard.statements
                            else -> false
                        }
                ApplyNowState.SILVER_CREDIT_CARD, ApplyNowState.BLACK_CREDIT_CARD, ApplyNowState.GOLD_CREDIT_CARD -> isChatVisibleForAccountDetailProduct(applyNowState) &&
                        when (getActivityName()) {
                            AccountSignedInActivity::class.java.simpleName -> !creditCard.landing
                            PayMyAccountActivity::class.java.simpleName -> !creditCard.paymentOptions
                            WTransactionsActivity::class.java.simpleName -> !creditCard.transactions
                            AbsaStatementsActivity::class.java.simpleName, StatementActivity::class.java.simpleName -> !creditCard.statements
                            else -> false
                        }
            }
        }
    }

    fun saveInAppChatTooltip(applyNowState: ApplyNowState) {
        val appInstanceObject = AppInstanceObject.get()
        appInstanceObject.inAppChatTipAcknowledgements?.apply {
            when (applyNowState) {
                ApplyNowState.ACCOUNT_LANDING -> accountsLanding = true
                ApplyNowState.PERSONAL_LOAN -> when (getActivityName()) {
                    AccountSignedInActivity::class.java.simpleName -> personalLoan.landing = true
                    PayMyAccountActivity::class.java.simpleName -> personalLoan.paymentOptions = true
                    WTransactionsActivity::class.java.simpleName -> personalLoan.transactions = true
                    AbsaStatementsActivity::class.java.simpleName, StatementActivity::class.java.simpleName -> personalLoan.statements = true
                }
                ApplyNowState.STORE_CARD -> when (getActivityName()) {
                    AccountSignedInActivity::class.java.simpleName -> storeCard.landing = true
                    PayMyAccountActivity::class.java.simpleName -> storeCard.paymentOptions = true
                    WTransactionsActivity::class.java.simpleName -> storeCard.transactions = true
                    AbsaStatementsActivity::class.java.simpleName, StatementActivity::class.java.simpleName -> storeCard.statements = true
                }


                ApplyNowState.SILVER_CREDIT_CARD, ApplyNowState.BLACK_CREDIT_CARD, ApplyNowState.GOLD_CREDIT_CARD -> when (getActivityName()) {
                    AccountSignedInActivity::class.java.simpleName -> creditCard.landing = true
                    PayMyAccountActivity::class.java.simpleName -> creditCard.paymentOptions = true
                    WTransactionsActivity::class.java.simpleName -> creditCard.transactions = true
                    AbsaStatementsActivity::class.java.simpleName, StatementActivity::class.java.simpleName -> creditCard.statements = true
                }
            }
        }
        appInstanceObject.save()
    }

    fun getProductOfferingIdAndAccountNumber(applyNowState: ApplyNowState): Pair<String, String> {
        // Retrieve productOfferingId and cardNumber in account landing page
        if (applyNowState == ApplyNowState.ACCOUNT_LANDING) {
            var productGroupCodeAccount: Account? = null
            accountList?.forEach { account ->
                if (!account.productOfferingGoodStanding && account.productOfferingStatus == Utils.ACCOUNT_ACTIVE) {
                    productGroupCodeAccount = account
                    return@forEach
                }
            }
            val productGroupCode = productGroupCodeAccount?.productGroupCode?.toLowerCase(Locale.getDefault())
                    ?: ""
            return if (productGroupCode == STORE_CARD_PRODUCT_GROUP_CODE || productGroupCode == PERSONAL_LOAN_PRODUCT_GROUP_CODE) Pair(productGroupCodeAccount?.productOfferingId?.toString()
                    ?: "0", productGroupCodeAccount?.accountNumber
                    ?: "") else Pair(productGroupCodeAccount?.productOfferingId?.toString()
                    ?: "0", productGroupCodeAccount?.primaryCard?.cards?.get(0)?.cardNumber ?: "")
        }

        //  Retrieve productOfferingId and cardNumber for other sections
        val productGroupCode = when (applyNowState) {
            ApplyNowState.STORE_CARD -> STORE_CARD_PRODUCT_GROUP_CODE
            ApplyNowState.BLACK_CREDIT_CARD, ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.SILVER_CREDIT_CARD -> CREDIT_CARD_PRODUCT_GROUP_CODE
            ApplyNowState.PERSONAL_LOAN -> PERSONAL_LOAN_PRODUCT_GROUP_CODE
            else -> ""
        }

        var productGroupCodeAccount: Account? = null
        accountList?.forEach { account ->
            if (account.productGroupCode.toLowerCase(Locale.getDefault()) == productGroupCode) {
                productGroupCodeAccount = account
                return@forEach
            }
        }

        return if (applyNowState == ApplyNowState.STORE_CARD || applyNowState == ApplyNowState.PERSONAL_LOAN) Pair(productGroupCodeAccount?.productOfferingId?.toString()
                ?: "0", productGroupCodeAccount?.accountNumber
                ?: "") else Pair(productGroupCodeAccount?.productOfferingId?.toString()
                ?: "0", productGroupCodeAccount?.primaryCard?.cards?.get(0)?.cardNumber ?: "")
    }

    fun getUsername(): String? = ChatCustomerInfo().getUsername()

    fun getActivityName(): String? = activity::class.java.simpleName
}