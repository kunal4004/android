package za.co.woolworths.financial.services.android.models.dto

data class CreditView(var transUnionLink: String, var transUnionPrivacyPolicyUrl: String, val minimumSupportedAppBuildNumber: String, var isEnabled: Boolean = false)