package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.SpannableString
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.enter_otp_fragment.*
import za.co.woolworths.financial.services.android.contracts.IOTPLinkStoreCard
import za.co.woolworths.financial.services.android.models.dto.npc.LinkNewCardOTP
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import za.co.woolworths.financial.services.android.util.KotlinUtils
import java.util.*
import android.view.MenuInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardActivityExtension
import za.co.woolworths.financial.services.android.ui.activities.store_card.RequestOTPActivity
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.NetworkManager
import android.view.WindowManager
import za.co.woolworths.financial.services.android.ui.activities.card.InstantStoreCardReplacementActivity
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import java.net.ConnectException
import java.net.UnknownHostException

class EnterOtpFragment : OTPInputListener(), IOTPLinkStoreCard<LinkNewCardOTP> {

    private var mStoreCardRequest: StoreCardOTPRequest? = null
    private var mOTPError: String = ""
    private var shouldDisableKeyboardOnOTPCall: Boolean = false
    private var mResendOTPFragment: ResendOTPFragment? = null
    private var isUnblockVirtualCard = false

    companion object {
        const val OTP_SENT_TO = "OTP_SENT_TO"
        const val IS_UNBLOCK_VIRTUAL_CARD = "IS_UNBLOCK_VIRTUAL_CARD"
        const val OTP_ERROR = "OTP_ERROR"
        fun newInstance() = EnterOtpFragment()
        fun newInstance(OTPError: String?, otpToSent: String?) = EnterOtpFragment().withArgs {
            putString(OTP_ERROR, OTPError)
            putString(OTP_SENT_TO, otpToSent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let { bundle ->
            saveOTP(bundle.getString(OTP_SENT_TO, ""))
            mOTPError = bundle.getString(OTP_ERROR, "")
            isUnblockVirtualCard = bundle.getBoolean(IS_UNBLOCK_VIRTUAL_CARD, false)

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity?.runOnUiThread { activity?.window?.addFlags(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE) }
        return inflater.inflate(R.layout.enter_otp_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MyCardActivityExtension)?.apply {
            //startSMSListener()
            showBackIcon()
            setupInputListeners()
            configureUI()
            clickEvent()
            imNextProcessLinkCard?.isEnabled = false
            if (mOTPError.isEmpty()) {
                setOTPDescription(mOtpSentTo?.toLowerCase(Locale.getDefault()))
                requestOTPApi(getOTPMethodType())
            } else {
                setOTPDescription(getSavedOTP())
                val otpNumber = getOtpNumber()
                if (!TextUtils.isEmpty(otpNumber)) {
                    with(otpNumber.split("")) {
                        edtVerificationCode1?.setText(this[1])
                        edtVerificationCode2?.setText(this[2])
                        edtVerificationCode3?.setText(this[3])
                        edtVerificationCode4?.setText(this[4])
                        edtVerificationCode5?.setText(this[5])
                    }
                    edtVerificationCode1?.setSelection(0)
                }
                showOTPErrorOnOTPFragment()
            }
        }

        uniqueIdsForEnterOTPScreen()
    }

    private fun uniqueIdsForEnterOTPScreen() {
        activity?.resources?.apply {
            tvEnterOtpTitle?.contentDescription = getString(R.string.enter_otp_title)
            enterOTPDescriptionScreen?.contentDescription =
                    getString(R.string.icr_enter_otp_description)
            viewOTPBackground?.contentDescription = getString(R.string.verification_code_container)
            loadingProgressIndicatorViewGroup?.contentDescription =
                    getString(R.string.load_otp_description)
            vLinkBottomNavigation?.contentDescription = getString(R.string.did_not_receive_title)
            otpErrorTextView?.contentDescription = getString(R.string.enter_otp_error)
        }
    }

    private fun setOTPDescription(otpType: String?) {
        (activity as? MyCardActivityExtension)?.apply {
            mOtpSentTo = otpType
            val otpDescriptionLabel = when (getOTPMethodType()) {
                OTPMethodType.SMS -> setResource(R.string.icr_otp_phone_desc, otpType)
                OTPMethodType.EMAIL -> setResource(R.string.icr_otp_email_desc, otpType)
                else -> return
            }

            val otpDescription = activity?.let { activity -> otpType?.let { type -> KotlinUtils.highlightTextInDesc(activity, SpannableString(otpDescriptionLabel), type, false) } }
            enterOTPDescriptionScreen?.apply {
                text = otpDescription
                movementMethod = LinkMovementMethod.getInstance()
                highlightColor = Color.TRANSPARENT
                visibility = VISIBLE
            }
        }
    }

    private fun setResource(enterOtpPhone: Int, otpType: String?) = activity?.resources?.getString(enterOtpPhone, otpType)

    private fun configureUI() {
        didNotReceiveOTPTextView?.apply { paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG }
        if (activityIsInstanceStoreCardActivity()) didNotReceiveOTPTextView?.isAllCaps = true

    }

    private fun activityIsInstanceStoreCardActivity() = activity is InstantStoreCardReplacementActivity

    private fun clickEvent() {
        imNextProcessLinkCard?.setOnClickListener {
            if (isUnblockVirtualCard) sendOTBack() else navigateToLinkStoreCard()
        }
        didNotReceiveOTPTextView?.setOnClickListener {
            if (shouldDisableKeyboardOnOTPCall) return@setOnClickListener
            hideKeyboard()
            (activity as? AppCompatActivity)?.apply {
                mResendOTPFragment =
                        ResendOTPFragment.newInstance(this@EnterOtpFragment, getSavedNumber())
                mResendOTPFragment?.show(supportFragmentManager.beginTransaction(), ResendOTPFragment::class.java.simpleName)
            }
        }
    }

    private fun navigateToLinkStoreCard() {
        hideKeyboard()
        val otpNumber = getNumberFromEditText(edtVerificationCode1)
                .plus(getNumberFromEditText(edtVerificationCode2))
                .plus(getNumberFromEditText(edtVerificationCode3))
                .plus(getNumberFromEditText(edtVerificationCode4))
                .plus(getNumberFromEditText(edtVerificationCode5))

        (activity as? MyCardActivityExtension)?.setOTPNumber(otpNumber)
        replaceFragment(
                fragment = LinkStoreCardFragment.newInstance(),
                tag = LinkStoreCardFragment::class.java.simpleName,
                containerViewId = R.id.flMyCard,
                allowStateLoss = true,
                enterAnimation = R.anim.slide_in_from_right,
                exitAnimation = R.anim.slide_to_left,
                popEnterAnimation = R.anim.slide_from_left,
                popExitAnimation = R.anim.slide_to_right
        )
    }

    private fun sendOTBack() {
        val otpNumber =
                getNumberFromEditText(edtVerificationCode1).plus(getNumberFromEditText(edtVerificationCode2)).plus(getNumberFromEditText(edtVerificationCode3)).plus(getNumberFromEditText(edtVerificationCode4)).plus(getNumberFromEditText(edtVerificationCode5))
        activity?.apply {
            setResult(Activity.RESULT_OK, Intent().putExtra(RequestOTPActivity.OTP_VALUE, otpNumber))
            (this as RequestOTPActivity).finishActivity()
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.supportActionBar?.show()
        requestEditTextFocus()
    }

    private fun requestEditTextFocus() {
        (activity as? AppCompatActivity)?.let { activity ->
            edtVerificationCode1?.apply {
                requestFocus()
                showSoftKeyboard(activity, this)
            }
        }
    }

    override fun requestOTPApi(otpMethodType: OTPMethodType) {
        super.requestOTPApi(otpMethodType)
        saveSelectedOTP(otpMethodType)
        clearOTP()
        requestEditTextFocus()
        activity?.let { activity ->
            mStoreCardRequest = StoreCardOTPRequest(activity, otpMethodType)
            if (NetworkManager().isConnectedToNetwork(activity)) {
                mStoreCardRequest?.make(object : IOTPLinkStoreCard<LinkNewCardOTP> {

                    override fun showProgress() {
                        shouldDisableKeyboardOnOTPCall = true
                        super.showProgress()
                        activity.resources?.let { resources ->
                            enterOTPDescriptionScreen?.text =
                                    resources.getString(R.string.sending_otp_text)
                        }
                        loadingProgressIndicatorViewGroup?.visibility = VISIBLE
                        enterOTPDescriptionScreen?.text = ""
                        disableEditText(edtVerificationCode1)
                        disableEditText(edtVerificationCode2)
                        disableEditText(edtVerificationCode3)
                        disableEditText(edtVerificationCode4)
                        disableEditText(edtVerificationCode5)
                    }

                    override fun hideProgress() {
                        super.hideProgress()
                        shouldDisableKeyboardOnOTPCall = false
                        loadingProgressIndicatorViewGroup?.visibility = GONE
                        enableEditText(edtVerificationCode1)
                        enableEditText(edtVerificationCode2)
                        enableEditText(edtVerificationCode3)
                        enableEditText(edtVerificationCode4)
                        enableEditText(edtVerificationCode5)
                        requestEditTextFocus()
                    }

                    override fun onSuccessHandler(response: LinkNewCardOTP) {
                        super.onSuccessHandler(response)
                        saveOTP(response.otpSentTo)
                        if ((activity as? MyCardActivityExtension)?.getOTPMethodType() == OTPMethodType.SMS) {
                            saveNumber(response.otpSentTo)
                            setOTPDescription(getSavedOTP())
                        } else {
                            setOTPDescription(response.otpSentTo?.toLowerCase(Locale.getDefault()))
                        }
                    }

                    override fun onFailureHandler() {
                        super.onFailureHandler()
                        setOTPDescription(getSavedOTP())
                    }

                    override fun onFailureHandler(error: Throwable?) {
                        super.onFailureHandler(error)
                        if (error is ConnectException || error is UnknownHostException) {
                            activity.resources?.let { resources ->
                                enterOTPDescriptionScreen?.text =
                                        resources.getString(R.string.check_connection_status)
                            }
                        }
                    }
                })
            } else {
                ErrorHandlerView(activity).showToast()
                return
            }
        }
    }


    private fun saveSelectedOTP(otpMethodType: OTPMethodType) = (activity as? MyCardActivityExtension)?.setOTPType(otpMethodType)

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                (activity as? MyCardActivityExtension)?.hideBackIcon()
                activity?.onBackPressed()
                super.onOptionsItemSelected(item)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getNumberFromEditText(numberEditText: EditText?) = numberEditText?.text?.toString()
            ?: ""

    override fun onDestroy() {
        super.onDestroy()
        activity?.runOnUiThread { activity?.window?.clearFlags(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE) }
        //cancelSMSListener()
    }

    fun onOTPReceived(otp: String?) {
        displayRetrievedOTP(edtVerificationCode1, otp, 0)
        displayRetrievedOTP(edtVerificationCode2, otp, 1)
        displayRetrievedOTP(edtVerificationCode3, otp, 2)
        displayRetrievedOTP(edtVerificationCode4, otp, 3)
        displayRetrievedOTP(edtVerificationCode5, otp, 4)
        //cancelSMSListener()
    }

    private fun cancelSMSListener() = (activity as? MyCardActivityExtension)?.cancelSMSRetriever()

    private fun displayRetrievedOTP(editText: EditText?, otp: String?, position: Int) {
        editText?.setText("")
        editText?.setText(otp?.toCharArray()?.get(position)?.toString())
    }

    private fun enableEditText(editText: EditText?) {
        editText?.isFocusableInTouchMode = true
        editText?.isFocusable = true
        editText?.isEnabled = true
        editText?.inputType = 1
    }

    private fun disableEditText(editText: EditText?) {
        editText?.isFocusableInTouchMode = false
        editText?.isFocusable = false
        editText?.isEnabled = false
        editText?.inputType = 0
        otpErrorTextView?.visibility = GONE
    }

    private fun showOTPErrorOnOTPFragment() {
        otpErrorTextView?.text = if (activityIsInstanceStoreCardActivity()) setResource(R.string.icr_wrong_otp_error, "") else setResource(R.string.wrong_otp_desc, "")
        otpErrorTextView?.visibility = VISIBLE
        edtVerificationCode1?.requestFocus()
        requestEditTextFocus()
        imNextProcessLinkCard?.isEnabled = false
        setOtpErrorBackground(R.drawable.otp_box_error_background)
    }

    private fun saveOTP(otpSendTo: String?) {
        (activity as? MyCardActivityExtension)?.mOtpSentTo = otpSendTo
    }

    fun getSavedOTP() = (activity as? MyCardActivityExtension)?.mOtpSentTo

    private fun saveNumber(otpSendTo: String?) {
        (activity as? MyCardActivityExtension)?.mPhoneNumberOTP = otpSendTo
    }

    private fun getSavedNumber() = (activity as? MyCardActivityExtension)?.mPhoneNumberOTP
}