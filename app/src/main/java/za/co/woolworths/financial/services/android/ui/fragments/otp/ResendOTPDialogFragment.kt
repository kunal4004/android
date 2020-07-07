package za.co.woolworths.financial.services.android.ui.fragments.otp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.npc_resend_otp_fragment.*
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardActivityExtension
import za.co.woolworths.financial.services.android.ui.adapters.ResendOTPAdapter
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.npc.EnterOtpFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.Utils

class ResendOTPDialogFragment : WBottomSheetDialogFragment() {

    interface IResendOTPOptionSelection {
        fun onOTPMethodSelected(otpMethodType: OTPMethodType)
    }

    private var listener: IResendOTPOptionSelection? = null
    private var mOtpSentTo: String = ""

    companion object {
        private const val PHONE_NUMBER = "0861 50 20 20"
        fun newInstance(onSelectedOTPMethod: IResendOTPOptionSelection?, otpSendTo: String?) = ResendOTPDialogFragment().apply {
            listener = onSelectedOTPMethod
            withArgs {
                putString(EnterOtpFragment.OTP_SENT_TO, otpSendTo)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let { bundle -> mOtpSentTo = bundle.getString(EnterOtpFragment.OTP_SENT_TO, "") }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.npc_resend_otp_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MyCardActivityExtension)?.showBackIcon()

        val resendOtpAdapter = ResendOTPAdapter { selectedPosition ->
            when (selectedPosition) {
                0 -> dismissView(OTPMethodType.SMS)
                1 -> dismissView(OTPMethodType.EMAIL)
                2 -> activity?.let { activity -> Utils.makeCall( PHONE_NUMBER) }
            }
        }

        activity?.let { activity -> rvResendOTPContent?.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false) }
        rvResendOTPContent?.adapter = resendOtpAdapter
        resendOtpAdapter.setItem(resendOTPOption())
        tvCancel?.setOnClickListener { dismissView(OTPMethodType.NONE) }

        uniqueIdForResendOTP()
    }

    private fun uniqueIdForResendOTP() {
        activity?.resources?.apply {
            resendOtpRootConstraintLayout?.contentDescription = getString(R.string.resend_otp_layout)
            tvResendOTP?.contentDescription = getString(R.string.resend_otp_title)
            rvResendOTPContent?.contentDescription = getString(R.string.resend_Otp_recyclerview)
            tvCancel?.contentDescription = getString(R.string.cancel)
        }
    }

    private fun dismissView(otpMethodType: OTPMethodType) {
        listener?.onOTPMethodSelected(otpMethodType)
        dismissAllowingStateLoss()
    }

    private fun resendOTPOption(): MutableList<Triple<Int, Int, String>> {
        val resendOptions: MutableList<Triple<Int, Int, String>> = mutableListOf()
        with(resendOptions) {
            add(Triple(R.drawable.icon_mobile_phone, R.string.resend_sms_to, if (mOtpSentTo.contains("@")) " *** **** 1234" else mOtpSentTo))
            add(Triple(R.drawable.icon_email, R.string.send_to_your_email_address, ""))
            add(Triple(R.drawable.icon_mobile_call, R.string.call_center_desc, PHONE_NUMBER))
        }
        return resendOptions
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

}
