package za.co.woolworths.financial.services.android.models.dto;

data class BalanceProtectionInsuranceObject (
        val minimumSupportedAppBuildNumber: Int? = null,
        val coverage: BpiCoverage? = null
)

data class BpiCoverage (
        val slide1: BpiSlideText,
        val slide2: BpiSlideText,
        val slide3: BpiSlideText,
)

data class BpiSlideText (
        val title: String? = null,
        val description: String? = null,
        val descriptionBoldParts: List<String>
)
