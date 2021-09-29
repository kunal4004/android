package za.co.woolworths.financial.services.android.ui.fragments.mypreferences

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Paint
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import com.awfs.coordination.R
import com.google.android.gms.location.*
import com.google.firebase.installations.FirebaseInstallations
import kotlinx.android.synthetic.main.enter_otp_fragment.*
import kotlinx.android.synthetic.main.fragment_enter_otp.buttonNext
import kotlinx.android.synthetic.main.fragment_enter_otp.didNotReceiveOTPTextView
import kotlinx.android.synthetic.main.fragment_link_device_otp.*
import kotlinx.android.synthetic.main.fragment_link_device_otp.sendinOTPLayout
import kotlinx.android.synthetic.main.fragment_unlink_device_otp.*
import kotlinx.android.synthetic.main.layout_link_device_result.*
import kotlinx.android.synthetic.main.layout_link_device_validate_otp.*
import kotlinx.android.synthetic.main.layout_sending_otp_request.*
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.CreditCardDeliveryCardTypes
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.account.CreditCardDeliveryStatus
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.CreditCardDeliveryStatusResponse
import za.co.woolworths.financial.services.android.models.dto.linkdevice.LinkedDeviceResponse
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.models.dto.otp.RetrieveOTPResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.CreditCardActivationActivity
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity
import za.co.woolworths.financial.services.android.ui.activities.MyPreferencesInterface
import za.co.woolworths.financial.services.android.ui.activities.account.LinkDeviceConfirmationActivity
import za.co.woolworths.financial.services.android.ui.activities.account.LinkDeviceConfirmationInterface
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.activities.credit_card_delivery.CreditCardDeliveryActivity
import za.co.woolworths.financial.services.android.ui.extension.asEnumOrDefault
import za.co.woolworths.financial.services.android.ui.extension.cancelRetrofitRequest
import za.co.woolworths.financial.services.android.ui.fragments.account.MyAccountsFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.StoreCardOptionsFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.card.AccountsOptionFragment
import za.co.woolworths.financial.services.android.ui.fragments.npc.MyCardDetailFragment
import za.co.woolworths.financial.services.android.ui.fragments.npc.OTPViewTextWatcher
import za.co.woolworths.financial.services.android.util.*
import java.util.*


class LinkDeviceOTPFragment : Fragment(), View.OnClickListener, NetworkChangeListener {

