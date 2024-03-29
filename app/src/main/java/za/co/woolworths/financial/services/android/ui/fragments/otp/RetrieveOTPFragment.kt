package za.co.woolworths.financial.services.android.ui.fragments.otp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import com.awfs.coordination.databinding.RetrieveOtpFragmentBinding
import za.co.woolworths.financial.services.android.contracts.IProgressAnimationState
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.models.dto.otp.RetrieveOTPResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.extension.findFragmentByTag
import za.co.woolworths.financial.services.android.ui.fragments.npc.ProgressStateFragment
import za.co.woolworths.financial.services.android.util.BundleKeysConstants
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class RetrieveOTPFragment : BaseFragmentBinding<RetrieveOtpFragmentBinding>(RetrieveOtpFragmentBinding::inflate), IProgressAnimationState,View.OnClickListener {

    var navController: NavController? = null
    lateinit var absaCardToken: String
    lateinit var productOfferingId: String
    var bundle: Bundle? = null
    lateinit var otpMethodType: OTPMethodType
    var retrieveOTPResponse: RetrieveOTPResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle("bundle")
        bundle?.apply {
            absaCardToken = getString("absaCardToken", "")
            productOfferingId = getString(BundleKeysConstants.PRODUCT_OFFERINGID, "")
            otpMethodType = OTPMethodType.valueOf(getString("otpMethodType", OTPMethodType.SMS.name))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        binding.errorView.retry.setOnClickListener(this)
        binding.errorView.needHelp.setOnClickListener(this)
        binding.startProgress()
        initRetrieveOTP(otpMethodType)
    }

    private fun initRetrieveOTP(otpMethodType: OTPMethodType) {
        OneAppService().retrieveOTP(otpMethodType, productOfferingId).enqueue(CompletionHandler(object : IResponseListener<RetrieveOTPResponse> {
            override fun onSuccess(response: RetrieveOTPResponse?) {
                response?.apply {
                    this@RetrieveOTPFragment.retrieveOTPResponse = this
                    handleRetrieveOTPResponse(this)
                }
            }

            override fun onFailure(error: Throwable?) {
                getProgressState()?.animateSuccessEnd(false)
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
                        getProgressState()?.let { activity?.supportFragmentManager?.beginTransaction()?.remove(it)?.commitAllowingStateLoss() }
                        putString("otpSentTo", otpSentTo)
                        putString("otpMethodType", otpMethodType.name)
                        if (otpMethodType == OTPMethodType.SMS)
                            putString("numberToOTPSent", otpSentTo)
                        navController?.navigate(R.id.action_to_enterOTPFragment, bundleOf("bundle" to this))
                    }
                }
                else -> getProgressState()?.animateSuccessEnd(false)
            }
        }
    }

    private fun getProgressState(): ProgressStateFragment? = (activity as? AppCompatActivity)?.findFragmentByTag(ProgressStateFragment::class.java.simpleName) as? ProgressStateFragment

    private fun onRequestOTPRetry() {
        binding.apply {
            errorView.root.visibility = View.GONE
            pageHeader.visibility = View.VISIBLE
            processingLayout.root.visibility = View.VISIBLE
            getProgressState()?.restartSpinning()
            initRetrieveOTP(otpMethodType)
        }
    }

    private fun RetrieveOtpFragmentBinding.startProgress() {
        (activity as? AppCompatActivity)?.addFragment(
                fragment = ProgressStateFragment.newInstance(this@RetrieveOTPFragment),
                tag = ProgressStateFragment::class.java.simpleName,
                containerViewId = R.id.flProgressIndicator
        )
        processingLayout.root.visibility = View.VISIBLE
    }

    private fun RetrieveOtpFragmentBinding.showErrorView() {
        pageHeader.visibility = View.INVISIBLE
        processingLayout.root.visibility = View.GONE
        errorView.root.visibility = View.VISIBLE
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.retry->onRequestOTPRetry()
            R.id.needHelp->activity?.apply { Utils.makeCall("0861 50 20 20") }
        }
    }

    override fun onAnimationEnd(isAnimationFinished: Boolean) {
        super.onAnimationEnd(isAnimationFinished)
        if(!isAnimationFinished){
            binding.showErrorView()
        }
    }
}