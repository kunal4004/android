package za.co.woolworths.financial.services.android.contracts

import za.co.absa.openbankingapi.woolworths.integration.dto.ValidateCardAndPinResponse

interface IValidatePinCodeDialogInterface {
    fun onSuccessHandler(validateCardPin: ValidateCardAndPinResponse?)
    fun onFailureHandler(responseMessage: String)
}