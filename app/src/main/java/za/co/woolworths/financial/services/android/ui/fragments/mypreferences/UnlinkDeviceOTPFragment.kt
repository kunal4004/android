package za.co.woolworths.financial.services.android.ui.fragments.mypreferences

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Paint
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import com.awfs.coordination.R
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.enter_otp_fragment.*
import kotlinx.android.synthetic.main.fragment_enter_otp.buttonNext
import kotlinx.android.synthetic.main.fragment_enter_otp.didNotReceiveOTPTextView
import kotlinx.android.synthetic.main.fragment_link_device_otp.*
import kotlinx.android.synthetic.main.fragment_unlink_device_otp.*
import kotlinx.android.synthetic.main.fragment_unlink_device_otp.sendinOTPLayout
import kotlinx.android.synthetic.main.layout_link_device_result.*
import kotlinx.android.synthetic.main.layout_link_device_validate_otp.*
import kotlinx.android.synthetic.main.layout_sending_otp_request.*
import kotlinx.android.synthetic.main.layout_unlink_device_result.*
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.linkdevice.LinkDeviceValidateBody
import za.co.woolworths.financial.services.android.models.dto.linkdevice.ViewAllLinkedDeviceResponse
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.models.dto.otp.RetrieveOTPResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity
import za.co.woolworths.financial.services.android.ui.activities.MyPreferencesInterface
import za.co.woolworths.financial.services.android.ui.activities.account.LinkDeviceConfirmationInterface
import za.co.woolworths.financial.services.android.ui.extension.cancelRetrofitRequest
import za.co.woolworths.financial.services.android.ui.fragments.npc.OTPViewTextWatcher
import za.co.woolworths.financial.services.android.util.*
import java.util.*


class UnlinkDeviceOTPFragment : Fragment(), View.OnClickListener, NetworkChangeListener {

