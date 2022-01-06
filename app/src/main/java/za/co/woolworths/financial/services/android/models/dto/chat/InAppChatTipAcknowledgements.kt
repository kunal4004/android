package za.co.woolworths.financial.services.android.models.dto.chat

import za.co.woolworths.financial.services.android.models.dto.app_config.chat.ConfigChatEnabledForProductFeatures

data class InAppChatTipAcknowledgements(var accountsLanding: Boolean = false, var storeCard: ConfigChatEnabledForProductFeatures, var creditCard: ConfigChatEnabledForProductFeatures, var personalLoan: ConfigChatEnabledForProductFeatures, var isWhatsAppOnBoardingScreenVisible: Boolean = false)

