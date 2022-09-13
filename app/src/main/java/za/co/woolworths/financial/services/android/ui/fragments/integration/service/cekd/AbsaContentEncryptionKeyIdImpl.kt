package za.co.woolworths.financial.services.android.ui.fragments.integration.service.cekd

import android.util.Base64
import za.co.absa.openbankingapi.AsymmetricCryptoHelper
import za.co.absa.openbankingapi.Cryptography
import za.co.absa.openbankingapi.KeyGenerationFailureException
import za.co.absa.openbankingapi.SessionKey
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.ui.fragments.integration.helper.AbsaTemporaryDataSourceSingleton
import za.co.woolworths.financial.services.android.ui.fragments.integration.remote.AbsaRemoteApi
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.model.AbsaProxyResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.NetworkState
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.resultOf
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager.Companion.logException
import za.co.woolworths.financial.services.android.util.Utils
import java.io.UnsupportedEncodingException

class AbsaContentEncryptionKeyIdImpl : IAbsaContentEncryptionKeyId {

    override var derivedSeeds: ByteArray? = null

    override fun seed(): ByteArray? {
        return SessionKey.generateKey(SessionKey.OUTPUT_KEY_LENGTH).encoded
    }

    override fun derivedSeed(deviceId: String, seed: ByteArray?): ByteArray? {
        return Cryptography.PasswordBasedKeyDerivationFunction2(deviceId, seed, 1000, 256);
    }

    override fun absaUniqueDeviceID(): String? = Utils.getAbsaUniqueDeviceID()

    override fun contentEncryptionSeed(seed: ByteArray?): ByteArray? {
        return AsymmetricCryptoHelper().encryptSymmetricKey(seed, getContentEncryptionPublicKey())
    }

    override fun getContentEncryptionPublicKey(): String? {
        return AppConfigSingleton.absaBankingOpenApiServices?.contentEncryptionPublicKey
    }

    override fun createCekdRequestProperty(derivedSeed : (ByteArray?) -> Unit): CekdRequestProperty? {
        try {
            val deviceId = absaUniqueDeviceID()
            val seed = SessionKey.generateKey(SessionKey.OUTPUT_KEY_LENGTH).encoded
            val contentEncryptionSeed = AsymmetricCryptoHelper().encryptSymmetricKey(seed, getContentEncryptionPublicKey())
            val  derivedSeeds = Cryptography.PasswordBasedKeyDerivationFunction2(deviceId, seed, 1000, 256)
            derivedSeed(derivedSeeds)
            return deviceId?.let { CekdRequestProperty(deviceId = it, contentEncryptionSeed = Base64.encodeToString(contentEncryptionSeed, Base64.NO_WRAP)) }
        } catch (e: UnsupportedEncodingException) {
            logException(e)
        } catch (e: KeyGenerationFailureException) {
            logException(e)
        } catch (e: AsymmetricCryptoHelper.AsymmetricEncryptionFailureException) {
            logException(e)
        } catch (e: AsymmetricCryptoHelper.AsymmetricKeyGenerationFailureException) {
            logException(e)
        }
        return null
    }

    override suspend fun fetchAbsaContentEncryptionKeyId(): NetworkState<AbsaProxyResponseProperty> {
        val cekdRequestProperty = createCekdRequestProperty { derivedSeeds ->
            AbsaTemporaryDataSourceSingleton.deriveSeeds = derivedSeeds
            AbsaTemporaryDataSourceSingleton.xEncryptionKey = derivedSeeds
        }
        return resultOf(AbsaRemoteApi.service.queryAbsaContentEncryptionKeyID(cekdRequestProperty))
    }
}