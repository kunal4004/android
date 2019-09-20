package za.co.woolworths.financial.services.android.contracts

import com.android.volley.VolleyError
import za.co.absa.openbankingapi.woolworths.integration.dao.JSession

interface IValidatePinCodeDialogInterface {
    fun onSuccessHandler(aliasID: String)
    fun onFailureHandler(responseMessage: String, dismissActivity: Boolean)
    fun onFatalError(error: VolleyError?)
    fun onSuccessOFOTPSureCheck(userCelleNumber: String?)
}