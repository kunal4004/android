package za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.opt_in.otp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.credit_card_activation_progress_layout.*
import kotlinx.android.synthetic.main.credit_card_activation_success_layout.*
import kotlinx.android.synthetic.main.my_accounts_fragment.*
import za.co.woolworths.financial.services.android.contracts.IProgressAnimationState
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.bpi.InsuranceTypeOptInBody
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.findFragmentByTag
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity
import za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel.BPIViewModel
import za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel.FailureHandler

import za.co.woolworths.financial.services.android.ui.fragments.npc.ProgressStateFragment
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.SessionUtilities


class BPIProcessingRequestFragment : Fragment(), IProgressAnimationState {

    private var mAccounts: Account? = null
    private val bpiViewModel: BPIViewModel? by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bpi_processing_your_request_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        successTitleTextView?.text = bindString(R.string.bpi_you_have_opted_in_success)

        (activity as? BalanceProtectionInsuranceActivity)?.hideDisplayHomeAsUpEnabled()

        fetchInsuranceLeadGenOptIn(activity)
        observeInsuranceLeadOptInResult()

        okGotItButton?.setOnClickListener {
           activity?.apply {
               val intent  = Intent()
               intent.putExtra(BalanceProtectionInsuranceActivity.ACCOUNT_RESPONSE,  Gson().toJson(mAccounts))
               setResult(AppConstant.BALANCE_PROTECTION_INSURANCE_OPT_IN_SUCCESS_RESULT_CODE,intent)
               finish()
               overridePendingTransition(0,0)
           }
        }
    }

    private fun observeInsuranceLeadOptInResult() {
        bpiViewModel?.apply {
            insuranceLeadGenOptIn.observe(viewLifecycleOwner, { accounts ->
                mAccounts = accounts
                isApiResultSuccess(true)
                activationProcessingLayout?.visibility = View.GONE
                activationSuccessView?.visibility = View.VISIBLE

            })

            failureHandler.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is FailureHandler.NoInternetConnection -> {
                        ErrorHandlerView(requireContext()).showToast()
                        navigateErrorScreen()
                    }
                    is FailureHandler.SessionTimeout -> SessionUtilities.getInstance()
                        .setSessionState(SessionDao.SESSION_STATE.INACTIVE, result.stsParams)
                    is FailureHandler.UnknownException,
                    is FailureHandler.UnknownHttpCode -> {
                        isApiResultSuccess(false)
                        navigateErrorScreen()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun navigateErrorScreen() {
        findNavController().navigate(R.id.action_BPIProcessingRequestFragment_to_bpiValidateOTPErrorFragment, bundleOf("bundle" to bundleOf("screenType" to BPIProcessingRequestFragment::class.java.simpleName)))
    }

    private fun fetchInsuranceLeadGenOptIn(activity: Activity?){
        startProgress()
        bpiViewModel?.apply {
            val validateOTPRequest = mValidateOTPRequest
            val insuranceTypeOptInBody = InsuranceTypeOptInBody(getProductGroupCode(), validateOTPRequest?.otp, validateOTPRequest?.otpMethod)
            fetchInsuranceLeadGenOptIn(activity,"bpi",insuranceTypeOptInBody)
        }
    }

    private fun startProgress() {
            (activity as? AppCompatActivity)?.addFragment(
                fragment = ProgressStateFragment.newInstance(this@BPIProcessingRequestFragment),
                tag = ProgressStateFragment::class.java.simpleName,
                containerViewId = R.id.flProgressIndicator
            )
            activationProcessingLayout?.visibility = View.VISIBLE
    }

    private fun getProgressState(): ProgressStateFragment? = (activity as? AppCompatActivity)?.findFragmentByTag(ProgressStateFragment::class.java.simpleName) as? ProgressStateFragment

    private fun isApiResultSuccess(state: Boolean) { getProgressState()?.animateSuccessEnd(state) }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.clear()
    }
}