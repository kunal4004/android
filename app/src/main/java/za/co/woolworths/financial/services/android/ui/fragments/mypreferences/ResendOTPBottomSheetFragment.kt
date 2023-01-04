package za.co.woolworths.financial.services.android.ui.fragments.mypreferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentLinkDeviceResendOtpBinding
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

class ResendOTPBottomSheetFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    private lateinit var binding: FragmentLinkDeviceResendOtpBinding
    private var otpSMSNumber: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
           otpSMSNumber = it[OTP_SMS_NUMBER] as? String
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentLinkDeviceResendOtpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            context?.let { resendSMSto.text = it.getString(R.string.resend_sms_to, otpSMSNumber) }
            resendSMSto.setOnClickListener(this@ResendOTPBottomSheetFragment)
            resendEmailOTP.setOnClickListener(this@ResendOTPBottomSheetFragment)
            resendCallCenter.setOnClickListener(this@ResendOTPBottomSheetFragment)
            resendDialogCancel.setOnClickListener(this@ResendOTPBottomSheetFragment)
        }
    }

    companion object {
        const val OTP_SMS_NUMBER = "otpSMSNumber"
    }

    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.resendSMSto -> {
                setFragmentResult("resendOTPType", bundleOf(
                        "type" to "SMS"
                ))
                dismissAllowingStateLoss()
            }
            R.id.resendEmailOTP -> {
                setFragmentResult("resendOTPType", bundleOf(
                        "type" to "EMAIL"
                ))
                dismissAllowingStateLoss()

            }
            R.id.resendCallCenter -> {
                setFragmentResult("resendOTPType", bundleOf(
                        "type" to "CALL CENTER"
                ))
                dismissAllowingStateLoss()

            }
            R.id.resendDialogCancel -> {
                dismissAllowingStateLoss()
            }
            else -> {
                dismissAllowingStateLoss()
            }
        }
    }
}