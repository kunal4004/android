package za.co.woolworths.financial.services.android.ui.fragments.otp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_enter_otp, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        buttonNext?.isEnabled = false
        setupInputListeners()
        clickEvent()
        enterOTPDescriptionScreen.text = activity?.resources?.getString(R.string.icr_otp_phone_desc, otpSentTo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle("bundle")
        bundle?.apply {
            otpSentTo = getString("otpSentTo", "")
            numberToOTPSent = getString("numberToOTPSent", "")
        }
    }

    private fun clickEvent() {
        buttonNext?.setOnClickListener {
            navigateToValidateOTP()
        }
        didNotReceiveOTPTextView?.setOnClickListener {
            hideKeyboard()
            (activity as? AppCompatActivity)?.apply {
                mResendOTPDialogFragment = ResendOTPDialogFragment.newInstance(this@EnterOTPFragment, "")
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
        bundle?.putString("otpMethodType", otpMethodType.name)
        navController?.navigate(R.id.action_to_retrieveOTPFragment, bundleOf("bundle" to bundle))
    }


}