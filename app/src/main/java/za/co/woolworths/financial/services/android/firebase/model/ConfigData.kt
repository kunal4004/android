package za.co.woolworths.financial.services.android.firebase.model

data class ConfigData(
    val activeConfiguration: ActiveConfiguration?,
    val expiryTime: Long,
    val inactiveConfiguration: InactiveConfiguration?
)