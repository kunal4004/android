package za.co.woolworths.financial.services.android.ui.fragments.integration.service.create_alias

import android.util.Base64
import za.co.absa.openbankingapi.DecryptionFailureException
import za.co.absa.openbankingapi.SymmetricCipher
import za.co.woolworths.financial.services.android.ui.extension.json
import za.co.woolworths.financial.services.android.ui.fragments.integration.helper.AbsaTemporaryDataSourceSingleton
import za.co.woolworths.financial.services.android.ui.fragments.integration.remote.AbsaRemoteApi
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.common.ISessionKeyGenerator
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.model.AbsaProxyResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.*
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager.Companion.logException
import za.co.woolworths.financial.services.android.util.Utils
import java.nio.charset.StandardCharsets

class AbsaCreateAliasImpl(private val sessionKeyGenerator: ISessionKeyGenerator) : ICreateAlias {

    override fun createAliasRespRequestProperty(): CreateAliasRequestProperty {
        val deviceId = Utils.getAbsaUniqueDeviceID()
        val sessionKey = sessionKeyGenerator.generateSessionKey()
        val gatewaySymmetricKey = sessionKeyGenerator.getGatewaySymmetricKey(sessionKey)
        AbsaTemporaryDataSourceSingleton.sessionKey = sessionKey
        val encryptedIVBase64Encoded = sessionKeyGenerator.getEncryptedIVBase64Encoded(sessionKey)
        return CreateAliasRequestProperty(
            deviceId,
            gatewaySymmetricKey,
            encryptedIVBase64Encoded
        )
    }

    override suspend fun fetchCreateAlias(): NetworkState<AbsaProxyResponseProperty> {
        val createAliasRespRequestProperty = createAliasRespRequestProperty().json()
        createAliasRespRequestProperty.contentLength()
        val withEncryptedBody = createAliasRespRequestProperty.toAes256Encrypt()
        return resultOf(AbsaRemoteApi.service.queryAbsaServiceCreateAlias(AbsaTemporaryDataSourceSingleton.cookie,withEncryptedBody))
    }

    override fun handleCreateAliasResult(createAliasResponseProperty: CreateAliasResponseProperty?): String? {
         return try {
            val sessionKey = AbsaTemporaryDataSourceSingleton.sessionKey
            val encryptedAliasBytes: ByteArray? = createAliasResponseProperty?.aliasId?.toByteArray(StandardCharsets.UTF_8)
            val encryptedAliasBase64DecodedBytes = Base64.decode(encryptedAliasBytes, Base64.NO_WRAP)
            val aliasBytes: ByteArray = SymmetricCipher.Aes256Decrypt(sessionKey?.key, encryptedAliasBase64DecodedBytes, sessionKey?.iv)
            val aliasBase64DecodedBytes = Base64.decode(Base64.encodeToString(aliasBytes, Base64.NO_WRAP), Base64.DEFAULT)
            String(aliasBase64DecodedBytes, StandardCharsets.UTF_8)
        } catch (e: DecryptionFailureException) {
            //TODO: Handle decryption issue
            logException(e)
            null
        }
    }
}
