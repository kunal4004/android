package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.enter_otp_fragment.*
import za.co.woolworths.financial.services.android.contracts.IOTPLinkStoreCard
import za.co.woolworths.financial.services.android.models.dto.npc.LinkNewCardOTP
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.ui.activities.card.InstantStoreCardReplacementActivity
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.util.KotlinUtils
import java.util.*
import android.view.MenuInflater
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardActivityExtension

class EnterOtpFragment : MyCardExtension(), IOTPLinkStoreCard<LinkNewCardOTP> {

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
        (activity as? MyCardActivityExtension)?.showBackIcon()
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
            val defaultOtp = (activity as? InstantStoreCardReplacementActivity)?.mDefaultOtpSentTo
            (activity as? AppCompatActivity)?.apply {
                val resendOTPFragment = ResendOTPFragment.newInstance(this@EnterOtpFragment, defaultOtp)
                resendOTPFragment.show(supportFragmentManager.beginTransaction(), ResendOTPFragment::class.java.simpleName)
            }
        }
    }

    private fun setupInputListeners() = arrayOf<EditText>(edtVericationCode1, edtVerificationCode2,
            edtVerificationCode3, edtVerificationCode4, edtVerificationCode5).apply {
        val listSize = size - 1
        for ((index, currentEditText) in withIndex()) {
            val nextEditText: EditText? = if (index < listSize) this[index + 1] else null
            val previousEditText: EditText? = if (index > 0) this[index - 1] else null
            currentEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    when (index) {
                        0 -> if (s.length == 1) nextEditText?.requestFocus(View.FOCUS_DOWN) // First EditText
                        listSize -> if (count == 0) previousEditText?.requestFocus(View.FOCUS_DOWN) // Last EditText
                        else -> { // Remaining editText
                            if (count == 0) {
                                previousEditText?.requestFocus(View.FOCUS_DOWN)
                            } else {
                                if (s.length == 1) {
                                    nextEditText?.requestFocus(View.FOCUS_DOWN)
                                }
                            }
                        }
                    }
                }

                override fun afterTextChanged(s: Editable) {
                    when (index) {
                        listSize -> currentEditText.apply { isCursorVisible = s.isEmpty() }
                    }
                    validateVerificationCode()
                }
            })
        }
    }

    private fun validateVerificationCode() {
        when ((edtVericationCode1.length() == 1)
                && (edtVerificationCode2.length() == 1)
                && (edtVerificationCode3.length() == 1)
                && (edtVerificationCode4.length() == 1)
                && (edtVerificationCode5.length() == 1)) {
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
    }

    private fun navigateToLinkStoreCard() {
        val otpNumber = getNumberFromEditText(edtVericationCode1).plus(getNumberFromEditText(edtVerificationCode2)).plus(getNumberFromEditText(edtVerificationCode3)).plus(getNumberFromEditText(edtVerificationCode4)).plus(getNumberFromEditText(edtVerificationCode5))
        (activity as? InstantStoreCardReplacementActivity)?.setOTPNumber(otpNumber)

        replaceFragment(
                fragment = LinkStoreCardFragment.newInstance(),
                tag = AnimatedProgressBarFragment::class.java.simpleName,
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
            edtVericationCode1?.apply {
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
        replaceFragment(
                fragment = ResendOTPLoaderFragment.newInstance(otpMethodType, this),
                tag = ProcessBlockCardFragment::class.java.simpleName,
                containerViewId = R.id.flMyCard,
                allowStateLoss = true,
                enterAnimation = R.anim.stay,
                exitAnimation = R.anim.stay,
                popEnterAnimation = R.anim.stay,
                popExitAnimation = R.anim.stay)
    }

    private fun saveSelectedOTP(otpMethodType: OTPMethodType) = (activity as? InstantStoreCardReplacementActivity)?.setOTPType(otpMethodType)

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

    private fun getNumberFromEditText(numberEditText: EditText?) = numberEditText?.text?.toString() ?: ""
}