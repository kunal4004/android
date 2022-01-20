package za.co.woolworths.financial.services.android.ui.fragments.integration.service.cekd

import za.co.woolworths.financial.services.android.ui.fragments.integration.service.model.AbsaProxyResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.NetworkState

interface IAbsaContentEncryptionKeyId {

    var derivedSeeds: ByteArray?

    fun seed(): ByteArray?
    fun derivedSeed(deviceId: String, seed: ByteArray?): ByteArray?
    fun absaUniqueDeviceID(): String?
    fun contentEncryptionSeed(seed: ByteArray?): ByteArray?
    fun getContentEncryptionPublicKey(): String?
    fun createCekdRequestProperty(derivedSeed: (ByteArray?) -> Unit): CekdRequestProperty?
    suspend fun fetchAbsaContentEncryptionKeyId(): NetworkState<AbsaProxyResponseProperty>
}