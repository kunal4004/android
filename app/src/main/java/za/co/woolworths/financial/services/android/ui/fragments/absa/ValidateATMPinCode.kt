package za.co.woolworths.financial.services.android.ui.fragments.absa

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
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import java.net.HttpCookie
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class ValidateATMPinCode(cardToken: String?, pinCode: String, validatePinCodeDialogInterface: IValidatePinCodeDialogInterface) {

    companion object {
        private const val POLLING_INTERVAL: Long = 10
    }

    private var acceptedResultMessages = mutableListOf("success", "processing")
    private var mValidatePinCodeDialogInterface: IValidatePinCodeDialogInterface? = validatePinCodeDialogInterface
    private var mScheduleValidateSureCheck: ScheduledFuture<*>? = null
    private var mCardToken: String? = cardToken
    private var mPinCode = pinCode
    private var mPollingCount: Int = 0

    fun make() {
        mCardToken?.let { validateCardAndPin(it, mPinCode) }
    }

    private fun validateCardAndPin(cardToken: String, pin: String) {
        AbsaValidateCardAndPinRequest(WoolworthsApplication.getAppContext()).make(cardToken, pin,
                object : AbsaBankingOpenApiResponse.ResponseDelegate<ValidateCardAndPinResponse> {
                    override fun onSuccess(response: ValidateCardAndPinResponse?, cookies: MutableList<HttpCookie>?) {
                        val jSession = JSession()
                        response?.apply {
                            jSession.id = header?.jsessionId

                            for (cookie in cookies!!) {
                                if (cookie.name.equals("jsessionid", ignoreCase = true)) {
                                    jSession.cookie = cookie
                                    break
                                }
                            }

                            result?.let {
                                if (it.toLowerCase() in acceptedResultMessages) { // in == contains
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
                })
    }

    private fun failureHandler(responseMessage: String?, shouldDismissActivity: Boolean) {
        mValidatePinCodeDialogInterface?.onFailureHandler(responseMessage
                ?: "Technical error occured", shouldDismissActivity)
    }

    private fun validateSureCheck(jSession: JSession) {
        mScheduleValidateSureCheck = Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay({
            AbsaValidateSureCheckRequest(WoolworthsApplication.getAppContext()).make(jSession,
                    object : AbsaBankingOpenApiResponse.ResponseDelegate<ValidateSureCheckResponse> {
                        override fun onSuccess(validateCardAndPinResponse: ValidateSureCheckResponse?, cookies: MutableList<HttpCookie>?) {
                            val resultMessage: String? = validateCardAndPinResponse?.result?.toLowerCase()
                                    ?: ""
                            mPollingCount += 1
                            if (mPollingCount > 5) {
                                stopPolling()
                                failureHandler("Maximum polling rate reached", true)
                            }
                            when (resultMessage) {
                                "processing" -> {
                                    // SureCheck was sent, no client response yet. If > 60 seconds,
                                    // prompt to resend. If resend, same polling as above.
                                    // (5 resends allowed)
                                }

                                "failed" -> {
                                    // Sending of the SureCheck failed for some reason. Stop registration details.
                                    // Display an error message and advise to try again later
                                    failureHandler("An error has occured. Please try again later.", true)

                                }

                                "revertback" -> {
                                    // Unable to send surecheck (USSD).
                                    // Present an input screen for the OTP,
                                    // as well as a different request payload.
                                    // #note: consider as rejected for now
                                    failureHandler("An error has occured. Please try again later.", true)
                                }
                                else -> {
                                    when (resultMessage) {
                                        "rejected" -> {
                                            //send sure check again
                                            // SureCheck was rejected/declined, Stop registration process
                                            failureHandler("An error has occured. Please try again later.", true)
                                        }
                                        "processed" -> {
                                            //SureCheck was accepted, continue with registration process
                                            createAlias(jSession)
                                        }
                                    }

                                    stopPolling()
                                }
                            }
                        }

                        override fun onFailure(errorMessage: String) {
                            failureHandler(errorMessage, false)

                        }
                    })
        }, 0, POLLING_INTERVAL, TimeUnit.SECONDS)
    }

    fun createAlias(jSession: JSession) {
        val deviceId = UUID.randomUUID().toString().replace("-", "")
        AbsaCreateAliasRequest(WoolworthsApplication.getAppContext()).make(deviceId, jSession, object : AbsaBankingOpenApiResponse.ResponseDelegate<CreateAliasResponse> {
            override fun onSuccess(response: CreateAliasResponse?, cookies: MutableList<HttpCookie>?) {
                response?.apply {
                    if (header?.resultMessages?.size == 0 || aliasId != null) {
                        var sessionDao: SessionDao? = SessionDao.getByKey(SessionDao.KEY.ABSA_DEVICEID)
                        sessionDao?.value = deviceId
                        sessionDao?.save()

                        sessionDao = SessionDao.getByKey(SessionDao.KEY.ABSA_ALIASID)
                        sessionDao?.value = aliasId
                        sessionDao?.save()

                        navigateToRegisterCredential(jSession)
                    } else {
                        failureHandler("An error occured while attempting to decode the server response.", true)
                    }
                }
            }

            override fun onFailure(errorMessage: String) {
                failureHandler(errorMessage, false)
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

    private fun navigateToRegisterCredential(jSession: JSession) {
        mValidatePinCodeDialogInterface?.onSuccessHandler(jSession)
    }
}