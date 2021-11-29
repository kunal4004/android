package za.co.woolworths.financial.services.android.ui.fragments.integration.utils

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */

sealed class NetworkState<out T : Any> {
    data class Success<out T : Any>(val data: T) : NetworkState<T>()
    data class Error(val error: AbsaApiFailureHandler) : NetworkState<Nothing>()
    object Loading : NetworkState<Nothing>()
}

sealed class AbsaApiFailureHandler {
    /**
     * Global Failure classes
     * These failures will be used across all over the app including Data Layer, Domain Layer, Framework Layer
     */
    class HttpException(var message: String?, var errorCode: Int? = null) : AbsaApiFailureHandler()
    class Exception(var message: String?, var errorCode: Int? = null) : AbsaApiFailureHandler()
    object NoInternetApiFailure : AbsaApiFailureHandler()

    /**
     * Feature based failures
     */

    sealed class AppServerFailure : AbsaApiFailureHandler() {
        class SessionTimeout(var stsParams: String?, var errorCode: Int? = null) : AppServerFailure()
        class GeneralFailure(var message: String?, var errorCode: Int? = null) : AppServerFailure()
    }

    sealed class FeatureValidateCardAndPin : AbsaApiFailureHandler() {
        class ValidateCardAndPinStatusCodeInvalid(var message: String ?, var isActivityRunning: Boolean) : FeatureValidateCardAndPin()
        class ValidateSureCheckStatusCodeInvalid(var message: String ?, var errorCode: Int? = null) : FeatureValidateCardAndPin()
        class InvalidValidateSureCheckFailedMessage(var message: String ?, var isActivityRunning: Boolean) : FeatureValidateCardAndPin()
        class InvalidValidateSureCheckContinuePolling(var message: String ?, var isActivityRunning: Boolean) : FeatureValidateCardAndPin()
        class InvalidAliasIdStatusCode(var message: String ?, var isActivityRunning: Boolean = false) : FeatureValidateCardAndPin()
        class InvalidAbsaRegisterCredentialStatusCode(var message: String?, var errorCode: Int? = null) : FeatureValidateCardAndPin()
    }

    sealed class FeatureRegisterCredentials: AbsaApiFailureHandler(){
        class StatusCodeInvalid(var message: String?, var errorCode: Int? = null) : FeatureRegisterCredentials()
    }
    sealed class ListStatement: AbsaApiFailureHandler(){
        class FacadeStatusCodeInvalid(var message: String?, var errorCode: Int? = null) : ListStatement()
        class ArchiveStatusCodeInvalid(var message: String?, var errorCode: Int? = null) : ListStatement()
    }

}