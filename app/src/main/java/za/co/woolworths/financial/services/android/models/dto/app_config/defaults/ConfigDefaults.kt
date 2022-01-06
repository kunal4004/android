package za.co.woolworths.financial.services.android.models.dto.app_config.defaults

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigDefaults(
    val GCM: String?,
    val analitics: String?,
    val server_down_msg: String?,
    val supportNumber: String?,
    val applyNowLink: String?,
    val registerTCLink: String?,
    val faqLink: String?,
    val wrewardsLink: String?,
    val rewardingLink: String?,
    val howtosaveLink: String?,
    val wrewardsTCLink: String?,
    val cartCheckoutLink: String?,
    val firebaseUserPropertiesForDelinquentProductGroupCodes: ConfigUserPropertiesForDelinquentCodes?,
    val logPublicKey: String?
) : Parcelable
