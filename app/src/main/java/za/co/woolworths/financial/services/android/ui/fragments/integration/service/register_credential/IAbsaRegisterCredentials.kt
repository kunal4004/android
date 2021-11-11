package za.co.woolworths.financial.services.android.ui.fragments.integration.service.register_credential

import za.co.woolworths.financial.services.android.ui.fragments.integration.helper.AbsaResultWrapper
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.model.AbsaProxyResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.NetworkState

interface IAbsaRegisterCredentials {
    val mobileApp5DigitPin: String
    fun getAbsaUniqueDeviceId(): String?
    fun getCredentialsVOs(encryptedAlias: String?,base64EncodedEncryptedDerivedKey: String):Array<CredentialVO>
    fun createRegisterCredentialsRequestBody(aliasId: String?, passcode: String?): AbsaRegisterCredentialRequestProperty?
    suspend fun fetchAbsaRegisterCredentials(aliasId: String?, passcode: String?): NetworkState<AbsaProxyResponseProperty>
}