package za.co.woolworths.financial.services.android.ui.fragments.otp

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_enter_otp.*
import kotlinx.android.synthetic.main.fragment_enter_otp.didNotReceiveOTPTextView
import kotlinx.android.synthetic.main.fragment_enter_otp.edtVerificationCode1
import kotlinx.android.synthetic.main.fragment_enter_otp.edtVerificationCode2
import kotlinx.android.synthetic.main.fragment_enter_otp.edtVerificationCode3
import kotlinx.android.synthetic.main.fragment_enter_otp.edtVerificationCode4
import kotlinx.android.synthetic.main.fragment_enter_otp.edtVerificationCode5
import kotlinx.android.synthetic.main.fragment_enter_otp.enterOTPDescriptionScreen
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType

class EnterOTPFragment : EnterOTPFragmentExtension(), ResendOTPDialogFragment.IResendOTPOptionSelection {

    var navController: NavController? = null
    private var mResendOTPDialogFragment: ResendOTPDialogFragment? = null
    var bundle: Bundle? = null
    lateinit var otpSentTo: String
    lateinit var numberToOTPSent: String
    lateinit var otpValue: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity?.runOnUiThread { activity?.window?.addFlags(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE) }
        return inflater.inflate(R.layout.fragment_enter_otp, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        buttonNext?.isEnabled = false
        setupInputListeners()
        clickEvent()
        configureUI()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle("bundle")
        bundle?.apply {
            otpSentTo = getString("otpSentTo", "")
            numberToOTPSent = getString("numberToOTPSent", "")
            otpValue = getString("otpValue", "")
        }
    }

    fun configureUI() {
        enterOTPDescriptionScreen.text = activity?.resources?.getString(R.string.icr_otp_phone_desc, otpSentTo)
        if (otpValue.isNotEmpty())
            showWrongOTP()
    }

    private fun clickEvent() {
        buttonNext?.setOnClickListener {
            navigateToValidateOTP()
        }
        didNotReceiveOTPTextView?.setOnClickListener {
            hideKeyboard()
            (activity as? AppCompatActivity)?.apply {
                mResendOTPDialogFragment = ResendOTPDialogFragment.newInstance(this@EnterOTPFragment, numberToOTPSent)
                mResendOTPDialogFragment?.show(supportFragmentManager.beginTransaction(), ResendOTPDialogFragment::class.java.simpleName)
            }
        }
    }

    private fun navigateToValidateOTP() {
        hideKeyboard()
        val otpValue = getNumberFromEditText(edtVerificationCode1)
                .plus(getNumberFromEditText(edtVerificationCode2))
                .plus(getNumberFromEditText(edtVerificationCode3))
                .plus(getNumberFromEditText(edtVerificationCode4))
                .plus(getNumberFromEditText(edtVerificationCode5))
        bundle?.putString("otpValue", otpValue)
        navController?.navigate(R.id.action_to_validateOTPFragment, bundleOf("bundle" to bundle))
    }

    override fun onOTPMethodSelected(otpMethodType: OTPMethodType) {
        if (otpMethodType == OTPMethodType.NONE)
            requestEditTextFocus()
        else {
            bundle?.putString("otpMethodType", otpMethodType.name)
            bundle?.putString("otpValue", "")
            navController?.navigate(R.id.action_to_retrieveOTPFragment, bundleOf("bundle" to bundle))
        }
    }

    private fun requestEditTextFocus() {
        (activity as? AppCompatActivity)?.let { activity ->
            edtVerificationCode1?.apply {
                requestFocus()
                showSoftKeyboard(activity, this)
            }
        }
    }

    private fun showWrongOTP() {
        if (!TextUtils.isEmpty(otpValue)) {
            with(otpValue.split("")) {
                edtVerificationCode1?.setText(this[1])
                edtVerificationCode2?.setText(this[2])
                edtVerificationCode3?.setText(this[3])
                edtVerificationCode4?.setText(this[4])
                edtVerificationCode5?.setText(this[5])
            }
            edtVerificationCode1?.setSelection(0)
        }
        otpErrorTextView.visibility = View.VISIBLE
        setOtpErrorBackground(R.drawable.otp_box_error_background)
    }

    override fun onResume() {
        super.onResume()
        requestEditTextFocus()
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.runOnUiThread { activity?.window?.clearFlags(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE) }
    }


}