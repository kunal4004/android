package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.os.Bundle
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.npc_resend_otp_fragment.*
import za.co.woolworths.financial.services.android.contracts.IStoreCardOTPCallback
import za.co.woolworths.financial.services.android.models.dto.npc.LinkNewCardOTP
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.KotlinUtils
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

        activity?.let { activity -> KotlinUtils.contactCustomerCare(activity, SpannableString(getString(R.string.call_center_desc)), PHONE_NUMBER, tvCallTitle, false) }
        sendOTPBySMSview?.setOnClickListener {
            dismissView(OTPMethodType.SMS)
        }
        sendOTPByEmailView?.setOnClickListener {
            dismissView(OTPMethodType.EMAIL)
        }

        sendToCallCenterView?.setOnClickListener { activity?.let { activity -> Utils.makeCall(activity, PHONE_NUMBER) } }
        tvCancel?.setOnClickListener { dismissView(OTPMethodType.NONE) }

    }

    private fun dismissView(otpMethodType: OTPMethodType) {
        if (otpMethodType != OTPMethodType.NONE)
            iStoreCardOTPCallback?.requestOTPApi(otpMethodType)
        dismissAllowingStateLoss()
    }
}
