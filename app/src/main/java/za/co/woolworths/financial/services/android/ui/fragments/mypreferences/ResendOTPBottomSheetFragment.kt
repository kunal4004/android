package za.co.woolworths.financial.services.android.ui.fragments.mypreferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_link_device_resend_otp.*
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

class ResendOTPBottomSheetFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    private var otpNumber: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
           otpNumber = it[OTP_NUMBER] as? String
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_link_device_resend_otp, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context?.let {resendSMSto.text =  it.getString(R.string.resend_sms_to, otpNumber)}
        resendSMSto.setOnClickListener(this)
        resendEmailOTP.setOnClickListener(this)
        resendCallCenter.setOnClickListener(this)
        resendDialogCancel.setOnClickListener(this)
    }

    companion object {
        const val OTP_NUMBER = "otpNumber"
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