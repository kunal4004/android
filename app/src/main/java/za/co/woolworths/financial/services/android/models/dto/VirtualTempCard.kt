package za.co.woolworths.financial.services.android.models.dto

data class VirtualTempCard(val minimumSupportedAppBuildNumber: String? = "", val cardDisplayTimeoutInSeconds: Long? = 10, var isEnabled: Boolean = false)