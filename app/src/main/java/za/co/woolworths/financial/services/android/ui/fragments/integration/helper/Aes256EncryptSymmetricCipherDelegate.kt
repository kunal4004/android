package za.co.woolworths.financial.services.android.ui.fragments.integration.helper

import android.util.Base64
import za.co.absa.openbankingapi.DecryptionFailureException
import za.co.absa.openbankingapi.KeyGenerationFailureException
import za.co.absa.openbankingapi.SessionKey
import za.co.absa.openbankingapi.SymmetricCipher
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class Aes256EncryptSymmetricCipherDelegate : ReadWriteProperty<Any?, String?> {

    var bodyProperty: String? = null

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
        bodyProperty = encryptAes256Body(value)
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): String? = bodyProperty

    private fun encryptAes256Body(body: String?): String? {
        var encryptionResult: String? = null
    val outputStream = ByteArrayOutputStream()
    var iv: ByteArray? = null
    try {
        iv = SessionKey.generateKey(SessionKey.OUTPUT_KEY_LENGTH_IV).encoded
        AbsaTemporaryDataSourceSingleton.xEncryptedIv = iv
    } catch (e: KeyGenerationFailureException) {
        FirebaseManager.logException(e)
    }
    try {
        outputStream.write(iv)
        outputStream.write(body?.toByteArray(StandardCharsets.UTF_8))
    } catch (e: IOException) {
        FirebaseManager.logException(e)
    }
    try {
       val derivedSeed = AbsaTemporaryDataSourceSingleton.deriveSeeds
        encryptionResult = Base64.encodeToString(
            SymmetricCipher.Aes256Encrypt(
                derivedSeed,
                outputStream.toByteArray(),
                iv
            ), Base64.NO_WRAP
        )
    } catch (e: DecryptionFailureException) {
        FirebaseManager.logException(e)
    }
    return encryptionResult
    }
}