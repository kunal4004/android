package za.co.woolworths.financial.services.android.ui.fragments.mypreferences

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Paint
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentLinkDeviceOtpBinding
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.linkdevice.LinkedDeviceResponse
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.models.dto.otp.RetrieveOTPResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.AbsaStatementsActivity
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity
import za.co.woolworths.financial.services.android.ui.activities.MyPreferencesInterface
import za.co.woolworths.financial.services.android.ui.activities.account.LinkDeviceConfirmationActivity
import za.co.woolworths.financial.services.android.ui.activities.account.LinkDeviceConfirmationInterface
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.extension.cancelRetrofitRequest
import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.personal_loan.PersonalLoanFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.card.AccountsOptionFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.device_security.updateLinkedDevices
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity.Companion.ACTIVATE_VIRTUAL_CARD_DETAIL
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity.Companion.BLOCK_CARD_DETAIL
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity.Companion.FREEZE_CARD_DETAIL
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity.Companion.GET_REPLACEMENT_CARD_DETAIL
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity.Companion.PAY_WITH_CARD_DETAIL
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity.Companion.SHOW_ACTIVATE_VIRTUAL_CARD_SCREEN
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity.Companion.SHOW_BLOCK_CARD_SCREEN
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity.Companion.SHOW_GET_REPLACEMENT_CARD_SCREEN
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity.Companion.SHOW_PAY_WITH_CARD_SCREEN
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity.Companion.SHOW_TEMPORARY_FREEZE_DIALOG
import za.co.woolworths.financial.services.android.ui.fragments.npc.OTPViewTextWatcher
import za.co.woolworths.financial.services.android.ui.fragments.statement.StatementFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.EnableLocationSettingsFragment
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_device_security.navigation.DeviceSecurityActivityResult.Companion.RESULT_CODE_LINK_DEVICE
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding
import za.co.woolworths.financial.services.android.util.location.DynamicGeocoder
import za.co.woolworths.financial.services.android.util.location.Event
import za.co.woolworths.financial.services.android.util.location.EventType
import za.co.woolworths.financial.services.android.util.location.Locator
import za.co.woolworths.financial.services.android.util.pushnotification.NotificationUtils

