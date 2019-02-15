package za.co.woolworths.financial.services.android.ui.fragments.absa

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.absa_validate_card_pin_dialog.*
import za.co.absa.openbankingapi.SessionKey
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

class AbsaValidateCardAndPinDialogFragment : DialogFragment() {

    private var mAbsaValidateCardAndPinRequest: Unit? = null
    private var mCardToken: String? = null
    private var mCardPin: String? = null
    private var acceptedResultMessages = mutableListOf("success", "processing")
    private var mValidatePinCodeDialogLinterface: IValidatePinCodeDialogInterface? = null
    private var mAbsaValidateSurSchedule: ScheduledFuture<*>? = null

    companion object {
        private const val CARD_TOKEN = "cardToken"
        private const val CARD_PIN = "cardPin"

        fun newInstance(cardToken: String, cardPin: String) = AbsaValidateCardAndPinDialogFragment().apply {
            arguments = Bundle(2).apply {
                putString(CARD_TOKEN, cardToken)
                putString(CARD_PIN, cardPin)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.apply {
            window?.apply {
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        arguments?.let {
            mCardToken = it.getString(CARD_TOKEN)
            mCardPin = it.getString(CARD_PIN)
        }
        return inflater!!.inflate(R.layout.absa_validate_card_pin_dialog, container)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
        validateCardAndPin(mCardToken!!, mCardPin!!)
        btnCancelRequest.setOnClickListener { cancelValidateCardAndPinRequest() }
    }

    private fun initListener() {
        try {
            mValidatePinCodeDialogLinterface = targetFragment?.let { it as? IValidatePinCodeDialogInterface }
        } catch (e: ClassCastException) {
            throw ClassCastException(
                    "The calling Fragment must implement MyDialogFragment.onChangeListener")
        }
    }

    private fun validateCardAndPin(cardToken: String, pin: String) {
        activity?.apply {
            mAbsaValidateCardAndPinRequest = AbsaValidateCardAndPinRequest(this).make(cardToken, pin,
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
                                        successHandler(jSession)
                                        return
                                    }
                                }
                                // navigate to failure handler if result is null or not in acceptedResultMessages
                                failureHandler(header?.resultMessages?.first()?.responseMessage)
                            }
                        }

                        override fun onFailure(errorMessage: String?) {
                            errorMessage?.let { failureHandler(it) }
                        }
                    })
        }
    }

    private fun failureHandler(responseMessage: String?) {
        mValidatePinCodeDialogLinterface?.onFailureHandler(responseMessage
                ?: "Technical error occured")
        dismiss()
    }

    private fun successHandler(jSession: JSession) {
        validateSureCheck(jSession)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(activity, theme) {
            override fun onBackPressed() {
                cancelValidateCardAndPinRequest()
            }
        }
    }

    private fun cancelValidateCardAndPinRequest() {
        dismiss()
    }

    private fun validateSureCheck(jSession: JSession) {
        val scheduler = Executors.newSingleThreadScheduledExecutor()
        activity?.apply {
            mAbsaValidateSurSchedule = scheduler.scheduleWithFixedDelay({
                AbsaValidateSureCheckRequest(this).make(jSession,

                        object : AbsaBankingOpenApiResponse.ResponseDelegate<ValidateSureCheckResponse> {
                            override fun onSuccess(validateCardAndPinResponse: ValidateSureCheckResponse?, cookies: MutableList<HttpCookie>?) {

                                val resultMessage: String? = validateCardAndPinResponse?.result?.toLowerCase()
                                        ?: ""
                                when (resultMessage) {
                                    "processing" -> {

                                    }
                                    else -> {
                                        when (resultMessage) {
                                            "rejected" -> {
                                                //send sure check again
                                                //TODO:: Handle rejected result message
                                            }
                                            "processed" -> {
                                                createAlias(jSession)
                                            }
                                        }

                                        stopPolling()
                                    }
                                }
                                Log.e("valideCardPin", "onSuccess - AbsaBankingOpenApiResponse")
                            }

                            override fun onFailure(errorMessage: String) {
                                Log.e("valideCardPin", "onFailure - AbsaBankingOpenApiResponse")
                            }
                        })
            }, 0, 2, TimeUnit.SECONDS)
        }
    }

    private fun stopPolling() {
        mAbsaValidateSurSchedule?.apply {
            if (!isCancelled) {
                cancel(true)
            }
        }
    }

    fun createAlias(jSession: JSession) {
        activity?.apply {
            //we'll need to keep reference to our symmKey
            //as we'll need it to decrypt the response
            val sessionKey = SessionKey.generate(WoolworthsApplication.getAppContext())
            //create alias
            val deviceId = UUID.randomUUID().toString().replace("-", "")
            AbsaCreateAliasRequest(this).make(deviceId, jSession, object : AbsaBankingOpenApiResponse.ResponseDelegate<CreateAliasResponse> {
                override fun onSuccess(createAliasResponse: CreateAliasResponse?, cookies: MutableList<HttpCookie>?) {
                    var sessionDao: SessionDao? = SessionDao.getByKey(SessionDao.KEY.ABSA_DEVICEID)
                    sessionDao?.value = deviceId
                    sessionDao?.save()

                    sessionDao = SessionDao.getByKey(SessionDao.KEY.ABSA_ALIASID)
                    sessionDao?.value = createAliasResponse?.aliasId
                    sessionDao?.save()

                    navigateToRegisterCredential(jSession)
                }

                override fun onFailure(errorMessage: String) {
                    Log.e("valideCardPin", "onFailure - AbsaCreateAliasRequest")
                }
            })
        }
    }

    private fun navigateToRegisterCredential(jSession: JSession) {
        mValidatePinCodeDialogLinterface?.onSuccessHandler(jSession)
    }


    override fun onDetach() {
        super.onDetach()
        stopPolling()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPolling()
    }
}