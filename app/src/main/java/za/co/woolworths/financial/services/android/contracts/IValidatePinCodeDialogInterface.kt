package za.co.woolworths.financial.services.android.contracts

import za.co.absa.openbankingapi.woolworths.integration.dao.JSession

interface IValidatePinCodeDialogInterface {
    fun onSuccessHandler(jSession: JSession)
    fun onFailureHandler(responseMessage: String)
}