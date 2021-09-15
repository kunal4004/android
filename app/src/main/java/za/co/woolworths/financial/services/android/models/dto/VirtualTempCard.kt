package za.co.woolworths.financial.services.android.models.dto

data class VirtualTempCard(
        val minimumSupportedAppBuildNumber: String? = "",
        val cardDisplayTimeoutInSeconds: Long? = 10,
        val cardDisplayTitle: String? = null,
        val barcodeDisplayTitle: String? = null,
        val barcodeDisplaySubtitle: String? = null,
        var isEnabled: Boolean = false
)