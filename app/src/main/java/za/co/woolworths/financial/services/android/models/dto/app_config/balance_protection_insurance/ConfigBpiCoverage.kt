package za.co.woolworths.financial.services.android.models.dto.app_config.balance_protection_insurance

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigBpiCoverage (
    val slide1: ConfigBpiSlideText,
    val slide2: ConfigBpiSlideText,
    val slide3: ConfigBpiSlideText,
) : Parcelable