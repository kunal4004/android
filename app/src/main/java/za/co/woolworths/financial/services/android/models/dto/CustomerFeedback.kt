package za.co.woolworths.financial.services.android.models.dto

data class CustomerFeedback(
        val minimumSupportedAppBuildNumber: String? = "",
        val triggerEvents: List<String>? = null
)
