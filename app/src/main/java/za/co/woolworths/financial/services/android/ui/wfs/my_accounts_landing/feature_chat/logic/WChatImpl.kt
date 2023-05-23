package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_chat.logic


import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.model.UserAccountResponse
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

interface WChat {
    fun isChatVisibleForAccountLanding(userAccountResponse: UserAccountResponse): Boolean
    fun isInAppChatFeatureEnabled():Boolean
    fun build()
}

class WChatImpl @Inject constructor() : WChat {

    /**
     * In Accounts Landing: Loop through all the Products returned with the Accounts Response.
     * Show the Chat FAB when any at least one Product has
     * "productOfferingGoodStanding": false && "productOfferingStatus": "ACTIVE"
     */

    override fun isChatVisibleForAccountLanding(userAccountResponse: UserAccountResponse): Boolean {
        if (!isInAppChatFeatureEnabled()) return false
        for (account in userAccountResponse.accountList ?: mutableListOf()){
            if (account.productOfferingGoodStanding == false && account.productOfferingStatus == Utils.ACCOUNT_ACTIVE)
                return true
        }

        return false
    }

    // config.inAppChat.minimumSupportedAppBuildNumber >= currentAppBuildNumber
    override fun isInAppChatFeatureEnabled(): Boolean {
        return AppConfigSingleton.inAppChat?.isEnabled ?: false
    }

    override fun build() {
        if (!isInAppChatFeatureEnabled()) return

    }

}