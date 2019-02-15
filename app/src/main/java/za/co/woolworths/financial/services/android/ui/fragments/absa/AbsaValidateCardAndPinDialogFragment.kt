package za.co.woolworths.financial.services.android.ui.fragments.absa

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.absa_validate_card_pin_dialog.*
import za.co.absa.openbankingapi.woolworths.integration.AbsaCreateAliasRequest
import za.co.absa.openbankingapi.woolworths.integration.AbsaValidateCardAndPinRequest
import za.co.absa.openbankingapi.woolworths.integration.AbsaValidateSureCheckRequest
import za.co.absa.openbankingapi.woolworths.integration.dao.JSession
import za.co.absa.openbankingapi.woolworths.integration.dto.CreateAliasResponse
import za.co.absa.openbankingapi.woolworths.integration.dto.ValidateCardAndPinResponse
import za.co.absa.openbankingapi.woolworths.integration.dto.ValidateSureCheckResponse
import za.co.absa.openbankingapi.woolworths.integration.service.IAbsaBankingOpenApiResponseListener
import za.co.woolworths.financial.services.android.contracts.IValidatePinCodeDialogInterface
import java.net.HttpCookie
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit


class AbsaValidateCardAndPinDialogFragment : DialogFragment() {

    private var mAbsaValidateCardAndPinRequest: Unit? = null
    private var mCardToken: String? = null
    private var mCardPin: String? = null
    private var acceptedResultMessages = mutableListOf("success", "processing")
    private var iValidatePinCodeDialogInterface: IValidatePinCodeDialogInterface? = null
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
        absaValidateCardAndPinRequest(mCardToken!!, mCardPin!!)
        btnCancelRequest.setOnClickListener { cancelValidateCardAndPinRequest() }
    }

    private fun initListener() {
        try {
            iValidatePinCodeDialogInterface = targetFragment?.let { it as? IValidatePinCodeDialogInterface }
        } catch (e: ClassCastException) {
            throw ClassCastException(
                    "The calling Fragment must implement MyDialogFragment.onChangeListener")
        }
    }

    private fun absaValidateCardAndPinRequest(cardToken: String, cardPin: String) {
        activity?.apply {
            mAbsaValidateCardAndPinRequest = AbsaValidateCardAndPinRequest(this).make(cardToken, cardPin,
                    object : IAbsaBankingOpenApiResponseListener<ValidateCardAndPinResponse> {
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
        iValidatePinCodeDialogInterface?.onFailureHandler(responseMessage
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
                        object : IAbsaBankingOpenApiResponseListener<ValidateSureCheckResponse> {
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
                                            }
                                            "processed" -> {

                                            }
                                        }

                                        stopPolling()
                                    }
                                }
                                Log.e("valideCardPin", "onSuccess")
                            }

                            override fun onFailure(errorMessage: String) {
                                Log.e("valideCardPin", "onFailure")
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
            AbsaCreateAliasRequest(this).make("", jSession, object : IAbsaBankingOpenApiResponseListener<CreateAliasResponse> {
                override fun onSuccess(validateSureCheckResponse: CreateAliasResponse?, cookies: MutableList<HttpCookie>?) {
                    //check if response indicates that the SureCheck has not been
                    //interacted with yet. If already interacted, continue with handlers
                    //otherwise attempt API again... perhaps after 5 seconds

                }

                override fun onFailure(errorMessage: String) {
                    Log.e("valideCardPin", "onFailure")
                }
            })
        }
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