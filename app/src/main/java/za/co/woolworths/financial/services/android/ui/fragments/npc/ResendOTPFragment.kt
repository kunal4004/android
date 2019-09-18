package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup


import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.npc_resend_otp_fragment.*
import za.co.woolworths.financial.services.android.contracts.IOTPLinkStoreCard
import za.co.woolworths.financial.services.android.models.dto.npc.LinkNewCardOTP
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.ui.activities.card.InstantStoreCardReplacementActivity
import za.co.woolworths.financial.services.android.ui.adapters.ResendOTPAdapter
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.npc.EnterOtpFragment.Companion.OTP_SENT_TO
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.Utils

class ResendOTPFragment : WBottomSheetDialogFragment() {

    private var linkStoreCardOtp: IOTPLinkStoreCard<LinkNewCardOTP>? = null
    private var mOtpSentTo: String = ""

    companion object {
        private const val PHONE_NUMBER = "0861 50 20 20"
        fun newInstance(otpLink: IOTPLinkStoreCard<LinkNewCardOTP>?, otpSendTo: String?) = ResendOTPFragment().apply {
            linkStoreCardOtp = otpLink
            withArgs {
                putString(OTP_SENT_TO, otpSendTo)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let { bundle -> mOtpSentTo = bundle.getString(OTP_SENT_TO, "") }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.npc_resend_otp_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? InstantStoreCardReplacementActivity)?.showBackIcon()

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
        if (otpMethodType != OTPMethodType.NONE){
            linkStoreCardOtp?.requestOTPApi(otpMethodType)
        }
        dismissAllowingStateLoss()
    }

    private fun resendOTPOption(): MutableList<Triple<Int, Int, String>> {
        val resendOptions: MutableList<Triple<Int, Int, String>> = mutableListOf()
        with(resendOptions) {
            add(Triple(R.drawable.icon_mobile_phone, R.string.resend_sms_to, "1234"))
            add(Triple(R.drawable.icon_email, R.string.send_to_your_email_address, ""))
            add(Triple(R.drawable.icon_mobile_call, R.string.call_center_desc, PHONE_NUMBER))
        }
        return resendOptions
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                (activity as? InstantStoreCardReplacementActivity)?.hideBackIcon()
                activity?.onBackPressed()
                super.onOptionsItemSelected(item)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
