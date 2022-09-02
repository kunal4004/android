package za.co.woolworths.financial.services.android.ui.fragments.integration.helper

import android.util.Base64
import za.co.absa.openbankingapi.DecryptionFailureException
import za.co.absa.openbankingapi.SymmetricCipher
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class Aes256DecryptSymmetricCipherDelegate : ReadWriteProperty<Any?, String?> {

    private var bodyProperty: String? = null

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
        bodyProperty = decryptAes256Body(value)
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): String? {
        return bodyProperty
    }

    private fun decryptAes256Body(bodyParams: String?): String? {
        var decryptedResponse: String? = null
            val derivedSeed = AbsaTemporaryDataSourceSingleton.deriveSeeds
            val response = Base64.decode(bodyParams, Base64.DEFAULT)
            val ivForDecrypt = Arrays.copyOfRange(response, 0, 16)
            val encryptedResponse = Arrays.copyOfRange(response, 16, response.size)
            try {
                decryptedResponse = String(
                    SymmetricCipher.Aes256Decrypt(
                        derivedSeed,
                        encryptedResponse,
                        ivForDecrypt
                    ), StandardCharsets.UTF_8
                )
            } catch (e: DecryptionFailureException) {
                FirebaseManager.logException(e)
            }
        return decryptedResponse
    }
}