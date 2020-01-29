package za.co.woolworths.financial.services.android.ui.fragments.otp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.contracts.RequestListener
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.models.dto.otp.RetrieveOTPResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService

class RetrieveOTPFragment : Fragment() {

    var navController: NavController? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.retrieve_otp_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        initRetrieveOTP()
    }

    private fun initRetrieveOTP() {
        OneAppService.retrieveOTP(OTPMethodType.SMS, "20").enqueue(CompletionHandler(object : RequestListener<RetrieveOTPResponse> {
            override fun onSuccess(retrieveOTPResponse: RetrieveOTPResponse?) {
                navController?.navigate(R.id.action_to_enterOTPFragment)
                /*when (retrieveOTPResponse?.httpCode) {
                    200 -> navController?.navigate(R.id.action_to_enterOTPFragment)
                    else -> navController?.navigate(R.id.action_to_retrieveOTPErrorFragment)
                }*/
            }

            override fun onFailure(error: Throwable) {
                navController?.navigate(R.id.action_to_retrieveOTPErrorFragment)
            }
        }, RetrieveOTPResponse::class.java))
    }
}