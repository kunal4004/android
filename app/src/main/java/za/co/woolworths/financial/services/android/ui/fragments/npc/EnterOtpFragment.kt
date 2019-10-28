package za.co.woolworths.financial.services.android.ui.fragments.npc

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
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.util.KotlinUtils
import java.util.*
import android.view.MenuInflater
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardActivityExtension

class EnterOtpFragment : OTPInputListener(), IOTPLinkStoreCard<LinkNewCardOTP> {

    private var mResendOTPFragment: ResendOTPFragment? = null
    private var mOtpSentTo: String? = null

    companion object {
        const val OTP_SENT_TO = "OTP_SENT_TO"
        fun newInstance(otpSentTo: String) = EnterOtpFragment().withArgs {
            putString(OTP_SENT_TO, otpSentTo)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let { bundle -> mOtpSentTo = bundle.getString(OTP_SENT_TO, "") }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.enter_otp_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MyCardActivityExtension)?.apply {
            startSMSListener()
            showBackIcon()
        }
        setupInputListeners()
        configureUI()
        clickEvent()
        setOTPDescription(mOtpSentTo?.toLowerCase(Locale.getDefault()))
        imNextProcessLinkCard?.isEnabled = false
    }

    private fun setOTPDescription(otpType: String?) {
        mOtpSentTo = otpType
        val desc = activity?.resources?.getString(R.string.enter_otp_desc, otpType)
        activity?.let { activity -> otpType?.let { type -> KotlinUtils.highlightTextInDesc(activity, SpannableString(desc), type, tvEnterOtpDesc, false) } }
    }

    private fun configureUI() {
        tvDidNotReceivedOTP?.paintFlags = tvDidNotReceivedOTP.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    private fun clickEvent() {
        imNextProcessLinkCard?.setOnClickListener {
            navigateToLinkStoreCard()
        }
        tvDidNotReceivedOTP?.setOnClickListener {
            val defaultOtp = (activity as? MyCardActivityExtension)?.mDefaultOtpSentTo
            (activity as? AppCompatActivity)?.apply {
                mResendOTPFragment = ResendOTPFragment.newInstance(this@EnterOtpFragment, defaultOtp)
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

    override fun onResume() {
        super.onResume()
        activity?.let { activity ->
            edtVerificationCode1?.apply {
                requestFocus()
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
}