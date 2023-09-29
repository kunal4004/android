package za.co.woolworths.financial.services.android.models.dto.app_config

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EnhanceSubstitution(
    val minimumSupportedAppBuildNumber:Int?,
    val thresholdQuantityForSubstitutionProduct:Int,
    var isEnhancedSubstitutionEnable: Boolean
):Parcelable
