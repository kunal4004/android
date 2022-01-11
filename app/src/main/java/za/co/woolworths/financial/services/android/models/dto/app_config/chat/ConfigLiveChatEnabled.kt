package za.co.woolworths.financial.services.android.models.dto.app_config.chat

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigLiveChatEnabled(
    var accountsLanding: Boolean,
    var storeCard: ConfigChatEnabledForProductFeatures,
    var creditCard: ConfigChatEnabledForProductFeatures,
    var personalLoan: ConfigChatEnabledForProductFeatures
) : Parcelable