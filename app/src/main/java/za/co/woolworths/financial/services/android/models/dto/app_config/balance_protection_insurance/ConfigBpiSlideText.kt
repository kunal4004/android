package za.co.woolworths.financial.services.android.models.dto.app_config.balance_protection_insurance

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigBpiSlideText (
    val title: String? = null,
    val description: String? = null,
    val descriptionBoldParts: List<String>
) : Parcelable
