package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.text.SpannableString
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.contracts.IOTPLinkStoreCard
import za.co.woolworths.financial.services.android.models.dto.npc.LinkNewCardOTP
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import java.util.*
import android.view.MenuInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardActivityExtension
import za.co.woolworths.financial.services.android.ui.activities.store_card.RequestOTPActivity
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.NetworkManager
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.awfs.coordination.databinding.EnterOtpFragmentBinding
import za.co.woolworths.financial.services.android.ui.activities.card.InstantStoreCardReplacementActivity
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.util.KotlinUtils
import java.net.ConnectException
import java.net.UnknownHostException

@Suppress("IMPLICIT_CAST_TO_ANY")
class EnterOtpFragment : MyCardExtension(R.layout.enter_otp_fragment), IOTPLinkStoreCard<LinkNewCardOTP> {

    private lateinit var binding: EnterOtpFragmentBinding
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = EnterOtpFragmentBinding.bind(view)

        activity?.runOnUiThread { activity?.window?.addFlags(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE) }

        binding.apply {
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
                        showOTPErrorOnOTPFragment()
                        setOTPDescription(getSavedOTP())
                        enterOTPDescriptionScreenTextView?.visibility = VISIBLE
                    }
                }
            }

            uniqueIdsForEnterOTPScreen()
        }
    }

    private fun EnterOtpFragmentBinding.uniqueIdsForEnterOTPScreen() {
        activity?.resources?.apply {
            tvEnterOtpTitle?.contentDescription = getString(R.string.enter_otp_title)
            enterOTPDescriptionScreenTextView?.contentDescription = getString(R.string.icr_enter_otp_description)
            viewOTPBackground?.contentDescription = getString(R.string.verification_code_container)
            loadingProgressIndicatorViewGroup?.contentDescription = getString(R.string.load_otp_description)
            vLinkBottomNavigation?.contentDescription = getString(R.string.did_not_receive_title)
            otpErrorTextView?.contentDescription = getString(R.string.enter_otp_error)
        }
    }

    private fun EnterOtpFragmentBinding.setOTPDescription(otpType: String?) {
        (activity as? MyCardActivityExtension)?.apply {
            mOtpSentTo = otpType
            val otpDescriptionLabel = when (getOTPMethodType()) {
                OTPMethodType.SMS -> setResource(R.string.icr_otp_phone_desc, otpType)
                OTPMethodType.EMAIL -> setResource(R.string.icr_otp_email_desc, otpType)
                OTPMethodType.NONE -> ""
            }

            val otpDescription = activity?.let { activity -> otpType?.let { type -> KotlinUtils.highlightTextInDesc(activity, SpannableString(otpDescriptionLabel as CharSequence?), type, false) } }
            Handler().postDelayed({
                enterOTPDescriptionScreenTextView?.apply {
                    text = otpDescription
                    movementMethod = LinkMovementMethod.getInstance()
                    visibility = VISIBLE
                }
            }, 300)
        }
    }

    private fun setResource(enterOtpPhone: Int, otpType: String?) = activity?.resources?.getString(enterOtpPhone, otpType)

    private fun EnterOtpFragmentBinding.configureUI() {
        didNotReceiveOTPTextView?.apply { paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG }
        if (activityIsInstanceStoreCardActivity()) didNotReceiveOTPTextView?.isAllCaps = true
    }

    private fun activityIsInstanceStoreCardActivity() = activity is InstantStoreCardReplacementActivity

    private fun EnterOtpFragmentBinding.clickEvent() {
        imNextProcessLinkCard?.setOnClickListener {
            if (isUnblockVirtualCard) sendOTBack() else navigateToLinkStoreCard()
        }
        didNotReceiveOTPTextView?.setOnClickListener {
            if (shouldDisableKeyboardOnOTPCall) return@setOnClickListener
            hideKeyboard()
            (activity as? AppCompatActivity)?.apply {
                mResendOTPFragment = ResendOTPFragment.newInstance(this@EnterOtpFragment, getSavedNumber())
                mResendOTPFragment?.show(supportFragmentManager.beginTransaction(), ResendOTPFragment::class.java.simpleName)
            }
        }
    }

    private fun EnterOtpFragmentBinding.navigateToLinkStoreCard() {
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

    private fun EnterOtpFragmentBinding.sendOTBack() {
        val otpNumber = getNumberFromEditText(edtVerificationCode1).plus(getNumberFromEditText(edtVerificationCode2)).plus(getNumberFromEditText(edtVerificationCode3)).plus(getNumberFromEditText(edtVerificationCode4)).plus(getNumberFromEditText(edtVerificationCode5))
        activity?.apply {
            setResult(Activity.RESULT_OK, Intent().putExtra(RequestOTPActivity.OTP_VALUE, otpNumber))
            (this as RequestOTPActivity).finishActivity()
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.supportActionBar?.show()
        binding.requestEditTextFocus()
    }

    private fun EnterOtpFragmentBinding.requestEditTextFocus() {
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
        binding.clearOTP()
        binding.requestEditTextFocus()
        (activity as? AppCompatActivity)?.let { activity ->
            binding.apply {
                mStoreCardRequest = StoreCardOTPRequest(activity, otpMethodType)
                if (NetworkManager().isConnectedToNetwork(activity)) {
                    mStoreCardRequest?.make(object : IOTPLinkStoreCard<LinkNewCardOTP> {

                        override fun showProgress() {
                            shouldDisableKeyboardOnOTPCall = true
                            super.showProgress()
                            activity.resources?.let { resources ->
                                enterOTPDescriptionScreenTextView?.text =
                                    resources.getString(R.string.sending_otp_text)
                            }
                            loadingProgressIndicatorViewGroup?.visibility = VISIBLE
                            disableEditText(edtVerificationCode1)
                            disableEditText(edtVerificationCode2)
                            disableEditText(edtVerificationCode3)
                            disableEditText(edtVerificationCode4)
                            disableEditText(edtVerificationCode5)
                            setIsOtpApiInProgress(true)
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
                            setIsOtpApiInProgress(false)
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
                            setIsOtpApiInProgress(false)
                        }

                        override fun onFailureHandler() {
                            super.onFailureHandler()
                            setIsOtpApiInProgress(false)
                            setOTPDescription(getSavedOTP())
                        }

                        override fun onFailureHandler(error: Throwable?) {
                            super.onFailureHandler(error)
                            setIsOtpApiInProgress(false)
                            if (error is ConnectException || error is UnknownHostException) {
                                activity.resources?.let { resources ->
                                    enterOTPDescriptionScreenTextView?.text =
                                        resources.getString(R.string.check_connection_status)
                                }
                            }
                        }
                    })
                } else {
                    setIsOtpApiInProgress(false)
                    if (!isAdded) return
                    ErrorHandlerView(activity).showToast()
                    return
                }
            }
        }
    }

    private fun setIsOtpApiInProgress(state: Boolean) {
        MyCardActivityExtension.requestOTPFragmentIsActivated = state
    }


    private fun saveSelectedOTP(otpMethodType: OTPMethodType) = (activity as? MyCardActivityExtension)?.setOTPType(otpMethodType)

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (!MyCardActivityExtension.requestOTPFragmentIsActivated) {
                    (activity as? MyCardActivityExtension)?.hideBackIcon()
                    activity?.onBackPressed()
                }
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
        binding.apply {
            displayRetrievedOTP(edtVerificationCode1, otp, 0)
            displayRetrievedOTP(edtVerificationCode2, otp, 1)
            displayRetrievedOTP(edtVerificationCode3, otp, 2)
            displayRetrievedOTP(edtVerificationCode4, otp, 3)
            displayRetrievedOTP(edtVerificationCode5, otp, 4)
            //cancelSMSListener()
        }
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

    private fun EnterOtpFragmentBinding.disableEditText(editText: EditText?) {
        editText?.isFocusableInTouchMode = false
        editText?.isFocusable = false
        editText?.isEnabled = false
        editText?.inputType = 0
        otpErrorTextView?.visibility = GONE
    }

    private fun EnterOtpFragmentBinding.showOTPErrorOnOTPFragment() {
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

    fun EnterOtpFragmentBinding.setupInputListeners() {
        KotlinUtils.lowercaseEditText(edtVerificationCode1)
        KotlinUtils.lowercaseEditText(edtVerificationCode2)
        KotlinUtils.lowercaseEditText(edtVerificationCode3)
        KotlinUtils.lowercaseEditText(edtVerificationCode4)
        KotlinUtils.lowercaseEditText(edtVerificationCode5)

        edtVerificationCode1?.addTextChangedListener(OTPViewTextWatcher(edtVerificationCode1, edtVerificationCode1, edtVerificationCode2) {validateVerificationCode()})
        edtVerificationCode2?.addTextChangedListener(OTPViewTextWatcher(edtVerificationCode1, edtVerificationCode2, edtVerificationCode3) {validateVerificationCode()})
        edtVerificationCode3?.addTextChangedListener(OTPViewTextWatcher(edtVerificationCode2, edtVerificationCode3, edtVerificationCode4) {validateVerificationCode()})
        edtVerificationCode4?.addTextChangedListener(OTPViewTextWatcher(edtVerificationCode3, edtVerificationCode4, edtVerificationCode5) {validateVerificationCode()})
        edtVerificationCode5?.addTextChangedListener(OTPViewTextWatcher(edtVerificationCode4, edtVerificationCode5, edtVerificationCode5) {validateVerificationCode()})

        edtVerificationCode1?.setOnKeyListener(View.OnKeyListener
        { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                if (edtVerificationCode1?.text?.isEmpty() == true) {
                    edtVerificationCode1?.setSelection(edtVerificationCode1?.text?.length ?: 0)
                    edtVerificationCode1?.requestFocus(View.FOCUS_DOWN)
                }
                edtVerificationCode1?.text?.clear()
                validateVerificationCode()
                return@OnKeyListener true
            }
            false
        })

        edtVerificationCode2?.setOnKeyListener(View.OnKeyListener
        { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {

                if (edtVerificationCode2?.text?.isEmpty() == true) {
                    edtVerificationCode1?.setSelection(edtVerificationCode1?.text?.length ?: 0)
                    edtVerificationCode1?.requestFocus(View.FOCUS_DOWN)
                }

                edtVerificationCode2?.text?.clear()
                validateVerificationCode()
                return@OnKeyListener true
            }
            false
        })

        edtVerificationCode3?.setOnKeyListener(View.OnKeyListener
        { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {

                if (edtVerificationCode3?.text?.isEmpty() == true) {
                    edtVerificationCode2?.setSelection(edtVerificationCode2?.text?.length ?: 0)
                    edtVerificationCode2?.requestFocus(View.FOCUS_DOWN)
                }
                edtVerificationCode3?.text?.clear()
                validateVerificationCode()
                return@OnKeyListener true
            }
            false
        })

        edtVerificationCode4?.setOnKeyListener(View.OnKeyListener
        { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                if (edtVerificationCode4?.text?.isEmpty() == true) {
                    edtVerificationCode3?.setSelection(edtVerificationCode3?.text?.length ?: 0)
                    edtVerificationCode3?.requestFocus(View.FOCUS_DOWN)
                }
                edtVerificationCode4?.text?.clear()
                validateVerificationCode()
                return@OnKeyListener true
            }
            false
        })

        edtVerificationCode5?.setOnKeyListener(View.OnKeyListener
        { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                if (edtVerificationCode5?.text?.isEmpty() == true) {
                    edtVerificationCode4?.setSelection(edtVerificationCode4?.text?.length ?: 0)
                    edtVerificationCode4?.requestFocus(View.FOCUS_DOWN)
                }
                edtVerificationCode5?.text?.clear()
                validateVerificationCode()
                return@OnKeyListener true
            }
            false
        })

    }

    private fun EnterOtpFragmentBinding.validateVerificationCode() {
        when ((edtVerificationCode1?.length() == 1)
                && (edtVerificationCode2?.length() == 1)
                && (edtVerificationCode3?.length() == 1)
                && (edtVerificationCode4?.length() == 1)
                && (edtVerificationCode5?.length() == 1)) {
            true -> {
                imNextProcessLinkCard?.isEnabled = true
                imNextProcessLinkCard?.alpha = 1.0f
                imNextProcessLinkCard?.isFocusable = false
            }
            false -> {
                imNextProcessLinkCard?.isEnabled = false
                imNextProcessLinkCard?.alpha = 0.5f
                imNextProcessLinkCard?.isFocusable = true
            }
        }

        if (otpErrorTextView.visibility == VISIBLE) {
            clearOTP()
            setOtpErrorBackground(R.drawable.otp_box_background_focus_selector)
        }

        otpErrorTextView?.visibility = GONE
    }

    fun EnterOtpFragmentBinding.clearOTP() {
        edtVerificationCode1?.text?.clear()
        edtVerificationCode2?.text?.clear()
        edtVerificationCode3?.text?.clear()
        edtVerificationCode4?.text?.clear()
        edtVerificationCode5?.text?.clear()
    }

    fun EnterOtpFragmentBinding.setOtpErrorBackground(drawableId: Int) {
        context?.let { context ->
            ContextCompat.getDrawable(context, drawableId)?.apply {
                edtVerificationCode1?.setBackgroundResource(drawableId)
                edtVerificationCode2?.setBackgroundResource(drawableId)
                edtVerificationCode3?.setBackgroundResource(drawableId)
                edtVerificationCode4?.setBackgroundResource(drawableId)
                edtVerificationCode5?.setBackgroundResource(drawableId)
            }
        }
    }
}