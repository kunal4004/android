package za.co.woolworths.financial.services.android.ui.fragments.temporary_store_card

import android.app.Activity.RESULT_OK
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
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardActivityExtension
import za.co.woolworths.financial.services.android.ui.activities.store_card.RequestOTPActivity
import za.co.woolworths.financial.services.android.ui.activities.store_card.RequestOTPActivity.Companion.OTP_VALUE
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.npc.OTPInputListener
import za.co.woolworths.financial.services.android.ui.fragments.npc.ResendOTPFragment
import za.co.woolworths.financial.services.android.util.KotlinUtils
import java.util.*

class RequestOTPFragment : OTPInputListener(), IOTPLinkStoreCard<LinkNewCardOTP> {

    private var mResendOTPFragment: ResendOTPFragment? = null
    private var mOtpSentTo: String? = null

    companion object {
        const val OTP_SENT_TO = "OTP_SENT_TO"
        fun newInstance(otpSentTo: String) = RequestOTPFragment().withArgs {
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
            sendOTBack()
        }
        tvDidNotReceivedOTP?.setOnClickListener {
            val defaultOtp = (activity as? RequestOTPActivity)?.mOtpSentTo
            (activity as? AppCompatActivity)?.apply {
                mResendOTPFragment = ResendOTPFragment.newInstance(this@RequestOTPFragment, defaultOtp)
                mResendOTPFragment?.show(supportFragmentManager.beginTransaction(), ResendOTPFragment::class.java.simpleName)
            }
        }
    }

    private fun sendOTBack() {
        val otpNumber = getNumberFromEditText(edtVerificationCode1).plus(getNumberFromEditText(edtVerificationCode2)).plus(getNumberFromEditText(edtVerificationCode3)).plus(getNumberFromEditText(edtVerificationCode4)).plus(getNumberFromEditText(edtVerificationCode5))
        activity?.apply {
            setResult(RESULT_OK, Intent().putExtra(OTP_VALUE, otpNumber))
            (this as RequestOTPActivity).finishActivity()
        }
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                super.onOptionsItemSelected(item)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun getNumberFromEditText(numberEditText: EditText?) = numberEditText?.text?.toString()
            ?: ""
}