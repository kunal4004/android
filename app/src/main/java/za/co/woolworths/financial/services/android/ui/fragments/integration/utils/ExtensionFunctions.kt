package za.co.woolworths.financial.services.android.ui.fragments.integration.utils

import android.util.Base64
import retrofit2.HttpException
import za.co.absa.openbankingapi.AsymmetricCryptoHelper
import za.co.absa.openbankingapi.DecryptionFailureException
import za.co.absa.openbankingapi.SymmetricCipher
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.fragments.integration.helper.AbsaTemporaryDataSourceSingleton
import za.co.woolworths.financial.services.android.ui.fragments.integration.helper.Aes256DecryptSymmetricCipherDelegate
import za.co.woolworths.financial.services.android.ui.fragments.integration.helper.Aes256EncryptSymmetricCipherDelegate
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.model.AbsaProxyResponseProperty
import za.co.woolworths.financial.services.android.util.FirebaseManager
import za.co.woolworths.financial.services.android.util.NetworkManager
import java.util.*

fun resultOf(absaProxyResponseProperty: AbsaProxyResponseProperty): NetworkState<AbsaProxyResponseProperty> {
    return when (NetworkManager.getInstance().isConnectedToNetwork(WoolworthsApplication.getAppContext())) {
        true -> {
            NetworkState.Loading
            try {
                NetworkState.Success(absaProxyResponseProperty)
            } catch (h: HttpException) {
                NetworkState.Error(AbsaApiFailureHandler.HttpException(h.message(), h.code()))
            } catch (e: Exception) {
                NetworkState.Error(AbsaApiFailureHandler.Exception(e.message, e.hashCode()))
            }
        }
        false -> NetworkState.Error(AbsaApiFailureHandler.NoInternetApiFailure)
    }
}


fun ByteArray.toHex(separator: String = " "): String = joinToString(separator = separator) { eachByte -> "%02x".format(eachByte) }

fun ByteArray.toEncryptedHex(): String? {
   val logPublicKey =  WoolworthsApplication.getLogPublicKey()
   return if (logPublicKey!=null) AsymmetricCryptoHelper().encryptToString(this.toHex(), WoolworthsApplication.getLogPublicKey()) else null
}

fun String.contentLength(): Int? {
    AbsaTemporaryDataSourceSingleton.contentLength = length
    return AbsaTemporaryDataSourceSingleton.contentLength
}

fun String.toAes256Encrypt(): String {
    var encrypt: String? by Aes256EncryptSymmetricCipherDelegate()
    encrypt = this
    return encrypt ?: ""
}

fun String.toAes256Decrypt(): String {
    var decrypt: String? by Aes256DecryptSymmetricCipherDelegate()
    decrypt = this
    return decrypt ?: ""
}


fun String.toAes256DecryptBase64BodyToByteArray(): ByteArray? {
    val derivedSeed = AbsaTemporaryDataSourceSingleton.deriveSeeds
    val response = Base64.decode(this, Base64.DEFAULT)
    val ivForDecrypt = Arrays.copyOfRange(response, 0, 16)
    val encryptedResponse = Arrays.copyOfRange(response, 16, response.size)
    try {
        return SymmetricCipher.Aes256Decrypt(
            derivedSeed,
            encryptedResponse,
            ivForDecrypt
        )
    } catch (e: DecryptionFailureException) {
        FirebaseManager.logException(e)
    }
    return null
}