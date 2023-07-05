package za.co.woolworths.financial.services.android.enhancedSubstitution.util

import za.co.woolworths.financial.services.android.models.AppConfigSingleton

fun isEnhanceSubstitutionFeatureEnable() =
    AppConfigSingleton.enhanceSubstitution?.isEnhancedSubstitutionEnable