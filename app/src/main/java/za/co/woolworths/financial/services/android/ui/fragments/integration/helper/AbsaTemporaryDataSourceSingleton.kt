package za.co.woolworths.financial.services.android.ui.fragments.integration.helper

import za.co.absa.openbankingapi.SessionKey

object AbsaTemporaryDataSourceSingleton {
    var cookie: String? = null
    var deriveSeeds: ByteArray? = null
    var keyId: String? = null
    var contentLength: Int? = 0
    var deviceId: String? = null
    var jsessionId: String? = null
    var sessionKey: SessionKey? = null
    var wfpt: String? = null
    var xfpt:String? = null
    var xEncryptedIv : ByteArray? = null
    var xEncryptionKey: ByteArray? = null
}