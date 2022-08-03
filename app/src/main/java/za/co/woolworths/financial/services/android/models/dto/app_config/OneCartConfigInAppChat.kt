package za.co.woolworths.financial.services.android.models.dto.app_config

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OneCartConfigInAppChat(
        val baseUrl: String? = null,
        val authKey: String? = null,
        val authSecretKey: String? = null,
        val apiKey: String? = null
) : Parcelable

