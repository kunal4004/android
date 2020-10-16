package za.co.woolworths.financial.services.android.ui.fragments.account.chat

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.wenum.ActivityType

class ChatTrackFirebaseEvent {

    fun chatOnline(applyNowState: ApplyNowState, activityType: ActivityType?) {
        val propertyName: String? = when (activityType) {
            ActivityType.ACCOUNT_LANDING, ActivityType.PRODUCT_LANDING -> when (applyNowState) {
                ApplyNowState.STORE_CARD -> FirebaseManagerAnalyticsProperties.SC_MYACCOUNTS_CHAT_ONLINE
                ApplyNowState.PERSONAL_LOAN -> FirebaseManagerAnalyticsProperties.PL_MYACCOUNTS_CHAT_ONLINE
                ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.BLACK_CREDIT_CARD, ApplyNowState.SILVER_CREDIT_CARD -> FirebaseManagerAnalyticsProperties.CC_MYACCOUNTS_CHAT_ONLINE
                else -> return
            }

            ActivityType.PAYMENT_OPTIONS -> when (applyNowState) {
                ApplyNowState.STORE_CARD -> FirebaseManagerAnalyticsProperties.SC_PAYMENT_OPTIONS_CHAT_ONLINE
                ApplyNowState.PERSONAL_LOAN -> FirebaseManagerAnalyticsProperties.PL_PAYMENT_OPTIONS_CHAT_ONLINE
                ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.BLACK_CREDIT_CARD, ApplyNowState.SILVER_CREDIT_CARD -> FirebaseManagerAnalyticsProperties.CC_PAYMENT_OPTIONS_CHAT_ONLINE
                else -> return
            }

            ActivityType.TRANSACTION -> when (applyNowState) {
                ApplyNowState.STORE_CARD -> FirebaseManagerAnalyticsProperties.SC_TRANSACTION_CHAT_ONLINE
                ApplyNowState.PERSONAL_LOAN -> FirebaseManagerAnalyticsProperties.PL_TRANSACTION_CHAT_ONLINE
                ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.BLACK_CREDIT_CARD, ApplyNowState.SILVER_CREDIT_CARD -> FirebaseManagerAnalyticsProperties.CC_TRANSACTION_CHAT_ONLINE
                else -> return
            }

            ActivityType.STATEMENT -> when (applyNowState) {
                ApplyNowState.STORE_CARD -> FirebaseManagerAnalyticsProperties.SC_STATEMENT_CHAT_ONLINE
                ApplyNowState.PERSONAL_LOAN -> FirebaseManagerAnalyticsProperties.PL_STATEMENT_CHAT_ONLINE
                ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.BLACK_CREDIT_CARD, ApplyNowState.SILVER_CREDIT_CARD -> FirebaseManagerAnalyticsProperties.CC_STATEMENT_CHAT_ONLINE
                else -> return
            }
            else -> null
        }

        propertyName?.let { event -> Utils.triggerFireBaseEvents(event) }
    }

    fun chatBreak(applyNowState: ApplyNowState, activityType: ActivityType?) {
        val propertyName: String? = when (activityType) {
            ActivityType.ACCOUNT_LANDING, ActivityType.PRODUCT_LANDING -> when (applyNowState) {
                ApplyNowState.STORE_CARD -> FirebaseManagerAnalyticsProperties.SC_MYACCOUNTS_CHAT_BREAK
                ApplyNowState.PERSONAL_LOAN -> FirebaseManagerAnalyticsProperties.PL_MYACCOUNTS_CHAT_BREAK
                ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.BLACK_CREDIT_CARD, ApplyNowState.SILVER_CREDIT_CARD -> FirebaseManagerAnalyticsProperties.CC_MYACCOUNTS_CHAT_BREAK
                else -> return
            }

            ActivityType.PAYMENT_OPTIONS -> when (applyNowState) {
                ApplyNowState.STORE_CARD -> FirebaseManagerAnalyticsProperties.SC_PAYMENT_OPTIONS_CHAT_BREAK
                ApplyNowState.PERSONAL_LOAN -> FirebaseManagerAnalyticsProperties.PL_PAYMENT_OPTIONS_CHAT_BREAK
                ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.BLACK_CREDIT_CARD, ApplyNowState.SILVER_CREDIT_CARD -> FirebaseManagerAnalyticsProperties.CC_PAYMENT_OPTIONS_CHAT_BREAK
                else -> return
            }

            ActivityType.TRANSACTION -> when (applyNowState) {
                ApplyNowState.STORE_CARD -> FirebaseManagerAnalyticsProperties.SC_TRANSACTION_CHAT_BREAK
                ApplyNowState.PERSONAL_LOAN -> FirebaseManagerAnalyticsProperties.PL_TRANSACTION_CHAT_BREAK
                ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.BLACK_CREDIT_CARD, ApplyNowState.SILVER_CREDIT_CARD -> FirebaseManagerAnalyticsProperties.CC_TRANSACTION_CHAT_BREAK
                else -> return
            }

            ActivityType.STATEMENT -> when (applyNowState) {
                ApplyNowState.STORE_CARD -> FirebaseManagerAnalyticsProperties.SC_STATEMENTS_CHAT_BREAK
                ApplyNowState.PERSONAL_LOAN -> FirebaseManagerAnalyticsProperties.PL_STATEMENTS_CHAT_BREAK
                ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.BLACK_CREDIT_CARD, ApplyNowState.SILVER_CREDIT_CARD -> FirebaseManagerAnalyticsProperties.CC_STATEMENTS_CHAT_BREAK
                else -> return
            }
            else -> null
        }

        propertyName?.let { event -> Utils.triggerFireBaseEvents(event) }
    }

