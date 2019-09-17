package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.npc_resend_otp_fragment.*
import za.co.woolworths.financial.services.android.contracts.IStoreCardOTPCallback
import za.co.woolworths.financial.services.android.models.dto.npc.LinkNewCardOTP
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.ui.adapters.ResendOTPAdapter
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.Utils

class ResendOTPFragment : WBottomSheetDialogFragment() {

    private var iStoreCardOTPCallback: IStoreCardOTPCallback<LinkNewCardOTP>? = null

    companion object {
        private const val PHONE_NUMBER = "0861 50 20 20"
        fun newInstance(otpCallback: IStoreCardOTPCallback<LinkNewCardOTP>?) = ResendOTPFragment().apply {
            iStoreCardOTPCallback = otpCallback
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.npc_resend_otp_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val resendOtpAdapter = ResendOTPAdapter { selectedPosition ->
            when (selectedPosition) {
                0 -> dismissView(OTPMethodType.SMS)
                1 -> dismissView(OTPMethodType.EMAIL)
                2 -> activity?.let { activity -> Utils.makeCall(activity, PHONE_NUMBER) }
            }
        }

        activity?.let { activity -> rvResendOTPContent?.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false) }
        rvResendOTPContent?.adapter = resendOtpAdapter
        resendOtpAdapter.setItem(resendOTPOption())

        tvCancel?.setOnClickListener { dismissView(OTPMethodType.NONE) }
    }

    private fun dismissView(otpMethodType: OTPMethodType) {
        if (otpMethodType != OTPMethodType.NONE)
            iStoreCardOTPCallback?.requestOTPApi(otpMethodType)
        dismissAllowingStateLoss()
    }

    private fun resendOTPOption(): MutableList<Pair<Int, Int>> {
        val resendOptions: MutableList<Pair<Int, Int>> = mutableListOf()
        with(resendOptions) {
            add(Pair(R.drawable.icon_mobile_phone, R.string.resend_sms_to))
            add(Pair(R.drawable.icon_email, R.string.send_to_your_email_address))
            add(Pair(R.drawable.icon_mobile_call, R.string.call_center_desc))
        }
        return resendOptions
    }
}
