package za.co.woolworths.financial.services.android.models.dto

data class CustomerFeedback(
        val minimumSupportedAppBuildNumber: Int=0,
        val triggerEvents: List<String>? = null
)
