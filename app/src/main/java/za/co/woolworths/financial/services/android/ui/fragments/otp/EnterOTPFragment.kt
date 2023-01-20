package za.co.woolworths.financial.services.android.ui.fragments.otp

import android.graphics.Paint
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType

class EnterOTPFragment : EnterOTPFragmentExtension(), ResendOTPDialogFragment.IResendOTPOptionSelection {

    private var navController: NavController? = null
    private var mResendOTPDialogFragment: ResendOTPDialogFragment? = null
    var bundle: Bundle? = null
    lateinit var otpSentTo: String
    lateinit var numberToOTPSent: String
    lateinit var otpValue: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.runOnUiThread { activity?.window?.addFlags(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE) }
        navController = Navigation.findNavController(view)
        binding.buttonNext?.isEnabled = false
        binding.setupInputListeners()
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
        binding.enterOTPDescriptionScreen?.text = activity?.resources?.getString(R.string.sent_otp_desc, otpSentTo)
        binding.didNotReceiveOTPTextView?.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        if (otpValue.isNotEmpty())
            showWrongOTP()
    }

    private fun clickEvent() {
        binding.buttonNext?.setOnClickListener {
            navigateToValidateOTP()
        }
        binding.didNotReceiveOTPTextView?.setOnClickListener {
            hideKeyboard()
            (activity as? AppCompatActivity)?.apply {
                mResendOTPDialogFragment = ResendOTPDialogFragment.newInstance(this@EnterOTPFragment, numberToOTPSent)
                mResendOTPDialogFragment?.show(supportFragmentManager.beginTransaction(), ResendOTPDialogFragment::class.java.simpleName)
            }
        }
    }

    private fun navigateToValidateOTP() {
        binding.apply {
            hideKeyboard()
            val otpValue = getNumberFromEditText(edtVerificationCode1)
                .plus(getNumberFromEditText(edtVerificationCode2))
                .plus(getNumberFromEditText(edtVerificationCode3))
                .plus(getNumberFromEditText(edtVerificationCode4))
                .plus(getNumberFromEditText(edtVerificationCode5))
            bundle?.putString("otpValue", otpValue)
            navController?.navigate(
                R.id.action_to_validateOTPFragment,
                bundleOf("bundle" to bundle)
            )
        }
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
            binding.edtVerificationCode1?.apply {
                requestFocus()
                showSoftKeyboard(activity, this)
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            }
        }
    }

    private fun showWrongOTP() {
        binding.apply {
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
            otpErrorTextView?.visibility = View.VISIBLE
            setOtpErrorBackground(R.drawable.otp_box_error_background)
        }
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