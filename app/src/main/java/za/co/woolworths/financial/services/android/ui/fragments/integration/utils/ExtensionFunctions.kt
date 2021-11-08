package za.co.woolworths.financial.services.android.ui.fragments.integration.utils

import retrofit2.HttpException
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.fragments.integration.helper.AbsaTemporaryDataSourceSingleton
import za.co.woolworths.financial.services.android.ui.fragments.integration.helper.Aes256DecryptSymmetricCipherDelegate
import za.co.woolworths.financial.services.android.ui.fragments.integration.helper.Aes256EncryptSymmetricCipherDelegate
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.model.AbsaProxyResponseProperty
import za.co.woolworths.financial.services.android.util.NetworkManager

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

fun String.contentLength(): Int? {
    AbsaTemporaryDataSourceSingleton.contentLength = length
    return  AbsaTemporaryDataSourceSingleton.contentLength
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