package za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.opt_in.otp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.circle_progress_layout.*
import kotlinx.android.synthetic.main.retrieve_otp_error_fragment.*
import kotlinx.android.synthetic.main.insurance_lead_retrieve_otp_fragment.*
import za.co.woolworths.financial.services.android.contracts.IProgressAnimationState
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.models.dto.otp.RetrieveOTPResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.util.Utils

open class BPIRetrieveOtpFragment : Fragment(), IProgressAnimationState,View.OnClickListener {

    private var mCircularProgressIndicator: ProgressIndicator? = null
    var navController: NavController? = null
    lateinit var absaCardToken: String
    lateinit var productOfferingId: String
    var bundle: Bundle? = null
    lateinit var otpMethodType: OTPMethodType
    var retrieveOTPResponse: RetrieveOTPResponse? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bpi_retrieve_otp_fragment, container, false)
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
        retry?.setOnClickListener(this)
        needHelp?.setOnClickListener(this)

        if(!BpiEnterOtpFragment.shouldBackPressed){
            mCircularProgressIndicator = ProgressIndicator(circularProgressIndicator,success_frame,imFailureIcon,success_tick)
            mCircularProgressIndicator?.progressIndicatorListener {}

            initRetrieveOTP(otpMethodType)
        }
    }

    private fun initRetrieveOTP(otpMethodType: OTPMethodType) {
        startProgress()
        OneAppService.retrieveOTP(otpMethodType, productOfferingId).enqueue(CompletionHandler(object : IResponseListener<RetrieveOTPResponse> {
            override fun onSuccess(response: RetrieveOTPResponse?) {
                response?.apply {
                    this@BPIRetrieveOtpFragment.retrieveOTPResponse = this
                    handleRetrieveOTPResponse(this)
                }
            }

            override fun onFailure(error: Throwable?) {
                mCircularProgressIndicator?.apply {
                    animationStatus = ProgressIndicator.AnimationStatus.Failure
                    stopSpinning()
                }
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
        if (!isAdded)return
        response?.apply {
            when (httpCode) {
                200 -> {
                    bundle?.apply {
                        putString("otpSentTo", otpSentTo)
                        putString("otpMethodType", otpMethodType.name)
                        if (otpMethodType == OTPMethodType.SMS)
                            putString("numberToOTPSent", otpSentTo)
                        navController?.navigate(R.id.action_sendOtpFragment_to_bpiEnterOtpFragment, bundleOf("bundle" to this))
                    }
                }

                else -> {
                    mCircularProgressIndicator?.apply {
                        animationStatus = ProgressIndicator.AnimationStatus.Failure
                        stopSpinning()
                    }
                }
            }
        }
    }


    private fun onRequestOTPRetry() {
        errorView?.visibility = View.GONE
        pageHeader?.visibility = View.VISIBLE
        processingLayout?.visibility = View.VISIBLE
        mCircularProgressIndicator?.spin()
        initRetrieveOTP(otpMethodType)
    }

    private fun startProgress() {
        mCircularProgressIndicator?.animationStatus = ProgressIndicator.AnimationStatus.InProgress
        mCircularProgressIndicator?.spin()
        processingLayout?.visibility = View.VISIBLE
    }

    private fun showErrorView() {
        pageHeader?.visibility = View.INVISIBLE
        processingLayout?.visibility = View.GONE
        errorView?.visibility = View.VISIBLE
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
            showErrorView()
        }
    }
}