package za.co.woolworths.financial.services.android.models.dto


data class AbsaBankingOpenApiServices(var isEnabled: Boolean = false, val baseURL: String, val appPublicKey: String, val contentEncryptionPublicKey: String, val minSupportedAppVersion: String)