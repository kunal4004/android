package za.co.woolworths.financial.services.android.models.dto

data class InAppReview(val minimumSupportedAppBuildNumber: Int, val triggerEvents: ArrayList<String>, var isEnabled: Boolean = false)