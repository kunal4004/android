package za.co.woolworths.financial.services.android.ui.fragments.mypreferences

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentUnlinkDeviceOtpBinding
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.linkdevice.UserDevice
import za.co.woolworths.financial.services.android.models.dto.linkdevice.ViewAllLinkedDeviceResponse
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.models.dto.otp.RetrieveOTPResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity
import za.co.woolworths.financial.services.android.ui.activities.MyPreferencesActivity
import za.co.woolworths.financial.services.android.ui.activities.MyPreferencesInterface
import za.co.woolworths.financial.services.android.ui.activities.account.LinkDeviceConfirmationInterface
import za.co.woolworths.financial.services.android.ui.extension.cancelRetrofitRequest
import za.co.woolworths.financial.services.android.ui.fragments.npc.OTPViewTextWatcher
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class LinkPrimaryDeviceOTPFragment : BaseFragmentBinding<FragmentUnlinkDeviceOtpBinding>(
    FragmentUnlinkDeviceOtpBinding::inflate) , View.OnClickListener, NetworkChangeListener {

    private var mConnectionBroadCast: BroadcastReceiver? = null
    private var newPrimaryDevice: UserDevice? = null
    private var oldPrimaryDevice: UserDevice? = null
    private var otpNumber: String? = null
    private var otpSMSNumber: String? = null
    private var retryApiCall: String? = null
    private var otpMethod: String? = OTPMethodType.SMS.name
    private var deleteOldPrimaryDevice: Boolean = false

    private val mKeyListener = View.OnKeyListener { v, keyCode, event ->
        binding.unlinkDeviceOTPScreenConstraintLayout.apply {
        if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
            when {
                TextUtils.isEmpty(linkDeviceOTPEdtTxt1.text) -> {
                    linkDeviceOTPEdtTxt1.setSelection(linkDeviceOTPEdtTxt1.text?.length ?: 0)
                    linkDeviceOTPEdtTxt1.requestFocus(View.FOCUS_DOWN)
                }
                TextUtils.isEmpty(linkDeviceOTPEdtTxt2.text) -> {
                    linkDeviceOTPEdtTxt1.setSelection(linkDeviceOTPEdtTxt1.text?.length ?: 0)
                    linkDeviceOTPEdtTxt1.requestFocus(View.FOCUS_DOWN)
                }
                TextUtils.isEmpty(linkDeviceOTPEdtTxt3.text) -> {
                    linkDeviceOTPEdtTxt2.setSelection(linkDeviceOTPEdtTxt2.text?.length ?: 0)
                    linkDeviceOTPEdtTxt2.requestFocus(View.FOCUS_DOWN)
                }
                TextUtils.isEmpty(linkDeviceOTPEdtTxt4.text) -> {
                    linkDeviceOTPEdtTxt3.setSelection(linkDeviceOTPEdtTxt3.text?.length ?: 0)
                    linkDeviceOTPEdtTxt3.requestFocus(View.FOCUS_DOWN)
                }
                TextUtils.isEmpty(linkDeviceOTPEdtTxt5.text) -> {
                    linkDeviceOTPEdtTxt4.setSelection(linkDeviceOTPEdtTxt4.text?.length ?: 0)
                    linkDeviceOTPEdtTxt4.requestFocus(View.FOCUS_DOWN)
                }
            }
        } else if (keyCode == KeyEvent.KEYCODE_ENTER) {
            Utils.hideSoftKeyboard(activity)
        }
    }
        false
    }

    private fun validateNextButton() {
        binding.unlinkDeviceOTPScreenConstraintLayout.apply {
            if (TextUtils.isEmpty(linkDeviceOTPEdtTxt5.text) || TextUtils.isEmpty(
                    linkDeviceOTPEdtTxt4.text
                )
                || TextUtils.isEmpty(linkDeviceOTPEdtTxt3.text) || TextUtils.isEmpty(
                    linkDeviceOTPEdtTxt2.text
                )
                || TextUtils.isEmpty(linkDeviceOTPEdtTxt1.text)
            ) {
                disableNextButton()
            } else {
                enableNextButton()
            }

            if (TextUtils.isEmpty(linkDeviceOTPEdtTxt5.text) && TextUtils.isEmpty(
                    linkDeviceOTPEdtTxt4.text
                )
                && TextUtils.isEmpty(linkDeviceOTPEdtTxt3.text) && TextUtils.isEmpty(
                    linkDeviceOTPEdtTxt2.text
                )
                && TextUtils.isEmpty(linkDeviceOTPEdtTxt1.text)
            ) {
                clearErrorMessage()
            }
        }
    }

    private var mLinkDeviceOTPReq: Call<RetrieveOTPResponse>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            oldPrimaryDevice =
                it.getSerializable(ViewAllLinkedDevicesFragment.OLD_DEVICE) as UserDevice?
            newPrimaryDevice =
                it.getSerializable(ViewAllLinkedDevicesFragment.NEW_DEVICE) as UserDevice?
            deleteOldPrimaryDevice =
                it.getBoolean(ViewAllLinkedDevicesFragment.DELETE_PRIMARY_DEVICE)
        }
    }

    private fun resetOTPView() {
        clearOTP()
        clearErrorMessage()
    }

    private fun clearErrorMessage() {
        binding.unlinkDeviceOTPScreenConstraintLayout.apply {
            if (linkDeviceOTPErrorTxt.visibility == View.VISIBLE) {
                linkDeviceOTPErrorTxt.visibility = View.GONE
                setOtpErrorBackground(R.drawable.otp_box_background_focus_selector)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.runOnUiThread { activity?.window?.addFlags(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE) }
        // Use the Kotlin extension in the fragment-ktx artifact
        setFragmentResultListener("resendOTPType") { requestKey, bundle ->
            // We use a String here, but any type that can be put in a Bundle is supported
            val result = bundle.getString("type") as String
            // Do something with the result
            when {
                result.equals(OTPMethodType.SMS.name, true) -> {
                    resetOTPView()
                    callGetOTPAPI(OTPMethodType.SMS.name)
                }
                result.equals(OTPMethodType.EMAIL.name, true) -> {
                    resetOTPView()
                    callGetOTPAPI(OTPMethodType.EMAIL.name)
                }
                result.equals(OTP_CALL_CENTER, true) -> {
                    resetOTPView()
                    Utils.makeCall(AppConstant.WOOLWOORTH_CALL_CENTER_NUMBER)
                }
                else -> {
                }
            }
        }

        setToolbar()
        connectionDetector()

        binding.didNotReceiveOTPTextView.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.didNotReceiveOTPTextView.setOnClickListener(this)
        binding.unlinkDeviceOTPScreenConstraintLayout.apply {
            KotlinUtils.lowercaseEditText(linkDeviceOTPEdtTxt1)
            KotlinUtils.lowercaseEditText(linkDeviceOTPEdtTxt2)
            KotlinUtils.lowercaseEditText(linkDeviceOTPEdtTxt3)
            KotlinUtils.lowercaseEditText(linkDeviceOTPEdtTxt4)
            KotlinUtils.lowercaseEditText(linkDeviceOTPEdtTxt5)

            linkDeviceOTPEdtTxt1.addTextChangedListener(
                OTPViewTextWatcher(
                    linkDeviceOTPEdtTxt1,
                    linkDeviceOTPEdtTxt1,
                    linkDeviceOTPEdtTxt2
                ) { validateNextButton() })
            linkDeviceOTPEdtTxt2.addTextChangedListener(
                OTPViewTextWatcher(
                    linkDeviceOTPEdtTxt1,
                    linkDeviceOTPEdtTxt2,
                    linkDeviceOTPEdtTxt3
                ) { validateNextButton() })
            linkDeviceOTPEdtTxt3.addTextChangedListener(
                OTPViewTextWatcher(
                    linkDeviceOTPEdtTxt2,
                    linkDeviceOTPEdtTxt3,
                    linkDeviceOTPEdtTxt4
                ) { validateNextButton() })
            linkDeviceOTPEdtTxt4.addTextChangedListener(
                OTPViewTextWatcher(
                    linkDeviceOTPEdtTxt3,
                    linkDeviceOTPEdtTxt4,
                    linkDeviceOTPEdtTxt5
                ) { validateNextButton() })
            linkDeviceOTPEdtTxt5.addTextChangedListener(
                OTPViewTextWatcher(
                    linkDeviceOTPEdtTxt4,
                    linkDeviceOTPEdtTxt5,
                    linkDeviceOTPEdtTxt5
                ) { validateNextButton() })

            linkDeviceOTPEdtTxt1.setOnKeyListener(mKeyListener)
            linkDeviceOTPEdtTxt2.setOnKeyListener(mKeyListener)
            linkDeviceOTPEdtTxt3.setOnKeyListener(mKeyListener)
            linkDeviceOTPEdtTxt4.setOnKeyListener(mKeyListener)
            linkDeviceOTPEdtTxt5.setOnKeyListener(mKeyListener)
        }
        binding.buttonNext.setOnClickListener(this)

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
                    showBackButton()
                }
            }
        }
    }

    private fun enableNextButton() {
        binding.buttonNext.isEnabled = true
        context?.let {
            binding.buttonNext.setImageDrawable(
                ContextCompat.getDrawable(
                    it,
                    R.drawable.next_button_icon
                )
            )
        }
    }

    private fun disableNextButton() {
        binding.buttonNext.isEnabled = false
        context?.let {
            binding.buttonNext.setImageDrawable(
                ContextCompat.getDrawable(
                    it,
                    R.drawable.next_button_inactive
                )
            )
        }
    }

    companion object {
        private const val OTP_CALL_CENTER = "CALL CENTER"
        const val RETRY_GET_OTP: String = "GET_OTP"
        const val RETRY_VALIDATE: String = "VALIDATE_OTP"
        const val RETRY_LINK_DEVICE: String = "LINK_DEVICE"
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.didNotReceiveOTPTextView -> {
                if (!isRetrieveOTPCallInProgress()) {
                    v.isEnabled = false
                    Handler().postDelayed({ v.isEnabled = true }, AppConstant.DELAY_1000_MS)
                    view?.findNavController()?.navigate(
                        R.id.action_linkDeviceOTPFragment_to_resendOTPBottomSheetFragment, bundleOf(
                            ResendOTPBottomSheetFragment.OTP_SMS_NUMBER to otpSMSNumber
                        )
                    )
                }
            }
            R.id.buttonNext -> {

                makeValidateOTPRequest()
            }
        }
    }

    private fun makeValidateOTPRequest() {
        binding.unlinkDeviceOTPScreenConstraintLayout.apply {
            otpNumber = getNumberFromEditText(linkDeviceOTPEdtTxt1)
                .plus(getNumberFromEditText(linkDeviceOTPEdtTxt2))
                .plus(getNumberFromEditText(linkDeviceOTPEdtTxt3))
                .plus(getNumberFromEditText(linkDeviceOTPEdtTxt4))
                .plus(getNumberFromEditText(linkDeviceOTPEdtTxt5))

            otpMethod = otpMethod ?: OTPMethodType.SMS.name

            if (TextUtils.isEmpty(otpNumber) || otpNumber!!.length < 5) {
                return
            }

            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(linkDeviceOTPEdtTxt5.windowToken, 0)

            if (!NetworkManager.getInstance().isConnectedToNetwork(activity)) {
                enterOTPSubtitle.text = context?.getString(R.string.internet_waiting_subtitle)
                return
            }
        }
        callLinkingDeviceAPI()
    }


    private fun isRetrieveOTPCallInProgress(): Boolean = binding.sendinOTPLayout.root.visibility == View.VISIBLE

    private fun getNumberFromEditText(numberEditText: EditText?) = numberEditText?.text?.toString()
        ?: ""

    private fun callGetOTPAPI(otpMethod: String?) {
        mLinkDeviceOTPReq = otpMethod?.let { type -> OneAppService.getLinkDeviceOtp(type) }
        this.otpMethod = otpMethod

        if (!NetworkManager.getInstance().isConnectedToNetwork(activity)) {
            binding.unlinkDeviceOTPScreenConstraintLayout.enterOTPSubtitle.text = context?.getString(R.string.internet_waiting_subtitle)
            retryApiCall = RETRY_GET_OTP
            return
        }

        context?.let {
            binding.didNotReceiveOTPTextView.setTextColor(
                ContextCompat.getColor(
                    it,
                    R.color.button_disable
                )
            )
        }
        showSendingOTPProcessing()
        mLinkDeviceOTPReq?.enqueue(CompletionHandler(object :
            IResponseListener<RetrieveOTPResponse> {
            override fun onSuccess(retrieveOTPResponse: RetrieveOTPResponse?) {
                if (!isAdded || activity == null) return
                context?.let {
                    binding.didNotReceiveOTPTextView.setTextColor(
                        ContextCompat.getColor(
                            it,
                            R.color.black
                        )
                    )
                }

                when (retrieveOTPResponse?.httpCode) {
                    AppConstant.HTTP_OK -> {

                        if (!isAdded) {
                            return
                        }
                        binding.sendinOTPLayout.root.visibility = View.GONE
                        binding.unlinkDeviceOTPScreenConstraintLayout.root.visibility = View.VISIBLE
                        retrieveOTPResponse.otpSentTo?.let {
                            if (otpMethod.equals(OTPMethodType.SMS.name, true)) {
                                otpSMSNumber = it
                            }
                            binding.unlinkDeviceOTPScreenConstraintLayout.enterOTPSubtitle.text =
                                activity?.resources?.getString(R.string.sent_otp_desc, it)
                            Handler().postDelayed({
                                binding.unlinkDeviceOTPScreenConstraintLayout.linkDeviceOTPEdtTxt1.requestFocus()
                                val imm: InputMethodManager? =
                                    context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                                imm?.showSoftInput(
                                    binding.unlinkDeviceOTPScreenConstraintLayout.linkDeviceOTPEdtTxt1,
                                    InputMethodManager.SHOW_IMPLICIT
                                )
                            }, AppConstant.DELAY_200_MS)
                        }
                    }

                    AppConstant.HTTP_SESSION_TIMEOUT_440 ->

                        activity?.apply {
                            if (!isFinishing) {
                                SessionUtilities.getInstance().setSessionState(
                                    SessionDao.SESSION_STATE.INACTIVE,
                                    retrieveOTPResponse.response?.stsParams,
                                    this
                                )
                            }
                        }
                    else -> retrieveOTPResponse?.response?.desc?.let { desc ->
                        context?.let {
                            binding.didNotReceiveOTPTextView.setTextColor(
                                ContextCompat.getColor(
                                    it,
                                    R.color.black
                                )
                            )
                        }

                        if (!NetworkManager.getInstance().isConnectedToNetwork(activity)) {
                            binding.sendinOTPLayout.sendOTPFailedGroup.visibility = View.GONE
                            binding.sendinOTPLayout.root.visibility = View.GONE
                            binding.unlinkDeviceOTPScreenConstraintLayout.root.visibility = View.VISIBLE
                            binding.unlinkDeviceOTPScreenConstraintLayout.enterOTPSubtitle.text =
                                context?.getString(R.string.internet_waiting_subtitle)
                            retryApiCall = RETRY_GET_OTP
                            return
                        }
                        binding.apply {
                            unlinkDeviceResultScreen.root.visibility = View.GONE
                            sendinOTPLayout.apply {
                                sendOTPProcessingGroup.visibility = View.GONE
                                sendOTPFailedGroup.visibility = View.VISIBLE
                                sendOTPFailedImageView.visibility = View.VISIBLE
                                sendOTPFailedTitle.visibility = View.VISIBLE
                                sendOTPFailedTitle.text = "Failed to send OTP."
                            }
                        }
                    }
                }
            }

            override fun onFailure(error: Throwable?) {
                context?.let {
                    binding.didNotReceiveOTPTextView.setTextColor(
                        ContextCompat.getColor(
                            it,
                            R.color.black
                        )
                    )
                }

                if (!NetworkManager.getInstance().isConnectedToNetwork(activity)) {
                    binding.sendinOTPLayout.sendOTPFailedGroup.visibility = View.GONE
                    binding.sendinOTPLayout.root.visibility = View.GONE
                    binding.unlinkDeviceOTPScreenConstraintLayout.root.visibility = View.VISIBLE
                    binding.unlinkDeviceOTPScreenConstraintLayout.enterOTPSubtitle.text = context?.getString(R.string.internet_waiting_subtitle)
                    retryApiCall = RETRY_GET_OTP
                    return
                }
                binding.apply {
                    unlinkDeviceResultScreen.root.visibility = View.GONE
                    sendinOTPLayout.apply {
                        sendOTPProcessingGroup.visibility = View.GONE
                        sendOTPFailedGroup.visibility = View.VISIBLE
                        sendOTPFailedImageView.visibility = View.VISIBLE
                        sendOTPFailedTitle.visibility = View.VISIBLE
                        sendOTPFailedTitle.text = "Failed to send OTP."
                    }
                }
            }
        }, RetrieveOTPResponse::class.java))
    }

    private fun showValidateOTPError(msg: String) {
        binding.sendinOTPLayout.root.visibility = View.GONE
        binding.unlinkDeviceOTPScreenConstraintLayout.root.visibility = View.VISIBLE
        binding.buttonNext.visibility = View.VISIBLE
        binding.didNotReceiveOTPTextView.visibility = View.VISIBLE

        if (!NetworkManager.getInstance().isConnectedToNetwork(activity)) {
            binding.unlinkDeviceOTPScreenConstraintLayout.enterOTPSubtitle.text = context?.getString(R.string.internet_waiting_subtitle)
            retryApiCall = RETRY_VALIDATE
            return
        }

        setOtpErrorBackground(R.drawable.otp_box_error_background)
        binding.unlinkDeviceOTPScreenConstraintLayout.apply {
            linkDeviceOTPErrorTxt.text = msg
            linkDeviceOTPErrorTxt.visibility = View.VISIBLE
        }
    }

    fun setOtpErrorBackground(drawableId: Int) {
        binding.unlinkDeviceOTPScreenConstraintLayout.apply {
            context?.let { context ->
                ContextCompat.getDrawable(context, drawableId)?.apply {
                    linkDeviceOTPEdtTxt1.setBackgroundResource(drawableId)
                    linkDeviceOTPEdtTxt2.setBackgroundResource(drawableId)
                    linkDeviceOTPEdtTxt3.setBackgroundResource(drawableId)
                    linkDeviceOTPEdtTxt4.setBackgroundResource(drawableId)
                    linkDeviceOTPEdtTxt5.setBackgroundResource(drawableId)
                }
            }
        }
    }

    fun clearOTP() {
        binding.unlinkDeviceOTPScreenConstraintLayout.apply {
            linkDeviceOTPEdtTxt1.text?.clear()
            linkDeviceOTPEdtTxt2.text?.clear()
            linkDeviceOTPEdtTxt3.text?.clear()
            linkDeviceOTPEdtTxt4.text?.clear()
            linkDeviceOTPEdtTxt5.text?.clear()
        }
    }

    private fun callLinkingDeviceAPI() {

        if (!NetworkManager.getInstance().isConnectedToNetwork(activity)) {
            binding.sendinOTPLayout.root.visibility = View.GONE
            binding.unlinkDeviceOTPScreenConstraintLayout.root.visibility = View.VISIBLE
            binding.unlinkDeviceOTPScreenConstraintLayout.enterOTPSubtitle.text = context?.getString(R.string.internet_waiting_subtitle)
            retryApiCall = RETRY_LINK_DEVICE
            return
        }

        showLinkingDeviceProcessing()
        handlePrimaryDevice()
    }

    private fun handlePrimaryDevice() {
        if (deleteOldPrimaryDevice) {
            performDeletePrimaryDevice()
        } else {
            performChangePrimaryDevice(newPrimaryDevice)
        }
    }

    private fun performDeletePrimaryDevice() {
        val oldPrimaryDevice = (activity as? MyPreferencesActivity)?.userPrimaryDevice
        oldPrimaryDevice?.deviceIdentityId?.toString()?.let { deviceIdentityId ->
            OneAppService.deleteDevice(
                deviceIdentityId,
                newPrimaryDevice?.deviceIdentityId.toString(),
                otpNumber,
                otpMethod
            )
                .enqueue(CompletionHandler(object : IResponseListener<ViewAllLinkedDeviceResponse> {
                    override fun onSuccess(response: ViewAllLinkedDeviceResponse?) {
                        handleDeletePrimaryDeviceSuccess(response)
                    }

                    override fun onFailure(error: Throwable?) {
                        handleChangeOrDeletePrimaryDeviceFailure()
                    }
                }, ViewAllLinkedDeviceResponse::class.java))
        }
    }

    private fun handleDeletePrimaryDeviceSuccess(response: ViewAllLinkedDeviceResponse?) {
        binding.sendinOTPLayout.root.visibility = View.GONE
        when (response?.httpCode) {
            AppConstant.HTTP_OK -> {
                val primaryDevice = getPrimaryDevice(response)
                if (primaryDevice != null) {
                    Utils.triggerFireBaseEvents(
                        FirebaseManagerAnalyticsProperties.DEVICESECURITY_LINK_CONFIRMED,
                        hashMapOf(
                            Pair(
                                FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE,
                                FirebaseManagerAnalyticsProperties.PropertyNames.linkDeviceConfirmed
                            )
                        ), activity
                    )
                    showDeviceChanged()

                    primaryDevice.deviceIdentityId?.let { saveDeviceId(it) }

                    setFragmentResult(
                        MyPreferencesFragment.RESULT_LISTENER_LINK_DEVICE, bundleOf(
                            ViewAllLinkedDevicesFragment.IS_UPDATE to true
                        )
                    )
                    Handler().postDelayed({
                        view?.findNavController()?.navigateUp()
                        view?.findNavController()?.navigateUp()
                    }, AppConstant.DELAY_1500_MS)
                } else {
                    handleChangeOrDeletePrimaryDeviceFailure()
                }
            }
            AppConstant.HTTP_SESSION_TIMEOUT_440 ->
                activity?.apply {
                    if (!isFinishing) {
                        SessionUtilities.getInstance().setSessionState(
                            SessionDao.SESSION_STATE.INACTIVE,
                            response.response?.stsParams, this
                        )
                    }
                }
            else -> response?.response?.desc?.let { desc ->
                showValidateOTPError(getString(R.string.icr_wrong_otp_error))
                Handler().postDelayed({
                    binding.unlinkDeviceOTPScreenConstraintLayout.apply {
                        linkDeviceOTPEdtTxt5.requestFocus()
                        val imm: InputMethodManager? =
                            context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                        imm?.showSoftInput(linkDeviceOTPEdtTxt5, InputMethodManager.SHOW_IMPLICIT)
                    }
                }, AppConstant.DELAY_200_MS)
            }
        }
    }

    private fun performChangePrimaryDevice(device: UserDevice?) {
        OneAppService.changePrimaryDeviceApi(
            device?.deviceIdentityId.toString(),
            otpNumber,
            otpMethod
        )
            .enqueue(CompletionHandler(object : IResponseListener<ViewAllLinkedDeviceResponse> {
                override fun onSuccess(response: ViewAllLinkedDeviceResponse?) {
                    handleChangePrimaryDeviceSuccess(response)
                }

                override fun onFailure(error: Throwable?) {
                    handleChangeOrDeletePrimaryDeviceFailure()
                }
            }, ViewAllLinkedDeviceResponse::class.java))
    }

    private fun handleChangePrimaryDeviceSuccess(response: ViewAllLinkedDeviceResponse?) {
        binding.sendinOTPLayout.root.visibility = View.GONE
        when (response?.httpCode) {
            AppConstant.HTTP_OK -> {
                val primaryDevice = getPrimaryDevice(response)
                if (primaryDevice != null) {
                    Utils.triggerFireBaseEvents(
                        FirebaseManagerAnalyticsProperties.DEVICESECURITY_LINK_CONFIRMED,
                        hashMapOf(
                            Pair(
                                FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE,
                                FirebaseManagerAnalyticsProperties.PropertyNames.linkDeviceConfirmed
                            )
                        ), activity
                    )
                    showDeviceChanged()

                    primaryDevice.deviceIdentityId?.let { saveDeviceId(it) }

                    setFragmentResult(
                        MyPreferencesFragment.RESULT_LISTENER_LINK_DEVICE, bundleOf(
                            ViewAllLinkedDevicesFragment.IS_UPDATE to true
                        )
                    )
                    Handler().postDelayed({
                        view?.findNavController()?.navigateUp()
                    }, AppConstant.DELAY_1500_MS)
                } else {
                    handleChangeOrDeletePrimaryDeviceFailure()
                }
            }
            AppConstant.HTTP_SESSION_TIMEOUT_440 ->
                activity?.apply {
                    if (!isFinishing) {
                        SessionUtilities.getInstance().setSessionState(
                            SessionDao.SESSION_STATE.INACTIVE,
                            response.response?.stsParams, this
                        )
                    }
                }
            else -> response?.response?.desc?.let { desc ->
                showValidateOTPError(getString(R.string.icr_wrong_otp_error))
                Handler().postDelayed({
                    binding.unlinkDeviceOTPScreenConstraintLayout.apply {
                        linkDeviceOTPEdtTxt5.requestFocus()
                        val imm: InputMethodManager? =
                            context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                        imm?.showSoftInput(linkDeviceOTPEdtTxt5, InputMethodManager.SHOW_IMPLICIT)
                    }
                }, AppConstant.DELAY_200_MS)
            }
        }
    }

    private fun getPrimaryDevice(response: ViewAllLinkedDeviceResponse): UserDevice? {
        if (response.userDevices != null &&
            response.userDevices?.isNotEmpty() == true
        ) {
            val primaryDevice =
                response.userDevices?.filter { userDevice -> userDevice.primarydDevice == true }
            if (primaryDevice?.isNotEmpty() == true) {
                return primaryDevice[0]
            }
        }
        return null
    }

    private fun handleChangeOrDeletePrimaryDeviceFailure() {
        binding.sendinOTPLayout.root.visibility = View.GONE
        binding.unlinkDeviceOTPScreenConstraintLayout.root.visibility = View.VISIBLE
        binding.buttonNext.visibility = View.VISIBLE
        binding.didNotReceiveOTPTextView.visibility = View.VISIBLE
        showErrorScreen(ErrorHandlerActivity.LINK_DEVICE_FAILED)
    }

    private fun saveDeviceId(deviceIdentityId: Long) {
        if (deviceIdentityId < 0) return
        val currentUserObject = AppInstanceObject.get().currentUserObject
        currentUserObject.linkedDeviceIdentityId = deviceIdentityId
        currentUserObject.save()
    }

    private fun showDeviceChanged() {
        binding.sendinOTPLayout.root.visibility = View.GONE
        binding.unlinkDeviceOTPScreenConstraintLayout.root.visibility = View.GONE
        binding.unlinkDeviceResultScreen.unlinkDeviceResultSubtitle.visibility = View.GONE
        binding.unlinkDeviceResultScreen.root.visibility = View.VISIBLE
        context?.let {
            if (deleteOldPrimaryDevice) {
                binding.unlinkDeviceResultScreen.unlinkDeviceResultTitle.text = it.getString(R.string.unlink_device_result_success)
            } else {
                binding.unlinkDeviceResultScreen.unlinkDeviceResultTitle.text =
                    it.getString(R.string.changing_primary_device_result_success)
            }
        }
    }

    private fun showLinkingDeviceProcessing() {
        binding.sendinOTPLayout.root.visibility = View.GONE
        binding.unlinkDeviceOTPScreenConstraintLayout.root.visibility = View.GONE
        binding.buttonNext.visibility = View.GONE
        binding.didNotReceiveOTPTextView.visibility = View.GONE
        binding.sendinOTPLayout.sendOTPTitle.visibility = View.GONE
        binding.sendinOTPLayout.sendOTPSubtitle.visibility = View.GONE

        context?.let {
            binding.sendinOTPLayout.sendOTPProcessingReq.text = it.getString(R.string.changing_preferred_device_processing)
        }
        binding.sendinOTPLayout.root.visibility = View.VISIBLE
    }


    private fun showSendingOTPProcessing() {
        binding.sendinOTPLayout.sendOTPTitle.visibility = View.VISIBLE
        binding.sendinOTPLayout.sendOTPSubtitle.visibility = View.VISIBLE
        binding.unlinkDeviceOTPScreenConstraintLayout.root.visibility = View.GONE
        binding.sendinOTPLayout.sendOTPFailedGroup.visibility = View.GONE

        context?.let {
            binding.sendinOTPLayout.sendOTPProcessingReq.text = it.getString(R.string.link_device_sending_otp_processing)
        }
        binding.sendinOTPLayout.root.visibility = View.VISIBLE
    }

    private fun showErrorScreen(errorType: Int, errorMessage: String = "") {

        if (!NetworkManager.getInstance().isConnectedToNetwork(activity)) {
            retryApiCall = RETRY_LINK_DEVICE
        }

        activity?.let {
            val intent = Intent(it, ErrorHandlerActivity::class.java)
            intent.putExtra("errorType", errorType)
            intent.putExtra("errorMessage", errorMessage)
            it.startActivityForResult(intent, ErrorHandlerActivity.ERROR_PAGE_REQUEST_CODE)
        }
    }

    private fun connectionDetector() {
        mConnectionBroadCast = Utils.connectionBroadCast(activity, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_CANCELED) {
            Handler().postDelayed({
                binding.unlinkDeviceOTPScreenConstraintLayout.linkDeviceOTPEdtTxt5.requestFocus()
                val imm: InputMethodManager? =
                    context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm?.showSoftInput(binding.unlinkDeviceOTPScreenConstraintLayout.linkDeviceOTPEdtTxt5, InputMethodManager.SHOW_IMPLICIT)
            }, AppConstant.DELAY_200_MS)
            binding.unlinkDeviceOTPScreenConstraintLayout.root.visibility = View.VISIBLE
        }

        when (requestCode) {
            ErrorHandlerActivity.ERROR_PAGE_REQUEST_CODE -> {
                when (resultCode) {
                    ErrorHandlerActivity.RESULT_RETRY -> {
                        callLinkingDeviceAPI()
                    }
                    ErrorHandlerActivity.RESULT_CALL_CENTER -> {
                        Utils.makeCall(AppConstant.WOOLWOORTH_CALL_CENTER_NUMBER)
                        setFragmentResult(
                            MyPreferencesFragment.RESULT_LISTENER_LINK_DEVICE, bundleOf(
                                MyPreferencesFragment.IS_DEVICE_LINKED to false
                            )
                        )
                        view?.findNavController()?.navigateUp()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.registerReceiver(
            mConnectionBroadCast,
            IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        )
    }

    override fun onPause() {
        super.onPause()
        activity?.unregisterReceiver(mConnectionBroadCast)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelRetrofitRequest(mLinkDeviceOTPReq)
        activity?.runOnUiThread { activity?.window?.clearFlags(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE) }
    }

    override fun onConnectionChanged() {
        val isConnected = NetworkManager.getInstance().isConnectedToNetwork(activity)

        if (!isConnected) {
            ErrorHandlerView(activity).showToast()
        } else {
            when (retryApiCall) {
                RETRY_GET_OTP -> {
                    callGetOTPAPI(otpMethod)
                }
                RETRY_VALIDATE -> {
                    makeValidateOTPRequest()
                }
                RETRY_LINK_DEVICE -> {
                    callLinkingDeviceAPI()
                }
            }
        }

    }
}