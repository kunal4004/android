package za.co.woolworths.financial.services.android.ui.fragments.otp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.models.dto.otp.RetrieveOTPResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService

class RetrieveOTPFragment : Fragment() {

    var navController: NavController? = null
    lateinit var absaCardToken: String
    lateinit var productOfferingId: String
    var bundle: Bundle? = null
    lateinit var otpMethodType: OTPMethodType
    var retrieveOTPResponse: RetrieveOTPResponse? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.retrieve_otp_fragment, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle("bundle")
        bundle?.apply {
            absaCardToken = getString("absaCardToken", "")
            productOfferingId = getString("productOfferingId", "")
            otpMethodType = OTPMethodType.valueOf(getString("otpMethodType", OTPMethodType.SMS.name))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        initRetrieveOTP(otpMethodType)
    }

    private fun initRetrieveOTP(otpMethodType: OTPMethodType) {
        OneAppService.retrieveOTP(otpMethodType, productOfferingId).enqueue(CompletionHandler(object : IResponseListener<RetrieveOTPResponse> {
            override fun onSuccess(retrieveOTPResponse: RetrieveOTPResponse?) {
                retrieveOTPResponse?.apply {
                    this@RetrieveOTPFragment.retrieveOTPResponse = this
                    handleRetrieveOTPResponse(this)
                }
            }

            override fun onFailure(error: Throwable) {
                navController?.navigate(R.id.action_to_retrieveOTPErrorFragment, bundleOf("bundle" to bundle))
            }
        }, RetrieveOTPResponse::class.java))
    }

    override fun onResume() {
        super.onResume()
        retrieveOTPResponse?.apply {
            handleRetrieveOTPResponse(this)
        }
    }

    private fun handleRetrieveOTPResponse(response: RetrieveOTPResponse?) {
        response?.apply {
            when (httpCode) {
                200 -> {
                    bundle?.apply {
                        putString("otpSentTo", otpSentTo)
                        putString("otpMethodType", otpMethodType.name)
                        if (otpMethodType == OTPMethodType.SMS)
                            putString("numberToOTPSent", otpSentTo)
                        navController?.navigate(R.id.action_to_enterOTPFragment, bundleOf("bundle" to this))
                    }
                }
                else -> navController?.navigate(R.id.action_to_retrieveOTPErrorFragment, bundleOf("bundle" to bundle))
            }
        }
    }

}