package za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.opt_in.otp

import android.graphics.Paint
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.BpiFragmentEnterOtpBinding
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity
import za.co.woolworths.financial.services.android.ui.fragments.otp.ResendOTPDialogFragment

class BpiEnterOtpFragment : BpiEnterOtpFragmentExtension(),
    ResendOTPDialogFragment.IResendOTPOptionSelection {

    private var navController: NavController? = null
    private var mResendOTPDialogFragment: ResendOTPDialogFragment? = null
    private var bundle: Bundle? = null
    private lateinit var otpSentTo: String
    private lateinit var numberToOTPSent: String
    private lateinit var otpValue: String

    companion object{
        var shouldBackPressed: Boolean = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.runOnUiThread { activity?.window?.addFlags(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE) }

        navController = Navigation.findNavController(view)
        (activity as? BalanceProtectionInsuranceActivity)?.showDisplayHomeAsUpEnabled()

        binding.apply {
            includeEnterOtpFragment.buttonNext?.isEnabled = false
            setupInputListeners()
            clickEvent()
            configureUI()
            shouldBackPressed = true
            includeEnterOtpFragment.apply {
                setOnFocusChangeListener(edtVerificationCode1)
                setOnFocusChangeListener(edtVerificationCode2)
                setOnFocusChangeListener(edtVerificationCode3)
                setOnFocusChangeListener(edtVerificationCode4)
                setOnFocusChangeListener(edtVerificationCode5)
            }
        }
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

    fun BpiFragmentEnterOtpBinding.configureUI() {
        includeEnterOtpFragment.apply {
            enterOTPDescriptionScreen?.text =
                activity?.resources?.getString(R.string.sent_otp_desc, otpSentTo)
            didNotReceiveOTPTextView?.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            if (otpValue.isNotEmpty())
                showWrongOTP()
        }
    }

    private fun BpiFragmentEnterOtpBinding.clickEvent() {
        includeEnterOtpFragment.apply {
            buttonNext?.setOnClickListener {
                navigateToValidateOTP()
            }
            didNotReceiveOTPTextView?.setOnClickListener {
                hideKeyboard()
                (activity as? AppCompatActivity)?.apply {
                    mResendOTPDialogFragment =
                        ResendOTPDialogFragment.newInstance(
                            this@BpiEnterOtpFragment,
                            numberToOTPSent
                        )
                    mResendOTPDialogFragment?.show(
                        supportFragmentManager.beginTransaction(),
                        ResendOTPDialogFragment::class.java.simpleName
                    )
                }
            }
        }
    }

    private fun BpiFragmentEnterOtpBinding.navigateToValidateOTP() {
        includeEnterOtpFragment.apply {
            hideKeyboard()
            val otpValue = getNumberFromEditText(edtVerificationCode1)
                .plus(getNumberFromEditText(edtVerificationCode2))
                .plus(getNumberFromEditText(edtVerificationCode3))
                .plus(getNumberFromEditText(edtVerificationCode4))
                .plus(getNumberFromEditText(edtVerificationCode5))
            bundle?.putString("otpValue", otpValue)
            navController?.navigate(
                R.id.action_bpiEnterOtpFragment_to_bpiValidateOTPFragment,
                bundleOf("bundle" to bundle)
            )
        }
    }

    override fun onOTPMethodSelected(otpMethodType: OTPMethodType) {
        binding.apply {
            if (otpMethodType == OTPMethodType.NONE)
                requestEditTextFocus()
            else {
                shouldBackPressed = false
                bundle?.putString("otpMethodType", otpMethodType.name)
                bundle?.putString("otpValue", "")
                view?.findNavController()?.navigate(
                    R.id.action_bpiEnterOtpFragment_to_sendOtpFragment,
                    bundleOf("bundle" to bundle)
                )
                configureUI()
            }
        }
    }



    private fun BpiFragmentEnterOtpBinding.showWrongOTP() {
        includeEnterOtpFragment.apply {
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
            removeFocus()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        activity?.runOnUiThread { activity?.window?.clearFlags(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE) }
    }

}