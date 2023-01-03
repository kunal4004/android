package za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.opt_in.otp

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import com.awfs.coordination.databinding.BpiRetrieveOtpFragmentBinding
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.models.dto.otp.RetrieveOTPResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

open class BPIRetrieveOtpFragment : BaseFragmentBinding<BpiRetrieveOtpFragmentBinding>(BpiRetrieveOtpFragmentBinding::inflate),View.OnClickListener {

    private var menuCloseIcon: MenuItem? = null
    private var mCircularProgressIndicator: ProgressIndicator? = null
    var navController: NavController? = null
    lateinit var absaCardToken: String
    lateinit var productOfferingId: String
    var bundle: Bundle? = null
    lateinit var otpMethodType: OTPMethodType
    var retrieveOTPResponse: RetrieveOTPResponse? = null
    private var otpMethodTypeKey = "otpMethodType"
    private val absaCardTokenKey = "absaCardToken"
    private val productOfferingIdKey = "productOfferingId"
    private val numberToOTPSentKey = "numberToOTPSent"
    private val otpSentToKey = "otpSentTo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        bundle = arguments?.getBundle("bundle")
        bundle?.apply {
            absaCardToken = getString(absaCardTokenKey, "")
            productOfferingId = getString(productOfferingIdKey, "")
            otpMethodType = OTPMethodType.valueOf(getString(otpMethodTypeKey, OTPMethodType.SMS.name))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        binding.includeInsuranceLeadRetrieveOtp.apply {
            errorView.retry?.setOnClickListener(this@BPIRetrieveOtpFragment)
            errorView.needHelp?.setOnClickListener(this@BPIRetrieveOtpFragment)

            if (!BpiEnterOtpFragment.shouldBackPressed) {
                includeCircleProgressLayout.apply {
                    mCircularProgressIndicator = ProgressIndicator(
                        circularProgressIndicator,
                        successFrame,
                        imFailureIcon,
                        successTick
                    )
                }
                mCircularProgressIndicator?.progressIndicatorListener {}
                initRetrieveOTP(otpMethodType)
            }
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
                failureHandler()
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
                AppConstant.HTTP_OK -> {
                    bundle?.apply {
                        putString(otpSentToKey, otpSentTo)
                        putString(otpMethodTypeKey, otpMethodType.name)
                        if (otpMethodType == OTPMethodType.SMS)
                            putString(numberToOTPSentKey, otpSentTo)
                        navController?.navigate(R.id.action_sendOtpFragment_to_bpiEnterOtpFragment, bundleOf("bundle" to this))
                    }
                }

                else -> { failureHandler()}
            }
        }
    }

    private fun failureHandler() {
        mCircularProgressIndicator?.apply {
            animationStatus = ProgressIndicator.AnimationStatus.Failure
            stopSpinning()
            showErrorView()
        }
    }


    private fun onRequestOTPRetry() {
        binding.includeInsuranceLeadRetrieveOtp.apply {
            errorView.root.visibility = View.GONE
            pageHeader?.visibility = View.VISIBLE
            processingLayout.root.visibility = View.VISIBLE
        }
        mCircularProgressIndicator?.spin()
        menuCloseIconVisibility(false)
        initRetrieveOTP(otpMethodType)
        showBackArrow(true)
    }

    private fun startProgress() {
        mCircularProgressIndicator?.animationStatus = ProgressIndicator.AnimationStatus.InProgress
        mCircularProgressIndicator?.spin()
        menuCloseIconVisibility(false)
        binding.includeInsuranceLeadRetrieveOtp.processingLayout.root.visibility = View.VISIBLE
        showBackArrow(true)

    }

    private fun showErrorView() {
        binding.includeInsuranceLeadRetrieveOtp.apply {
            pageHeader.visibility = View.INVISIBLE
            processingLayout.root.visibility = View.GONE
            errorView.root.visibility = View.VISIBLE
        }
        menuCloseIconVisibility(true)
        showBackArrow(false)

    }

    private fun showBackArrow(state: Boolean) {
        (activity as? BalanceProtectionInsuranceActivity)?.apply {
            if (state) showDisplayHomeAsUpEnabled() else hideDisplayHomeAsUpEnabled()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.close_menu_item, menu)
        super.onCreateOptionsMenu(menu, inflater)
        menuCloseIcon = menu.findItem(R.id.closeIcon)
        menuCloseIconVisibility(false)
    }

    private fun menuCloseIconVisibility(state : Boolean) {
        menuCloseIcon?.isVisible = state
    }


    override fun onClick(v: View?) {
        when(v?.id){
            R.id.retry->onRequestOTPRetry()
            R.id.needHelp->activity?.apply { Utils.makeCall("0861 50 20 20") }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.closeIcon -> {
                activity?.apply {
                    finish()
                    overridePendingTransition(0,0)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}