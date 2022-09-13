package za.co.woolworths.financial.services.android.models.dto.app_config

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigVirtualTempCard(
        val minimumSupportedAppBuildNumber: Int = 0,
        val cardDisplayTimeoutInSeconds: Long? = 10,
        val cardDisplayTitle: String? = null,
        val barcodeDisplayTitle: String? = null,
        val barcodeDisplaySubtitle: String? = null,
        val primaryCardBlockRequired: Boolean = true,
        val replacementCardSuccessfullyOrderedTitle: String? = null,
        val replacementCardSuccessfullyOrderedDescription: String? = null,
        var isEnabled: Boolean = false
) : Parcelable