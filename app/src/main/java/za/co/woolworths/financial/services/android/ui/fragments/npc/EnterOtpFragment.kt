package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.text.SpannableString
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


class EnterOtpFragment : OTPInputListener(), IOTPLinkStoreCard<LinkNewCardOTP> {

    private var shouldDisableKeyboardOnOTPCall: Boolean = false
    private var mResendOTPFragment: ResendOTPFragment? = null
    private var mOtpSentTo: String? = null
    private var mPhoneNumberOTP: String? = null
    private var isUnblockVirtualCard = false

    companion object {
        const val OTP_SENT_TO = "OTP_SENT_TO"
        const val IS_UNBLOCK_VIRTUAL_CARD = "IS_UNBLOCK_VIRTUAL_CARD"
        fun newInstance() = EnterOtpFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let { bundle ->
            mOtpSentTo = bundle.getString(OTP_SENT_TO, "")
            isUnblockVirtualCard = bundle.getBoolean(IS_UNBLOCK_VIRTUAL_CARD, false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.enter_otp_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MyCardActivityExtension)?.apply {
            startSMSListener()
            showBackIcon()

            setupInputListeners()
            configureUI()
            clickEvent()
            setOTPDescription(mOtpSentTo?.toLowerCase(Locale.getDefault()))
            imNextProcessLinkCard?.isEnabled = false

            requestOTPApi(getOTPMethodType())
        }
    }

    private fun setOTPDescription(otpType: String?) {
        mOtpSentTo = otpType
        val desc = activity?.resources?.getString(R.string.enter_otp_desc, otpType)
        activity?.let { activity -> otpType?.let { type -> KotlinUtils.highlightTextInDesc(activity, SpannableString(desc), type, enterOTPDescriptionScreen, false) } }
    }

    private fun configureUI() {
        didNotReceiveEditTextOTP?.apply { paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG }
    }

    private fun clickEvent() {
        imNextProcessLinkCard?.setOnClickListener {
            if (isUnblockVirtualCard) sendOTBack() else navigateToLinkStoreCard()
        }
        didNotReceiveEditTextOTP?.setOnClickListener {
            if (shouldDisableKeyboardOnOTPCall) return@setOnClickListener
            hideKeyboard()
            (activity as? AppCompatActivity)?.apply {
                mResendOTPFragment = ResendOTPFragment.newInstance(this@EnterOtpFragment, mPhoneNumberOTP)
                mResendOTPFragment?.show(supportFragmentManager.beginTransaction(), ResendOTPFragment::class.java.simpleName)
            }
        }
    }

    private fun navigateToLinkStoreCard() {
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
        val otpNumber = getNumberFromEditText(edtVerificationCode1).plus(getNumberFromEditText(edtVerificationCode2)).plus(getNumberFromEditText(edtVerificationCode3)).plus(getNumberFromEditText(edtVerificationCode4)).plus(getNumberFromEditText(edtVerificationCode5))
        activity?.apply {
            setResult(Activity.RESULT_OK, Intent().putExtra(RequestOTPActivity.OTP_VALUE, otpNumber))
            (this as RequestOTPActivity).finishActivity()
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.clearFlags(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        requestEditTextFocus()
    }

    private fun requestEditTextFocus() {
        activity?.let { activity ->
            edtVerificationCode1?.apply {
                requestFocus()
                isFocusable = true
                isCursorVisible = true
                showSoftKeyboard(activity, this)
            }
        }
    }


    override fun navigateToEnterOTPScreen(data: LinkNewCardOTP) {
        super.navigateToEnterOTPScreen(data)
        setOTPDescription(data.otpSentTo?.toLowerCase(Locale.getDefault()))
    }

    override fun requestOTPApi(otpMethodType: OTPMethodType) {
        super.requestOTPApi(otpMethodType)
        saveSelectedOTP(otpMethodType)
        clearOTP()
        requestEditTextFocus()
        activity?.let { activity ->
            if (NetworkManager().isConnectedToNetwork(activity)) {
                StoreCardOTPRequest(activity, otpMethodType).make(object : IOTPLinkStoreCard<LinkNewCardOTP> {

                    override fun showProgress() {
                        shouldDisableKeyboardOnOTPCall = true
                        super.showProgress()
                        activity.resources?.let { resources -> enterOTPDescriptionScreen?.text = resources.getString(R.string.sending_otp_text) }
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
                        if ((activity as? MyCardActivityExtension)?.getOTPMethodType() == OTPMethodType.SMS) {
                            mPhoneNumberOTP = response.otpSentTo
                            setOTPDescription(mPhoneNumberOTP)
                        } else {
                            setOTPDescription(response.otpSentTo?.toLowerCase(Locale.getDefault()))
                        }
                    }

                    override fun onFailureHandler() {
                        super.onFailureHandler()
                        setOTPDescription(mOtpSentTo?.toLowerCase(Locale.getDefault()))
                    }
                })
            } else {
                ErrorHandlerView(activity).showToast()
                return
            }
        }
    }

    private fun clearOTP() {
        edtVerificationCode1?.text?.clear()
        edtVerificationCode2?.text?.clear()
        edtVerificationCode3?.text?.clear()
        edtVerificationCode4?.text?.clear()
        edtVerificationCode5?.text?.clear()
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
        cancelSMSListener()
    }

    fun onOTPReceived(otp: String?) {
        displayRetrievedOTP(edtVerificationCode1, otp, 0)
        displayRetrievedOTP(edtVerificationCode2, otp, 1)
        displayRetrievedOTP(edtVerificationCode3, otp, 2)
        displayRetrievedOTP(edtVerificationCode4, otp, 3)
        displayRetrievedOTP(edtVerificationCode5, otp, 4)
        cancelSMSListener()
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
    }
}