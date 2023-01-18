package za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.opt_in.otp

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ValidateOtpFragmentBinding
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.models.dto.otp.ValidateOTPRequest
import za.co.woolworths.financial.services.android.models.dto.otp.ValidateOTPResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity
import za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel.BPIViewModel
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.BundleKeysConstants

class BpiValidateOTPFragment: Fragment(R.layout.validate_otp_fragment) {

    private lateinit var binding: ValidateOtpFragmentBinding
    private val bpiViewModel: BPIViewModel? by activityViewModels()

    var navController: NavController? = null
    var bundle: Bundle? = null
    private lateinit var otpValue: String
    lateinit var productOfferingId: String
    lateinit var otpMethodType: OTPMethodType
    lateinit var otpSentTo: String
    var validateOTPResponse: ValidateOTPResponse? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ValidateOtpFragmentBinding.bind(view)
        (activity as? BalanceProtectionInsuranceActivity)?.hideDisplayHomeAsUpEnabled()
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
        bundle?.putString("screenType", BpiValidateOTPFragment::class.java.simpleName)
    }

    private fun initValidateOTP() {
        val validateOTPRequest = ValidateOTPRequest(otpMethodType.name, otpValue)
        bpiViewModel?.setValidateOTPRequest(validateOTPRequest)
        OneAppService.validateOTP(validateOTPRequest, productOfferingId).enqueue(CompletionHandler(object : IResponseListener<ValidateOTPResponse> {
            override fun onSuccess(response: ValidateOTPResponse?) {
                response?.apply {
                    this@BpiValidateOTPFragment.validateOTPResponse = this
                    handleValidateOTPResponse(this)
                }
            }

            override fun onFailure(error: Throwable?) {
                if (!isAdded) return
                navigateToValidateOTPErrorFragment()
            }
        }, ValidateOTPResponse::class.java))
    }

    fun navigateToValidateOTPErrorFragment() {
        navController?.navigate(R.id.action_bpiValidateOTPFragment_to_bpiValidateOTPErrorFragment, bundleOf("bundle" to bundle))

    }

    fun handleValidateOTPResponse(validateOTPResponse: ValidateOTPResponse?) {
        if (!isAdded) return
        validateOTPResponse?.apply {
            when (httpCode) {
                AppConstant.HTTP_OK -> view?.findNavController()?.navigate(R.id.action_bpiValidateOTPFragment_to_BPIProcessingRequestFragment, bundleOf("bundle" to bundle))
                AppConstant.HTTP_EXPECTATION_FAILED_502 -> {
                    if (response?.code.equals("1060"))
                        navController?.navigate(R.id.action_bpiValidateOTPFragment_to_bpiEnterOtpFragment, bundleOf("bundle" to bundle))
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