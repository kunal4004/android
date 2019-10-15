package za.co.woolworths.financial.services.android.ui.fragments.temporary_store_card

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.enter_otp_fragment.*
import za.co.woolworths.financial.services.android.ui.activities.store_card.RequestOTPActivity.Companion.OTP_SENT_TO
import za.co.woolworths.financial.services.android.ui.activities.store_card.RequestOTPActivity.Companion.OTP_VALUE
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.util.KotlinUtils
import java.util.*

class RequestOTPFragment : Fragment() {

    private var mOtpSentTo: String? = null

    companion object {
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
            sendOTPBAck()
        }
        /*tvDidNotReceivedOTP?.setOnClickListener {
            (activity as? AppCompatActivity)?.apply {
                val resendOTPFragment = ResendOTPFragment.newInstance(this@RequestOTPFragment, mOtpSentTo)
                resendOTPFragment.show(supportFragmentManager.beginTransaction(), ResendOTPFragment::class.java.simpleName)
            }
        }*/
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

    private fun sendOTPBAck() {
        val otpNumber = getNumberFromEditText(edtVericationCode1).plus(getNumberFromEditText(edtVerificationCode2)).plus(getNumberFromEditText(edtVerificationCode3)).plus(getNumberFromEditText(edtVerificationCode4)).plus(getNumberFromEditText(edtVerificationCode5))
        activity?.apply {
            setResult(RESULT_OK, Intent().putExtra(OTP_VALUE, otpNumber))
            finish()
        }
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

    private fun getNumberFromEditText(editText: EditText) = editText?.text?.toString() ?: ""

    fun showSoftKeyboard(activity: Activity, editTextView: EditText) {
        activity.apply {
            (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.apply {
                showSoftInput(editTextView, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }
}