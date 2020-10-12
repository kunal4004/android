package za.co.woolworths.financial.services.android.ui.fragments.account.chat

import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.OneAppEvents

class ChatTrackPostEvent {

    fun onChatOffline(applyNowState: ApplyNowState) {

        val appScreenFeatureName: Pair<String, String> = when (applyNowState) {
            ApplyNowState.PERSONAL_LOAN -> Pair(OneAppEvents.AppScreen.CHAT_OFFLINE_PERSONAL_LOAN, OneAppEvents.FeatureName.WHATSAPP)
            ApplyNowState.STORE_CARD -> Pair(OneAppEvents.AppScreen.CHAT_OFFLINE_STORE_CARD, OneAppEvents.FeatureName.WHATSAPP)
            else -> Pair(OneAppEvents.AppScreen.CHAT_OFFLINE_CREDIT_LOAN, OneAppEvents.FeatureName.WHATSAPP)
        }

        postEvent(appScreenFeatureName.first, appScreenFeatureName.second)

    }

    fun onChatCollectionsLandingInitiateSession(applyNowState: ApplyNowState) {

        val appScreenFeatureName: Pair<String, String> = when (applyNowState) {
            ApplyNowState.PERSONAL_LOAN -> Pair(OneAppEvents.AppScreen.CHAT_COLLECTIONS_PL_LANDING_INITIATE_SESSION, OneAppEvents.FeatureName.CHAT_COLLECTIONS)
            ApplyNowState.STORE_CARD -> Pair(OneAppEvents.AppScreen.CHAT_COLLECTIONS_SC_LANDING_INITIATE_SESSION, OneAppEvents.FeatureName.CHAT_COLLECTIONS)
            else -> Pair(OneAppEvents.AppScreen.CHAT_COLLECTIONS_CC_LANDING_INITIATE_SESSION, OneAppEvents.FeatureName.CHAT_COLLECTIONS)
        }

        postEvent(appScreenFeatureName.first, appScreenFeatureName.second)
    }

    fun onPayOptionsInitiateSession(applyNowState: ApplyNowState) {
        val appScreenFeatureName: Pair<String, String> = when (applyNowState) {
            ApplyNowState.PERSONAL_LOAN -> Pair(OneAppEvents.AppScreen.CHAT_PAY_OPTIONS_PL_LANDING_INITIATE_SESSION, OneAppEvents.FeatureName.CHAT_COLLECTIONS)
            ApplyNowState.STORE_CARD -> Pair(OneAppEvents.AppScreen.CHAT_PAY_OPTIONS_SC_LANDING_INITIATE_SESSION, OneAppEvents.FeatureName.CHAT_COLLECTIONS)
            else -> Pair(OneAppEvents.AppScreen.CHAT_PAY_OPTIONS_CC_LANDING_INITIATE_SESSION, OneAppEvents.FeatureName.CHAT_COLLECTIONS)
        }

        postEvent(appScreenFeatureName.first, appScreenFeatureName.second)
    }

    fun onChatCollectionsEndSession(applyNowState: ApplyNowState) {

        val appScreenFeatureName: Pair<String, String> = when (applyNowState) {
            ApplyNowState.PERSONAL_LOAN -> Pair(OneAppEvents.AppScreen.CHAT_COLLECTIONS_PL_END_SESSION, OneAppEvents.FeatureName.CHAT_COLLECTIONS)
            ApplyNowState.STORE_CARD -> Pair(OneAppEvents.AppScreen.CHAT_COLLECTIONS_SC_END_SESSION, OneAppEvents.FeatureName.CHAT_COLLECTIONS)
            else -> Pair(OneAppEvents.AppScreen.CHAT_COLLECTIONS_CC_END_SESSION, OneAppEvents.FeatureName.CHAT_COLLECTIONS)
        }

        postEvent(appScreenFeatureName.first, appScreenFeatureName.second)
    }

    fun onTransactionsInitiateSession(applyNowState: ApplyNowState) {

        val appScreenFeatureName: Pair<String, String> = when (applyNowState) {
            ApplyNowState.PERSONAL_LOAN -> Pair(OneAppEvents.AppScreen.CHAT_CUSTOMER_SERVICE_PL_TRANSACTIONS_INITIATE_SESSION, OneAppEvents.FeatureName.CHAT_CUSTOMER_SERVICES)
            ApplyNowState.STORE_CARD -> Pair(OneAppEvents.AppScreen.CHAT_CUSTOMER_SERVICE_SC_TRANSACTIONS_INITIATE_SESSION, OneAppEvents.FeatureName.CHAT_CUSTOMER_SERVICES)
            else -> Pair(OneAppEvents.AppScreen.CHAT_CUSTOMER_SERVICE_CC_TRANSACTIONS_INITIATE_SESSION, OneAppEvents.FeatureName.CHAT_CUSTOMER_SERVICES)
        }

        postEvent(appScreenFeatureName.first, appScreenFeatureName.second)
    }

    fun onStatementsInitiateSession(applyNowState: ApplyNowState) {

        val appScreenFeatureName: Pair<String, String> = when (applyNowState) {
            ApplyNowState.PERSONAL_LOAN -> Pair(OneAppEvents.AppScreen.CHAT_CUSTOMER_SERVICE_PL_STATEMENT_INITIATE_SESSION, OneAppEvents.FeatureName.CHAT_CUSTOMER_SERVICES)
            ApplyNowState.STORE_CARD -> Pair(OneAppEvents.AppScreen.CHAT_CUSTOMER_SERVICE_SC_STATEMENT_INITIATE_SESSION, OneAppEvents.FeatureName.CHAT_CUSTOMER_SERVICES)
            else -> Pair(OneAppEvents.AppScreen.CHAT_CUSTOMER_SERVICE_CC_STATEMENT_INITIATE_SESSION, OneAppEvents.FeatureName.CHAT_CUSTOMER_SERVICES)
        }

        postEvent(appScreenFeatureName.first, appScreenFeatureName.second)
    }

    fun onChatCustomerServicesEndSession(applyNowState: ApplyNowState) {

        val appScreenFeatureName: Pair<String, String> = when (applyNowState) {
            ApplyNowState.PERSONAL_LOAN -> Pair(OneAppEvents.AppScreen.CHAT_COLLECTIONS_PL_END_SESSION, OneAppEvents.FeatureName.CHAT_CUSTOMER_SERVICES)
            ApplyNowState.STORE_CARD -> Pair(OneAppEvents.AppScreen.CHAT_COLLECTIONS_SC_END_SESSION, OneAppEvents.FeatureName.CHAT_CUSTOMER_SERVICES)
            else -> Pair(OneAppEvents.AppScreen.CHAT_COLLECTIONS_CC_END_SESSION, OneAppEvents.FeatureName.CHAT_CUSTOMER_SERVICES)
        }

        postEvent(appScreenFeatureName.first, appScreenFeatureName.second)
    }

    private fun postEvent(appScreen: String, featureName: String) = KotlinUtils.postOneAppEvent(appScreen, featureName)



}