    private var deviceIdentityId: String = ""
    private var mConnectionBroadCast: BroadcastReceiver? = null
    private var otpNumber: String? = null
    private var retryApiCall: String? = null
    private var otpMethod: String? = "SMS"
    private var currentLocation: Location? = null
    private var isOTPValidated: Boolean = false
    private var unlinkOrDeleteDeviceReq: Call<ViewAllLinkedDeviceResponse>? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationRequest = createLocationRequest()
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            for (location in locationResult.locations) {
                this@UnlinkDeviceOTPFragment.currentLocation = location
                stopLocationUpdates()
                callUnlinkingDeviceAPI()
                break
            }
        }
    }
    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher. You can use either a val, as shown in this snippet,
    // or a lateinit var in your onAttach() or onCreate() method.
    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                askToEnableLocationSettings()
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                callGetOTPAPI(OTPMethodType.SMS.name)
            }
        }
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

        if (TextUtils.isEmpty(linkDeviceOTPEdtTxt5.text) && TextUtils.isEmpty(linkDeviceOTPEdtTxt4.text)
                && TextUtils.isEmpty(linkDeviceOTPEdtTxt3.text) && TextUtils.isEmpty(linkDeviceOTPEdtTxt2.text)
                && TextUtils.isEmpty(linkDeviceOTPEdtTxt1.text)) {
            clearErrorMessage()
        }
    }

    private var mLinkDeviceOTPReq: Call<RetrieveOTPResponse>? = null
    private var mValidateLinkDeviceOTPReq: Call<RetrieveOTPResponse>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            deviceIdentityId = it.getString(ViewAllLinkedDevicesFragment.DEVICE, null)
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

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_unlink_device_otp, container, false)
    }

    private fun resetOTPView() {
        clearOTP()
        clearErrorMessage()
    }

    private fun clearErrorMessage() {
        if (linkDeviceOTPErrorTxt?.visibility == View.VISIBLE) {
            linkDeviceOTPErrorTxt?.visibility = View.GONE
            setOtpErrorBackground(R.drawable.otp_box_background_focus_selector)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setToolbar()

        activity?.apply {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        }

        connectionDetector()

        didNotReceiveOTPTextView?.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        didNotReceiveOTPTextView?.setOnClickListener(this)

        linkDeviceOTPEdtTxt1?.addTextChangedListener(OTPViewTextWatcher(linkDeviceOTPEdtTxt1, linkDeviceOTPEdtTxt1, linkDeviceOTPEdtTxt2) { validateNextButton() })
        linkDeviceOTPEdtTxt2?.addTextChangedListener(OTPViewTextWatcher(linkDeviceOTPEdtTxt1, linkDeviceOTPEdtTxt2, linkDeviceOTPEdtTxt3) { validateNextButton() })
        linkDeviceOTPEdtTxt3?.addTextChangedListener(OTPViewTextWatcher(linkDeviceOTPEdtTxt2, linkDeviceOTPEdtTxt3, linkDeviceOTPEdtTxt4) { validateNextButton() })
        linkDeviceOTPEdtTxt4?.addTextChangedListener(OTPViewTextWatcher(linkDeviceOTPEdtTxt3, linkDeviceOTPEdtTxt4, linkDeviceOTPEdtTxt5) { validateNextButton() })
        linkDeviceOTPEdtTxt5?.addTextChangedListener(OTPViewTextWatcher(linkDeviceOTPEdtTxt4, linkDeviceOTPEdtTxt5, linkDeviceOTPEdtTxt5) { validateNextButton() })

        linkDeviceOTPEdtTxt1?.setOnKeyListener(mKeyListener)
        linkDeviceOTPEdtTxt2?.setOnKeyListener(mKeyListener)
        linkDeviceOTPEdtTxt3?.setOnKeyListener(mKeyListener)
        linkDeviceOTPEdtTxt4?.setOnKeyListener(mKeyListener)
        linkDeviceOTPEdtTxt5?.setOnKeyListener(mKeyListener)

        buttonNext?.setOnClickListener(this)

        askLocationPermission()
    }

    private fun askLocationPermission() {
        context?.let { context ->
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // You can use the API that requires the permission.
                    askToEnableLocationSettings()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                    // In an educational UI, explain to the user why your app requires this
                    // permission for a specific feature to behave as expected. In this UI,
                    // include a "cancel" or "no thanks" button that allows the user to
                    // continue using your app without granting the permission.
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
                else -> {
                    // You can directly ask for the permission.
                    // The registered ActivityResultCallback gets the result of this request.
                    requestPermissionLauncher.launch(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                }
            }
        }

    }


    private fun askToEnableLocationSettings() {
        activity?.apply {
            Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.DEVICESECURITY_DELETE,
                hashMapOf(
                    Pair(
                        FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE,
                        FirebaseManagerAnalyticsProperties.PropertyNames.linkDeviceInitiated
                    )
                ), this
            )
            val locationRequest = LocationRequest.create()?.apply {
                interval = 100
                fastestInterval = 500
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            val client: SettingsClient = LocationServices.getSettingsClient(this)
            val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
            task.addOnSuccessListener { locationSettingsResponse ->
                // All location settings are satisfied. The client can initialize
                // location requests here.
                callGetOTPAPI(OTPMethodType.SMS.name)
            }

            task.addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        exception.startResolutionForResult(
                            this,
                            FuseLocationAPISingleton.REQUEST_CHECK_SETTINGS
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error.
                    }
                }
                //Even if fails to enable location settings navigate to link device
                callGetOTPAPI(OTPMethodType.SMS.name)
            }

        }

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
        buttonNext?.isEnabled = true
        context?.let { buttonNext?.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.next_button_icon)) }
    }

    private fun disableNextButton() {
        buttonNext?.isEnabled = false
        context?.let { buttonNext?.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.next_button_inactive)) }
    }

    companion object {
        private const val OTP_CALL_CENTER = "CALL CENTER"
        const val RETRY_GET_OTP: String = "GET_OTP"
        const val RETRY_VALIDATE: String = "VALIDATE_OTP"
        const val RETRY_UNLINK_DEVICE: String = "UNLINK_DEVICE"
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.didNotReceiveOTPTextView -> {
                if (!isRetrieveOTPCallInProgress()) {
                    v?.isEnabled = false
                    Handler().postDelayed({ v?.isEnabled = true }, AppConstant.DELAY_1000_MS)
                    view?.findNavController()?.navigate(R.id.action_unlinkDeviceOTPFragment_to_resendOTPBottomSheetFragment, bundleOf(
                            ResendOTPBottomSheetFragment.OTP_NUMBER to otpNumber
                    ))
                }
            }
            R.id.buttonNext -> {

                makeValidateOTPRequest()
            }
        }
    }

    private fun makeValidateOTPRequest() {

        val otpNumber = getNumberFromEditText(linkDeviceOTPEdtTxt1)
                .plus(getNumberFromEditText(linkDeviceOTPEdtTxt2))
                .plus(getNumberFromEditText(linkDeviceOTPEdtTxt3))
                .plus(getNumberFromEditText(linkDeviceOTPEdtTxt4))
                .plus(getNumberFromEditText(linkDeviceOTPEdtTxt5))

        if (TextUtils.isEmpty(otpNumber) || otpNumber.length < 5) {
            return
        }

        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(linkDeviceOTPEdtTxt5?.windowToken, 0)

        callValidatingOTPAPI(otpNumber)
    }


    private fun isRetrieveOTPCallInProgress(): Boolean = sendinOTPLayout?.visibility == View.VISIBLE

    fun createLocationRequest(): LocationRequest? {
        return LocationRequest.create().apply {
            interval = 100
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun startLocationUpdates() {
        context?.apply {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper())
        }
    }

    @SuppressLint("NewApi")
    private fun checkLocationPermission(): Boolean {
        activity?.apply {
            return ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun getNumberFromEditText(numberEditText: EditText?) = numberEditText?.text?.toString()
            ?: ""

    private fun callGetOTPAPI(otpMethod: String?) {
        mLinkDeviceOTPReq = otpMethod?.let { type -> OneAppService.getLinkDeviceOtp(type) }
        this.otpMethod = otpMethod

        if (!NetworkManager.getInstance().isConnectedToNetwork(activity)) {
            enterOTPSubtitle?.text = context?.getString(R.string.internet_waiting_subtitle)
            retryApiCall = RETRY_GET_OTP
            return
        }

        context?.let { didNotReceiveOTPTextView?.setTextColor(ContextCompat.getColor(it, R.color.button_disable)) }
        showSendingOTPProcessing()
        mLinkDeviceOTPReq?.enqueue(CompletionHandler(object : IResponseListener<RetrieveOTPResponse> {
            override fun onSuccess(retrieveOTPResponse: RetrieveOTPResponse?) {
                if (!isAdded || activity == null) return
                context?.let { didNotReceiveOTPTextView?.setTextColor(ContextCompat.getColor(it, R.color.black)) }

                when (retrieveOTPResponse?.httpCode) {
                    AppConstant.HTTP_OK -> {

                        if (!isAdded) {
                            return
                        }
                        sendinOTPLayout?.visibility = View.GONE
                        unlinkDeviceOTPScreenConstraintLayout?.visibility = View.VISIBLE
                        retrieveOTPResponse.otpSentTo?.let {
                            if (otpMethod.equals(OTPMethodType.SMS.name, true)) {
                                otpNumber = it
                            }
                            enterOTPSubtitle?.text = activity?.resources?.getString(R.string.sent_otp_desc, it)
                            Handler().postDelayed({
                                linkDeviceOTPEdtTxt1?.requestFocus()
                                val imm: InputMethodManager? = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                                imm?.showSoftInput(linkDeviceOTPEdtTxt1, InputMethodManager.SHOW_IMPLICIT)
                            }, AppConstant.DELAY_200_MS)
                        }
                    }

                    AppConstant.HTTP_SESSION_TIMEOUT_440 ->

                        activity?.apply {
                            if (!isFinishing) {
                                SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, retrieveOTPResponse.response?.stsParams, this)
                            }
                        }
                    else -> retrieveOTPResponse?.response?.desc?.let { desc ->
                        context?.let { didNotReceiveOTPTextView?.setTextColor(ContextCompat.getColor(it, R.color.black)) }

                        if (!NetworkManager.getInstance().isConnectedToNetwork(activity)) {
                            sendOTPFailedGroup?.visibility = View.GONE
                            sendinOTPLayout?.visibility = View.GONE
                            unlinkDeviceOTPScreenConstraintLayout?.visibility = View.VISIBLE
                            enterOTPSubtitle?.text = context?.getString(R.string.internet_waiting_subtitle)
                            retryApiCall = RETRY_GET_OTP
                            return
                        }
                        sendOTPProcessingGroup?.visibility = View.GONE
                        unlinkDeviceResultScreen?.visibility = View.GONE
                        sendOTPFailedGroup?.visibility = View.VISIBLE
                        sendOTPFailedImageView?.visibility = View.VISIBLE
                        sendOTPFailedTitle?.visibility = View.VISIBLE
                        sendOTPFailedTitle?.text = "Failed to send OTP."
                    }
                }
            }

            override fun onFailure(error: Throwable?) {
                context?.let { didNotReceiveOTPTextView?.setTextColor(ContextCompat.getColor(it, R.color.black)) }

                if (!NetworkManager.getInstance().isConnectedToNetwork(activity)) {
                    sendOTPFailedGroup?.visibility = View.GONE
                    sendinOTPLayout?.visibility = View.GONE
                    unlinkDeviceOTPScreenConstraintLayout?.visibility = View.VISIBLE
                    enterOTPSubtitle?.text = context?.getString(R.string.internet_waiting_subtitle)
                    retryApiCall = RETRY_GET_OTP
                    return
                }

                sendOTPProcessingGroup?.visibility = View.GONE
                unlinkDeviceResultScreen?.visibility = View.GONE
                sendOTPFailedGroup?.visibility = View.VISIBLE
                sendOTPFailedImageView?.visibility = View.VISIBLE
                sendOTPFailedTitle?.visibility = View.VISIBLE
                sendOTPFailedTitle?.text = "Failed to send OTP."
            }
        }, RetrieveOTPResponse::class.java))
    }

    private fun callValidatingOTPAPI(otp: String) {

        if (!NetworkManager.getInstance().isConnectedToNetwork(activity)) {
            enterOTPSubtitle?.text = context?.getString(R.string.internet_waiting_subtitle)
            return
        }

        mValidateLinkDeviceOTPReq = otpMethod?.let { type ->
            OneAppService.validateLinkDeviceOtp(LinkDeviceValidateBody(otp, otpMethod
                    ?: "SMS"))
        }

        showValidatingProcessing()
        unlinkDeviceOTPScreenConstraintLayout?.visibility = View.GONE
        isOTPValidated = false
        mValidateLinkDeviceOTPReq?.enqueue(CompletionHandler(object : IResponseListener<RetrieveOTPResponse> {
            override fun onSuccess(retrieveOTPResponse: RetrieveOTPResponse?) {
                sendinOTPLayout?.visibility = View.GONE
                when (retrieveOTPResponse?.httpCode) {
                    AppConstant.HTTP_OK -> {
                        if (!isAdded) {
                            return
                        }
                        if (!NetworkManager.getInstance().isConnectedToNetwork(activity)) {
                            enterOTPSubtitle?.text = context?.getString(R.string.internet_waiting_subtitle)
                            return
                        }

                        isOTPValidated = true
                        callUnlinkingDeviceAPI()
                    }
                    AppConstant.HTTP_SESSION_TIMEOUT_440 ->
                        activity?.apply {
                            if (!isFinishing) {
                                SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, retrieveOTPResponse.response?.stsParams, this)
                            }
                        }
                    else -> retrieveOTPResponse?.response?.desc?.let { desc ->

                        showValidateOTPError(desc)
                        Handler().postDelayed({
                            linkDeviceOTPEdtTxt5.requestFocus()
                            val imm: InputMethodManager? = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                            imm?.showSoftInput(linkDeviceOTPEdtTxt5, InputMethodManager.SHOW_IMPLICIT)
                        }, AppConstant.DELAY_200_MS)
                    }
                }
            }

            override fun onFailure(error: Throwable?) {
                context?.apply {
                    showValidateOTPError(getString(R.string.icr_wrong_otp_error))
                    Handler().postDelayed({
                        linkDeviceOTPEdtTxt5?.requestFocus()
                        val imm: InputMethodManager? = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                        imm?.showSoftInput(linkDeviceOTPEdtTxt5, InputMethodManager.SHOW_IMPLICIT)
                    }, AppConstant.DELAY_200_MS)
                }
            }
        }, RetrieveOTPResponse::class.java))
    }

    private fun showValidateOTPError(msg: String) {
        sendinOTPLayout?.visibility = View.GONE
        unlinkDeviceOTPScreenConstraintLayout?.visibility = View.VISIBLE
        buttonNext?.visibility = View.VISIBLE
        didNotReceiveOTPTextView?.visibility = View.VISIBLE

        if (!NetworkManager.getInstance().isConnectedToNetwork(activity)) {
            enterOTPSubtitle?.text = context?.getString(R.string.internet_waiting_subtitle)
            retryApiCall = RETRY_VALIDATE
            return
        }

        setOtpErrorBackground(R.drawable.otp_box_error_background)
        linkDeviceOTPErrorTxt?.text = msg
        linkDeviceOTPErrorTxt?.visibility = View.VISIBLE
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

    private fun callUnlinkingDeviceAPI() {

        if (!NetworkManager.getInstance().isConnectedToNetwork(activity)) {
            sendinOTPLayout?.visibility = View.GONE
            unlinkDeviceOTPScreenConstraintLayout?.visibility = View.VISIBLE
            enterOTPSubtitle?.text = context?.getString(R.string.internet_waiting_subtitle)
            retryApiCall = RETRY_UNLINK_DEVICE
            return
        }

        unlinkDeviceOTPScreenConstraintLayout?.visibility = View.GONE

        if (!isOTPValidated) {
            return
        }
        showUnlinkingDeviceProcessing()
        //Location permission granted but no current location found.
        if (checkLocationPermission() && Utils.isLocationEnabled(context) && currentLocation == null) {
            startLocationUpdates()
            return
        }

        unlinkDevice()
    }

    private fun unlinkDevice() {
        unlinkOrDeleteDeviceReq = OneAppService.deleteOrUnlinkDevice(deviceIdentityId)
        unlinkOrDeleteDeviceReq?.enqueue(CompletionHandler(
            object : IResponseListener<ViewAllLinkedDeviceResponse> {
                override fun onSuccess(response: ViewAllLinkedDeviceResponse?) {

                    when (response?.httpCode) {
                        AppConstant.HTTP_OK -> {
                            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.DEVICESECURITY_DELETE,
                                hashMapOf(Pair(FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE,
                                    FirebaseManagerAnalyticsProperties.PropertyNames.linkDeviceDelete)), activity)

                            showDeviceUnlinked()

                            setFragmentResult(MyPreferencesFragment.RESULT_LISTENER_LINK_DEVICE, bundleOf(
                                ViewAllLinkedDevicesFragment.IS_UPDATE to true
                            ))
                            Handler().postDelayed({
                                if (response.userDevices.isNullOrEmpty()) {
                                    view?.findNavController()?.navigateUp()
                                    return@postDelayed
                                }
                            }, AppConstant.DELAY_1500_MS)
                        }
                    }
                }

            }, ViewAllLinkedDeviceResponse::
            class.java))
    }

    private fun showUnlinkingDeviceProcessing() {
        sendOTPTitle?.visibility = View.GONE
        sendOTPSubtitle?.visibility = View.GONE

        context?.let {
            sendOTPProcessingReq?.text = it.getString(R.string.unlink_device_unlinking_processing)
        }
        sendinOTPLayout?.visibility = View.VISIBLE
    }

    private fun showDeviceUnlinked() {
        sendinOTPLayout?.visibility = View.GONE
        unlinkDeviceOTPScreenConstraintLayout?.visibility = View.GONE
        unlinkDeviceResultSubtitle?.visibility = View.GONE
        unlinkDeviceResultScreen?.visibility = View.VISIBLE
    }

    private fun showSendingOTPProcessing() {
        sendOTPTitle?.visibility = View.VISIBLE
        sendOTPSubtitle?.visibility = View.VISIBLE
        unlinkDeviceOTPScreenConstraintLayout?.visibility = View.GONE
        sendOTPFailedGroup?.visibility = View.GONE

        context?.let {
            sendOTPProcessingReq?.text = it.getString(R.string.link_device_sending_otp_processing)
        }
        sendinOTPLayout?.visibility = View.VISIBLE
    }

    private fun showValidatingProcessing() {
        sendOTPTitle?.visibility = View.GONE
        sendOTPSubtitle?.visibility = View.GONE
        buttonNext?.visibility = View.GONE
        didNotReceiveOTPTextView?.visibility = View.GONE

        context?.let {
            sendOTPProcessingReq?.text = it.getString(R.string.validating_otp)
        }
        sendinOTPLayout?.visibility = View.VISIBLE
    }

    private fun connectionDetector() {
        mConnectionBroadCast = Utils.connectionBroadCast(activity, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_CANCELED) {
            resetOTPView()
            unlinkDeviceOTPScreenConstraintLayout?.visibility = View.VISIBLE
        }

        when (requestCode) {
            ErrorHandlerActivity.ERROR_PAGE_REQUEST_CODE -> {
                when (resultCode) {
                    ErrorHandlerActivity.RESULT_RETRY -> {
                        callUnlinkingDeviceAPI()
                    }
                    ErrorHandlerActivity.RESULT_CALL_CENTER -> {
                        Utils.makeCall(AppConstant.WOOLWOORTH_CALL_CENTER_NUMBER)
                        setFragmentResult(MyPreferencesFragment.RESULT_LISTENER_LINK_DEVICE, bundleOf(
                                MyPreferencesFragment.IS_DEVICE_LINKED to false
                        ))
                        view?.findNavController()?.navigateUp()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.registerReceiver(mConnectionBroadCast, IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"))
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
                RETRY_UNLINK_DEVICE -> {
                    callUnlinkingDeviceAPI()
                }
            }
        }

    }
}