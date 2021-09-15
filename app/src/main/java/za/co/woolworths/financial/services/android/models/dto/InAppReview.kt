package za.co.woolworths.financial.services.android.models.dto

data class InAppReview(val minimumSupportedAppBuildNumber: String, val triggerEvents: ArrayList<String>, var isEnabled: Boolean = false)