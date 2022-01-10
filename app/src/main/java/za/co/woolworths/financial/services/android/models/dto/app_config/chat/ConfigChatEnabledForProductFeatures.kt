package za.co.woolworths.financial.services.android.models.dto.app_config.chat

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigChatEnabledForProductFeatures(
    var landing: Boolean = false,
    var paymentOptions: Boolean = false,
    var transactions: Boolean = false,
    var statements: Boolean = false
) : Parcelable
