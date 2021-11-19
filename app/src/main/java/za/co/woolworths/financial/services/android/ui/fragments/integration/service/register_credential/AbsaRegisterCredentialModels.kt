package za.co.woolworths.financial.services.android.ui.fragments.integration.service.register_credential

import za.co.absa.openbankingapi.woolworths.integration.dto.Header

data class AbsaRegisterCredentialRequestProperty(
    val header: Header = Header(),
    val aliasId: String?,
    val deviceId: String?,
    val credentialVOs: Array<CredentialVO?>,
    val symmetricKey: String?,
    val symmetricKeyIV: String?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AbsaRegisterCredentialRequestProperty

        if (aliasId != other.aliasId) return false
        if (deviceId != other.deviceId) return false
        if (!credentialVOs.contentEquals(other.credentialVOs)) return false
        if (symmetricKey != other.symmetricKey) return false
        if (symmetricKeyIV != other.symmetricKeyIV) return false

        return true
    }

    override fun hashCode(): Int {
        var result = aliasId.hashCode()
        result = 31 * result + deviceId.hashCode()
        result = 31 * result + credentialVOs.contentHashCode()
        result = 31 * result + symmetricKey.hashCode()
        result = 31 * result + symmetricKeyIV.hashCode()
        return result
    }
}

data class CredentialVO(
    private val aliasId: String?,
    private val type: String,
    private val credential: String
)

data class AbsaRegisterCredentialResponseProperty(val header: Header? = Header())