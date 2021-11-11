package za.co.woolworths.financial.services.android.models.dto

data class DeviceSecurity(
    val personalLoan: ProductSecurityDetails,
    val storeCard: ProductSecurityDetails,
    val creditCard: ProductSecurityDetails
)
