package za.co.woolworths.financial.services.android.ui.fragments.integration.service.login

import za.co.woolworths.financial.services.android.ui.fragments.integration.service.model.AbsaProxyResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.NetworkState

interface IAbsaLogin {
    fun getAbsaUniqueDeviceId(): String?
    fun requestBody(passcode: String): String?
    suspend fun fetchAbsaLogin(passcode: String): NetworkState<AbsaProxyResponseProperty>
}