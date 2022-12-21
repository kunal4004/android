package za.co.woolworths.financial.services.android.ui.fragments.otp

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ValidateOtpFragmentBinding
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.models.dto.otp.ValidateOTPRequest
import za.co.woolworths.financial.services.android.models.dto.otp.ValidateOTPResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.util.BundleKeysConstants

class ValidateOTPFragment : Fragment(R.layout.validate_otp_fragment) {

    private lateinit var binding: ValidateOtpFragmentBinding
    var navController: NavController? = null
    var bundle: Bundle? = null
    lateinit var otpValue: String
    lateinit var productOfferingId: String
    lateinit var otpMethodType: OTPMethodType
    lateinit var otpSentTo: String
    var validateOTPResponse: ValidateOTPResponse? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ValidateOtpFragmentBinding.bind(view)
        navController = Navigation.findNavController(view)
        binding.description.text = activity?.resources?.getString(R.string.icr_otp_phone_desc, otpSentTo)
        initValidateOTP()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle("bundle")
        bundle?.apply {
            otpSentTo = getString("otpSentTo", "")
            otpValue = getString("otpValue", "")
            productOfferingId = getString(BundleKeysConstants.PRODUCT_OFFERINGID, "")
            otpMethodType = OTPMethodType.valueOf(getString("otpMethodType", OTPMethodType.SMS.name))
        }
    }

    private fun initValidateOTP() {
        OneAppService.validateOTP(ValidateOTPRequest(otpMethodType.name, otpValue), productOfferingId).enqueue(CompletionHandler(object : IResponseListener<ValidateOTPResponse> {
            override fun onSuccess(response: ValidateOTPResponse?) {
                response?.apply {
                    this@ValidateOTPFragment.validateOTPResponse = this
                    handleValidateOTPResponse(this)
                }
            }

            override fun onFailure(error: Throwable?) {
                navigateToValidateOTPErrorFragment()
            }
        }, ValidateOTPResponse::class.java))
    }

    fun navigateToValidateOTPErrorFragment() {
        navController?.navigate(R.id.action_to_validateOTPErrorFragment, bundleOf("bundle" to bundle))
    }

    private fun handleValidateOTPResponse(validateOTPResponse: ValidateOTPResponse?) {
        validateOTPResponse?.apply {
            when (httpCode) {
                200 -> navController?.navigate(R.id.action_to_creditCardActivationProgressFragment, bundleOf("bundle" to bundle))
                502 -> {
                    if (response?.code.equals("1060"))
                        navController?.navigate(R.id.action_to_enterOTPFragment, bundleOf("bundle" to bundle))
                    else
                        navigateToValidateOTPErrorFragment()
                }
                else -> navigateToValidateOTPErrorFragment()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        validateOTPResponse?.apply {
            handleValidateOTPResponse(this)
        }
    }
}