    private var mConnectionBroadCast: BroadcastReceiver? = null
    private var mApplyNowState: ApplyNowState? = null
    private var otpNumber: String? = null
    private var retryApiCall: String? = null
    private var otpMethod: String? = OTPMethodType.SMS.name
    private var currentLocation: Location? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var cardWithPLCState: Card? = null
    private val locationRequest = createLocationRequest()
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            for (location in locationResult.locations) {
                this@LinkDeviceOTPFragment.currentLocation = location
                stopLocationUpdates()
                callLinkingDeviceAPI()
                break
            }
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mApplyNowState = it.getSerializable(AccountSignedInPresenterImpl.APPLY_NOW_STATE) as? ApplyNowState
                    ?: ApplyNowState.STORE_CARD
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
        return inflater.inflate(R.layout.fragment_link_device_otp, container, false)
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
        const val RETRY_LINK_DEVICE: String = "LINK_DEVICE"
        const val GO_TO_PRODUCT: Int = 999
        const val PLC = "PLC"
        const val KEY_ENVELOPE_NUMBER = "envelopeNumber"
        const val KEY_ABSA_CARD_TOKEN = "absaCardToken"
        const val KEY_PRODUCT_OFFERING_ID = "productOfferingId"
        const val KEY_ACCOUNT_BIN_NUMBER = "accountBinNumber"
        const val KEY_STATUS_RESPONSE = "StatusResponse"
        const val KEY_SETUP_DELIVERY_NOW_CLICKED = "setUpDeliveryNowClicked"
        const val KEY_BUNDLE = "bundle"
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.didNotReceiveOTPTextView -> {
                if (!isRetrieveOTPCallInProgress()) {
                    v.isEnabled = false
                    Handler().postDelayed({ v.isEnabled = true }, AppConstant.DELAY_1000_MS)
                    view?.findNavController()?.navigate(R.id.action_linkDeviceOTPFragment_to_resendOTPBottomSheetFragment, bundleOf(
                            ResendOTPBottomSheetFragment.OTP_NUMBER to otpNumber
                    ))
                }
            }
            R.id.buttonNext -> {

                makeLinkDeviceRequest()
            }
        }
    }

    private fun makeLinkDeviceRequest() {

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
        imm?.hideSoftInputFromWindow(linkDeviceOTPEdtTxt5?.windowToken, 0)

        linkDeviceOTPScreen?.visibility = View.GONE
        sendinOTPLayout?.visibility = View.GONE

        callLinkingDeviceAPI()
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
                        linkDeviceOTPScreen?.visibility = View.VISIBLE
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
                            linkDeviceOTPScreen?.visibility = View.VISIBLE
                            enterOTPSubtitle?.text = context?.getString(R.string.internet_waiting_subtitle)
                            retryApiCall = RETRY_GET_OTP
                            return
                        }
                        sendOTPProcessingGroup?.visibility = View.GONE
                        linkDeviceResultScreen?.visibility = View.GONE
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
                    linkDeviceOTPScreen?.visibility = View.VISIBLE
                    enterOTPSubtitle?.text = context?.getString(R.string.internet_waiting_subtitle)
                    retryApiCall = RETRY_GET_OTP
                    return
                }

                sendOTPProcessingGroup?.visibility = View.GONE
                linkDeviceResultScreen?.visibility = View.GONE
                sendOTPFailedGroup?.visibility = View.VISIBLE
                sendOTPFailedImageView?.visibility = View.VISIBLE
                sendOTPFailedTitle?.visibility = View.VISIBLE
                sendOTPFailedTitle?.text = "Failed to send OTP."
            }
        }, RetrieveOTPResponse::class.java))
    }

    private fun showValidateOTPError(msg: String) {
        sendinOTPLayout?.visibility = View.GONE
        linkDeviceOTPScreen?.visibility = View.VISIBLE
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

    private fun callLinkingDeviceAPI() {

        if (!NetworkManager.getInstance().isConnectedToNetwork(activity)) {
            sendinOTPLayout?.visibility = View.GONE
            linkDeviceOTPScreen?.visibility = View.VISIBLE
            enterOTPSubtitle?.text = context?.getString(R.string.internet_waiting_subtitle)
            retryApiCall = RETRY_LINK_DEVICE
            return
        }

        showLinkingDeviceProcessing()
        //Location permission granted but no current location found.
        if (checkLocationPermission() && Utils.isLocationEnabled(context) && currentLocation == null) {
            startLocationUpdates()
            return
        }

        retrieveTokenAndCallLinkDevice()
    }

    private fun retrieveTokenAndCallLinkDevice() {
        if (TextUtils.isEmpty(Utils.getToken())) {
            if (Utils.isGooglePlayServicesAvailable()) {

                FirebaseInstallations.getInstance().getToken(true).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result.token.let {
                            // Save fb token in DB.
                            Utils.setToken(it)
                            sendTokenToLinkDevice(it)
                            return@addOnCompleteListener
                        }
                    }
                    // token is null show error message to user
                    showErrorScreen(ErrorHandlerActivity.LINK_DEVICE_FAILED)
                }
            } else {
                // token is null show error message to user
                showErrorScreen(ErrorHandlerActivity.LINK_DEVICE_FAILED)
            }
        } else {
            sendTokenToLinkDevice(Utils.getToken())
        }
    }

    private fun sendTokenToLinkDevice(token: String) {
        linkDeviceOTPScreen?.visibility = View.GONE
        OneAppService.linkDeviceApi(
            KotlinUtils.getUserDefinedDeviceName(activity),
            Utils.getUniqueDeviceID(),
            getLocationAddress(currentLocation?.latitude, currentLocation?.longitude),
            true,
            token,
            otpNumber,
            otpMethod)
            .enqueue(CompletionHandler(object : IResponseListener<LinkedDeviceResponse> {
            override fun onSuccess(response: LinkedDeviceResponse?) {
                sendinOTPLayout?.visibility = View.GONE
                when (response?.httpCode) {
                    AppConstant.HTTP_OK_201.toString() -> {
                        activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.DEVICESECURITY_LINK_CONFIRMED, hashMapOf(Pair(FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE, FirebaseManagerAnalyticsProperties.PropertyNames.linkDeviceConfirmed)), this) }

                        if (!isAdded) {
                            return
                        }
                        showDeviceLinked()
                        response.deviceIdentityToken?.let{ SessionUtilities.getInstance().deviceIdentityToken =
                            it }
                        response.deviceIdentityId?.let { saveDeviceId(it) }
                        setFragmentResult(MyPreferencesFragment.RESULT_LISTENER_LINK_DEVICE, bundleOf(
                            MyPreferencesFragment.IS_DEVICE_LINKED to true
                        ))
                        Handler().postDelayed({

                            // This will execute only when linking comes from account products.
                            mApplyNowState?.let {
                                activity?.apply {
                                    if (this is LinkDeviceConfirmationActivity) {
                                        when (mApplyNowState) {
                                            //TODO: WOP-12578, WOP-12589 // credit card will be done after personal loan
                                            /**ApplyNowState.BLACK_CREDIT_CARD,
                                            ApplyNowState.GOLD_CREDIT_CARD,
                                            ApplyNowState.SILVER_CREDIT_CARD -> {
                                                //check if should activate credit card or should schedule delivery
                                                activateCreditCardOrScheduleCardDelivery()
                                            }*/
                                            ApplyNowState.STORE_CARD -> {
                                                MyAccountsFragment.updateLinkedDevices()
                                                when {
                                                    MyCardDetailFragment.FREEZE_CARD_DETAIL -> {
                                                        showFreezeStoreCardDialog()
                                                    }
                                                    MyCardDetailFragment.BLOCK_CARD_DETAIL -> {
                                                        showBlockStoreCardScreen()
                                                    }
                                                    StoreCardOptionsFragment.GET_REPLACEMENT_CARD_DETAIL -> {
                                                        showGetReplacementStoreCardScreen()
                                                    }
                                                    AccountsOptionFragment.CLI_DETAIL -> {
                                                        showCLIScreen()
                                                    }
                                                    else -> {
                                                        goToProduct()
                                                    }
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
                                SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, response.response.stsParams, this)
                            }
                        }
                    else -> response?.response?.desc?.let { desc ->
                        showValidateOTPError(getString(R.string.icr_wrong_otp_error))
                        Handler().postDelayed({
                            linkDeviceOTPEdtTxt5.requestFocus()
                            val imm: InputMethodManager? = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                            imm?.showSoftInput(linkDeviceOTPEdtTxt5, InputMethodManager.SHOW_IMPLICIT)
                        }, AppConstant.DELAY_200_MS)
                    }
                }
            }

            override fun onFailure(error: Throwable?) {
                sendinOTPLayout?.visibility = View.GONE
                linkDeviceOTPScreen?.visibility = View.VISIBLE
                buttonNext?.visibility = View.VISIBLE
                didNotReceiveOTPTextView?.visibility = View.VISIBLE
                showErrorScreen(ErrorHandlerActivity.LINK_DEVICE_FAILED)
            }
        }, LinkedDeviceResponse::class.java))
    }

    private fun showFreezeStoreCardDialog(){
        MyCardDetailFragment.SHOW_TEMPORARY_FREEZE_DIALOG = true
        MyCardDetailFragment.FREEZE_CARD_DETAIL = false
        activity?.finish()
    }

    private fun showBlockStoreCardScreen(){
        MyCardDetailFragment.SHOW_BLOCK_CARD_SCREEN = true
        MyCardDetailFragment.BLOCK_CARD_DETAIL = false
        activity?.finish()
    }

    private fun showGetReplacementStoreCardScreen(){
        StoreCardOptionsFragment.SHOW_GET_REPLACEMENT_CARD_SCREEN = true
        StoreCardOptionsFragment.GET_REPLACEMENT_CARD_DETAIL = false
        activity?.finish()
    }

    private fun showCLIScreen(){
        AccountsOptionFragment.SHOW_CLI_SCREEN = true
        AccountsOptionFragment.CLI_DETAIL = false
        activity?.finish()
    }

    private fun activateCreditCardOrScheduleCardDelivery() {
        activity?.apply {
            OneAppService.getCreditCardToken()
                .enqueue(CompletionHandler(object : IResponseListener<CreditCardTokenResponse> {
                    override fun onSuccess(response: CreditCardTokenResponse?) {
                        response?.apply {
                            if (!cards.isNullOrEmpty()) {
                                cardWithPLCState = getCardWithPLCState(cards)
                                cards?.get(0)?.apply {
                                    when (envelopeNumber.isNullOrEmpty()) {
                                        true -> {
                                            when (cardStatus) {
                                                PLC -> {
                                                    when (isPLCInGoodStanding()) {
                                                        true -> checkCreditCardDeliveryStatus()

                                                        false -> goToCreditCardActivation()
                                                    }
                                                }
                                                else -> goToProduct()
                                            }
                                        }
                                        false -> checkCreditCardDeliveryStatus()
                                    }
                                }
                            } else {
                                goToProduct()
                            }
                        }
                    }

                    override fun onFailure(error: Throwable?) {
                        goToProduct()
                    }
                }, CreditCardTokenResponse::class.java))
        }
    }

    private fun checkCreditCardDeliveryStatus() {
        cardWithPLCState?.envelopeNumber?.let {
            OneAppService.getCreditCardDeliveryStatus(
                it,
                getAccount()?.productOfferingId.toString())
                .enqueue(CompletionHandler(object : IResponseListener<CreditCardDeliveryStatusResponse> {
                    override fun onSuccess(response: CreditCardDeliveryStatusResponse?) {
                        when (response?.statusResponse?.deliveryStatus?.statusDescription?.asEnumOrDefault(
                            CreditCardDeliveryStatus.DEFAULT)) {
                            CreditCardDeliveryStatus.CARD_RECEIVED ->
                                goToScheduleDelivery(response)
                            else -> goToProduct()
                        }
                    }

                    override fun onFailure(error: Throwable?) {
                        goToProduct()
                    }
                }, CreditCardDeliveryStatusResponse::class.java))
        }
    }

    private fun getAccount(): Account? {
        return MyAccountsFragment.mAccountResponse.accountList?.filter { account ->
            account?.productGroupCode == AccountSignedInPresenterImpl.getProductCode(mApplyNowState!!) }?.get(0)
    }

    private fun getCardWithPLCState(cards: ArrayList<Card>?): Card? {
        var cardWithPLCState: Card? = null
        cards?.apply {
            if (this.isNotEmpty())
                cardWithPLCState = this[0]
        }
        return cardWithPLCState
    }

    private fun isPLCInGoodStanding(): Boolean {
        var isEnable = false
        if (!cardWithPLCState?.envelopeNumber.isNullOrEmpty()) {
            val cardTypes: List<CreditCardDeliveryCardTypes> = WoolworthsApplication.getCreditCardDelivery().cardTypes
            for ((binNumber, minimumSupportedAppBuildNumber) in cardTypes) {
                if (binNumber.equals(getAccount()?.accountNumberBin, ignoreCase = true)
                    && Utils.isFeatureEnabled(minimumSupportedAppBuildNumber)) {
                    isEnable = true
                }
            }
        }
        return isEnable
    }

    private fun goToProduct() {
        val intent = Intent()
        intent.putExtra(AccountSignedInPresenterImpl.APPLY_NOW_STATE, mApplyNowState)
        intent.putExtra(MyPreferencesFragment.RESULT_LISTENER_LINK_DEVICE, true)
        activity?.setResult(MyAccountsFragment.RESULT_CODE_LINK_DEVICE, intent)
        activity?.finish()
    }

    private fun goToCreditCardActivation() {
        activity?.apply {
            if (Utils.isCreditCardActivationEndpointAvailable()){
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.CC_ACTIVATE_NEW_CARD, hashMapOf(Pair(
                    FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE,
                    FirebaseManagerAnalyticsProperties.PropertyNames.activationInitiated
                )), this)
                val mIntent = Intent(this, CreditCardActivationActivity::class.java)
                val mBundle = Bundle()
                mBundle.putString(KEY_ABSA_CARD_TOKEN, cardWithPLCState?.absaCardToken)
                mBundle.putString(KEY_PRODUCT_OFFERING_ID, getAccount()?.productOfferingId.toString())
                mIntent.putExtra(KEY_BUNDLE, mBundle)
                mIntent.putExtra(AccountSignedInPresenterImpl.APPLY_NOW_STATE, mApplyNowState)
                startActivityIfNeeded(mIntent,
                    AccountsOptionFragment.REQUEST_CREDIT_CARD_ACTIVATION
                )
                overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
            }
            else {
                goToProduct()
            }
        }
    }

    private fun goToScheduleDelivery(response: CreditCardDeliveryStatusResponse) {
        val account = MyAccountsFragment.mAccountResponse.accountList[0]
        val intent = Intent(context, CreditCardDeliveryActivity::class.java)
        val mBundle = Bundle()
        mBundle.putString(KEY_ENVELOPE_NUMBER, account.cards[0].envelopeNumber)
        mBundle.putString(KEY_ACCOUNT_BIN_NUMBER, account.accountNumberBin)
        mBundle.putString(KEY_STATUS_RESPONSE, Utils.toJson(response.statusResponse))
        mBundle.putString(KEY_PRODUCT_OFFERING_ID, account.productOfferingId.toString())
        mBundle.putBoolean(KEY_SETUP_DELIVERY_NOW_CLICKED, true)
        mBundle.putSerializable(AccountSignedInPresenterImpl.APPLY_NOW_STATE, mApplyNowState)
        intent.putExtra(KEY_BUNDLE, mBundle)
        startActivity(intent)
    }

    private fun getLocationAddress(latitude: Double?, longitude: Double?): String? {

        var location = ""
        if (latitude == null || longitude == null) {
            return location
        }
        val gcd = Geocoder(context, Locale.getDefault())
        val addresses: List<Address> = gcd.getFromLocation(latitude, longitude, 2)
        if (addresses.isNotEmpty()) {
            location =
                    if (TextUtils.isEmpty(addresses[0].locality) || "null".equals(addresses[0].locality, ignoreCase = true)) {
                        for (address in addresses) {
                            if (!TextUtils.isEmpty(address.locality) && !"null".equals( address.locality, ignoreCase = true)) {
                                return address.locality + ", " + address.countryName
                            } else if (!TextUtils.isEmpty(address.subLocality) && !"null".equals( address.subLocality, ignoreCase = true)) {
                                return address.subLocality + ", " + address.countryName
                            }
                        }
                        return addresses[0].countryName
                    } else
                        return addresses[0].locality + ", " + addresses[0].countryName

        }
        return location
    }

    private fun saveDeviceId(deviceIdentityId: Long) {
        if (deviceIdentityId < 0) return
        val currentUserObject = AppInstanceObject.get().currentUserObject
        currentUserObject.linkedDeviceIdentityId = deviceIdentityId
        currentUserObject.save()
    }

    private fun showDeviceLinked() {
        sendinOTPLayout?.visibility = View.GONE
        linkDeviceOTPScreen?.visibility = View.GONE
        linkDeviceResultSubitle?.visibility = View.GONE

        linkDeviceResultScreen?.visibility = View.VISIBLE
    }

    private fun showLinkingDeviceProcessing() {
        buttonNext?.visibility = View.GONE
        sendOTPTitle?.visibility = View.GONE
        sendOTPSubtitle?.visibility = View.GONE
        didNotReceiveOTPTextView?.visibility = View.GONE

        context?.let {
            sendOTPProcessingReq?.text = it.getString(R.string.link_device_linking_processing)
        }
        sendinOTPLayout?.visibility = View.VISIBLE
    }


    private fun showSendingOTPProcessing() {
        sendOTPTitle?.visibility = View.VISIBLE
        sendOTPSubtitle?.visibility = View.VISIBLE
        linkDeviceOTPScreen?.visibility = View.GONE
        sendOTPFailedGroup?.visibility = View.GONE

        context?.let {
            sendOTPProcessingReq?.text = it.getString(R.string.link_device_sending_otp_processing)
        }
        sendinOTPLayout?.visibility = View.VISIBLE
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
            ErrorHandlerActivity.ERROR_PAGE_REQUEST_CODE -> {
                when (resultCode) {
                    ErrorHandlerActivity.RESULT_RETRY -> {
                        callLinkingDeviceAPI()
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
            else ->
                when (resultCode) {
                    Activity.RESULT_CANCELED -> {
                        Handler().postDelayed({
                            linkDeviceOTPEdtTxt5.requestFocus()
                            val imm: InputMethodManager? = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                            imm?.showSoftInput(linkDeviceOTPEdtTxt5, InputMethodManager.SHOW_IMPLICIT)
                        }, AppConstant.DELAY_200_MS)
                        linkDeviceOTPScreen?.visibility = View.VISIBLE
                    }
                    GO_TO_PRODUCT -> {
                        goToProduct()
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
                    makeLinkDeviceRequest()
                }
                RETRY_LINK_DEVICE -> {
                    callLinkingDeviceAPI()
                }
            }
        }

    }
}