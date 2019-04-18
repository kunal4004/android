package za.co.woolworths.financial.services.android.ui.fragments.absa

import com.android.volley.VolleyError
import za.co.absa.openbankingapi.woolworths.integration.AbsaCreateAliasRequest
import za.co.absa.openbankingapi.woolworths.integration.AbsaValidateCardAndPinRequest
import za.co.absa.openbankingapi.woolworths.integration.AbsaValidateSureCheckRequest
import za.co.absa.openbankingapi.woolworths.integration.dao.JSession
import za.co.absa.openbankingapi.woolworths.integration.dto.CreateAliasResponse
import za.co.absa.openbankingapi.woolworths.integration.dto.ValidateCardAndPinResponse
import za.co.absa.openbankingapi.woolworths.integration.dto.ValidateSureCheckResponse
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiResponse
import za.co.woolworths.financial.services.android.contracts.IValidatePinCodeDialogInterface
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import java.net.HttpCookie
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class ValidateATMPinCode(cardToken: String?, pinCode: String, validatePinCodeDialogInterface: IValidatePinCodeDialogInterface) {

    companion object {
        private const val POLLING_INTERVAL: Long = 10
        private const val technical_error_occurred = "Technical error occurred."
    }

    private var acceptedResultMessages = mutableListOf("success", "processing")
    private var mValidatePinCodeDialogInterface: IValidatePinCodeDialogInterface? = validatePinCodeDialogInterface
    private var mScheduleValidateSureCheck: ScheduledFuture<*>? = null
    private var mCardToken: String? = cardToken
    private var mPinCode = pinCode
    private var mPollingCount: Int = 0
    private var jSession = JSession()

    fun make() {
        mCardToken?.let { validateCardAndPin(it, mPinCode) }
    }

    private fun validateCardAndPin(cardToken: String, pin: String) {
        AbsaValidateCardAndPinRequest(WoolworthsApplication.getAppContext()).make(cardToken, pin,
                object : AbsaBankingOpenApiResponse.ResponseDelegate<ValidateCardAndPinResponse> {
                    override fun onSuccess(response: ValidateCardAndPinResponse?, cookies: MutableList<HttpCookie>?) {
                        response?.apply {
                            jSession.id = header?.jsessionId

                            cookies?.forEach { cookie ->
                                if (cookie.name.equals("jsessionid", ignoreCase = true)) {
                                    jSession.cookie = cookie
                                }
                            }

                            result?.toLowerCase().apply {
                                if (this in acceptedResultMessages) { // in == contains
                                    validateSureCheck(jSession)
                                    return
                                }
                            }
                            // navigate to failure handler if result is null or not in acceptedResultMessages
                            failureHandler(header?.resultMessages?.first()?.responseMessage, false)
                        }
                    }

                    override fun onFailure(errorMessage: String?) {
                        failureHandler(errorMessage, false)
                    }

                    override fun onFatalError(error: VolleyError?) {
                        fatalErrorHandler(error)
                    }
                })
    }

    private fun fatalErrorHandler(error: VolleyError?) {
        mValidatePinCodeDialogInterface?.onFatalError(error)
    }

    private fun failureHandler(responseMessage: String?, shouldDismissActivity: Boolean) {
        mValidatePinCodeDialogInterface?.onFailureHandler(responseMessage
                ?: "Technical error occured", shouldDismissActivity)
    }

    private fun validateSureCheck(jSession: JSession) {
        mScheduleValidateSureCheck = Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay({
            AbsaValidateSureCheckRequest().make(jSession,
                    object : AbsaBankingOpenApiResponse.ResponseDelegate<ValidateSureCheckResponse> {
                        override fun onSuccess(response: ValidateSureCheckResponse?, cookies: MutableList<HttpCookie>?) {

                            jSession.id = response?.header?.jsessionId

                            val acceptedResultMessages = mutableListOf("success", "processed")
                            val failedResultMessages = mutableListOf("failed", "rejected")
                            val continuePollingProcessResultMessage = mutableListOf("processing")
                            val presentOTPScreenResultMessage = mutableListOf("revertback")

                            response?.result?.toLowerCase().apply {
                                when (this) {
                                    in acceptedResultMessages -> {
                                        //SureCheck was accepted, continue with registration process
                                        createAlias(jSession)
                                        stopPolling()
                                    }
                                    in failedResultMessages -> {
                                        // Sending of the SureCheck failed for some reason. Stop registration details.
                                        // Display an error message and advise to try again later
                                        failureHandler("An error has occurred. Please try again later.", true)
                                        stopPolling()
                                    }

                                    in continuePollingProcessResultMessage -> {
                                        // SureCheck was sent, no client response yet. If > 60 seconds,
                                        // prompt to resend. If resend, same polling as above.
                                        // (5 resends allowed)
                                        mPollingCount += 1
                                        if (mPollingCount > 5) {
                                            failureHandler("Maximum polling rate reached", true)
                                            stopPolling()
                                        }
                                    }

                                    in presentOTPScreenResultMessage -> {
                                        // TODO:: Unable to send surecheck (USSD).
                                        // Present an input screen for the OTP,
                                        // as well as a different request payload.
                                        // #note: consider as rejected for now
                                        failureHandler("An error has occurred. Please try again later.", true)
                                        stopPolling()
                                    }
                                }
                            }
                        }

                        override fun onFailure(errorMessage: String) {
                            failureHandler(errorMessage, false)
                            stopPolling()
                        }

                        override fun onFatalError(error: VolleyError?) {
                            fatalErrorHandler(error)
                            stopPolling()
                        }
                    })
        }, 0, POLLING_INTERVAL, TimeUnit.SECONDS)
    }

    fun createAlias(jSession: JSession) {
        val deviceId = UUID.randomUUID().toString().replace("-", "")
        AbsaCreateAliasRequest(WoolworthsApplication.getAppContext()).make(deviceId, jSession, object : AbsaBankingOpenApiResponse.ResponseDelegate<CreateAliasResponse> {

            override fun onSuccess(response: CreateAliasResponse?, cookies: MutableList<HttpCookie>?) {
                response?.apply {

                    jSession.id = header?.jsessionId

                    if (header?.resultMessages?.size == 0 || aliasId != null) {
                        navigateToRegisterCredential(jSession, aliasId, deviceId)
                    } else {
                        failureHandler(header?.resultMessages?.first()?.responseMessage
                                ?: technical_error_occurred, true)
                    }
                }
            }

            override fun onFailure(errorMessage: String) {
               // failureHandler(errorMessage, false)
            }

            override fun onFatalError(error: VolleyError?) {
                fatalErrorHandler(error)
            }
        })
    }

    private fun stopPolling() {
        mScheduleValidateSureCheck?.apply {
            if (!isCancelled) {
                cancel(true)
            }
        }
    }

    private fun navigateToRegisterCredential(jSession: JSession, aliasId: String?, deviceId: String?) {
        mValidatePinCodeDialogInterface?.onSuccessHandler(jSession, aliasId!!, deviceId!!)
    }

}