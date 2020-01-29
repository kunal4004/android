package za.co.woolworths.financial.services.android.ui.fragments.otp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_enter_otp.*
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType

class EnterOTPFragment : EnterOTPFragmentExtension(), ResendOTPDialogFragment.IResendOTPOptionSelection {

    var navController: NavController? = null
    private var mResendOTPDialogFragment: ResendOTPDialogFragment? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_enter_otp, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        clickEvent()
        buttonNext?.isEnabled = false
        setupInputListeners()
        clickEvent()
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
        val otpNumber = getNumberFromEditText(edtVerificationCode1)
                .plus(getNumberFromEditText(edtVerificationCode2))
                .plus(getNumberFromEditText(edtVerificationCode3))
                .plus(getNumberFromEditText(edtVerificationCode4))
                .plus(getNumberFromEditText(edtVerificationCode5))

        navController?.navigate(R.id.action_to_validateOTPFragment)
    }

    override fun onOTPMethodSelected(otpMethodType: OTPMethodType) {
        navController?.navigate(R.id.action_to_retrieveOTPFragment)
    }


}