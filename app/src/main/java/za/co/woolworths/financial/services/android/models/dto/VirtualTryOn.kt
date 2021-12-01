package za.co.woolworths.financial.services.android.models.dto

data class VirtualTryOn(
        val minimumSupportedAppBuildNumber: Int?,
        var isEnabled: Boolean = false,
        val lightingTipText: String? = null
)