    fun chatEnd(applyNowState: ApplyNowState, activityType: ActivityType?) {
        val propertyName: String? = when (activityType) {
            ActivityType.ACCOUNT_LANDING, ActivityType.PRODUCT_LANDING -> when (applyNowState) {
                ApplyNowState.STORE_CARD -> FirebaseManagerAnalyticsProperties.SC_MYACCOUNTS_CHAT_END
                ApplyNowState.PERSONAL_LOAN -> FirebaseManagerAnalyticsProperties.PL_MYACCOUNTS_CHAT_END
                ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.BLACK_CREDIT_CARD, ApplyNowState.SILVER_CREDIT_CARD -> FirebaseManagerAnalyticsProperties.CC_MYACCOUNTS_CHAT_END
                else -> return
            }

            ActivityType.PAYMENT_OPTIONS -> when (applyNowState) {
                ApplyNowState.STORE_CARD -> FirebaseManagerAnalyticsProperties.SC_PAYMENT_OPTIONS_CHAT_END
                ApplyNowState.PERSONAL_LOAN -> FirebaseManagerAnalyticsProperties.PL_PAYMENT_OPTIONS_CHAT_END
                ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.BLACK_CREDIT_CARD, ApplyNowState.SILVER_CREDIT_CARD -> FirebaseManagerAnalyticsProperties.CC_PAYMENT_OPTIONS_CHAT_END
                else -> return
            }

            ActivityType.TRANSACTION -> when (applyNowState) {
                ApplyNowState.STORE_CARD -> FirebaseManagerAnalyticsProperties.SC_TRANSACTIONS_CHAT_END
                ApplyNowState.PERSONAL_LOAN -> FirebaseManagerAnalyticsProperties.PL_TRANSACTIONS_CHAT_END
                ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.BLACK_CREDIT_CARD, ApplyNowState.SILVER_CREDIT_CARD -> FirebaseManagerAnalyticsProperties.CC_TRANSACTIONS_CHAT_END
                else -> return
            }

            ActivityType.STATEMENT -> when (applyNowState) {
                ApplyNowState.STORE_CARD -> FirebaseManagerAnalyticsProperties.SC_STATEMENTS_CHAT_END
                ApplyNowState.PERSONAL_LOAN -> FirebaseManagerAnalyticsProperties.PL_STATEMENTS_CHAT_END
                ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.BLACK_CREDIT_CARD, ApplyNowState.SILVER_CREDIT_CARD -> FirebaseManagerAnalyticsProperties.CC_STATEMENTS_CHAT_END
                else -> return
            }
            else -> null
        }

        propertyName?.let { event -> Utils.triggerFireBaseEvents(event) }
    }

    fun chatOffline(applyNowState: ApplyNowState, activityType: ActivityType?) {
        val propertyName: String? = when (activityType) {
            ActivityType.ACCOUNT_LANDING, ActivityType.PRODUCT_LANDING -> when (applyNowState) {
                ApplyNowState.STORE_CARD -> FirebaseManagerAnalyticsProperties.SC_MYACCOUNTS_CHAT_OFFLINE
                ApplyNowState.PERSONAL_LOAN -> FirebaseManagerAnalyticsProperties.PL_MYACCOUNTS_CHAT_OFFLINE
                ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.BLACK_CREDIT_CARD, ApplyNowState.SILVER_CREDIT_CARD -> FirebaseManagerAnalyticsProperties.CC_MYACCOUNTS_CHAT_OFFLINE
                else -> return
            }

            ActivityType.PAYMENT_OPTIONS -> when (applyNowState) {
                ApplyNowState.STORE_CARD -> FirebaseManagerAnalyticsProperties.SC_PAYMENT_OPTIONS_CHAT_OFFLINE
                ApplyNowState.PERSONAL_LOAN -> FirebaseManagerAnalyticsProperties.PL_PAYMENT_OPTIONS_CHAT_OFFLINE
                ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.BLACK_CREDIT_CARD, ApplyNowState.SILVER_CREDIT_CARD -> FirebaseManagerAnalyticsProperties.CC_PAYMENT_OPTIONS_CHAT_OFFLINE
                else -> return
            }

            ActivityType.TRANSACTION -> when (applyNowState) {
                ApplyNowState.STORE_CARD -> FirebaseManagerAnalyticsProperties.SC_TRANSACTION_CHAT_OFFLINE
                ApplyNowState.PERSONAL_LOAN -> FirebaseManagerAnalyticsProperties.PL_TRANSACTION_CHAT_OFFLINE
                ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.BLACK_CREDIT_CARD, ApplyNowState.SILVER_CREDIT_CARD -> FirebaseManagerAnalyticsProperties.CC_TRANSACTION_CHAT_OFFLINE
                else -> return
            }

            ActivityType.STATEMENT -> when (applyNowState) {
                ApplyNowState.STORE_CARD -> FirebaseManagerAnalyticsProperties.SC_STATEMENT_CHAT_OFFLINE
                ApplyNowState.PERSONAL_LOAN -> FirebaseManagerAnalyticsProperties.PL_STATEMENT_CHAT_OFFLINE
                ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.BLACK_CREDIT_CARD, ApplyNowState.SILVER_CREDIT_CARD -> FirebaseManagerAnalyticsProperties.CC_STATEMENT_CHAT_OFFLINE
                else -> return
            }
            else -> null
        }

        propertyName?.let { event -> Utils.triggerFireBaseEvents(event) }
    }
}