package za.co.woolworths.financial.services.android.models.dto

data class DashConfig(
        val appURI: String,
        val minimumSupportedAppBuildNumber: Int?,
        var isEnabled: Boolean = false
)