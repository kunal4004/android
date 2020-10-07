package za.co.woolworths.financial.services.android.ui.fragments.account.chat

import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.extension.request

class ChatPostEventManager {

    // Chat EndSession
    fun postEvent() {

    }

    fun endSession(applyNowState: ApplyNowState) {
        when (applyNowState) {
            ApplyNowState.STORE_CARD -> {

            }
            ApplyNowState.PERSONAL_LOAN -> {

            }
            else -> { }
        }
    }

    fun chatOffline() {

    }

    fun landingInitiateSession() {

    }

    fun paymentOptionInitiateSession() {

    }

    fun transactionInitiateSession() {

    }

    fun statementInitiateSession() {

    }

    fun postEvent(featureName: String, appScreen: String) = request(OneAppService.queryServicePostEvent(featureName, appScreen))

}