class LinkDeviceOTPFragment :
    BaseFragmentBinding<FragmentLinkDeviceOtpBinding>(FragmentLinkDeviceOtpBinding::inflate),
    View.OnClickListener, NetworkChangeListener {

    companion object {
        private const val OTP_CALL_CENTER = "CALL CENTER"
        const val RETRY_GET_OTP: String = "GET_OTP"
        const val RETRY_VALIDATE: String = "VALIDATE_OTP"
        const val RETRY_LINK_DEVICE: String = "LINK_DEVICE"
    }

    private lateinit var locator: Locator
    private var mConnectionBroadCast: BroadcastReceiver? = null
    private var mApplyNowState: ApplyNowState? = null
    private var otpNumber: String? = null
    private var otpSMSNumber: String? = null
    private var retryApiCall: String? = null
    private var otpMethod: String? = OTPMethodType.SMS.name
    private var currentLocation: Location? = null
    private var mLinkDeviceOTPReq: Call<RetrieveOTPResponse>? = null
    private val mKeyListener = View.OnKeyListener { v, keyCode, event ->
        binding.linkDeviceOTPScreen.apply {
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
            }
        }
        false
    }

    private fun validateNextButton() {
        binding.linkDeviceOTPScreen.apply {
        if (TextUtils.isEmpty(linkDeviceOTPEdtTxt5.text) || TextUtils.isEmpty(linkDeviceOTPEdtTxt4.text)
            || TextUtils.isEmpty(linkDeviceOTPEdtTxt3.text) || TextUtils.isEmpty(
                linkDeviceOTPEdtTxt2.text
            )
            || TextUtils.isEmpty(linkDeviceOTPEdtTxt1.text)
        ) {
            disableNextButton()
        } else {
            enableNextButton()
        }

        if (TextUtils.isEmpty(linkDeviceOTPEdtTxt5.text) && TextUtils.isEmpty(linkDeviceOTPEdtTxt4.text)
            && TextUtils.isEmpty(linkDeviceOTPEdtTxt3.text) && TextUtils.isEmpty(
                linkDeviceOTPEdtTxt2.text
            )
            && TextUtils.isEmpty(linkDeviceOTPEdtTxt1.text)
        ) {
            clearErrorMessage()
        }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mApplyNowState =
                it.getSerializable(AccountSignedInPresenterImpl.APPLY_NOW_STATE) as? ApplyNowState
                    ?: ApplyNowState.STORE_CARD
        }
    }

    private fun resetOTPView() {
        clearOTP()
        clearErrorMessage()
    }

    private fun clearErrorMessage() {
        binding.linkDeviceOTPScreen.apply {
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

        activity?.apply {
            locator = Locator(activity as AppCompatActivity)
        }

        connectionDetector()

        binding.didNotReceiveOTPTextView.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.didNotReceiveOTPTextView.setOnClickListener(this)
        binding.linkDeviceOTPScreen.apply {

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

                makeLinkDeviceRequest()
            }
        }
    }

    private fun makeLinkDeviceRequest() {
        binding.linkDeviceOTPScreen.apply {

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

        binding.linkDeviceOTPScreen.root.visibility = View.GONE
        binding.sendinOTPLayout.root.visibility = View.GONE

        callLinkingDeviceAPI()
        }
    }


    private fun isRetrieveOTPCallInProgress(): Boolean = binding.sendinOTPLayout.root.visibility == View.VISIBLE

    private fun getNumberFromEditText(numberEditText: EditText?) = numberEditText?.text?.toString()
        ?: ""

    private fun callGetOTPAPI(otpMethod: String?) {
        mLinkDeviceOTPReq = otpMethod?.let { type -> OneAppService().getLinkDeviceOtp(type) }
        this.otpMethod = otpMethod

        if (!NetworkManager.getInstance().isConnectedToNetwork(activity)) {
            binding.linkDeviceOTPScreen.enterOTPSubtitle.text = context?.getString(R.string.internet_waiting_subtitle)
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
                        binding.linkDeviceOTPScreen.root.visibility = View.VISIBLE
                        retrieveOTPResponse.otpSentTo?.let {
                            if (otpMethod.equals(OTPMethodType.SMS.name, true)) {
                                otpSMSNumber = it
                            }
                            binding.linkDeviceOTPScreen.enterOTPSubtitle.text =
                                activity?.resources?.getString(R.string.sent_otp_desc, it)
                            Handler().postDelayed({
                                binding.linkDeviceOTPScreen.linkDeviceOTPEdtTxt1.requestFocus()
                                val imm: InputMethodManager? =
                                    context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                                imm?.showSoftInput(
                                    binding.linkDeviceOTPScreen.linkDeviceOTPEdtTxt1,
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
                            binding.linkDeviceOTPScreen.root.visibility = View.VISIBLE
                            binding.linkDeviceOTPScreen.enterOTPSubtitle.text =
                                context?.getString(R.string.internet_waiting_subtitle)
                            retryApiCall = RETRY_GET_OTP
                            return
                        }
                        binding.sendinOTPLayout.sendOTPProcessingGroup.visibility = View.GONE
                        binding.linkDeviceResultScreen.root.visibility = View.GONE
                        binding.sendinOTPLayout.sendOTPFailedGroup.visibility = View.VISIBLE
                        binding.sendinOTPLayout.sendOTPFailedImageView.visibility = View.VISIBLE
                        binding.sendinOTPLayout.sendOTPFailedTitle.visibility = View.VISIBLE
                        binding.sendinOTPLayout.sendOTPFailedTitle.text = "Failed to send OTP."
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
                    binding.linkDeviceOTPScreen.root.visibility = View.VISIBLE
                    binding.linkDeviceOTPScreen.enterOTPSubtitle.text = context?.getString(R.string.internet_waiting_subtitle)
                    retryApiCall = RETRY_GET_OTP
                    return
                }

                binding.sendinOTPLayout.sendOTPProcessingGroup.visibility = View.GONE
                binding.linkDeviceResultScreen.root.visibility = View.GONE
                binding.sendinOTPLayout.sendOTPFailedGroup.visibility = View.VISIBLE
                binding.sendinOTPLayout.sendOTPFailedImageView.visibility = View.VISIBLE
                binding.sendinOTPLayout.sendOTPFailedTitle.visibility = View.VISIBLE
                binding.sendinOTPLayout.sendOTPFailedTitle.text = "Failed to send OTP."
            }
        }, RetrieveOTPResponse::class.java))
    }

    private fun showValidateOTPError(msg: String) {
        binding.sendinOTPLayout.root.visibility = View.GONE
        binding.linkDeviceOTPScreen.root.visibility = View.VISIBLE
        binding.buttonNext.visibility = View.VISIBLE
        binding.didNotReceiveOTPTextView.visibility = View.VISIBLE
        binding.linkDeviceOTPScreen.apply {
            if (!NetworkManager.getInstance().isConnectedToNetwork(activity)) {
                enterOTPSubtitle.text = context?.getString(R.string.internet_waiting_subtitle)
                retryApiCall = RETRY_VALIDATE
                return
            }

            setOtpErrorBackground(R.drawable.otp_box_error_background)
            linkDeviceOTPErrorTxt.text = msg
            linkDeviceOTPErrorTxt.visibility = View.VISIBLE
        }
    }

    fun setOtpErrorBackground(drawableId: Int) {
        context?.let { context ->
            ContextCompat.getDrawable(context, drawableId)?.apply {
                binding.linkDeviceOTPScreen.apply {
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
        binding.linkDeviceOTPScreen.apply {
            linkDeviceOTPEdtTxt1.text?.clear()
            linkDeviceOTPEdtTxt2.text?.clear()
            linkDeviceOTPEdtTxt3.text?.clear()
            linkDeviceOTPEdtTxt4.text?.clear()
            linkDeviceOTPEdtTxt5.text?.clear()
        }
    }

    private fun callLinkingDeviceAPI(checkForLocationPermission: Boolean = true) {
        NetworkManager.getInstance()?.let {
            if (!it.isConnectedToNetwork(activity)) {
                binding.sendinOTPLayout.root.visibility = View.GONE
                binding.linkDeviceOTPScreen.root.visibility = View.VISIBLE
                binding.linkDeviceOTPScreen.enterOTPSubtitle.text = context?.getString(R.string.internet_waiting_subtitle)
                retryApiCall = RETRY_LINK_DEVICE
                return
            }
        }

        showLinkingDeviceProcessing()

        if (currentLocation == null && checkForLocationPermission) {
            startLocationDiscoveryProcess()
            return
        }

        locator.stopService()
        retrieveTokenAndCallLinkDevice()
    }

    private fun startLocationDiscoveryProcess() {
        locator.getCurrentLocation { locationEvent ->
            when (locationEvent) {
                is Event.Location -> handleLocationEvent(locationEvent)
                is Event.Permission -> handlePermissionEvent(locationEvent)
            }
        }
    }

    private fun handleLocationEvent(locationEvent: Event.Location) {
        Utils.saveLastLocation(locationEvent.locationData, context)
        currentLocation = locationEvent.locationData
        callLinkingDeviceAPI(checkForLocationPermission = false)
    }

    private fun handlePermissionEvent(permissionEvent: Event.Permission) {
        if (permissionEvent.event == EventType.LOCATION_PERMISSION_NOT_GRANTED) {
            Utils.saveLastLocation(null, activity)
        }
        callLinkingDeviceAPI(checkForLocationPermission = false)
    }

    private fun retrieveTokenAndCallLinkDevice() {
        if (TextUtils.isEmpty(Utils.getToken())) {
            context?.let { context ->
                NotificationUtils.getTokenFromMessagingService(
                    context,
                    onSuccessCallback = { token ->
                        Utils.setToken(token)
                        sendTokenToLinkDevice(token)
                    },
                    onFailureCallback = {
                        showErrorScreen(ErrorHandlerActivity.LINK_DEVICE_FAILED)
                    }
                )
            } ?: kotlin.run {
                showErrorScreen(ErrorHandlerActivity.LINK_DEVICE_FAILED)
            }
        } else {
            sendTokenToLinkDevice(Utils.getToken())
        }
    }

    private fun sendTokenToLinkDevice(token: String) {
        binding.linkDeviceOTPScreen.root.visibility = View.GONE
        DynamicGeocoder.getAddressFromLocation(
            context,
            currentLocation?.latitude,
            currentLocation?.longitude
        ) { address ->
            var locationAddress = address?.let {
                arrayOf(
                    it.city,
                    it.suburb,
                    it.countryName
                ).filter { item -> !item.isNullOrEmpty() }.joinToString(separator = ", ")
            } ?: null

            OneAppService().linkDeviceApi(
                KotlinUtils.getUserDefinedDeviceName(activity),
                Utils.getUniqueDeviceID(),
                locationAddress,
                true,
                token,
                if (Utils.isGooglePlayServicesAvailable()) NotificationUtils.TOKEN_PROVIDER_FIREBASE else NotificationUtils.TOKEN_PROVIDER_HMS,
                otpNumber,
                otpMethod
            )
                .enqueue(CompletionHandler(object : IResponseListener<LinkedDeviceResponse> {
                    override fun onSuccess(response: LinkedDeviceResponse?) {
                        binding.sendinOTPLayout.root.visibility = View.GONE
                        when (response?.httpCode) {
                            AppConstant.HTTP_OK_201.toString() -> {
                                activity?.apply {
                                    Utils.triggerFireBaseEvents(
                                        FirebaseManagerAnalyticsProperties.DEVICESECURITY_LINK_CONFIRMED,
                                        hashMapOf(
                                            Pair(
                                                FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE,
                                                FirebaseManagerAnalyticsProperties.PropertyNames.linkDeviceConfirmed
                                            )
                                        ), this
                                    )
                                }

                                if (!isAdded) {
                                    return
                                }
                                showDeviceLinked()
                                response.deviceIdentityToken?.let {
                                    SessionUtilities.getInstance().deviceIdentityToken =
                                        it
                                }
                                response.deviceIdentityId?.let { saveDeviceId(it) }

                                WoolworthsApplication
                                    .getInstance()
                                    .bus()
                                    .send(response)

                                setFragmentResult(
                                    MyPreferencesFragment.RESULT_LISTENER_LINK_DEVICE, bundleOf(
                                        MyPreferencesFragment.IS_DEVICE_LINKED to true
                                    )
                                )
                                Handler().postDelayed({

                                    // This will execute only when linking comes from account products.
                                    mApplyNowState?.let {
                                        activity?.apply {
                                            if (this is LinkDeviceConfirmationActivity) {
                                                updateLinkedDevices()
                                                when (mApplyNowState) {
                                                    ApplyNowState.STORE_CARD,
                                                    ApplyNowState.PERSONAL_LOAN -> {
                                                        when {
                                                            FREEZE_CARD_DETAIL -> {
                                                                showFreezeStoreCardDialog()
                                                            }
                                                            BLOCK_CARD_DETAIL -> {
                                                                showBlockStoreCardScreen()
                                                            }
                                                            PAY_WITH_CARD_DETAIL -> {
                                                                showPayWithCardScreen()
                                                            }
                                                            GET_REPLACEMENT_CARD_DETAIL -> {
                                                                showGetReplacementStoreCardScreen()
                                                            }
                                                            StatementFragment.VIEW_STATEMENT_DETAIL -> {
                                                                showSendStatementScreen()
                                                            }
                                                            ACTIVATE_VIRTUAL_CARD_DETAIL -> {
                                                                showActivateVirtualTempCardScreen()
                                                            }

                                                            PersonalLoanFragment.PL_WITHDRAW_FUNDS_DETAIL -> {
                                                                showPersonalLoanWithdrawFundsScreen()
                                                            }
                                                            StatementFragment.SEND_STATEMENT_DETAIL -> {
                                                                sendStatementToEmailScreen()
                                                            }
                                                            else -> {
                                                                goToProduct()
                                                            }
                                                        }
                                                    }

                                                    ApplyNowState.SILVER_CREDIT_CARD,
                                                    ApplyNowState.BLACK_CREDIT_CARD,
                                                    ApplyNowState.GOLD_CREDIT_CARD -> {
                                                        when {
                                                            AbsaStatementsActivity.VIEW_ABSA_CC_STATEMENT_DETAIL -> {
                                                                showViewAbsaCCStatementScreen()
                                                            }
                                                            AccountsOptionFragment.CREDIT_CARD_ACTIVATION_DETAIL -> {
                                                                activateCreditCard()
                                                            }
                                                            AccountsOptionFragment.CREDIT_CARD_SHECULE_OR_MANAGE -> {
                                                                scheduleOrManageCC()
                                                            }

                                                            else -> goToProduct()
                                                        }
                                                    }

                                                    else -> goToProduct()
                                                }
                                            }
                                        }
                                        return@postDelayed
                                    }

                                    view?.findNavController()?.navigateUp()
                                }, AppConstant.DELAY_1500_MS)

                            }
                            AppConstant.HTTP_SESSION_TIMEOUT_440.toString() ->
                                activity?.apply {
                                    if (!isFinishing) {
                                        SessionUtilities.getInstance().setSessionState(
                                            SessionDao.SESSION_STATE.INACTIVE,
                                            response.response.stsParams,
                                            this
                                        )
                                    }
                                }
                            else -> response?.response?.desc?.let { desc ->
                                activity?.let { showValidateOTPError(it.getString(R.string.icr_wrong_otp_error)) }
                                Handler().postDelayed({
                                    binding.linkDeviceOTPScreen.linkDeviceOTPEdtTxt5.requestFocus()
                                    val imm: InputMethodManager? =
                                        context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                                    imm?.showSoftInput(
                                        binding.linkDeviceOTPScreen.linkDeviceOTPEdtTxt5,
                                        InputMethodManager.SHOW_IMPLICIT
                                    )
                                }, AppConstant.DELAY_200_MS)
                            }
                        }
                    }

                    override fun onFailure(error: Throwable?) {
                        binding.sendinOTPLayout.root.visibility = View.GONE
                        binding.linkDeviceOTPScreen.root.visibility = View.VISIBLE
                        binding.buttonNext.visibility = View.VISIBLE
                        binding.didNotReceiveOTPTextView.visibility = View.VISIBLE
                        showErrorScreen(ErrorHandlerActivity.LINK_DEVICE_FAILED)
                    }
                }, LinkedDeviceResponse::class.java))
        }
    }

    private fun showFreezeStoreCardDialog() {
        SHOW_TEMPORARY_FREEZE_DIALOG = true
        FREEZE_CARD_DETAIL = false
        activity?.finish()
    }

    private fun showBlockStoreCardScreen() {
        SHOW_BLOCK_CARD_SCREEN = true
        BLOCK_CARD_DETAIL = false
        activity?.finish()
    }

    private fun showPayWithCardScreen() {
        SHOW_PAY_WITH_CARD_SCREEN = true
        PAY_WITH_CARD_DETAIL = false
        activity?.finish()
    }

    private fun showGetReplacementStoreCardScreen() {
        SHOW_GET_REPLACEMENT_CARD_SCREEN = true
        GET_REPLACEMENT_CARD_DETAIL = false
        activity?.finish()
    }

    private fun showSendStatementScreen() {
        StatementFragment.SHOW_VIEW_STATEMENT_SCREEN = true
        StatementFragment.VIEW_STATEMENT_DETAIL = false
        activity?.finish()
    }

    private fun sendStatementToEmailScreen() {
        StatementFragment.SEND_STATEMENT_SCREEN = true
        StatementFragment.SEND_STATEMENT_DETAIL = false
        activity?.finish()
    }

    private fun showActivateVirtualTempCardScreen() {
        ACTIVATE_VIRTUAL_CARD_DETAIL = false
        SHOW_ACTIVATE_VIRTUAL_CARD_SCREEN = true
        activity?.finish()
    }

    private fun showPersonalLoanWithdrawFundsScreen() {
        PersonalLoanFragment.SHOW_PL_WITHDRAW_FUNDS_SCREEN = true
        PersonalLoanFragment.PL_WITHDRAW_FUNDS_DETAIL = false
        activity?.finish()
    }

    private fun showViewAbsaCCStatementScreen() {
        AbsaStatementsActivity.SHOW_VIEW_ABSA_CC_STATEMENT_SCREEN = true
        AbsaStatementsActivity.VIEW_ABSA_CC_STATEMENT_DETAIL = false
        activity?.finish()
    }

    private fun activateCreditCard() {
        AccountsOptionFragment.SHOW_CREDIT_CARD_ACTIVATION_SCREEN = true
        AccountsOptionFragment.CREDIT_CARD_ACTIVATION_DETAIL = false
        activity?.finish()
    }

    private fun scheduleOrManageCC() {
        AccountsOptionFragment.SHOW_CREDIT_CARD_SHECULE_OR_MANAGE = true
        AccountsOptionFragment.CREDIT_CARD_SHECULE_OR_MANAGE = false
        activity?.finish()
    }

    private fun goToProduct() {
        val intent = Intent()
        intent.putExtra(AccountSignedInPresenterImpl.APPLY_NOW_STATE, mApplyNowState)
        intent.putExtra(MyPreferencesFragment.RESULT_LISTENER_LINK_DEVICE, true)
        activity?.setResult(RESULT_CODE_LINK_DEVICE, intent)
        activity?.finish()
    }

    private fun saveDeviceId(deviceIdentityId: Long) {
        if (deviceIdentityId < 0) return
        val currentUserObject = AppInstanceObject.get().currentUserObject
        currentUserObject.linkedDeviceIdentityId = deviceIdentityId
        currentUserObject.save()
    }

    private fun showDeviceLinked() {
        binding.sendinOTPLayout.root.visibility = View.GONE
        binding.linkDeviceOTPScreen.root.visibility = View.GONE
        binding.linkDeviceResultScreen.linkDeviceResultSubitle.visibility = View.GONE
        binding.linkDeviceResultScreen.root.visibility = View.VISIBLE
    }

    private fun showLinkingDeviceProcessing() {
        binding.buttonNext.visibility = View.GONE
        binding.sendinOTPLayout.sendOTPTitle.visibility = View.GONE
        binding.sendinOTPLayout.sendOTPSubtitle.visibility = View.GONE
        binding.didNotReceiveOTPTextView.visibility = View.GONE

        context?.let {
            binding.sendinOTPLayout.sendOTPProcessingReq.text = it.getString(R.string.link_device_linking_processing)
        }
        binding.sendinOTPLayout.root.visibility = View.VISIBLE
    }


    private fun showSendingOTPProcessing() {
        binding.sendinOTPLayout.sendOTPTitle.visibility = View.VISIBLE
        binding.sendinOTPLayout.sendOTPSubtitle.visibility = View.VISIBLE
        binding.linkDeviceOTPScreen.root.visibility = View.GONE
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

        when (requestCode) {
            EnableLocationSettingsFragment.ACCESS_MY_LOCATION_REQUEST_CODE -> {
                startLocationDiscoveryProcess()
            }
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
            else ->
                when (resultCode) {
                    Activity.RESULT_CANCELED -> {
                        Handler().postDelayed({
                            binding.linkDeviceOTPScreen.linkDeviceOTPEdtTxt5.requestFocus()
                            val imm: InputMethodManager? =
                                context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                            imm?.showSoftInput(
                                binding.linkDeviceOTPScreen.linkDeviceOTPEdtTxt5,
                                InputMethodManager.SHOW_IMPLICIT
                            )
                        }, AppConstant.DELAY_200_MS)
                        binding.linkDeviceOTPScreen.root.visibility = View.VISIBLE
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
                    makeLinkDeviceRequest()
                }
                RETRY_LINK_DEVICE -> {
                    callLinkingDeviceAPI()
                }
            }
        }

    }
}