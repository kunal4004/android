package za.co.woolworths.financial.services.android.ui.fragments.mypreferences

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.enter_otp_fragment.*
import kotlinx.android.synthetic.main.fragment_enter_otp.buttonNext
import kotlinx.android.synthetic.main.fragment_enter_otp.didNotReceiveOTPTextView
import kotlinx.android.synthetic.main.fragment_link_device_otp.*
import kotlinx.android.synthetic.main.layout_link_device_result.*
import kotlinx.android.synthetic.main.layout_link_device_validate_otp.*
import kotlinx.android.synthetic.main.layout_sending_otp_request.*
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.linkdevice.LinkDeviceValidateBody
import za.co.woolworths.financial.services.android.models.dto.linkdevice.LinkedDeviceResponse
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.models.dto.otp.RetrieveOTPResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity
import za.co.woolworths.financial.services.android.ui.activities.MyPreferencesInterface
import za.co.woolworths.financial.services.android.ui.activities.account.LinkDeviceConfirmationInterface
import za.co.woolworths.financial.services.android.ui.fragments.npc.OTPViewTextWatcher
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.FirebaseManager
import za.co.woolworths.financial.services.android.util.KeyboardUtils.Companion.hideKeyboard
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils


class LinkDeviceOTPFragment : Fragment(), View.OnClickListener {

    private var otpNumber: String? = null
    private var otpMethod: String? = null

