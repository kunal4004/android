package za.co.woolworths.financial.services.android.contracts

interface IValidatePinCodeDialogInterface {
    fun onSuccessHandler(aliasID: String)
    fun onFailureHandler(responseMessage: String, dismissActivity: Boolean)
    fun onFatalError()
}