    private val mKeyListener = View.OnKeyListener { v, keyCode, event ->
        if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
            when {
                TextUtils.isEmpty(linkDeviceOTPEdtTxt1?.text) -> {
                    linkDeviceOTPEdtTxt1?.setSelection(linkDeviceOTPEdtTxt1?.text?.length ?: 0)
                    linkDeviceOTPEdtTxt1?.requestFocus(View.FOCUS_DOWN)
                }
                TextUtils.isEmpty(linkDeviceOTPEdtTxt2?.text) -> {
                    linkDeviceOTPEdtTxt1?.setSelection(linkDeviceOTPEdtTxt1?.text?.length ?: 0)
                    linkDeviceOTPEdtTxt1?.requestFocus(View.FOCUS_DOWN)
                }
                TextUtils.isEmpty(linkDeviceOTPEdtTxt3?.text) -> {
                    linkDeviceOTPEdtTxt2?.setSelection(linkDeviceOTPEdtTxt2?.text?.length ?: 0)
                    linkDeviceOTPEdtTxt2?.requestFocus(View.FOCUS_DOWN)
                }
                TextUtils.isEmpty(linkDeviceOTPEdtTxt4?.text) -> {
                    linkDeviceOTPEdtTxt3?.setSelection(linkDeviceOTPEdtTxt3?.text?.length ?: 0)
                    linkDeviceOTPEdtTxt3?.requestFocus(View.FOCUS_DOWN)
                }
                TextUtils.isEmpty(linkDeviceOTPEdtTxt5?.text) -> {
                    linkDeviceOTPEdtTxt4?.setSelection(linkDeviceOTPEdtTxt4?.text?.length ?: 0)
                    linkDeviceOTPEdtTxt4?.requestFocus(View.FOCUS_DOWN)
                }
            }
            true
        }
        false
    }

    private fun validateNextButton() {
        if (TextUtils.isEmpty(linkDeviceOTPEdtTxt5.text) || TextUtils.isEmpty(linkDeviceOTPEdtTxt4.text)
                || TextUtils.isEmpty(linkDeviceOTPEdtTxt3.text) || TextUtils.isEmpty(linkDeviceOTPEdtTxt2.text)
                || TextUtils.isEmpty(linkDeviceOTPEdtTxt1.text)) {
            disableNextButton()
        } else {
            enableNextButton()
        }
    }

    private var mLinkDeviceOTPReq: Call<RetrieveOTPResponse>? = null
    private var mValidateLinkDeviceOTPReq: Call<RetrieveOTPResponse>? = null
    private var mlinkDeviceReq: Call<LinkedDeviceResponse>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        activity?.runOnUiThread { activity?.window?.addFlags(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE) }
        // Use the Kotlin extension in the fragment-ktx artifact
        setFragmentResultListener("resendOTPType") { requestKey, bundle ->
            // We use a String here, but any type that can be put in a Bundle is supported
            val result = bundle.getString("type") as String
            // Do something with the result
            when {
                result.equals(OTPMethodType.SMS.name, true) -> {
                    callGetOTPAPI(OTPMethodType.SMS.name)
                }
                result.equals(OTPMethodType.EMAIL.name, true) -> {
                    resetOTPView()
                    callGetOTPAPI(OTPMethodType.EMAIL.name)
                }
                result.equals(OTP_CALL_CENTER, true) -> {
                    Utils.makeCall(AppConstant.WOOLWOORTH_CALL_CENTER_NUMBER)
                }
                else -> {
                }
            }
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_link_device_otp, container, false)
    }

    private fun resetOTPView() {
        clearOTP()
        if (linkDeviceOTPErrorTxt?.visibility == View.VISIBLE) {
            linkDeviceOTPErrorTxt?.visibility = View.GONE
            setOtpErrorBackground(R.drawable.otp_box_background_focus_selector)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setToolbar()

        didNotReceiveOTPTextView?.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        didNotReceiveOTPTextView?.setOnClickListener(this)

        linkDeviceOTPEdtTxt1.addTextChangedListener(OTPViewTextWatcher(linkDeviceOTPEdtTxt1, linkDeviceOTPEdtTxt1, linkDeviceOTPEdtTxt2) {validateNextButton()})
        linkDeviceOTPEdtTxt2.addTextChangedListener(OTPViewTextWatcher(linkDeviceOTPEdtTxt1, linkDeviceOTPEdtTxt2, linkDeviceOTPEdtTxt3) {validateNextButton()})
        linkDeviceOTPEdtTxt3.addTextChangedListener(OTPViewTextWatcher(linkDeviceOTPEdtTxt2, linkDeviceOTPEdtTxt3, linkDeviceOTPEdtTxt4) {validateNextButton()})
        linkDeviceOTPEdtTxt4.addTextChangedListener(OTPViewTextWatcher(linkDeviceOTPEdtTxt3, linkDeviceOTPEdtTxt4, linkDeviceOTPEdtTxt5) {validateNextButton()})
        linkDeviceOTPEdtTxt5.addTextChangedListener(OTPViewTextWatcher(linkDeviceOTPEdtTxt4, linkDeviceOTPEdtTxt5, linkDeviceOTPEdtTxt5) {validateNextButton()})

        linkDeviceOTPEdtTxt1.setOnKeyListener(mKeyListener)
        linkDeviceOTPEdtTxt2.setOnKeyListener(mKeyListener)
        linkDeviceOTPEdtTxt3.setOnKeyListener(mKeyListener)
        linkDeviceOTPEdtTxt4.setOnKeyListener(mKeyListener)
        linkDeviceOTPEdtTxt5.setOnKeyListener(mKeyListener)

        buttonNext.setOnClickListener(this)

        callGetOTPAPI(OTPMethodType.SMS.name)
    }

    private fun setToolbar() {
        activity?.apply {
            when (this) {
                is MyPreferencesInterface -> {
                    setToolbarTitle("")
                }
                is LinkDeviceConfirmationInterface -> {
                    setToolbarTitle("")
                    hideToolbarButton()
                }
            }
        }
    }

    private fun enableNextButton() {
        buttonNext?.isEnabled = true
        context?.let { buttonNext.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.next_button_icon)) }
    }

    private fun disableNextButton() {
        buttonNext?.isEnabled = false
        context?.let { buttonNext.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.next_button_inactive)) }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
                LinkDeviceOTPFragment().apply {
                    arguments = Bundle().apply {
                    }
                }

        private const val TAG = "LinkDeviceOTPFragment"
        private const val OTP_CALL_CENTER = "CALL CENTER"
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.didNotReceiveOTPTextView -> {
                view?.findNavController()?.navigate(R.id.action_linkDeviceOTPFragment_to_resendOTPBottomSheetFragment, bundleOf(
                        ResendOTPBottomSheetFragment.OTP_NUMBER to otpNumber
                ))
            }
            R.id.buttonNext -> {

                activity?.apply {
                    hideKeyboard(this)
                }
                val otpNumber = getNumberFromEditText(linkDeviceOTPEdtTxt1)
                        .plus(getNumberFromEditText(linkDeviceOTPEdtTxt2))
                        .plus(getNumberFromEditText(linkDeviceOTPEdtTxt3))
                        .plus(getNumberFromEditText(linkDeviceOTPEdtTxt4))
                        .plus(getNumberFromEditText(linkDeviceOTPEdtTxt5))

                callValidatingOTPAPI(otpNumber)
            }
        }
    }

    private fun getNumberFromEditText(numberEditText: EditText?) = numberEditText?.text?.toString()
            ?: ""

    private fun callGetOTPAPI(otpMethod: String?) {
        mLinkDeviceOTPReq = otpMethod?.let { type -> OneAppService.getLinkDeviceOtp(type) }
        this.otpMethod = otpMethod

        showSendingOTPProcessing()
        linkDeviceOTPScreen?.visibility = View.GONE
        mLinkDeviceOTPReq?.enqueue(CompletionHandler(object : IResponseListener<RetrieveOTPResponse> {
            override fun onSuccess(retrieveOTPResponse: RetrieveOTPResponse?) {
                sendinOTPLayout?.visibility = View.GONE
                when (retrieveOTPResponse?.httpCode) {
                    200 -> {
                        linkDeviceOTPScreen?.visibility = View.VISIBLE
                        retrieveOTPResponse.otpSentTo?.let {
                            if (otpMethod.equals(OTPMethodType.SMS.name, true)) {
                                otpNumber = it
                            }
                            enterOTPSubtitle?.text = activity?.resources?.getString(R.string.sent_otp_desc, it)
                            Handler().postDelayed({
                                linkDeviceOTPEdtTxt1.requestFocus()
                            }, AppConstant.DELAY_100_MS)
                        }
                    }
                    440 ->
                        activity?.apply {
                            if (!isFinishing) {
                                SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, retrieveOTPResponse.response?.stsParams, this)
                            }
                        }
                    else -> retrieveOTPResponse?.response?.desc?.let { desc ->
                        try {
                            linkDeviceOTPScreen?.visibility = View.GONE
                        } catch (ex: IllegalStateException) {
                            FirebaseManager.logException(ex)
                        }
                    }
                }
            }

            override fun onFailure(error: Throwable?) {
                linkDeviceOTPScreen?.visibility = View.VISIBLE
            }
        }, RetrieveOTPResponse::class.java))

    }

    private fun callValidatingOTPAPI(otp: String) {

        mValidateLinkDeviceOTPReq = otpMethod?.let { type ->
            OneAppService.validateLinkDeviceOtp(LinkDeviceValidateBody(otp, otpMethod
                    ?: "SMS"))
        }

        showValidatingProcessing()
        linkDeviceOTPScreen?.visibility = View.GONE
        mValidateLinkDeviceOTPReq?.enqueue(CompletionHandler(object : IResponseListener<RetrieveOTPResponse> {
            override fun onSuccess(retrieveOTPResponse: RetrieveOTPResponse?) {
                sendinOTPLayout?.visibility = View.GONE
                when (retrieveOTPResponse?.httpCode) {
                    200 -> {
                        callLinkingDeviceAPI()
                    }
                    440 ->
                        activity?.apply {
                            if (!isFinishing) {
                                SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, retrieveOTPResponse.response?.stsParams, this)
                            }
                        }
                    else -> retrieveOTPResponse?.response?.desc?.let { desc ->
                        showValidateOTPError(desc)
                    }
                }
            }

            override fun onFailure(error: Throwable?) {
                context?.apply {
                    showValidateOTPError(getString(R.string.icr_wrong_otp_error))
                }
            }
        }, RetrieveOTPResponse::class.java))
    }

    private fun showValidateOTPError(msg: String) {
        setOtpErrorBackground(R.drawable.otp_box_error_background)
        linkDeviceOTPErrorTxt?.text = msg
        linkDeviceOTPErrorTxt?.visibility = View.VISIBLE
        linkDeviceOTPScreen?.visibility = View.VISIBLE

    }

    fun setOtpErrorBackground(drawableId: Int) {
        context?.let { context ->
            ContextCompat.getDrawable(context, drawableId)?.apply {
                linkDeviceOTPEdtTxt1?.setBackgroundResource(drawableId)
                linkDeviceOTPEdtTxt2?.setBackgroundResource(drawableId)
                linkDeviceOTPEdtTxt3?.setBackgroundResource(drawableId)
                linkDeviceOTPEdtTxt4?.setBackgroundResource(drawableId)
                linkDeviceOTPEdtTxt5?.setBackgroundResource(drawableId)
            }
        }
    }

    fun clearOTP() {
        linkDeviceOTPEdtTxt1?.text?.clear()
        linkDeviceOTPEdtTxt2?.text?.clear()
        linkDeviceOTPEdtTxt3?.text?.clear()
        linkDeviceOTPEdtTxt4?.text?.clear()
        linkDeviceOTPEdtTxt5?.text?.clear()
    }

    private fun callLinkingDeviceAPI() {
        linkDeviceOTPScreen?.visibility = View.GONE
        var deviceName = ""
        activity?.apply {
            try {
                if (TextUtils.isEmpty(deviceName)) {
                    deviceName = Settings.Secure.getString(contentResolver, "bluetooth_name")
                }
            } catch (e: Exception) {
            }
        }
        mlinkDeviceReq = OneAppService.linkDeviceApi(deviceName, Utils.getUniqueDeviceID(context), "", true, Utils.getToken())

        showLinkingDeviceProcessing()
        linkDeviceOTPScreen?.visibility = View.GONE

        mlinkDeviceReq?.enqueue(CompletionHandler(object : IResponseListener<LinkedDeviceResponse> {
            override fun onSuccess(linkedDeviceResponse: LinkedDeviceResponse?) {
                sendinOTPLayout?.visibility = View.GONE
                when (linkedDeviceResponse?.httpCode) {
                    "201" -> {
                        showDeviceLinked()
                        Utils.saveLinkedDeviceId(linkedDeviceResponse.deviceIdentityToken)
                        Handler().postDelayed({
                            setFragmentResult("linkDevice", bundleOf(
                                    "isLinked" to true
                            ))
                            view?.findNavController()?.navigateUp()
                        }, AppConstant.DELAY_1500_MS)
                    }
                    "440" ->
                        activity?.apply {
                            if (!isFinishing) {
                                SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, linkedDeviceResponse.response.stsParams, this)
                            }
                        }
                    else -> linkedDeviceResponse?.response?.desc?.let { desc ->
                        showErrorScreen(ErrorHandlerActivity.LINK_DEVICE_FAILED)
                    }
                }
            }

            override fun onFailure(error: Throwable?) {
                linkDeviceOTPScreen?.visibility = View.VISIBLE
                showErrorScreen(ErrorHandlerActivity.LINK_DEVICE_FAILED)
            }
        }, LinkedDeviceResponse::class.java))
    }

    private fun showDeviceLinked() {
        sendinOTPLayout?.visibility = View.GONE
        linkDeviceOTPScreen?.visibility = View.GONE
        linkDeviceResultSubitle?.visibility = View.GONE

        linkDeviceResultScreen?.visibility = View.VISIBLE
    }

    private fun showLinkingDeviceProcessing() {
        sendOTPTitle?.visibility = View.GONE
        sendOTPSubtitle?.visibility = View.GONE

        context?.let {
            sendOTPProcessingReq.text = it.getString(R.string.link_device_linking_processing)
        }
        sendinOTPLayout?.visibility = View.VISIBLE
    }


    private fun showSendingOTPProcessing() {
        sendOTPTitle?.visibility = View.VISIBLE
        sendOTPSubtitle?.visibility = View.VISIBLE

        context?.let {
            sendOTPProcessingReq.text = it.getString(R.string.link_device_sending_otp_processing)
        }
        sendinOTPLayout?.visibility = View.VISIBLE
    }

    private fun showValidatingProcessing() {
        sendOTPTitle?.visibility = View.GONE
        sendOTPSubtitle?.visibility = View.GONE

        context?.let {
            sendOTPProcessingReq.text = it.getString(R.string.validating_otp)
        }
        sendinOTPLayout?.visibility = View.VISIBLE
    }

    private fun showErrorScreen(errorType: Int, errorMessage: String = "") {

        activity?.let {
            val intent = Intent(it, ErrorHandlerActivity::class.java)
            intent.putExtra("errorType", errorType)
            intent.putExtra("errorMessage", errorMessage)
            it.startActivityForResult(intent, ErrorHandlerActivity.ERROR_PAGE_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_CANCELED) {
            clearOTP()
            linkDeviceOTPScreen?.visibility = View.VISIBLE
            Utils.setLinkDeviceConfirmationShown(true)
        }

        if (requestCode == ErrorHandlerActivity.ERROR_PAGE_REQUEST_CODE) {
            when (resultCode) {
                ErrorHandlerActivity.RESULT_RETRY -> {
                    callLinkingDeviceAPI()
                }
                ErrorHandlerActivity.RESULT_CALL_CENTER -> {
                    Utils.makeCall(AppConstant.WOOLWOORTH_CALL_CENTER_NUMBER)
                    Utils.setLinkDeviceConfirmationShown(true)
                    setFragmentResult("linkDevice", bundleOf(
                            "isLinked" to false
                    ))
                    view?.findNavController()?.navigateUp()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.runOnUiThread { activity?.window?.clearFlags(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE) }
    }
}