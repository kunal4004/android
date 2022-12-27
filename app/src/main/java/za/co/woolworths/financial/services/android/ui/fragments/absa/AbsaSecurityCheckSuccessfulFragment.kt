package za.co.woolworths.financial.services.android.ui.fragments.absa

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AbsaSecurityCheckSuccessfulLayoutBinding
import kotlinx.coroutines.*
import za.co.woolworths.financial.services.android.ui.activities.ABSAOnlineBankingRegistrationActivity
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import za.co.woolworths.financial.services.android.ui.fragments.absa.AbsaFiveDigitCodeFragment.Companion.ALIAS_ID
import za.co.woolworths.financial.services.android.ui.fragments.absa.AbsaFiveDigitCodeFragment.Companion.CREDIT_CARD_TOKEN
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.opt_in.otp.ProgressIndicator
import za.co.woolworths.financial.services.android.util.AppConstant

class AbsaSecurityCheckSuccessfulFragment : Fragment(R.layout.absa_security_check_successful_layout) {

    private lateinit var binding: AbsaSecurityCheckSuccessfulLayoutBinding
    private var mAliasId: String? = null
    companion object {
        private var mCreditCardToken: String? = null

        fun newInstance(aliasId: String?, creditCardToken: String?) =
            AbsaSecurityCheckSuccessfulFragment().apply {
                arguments = Bundle(3).apply {
                    putString(ALIAS_ID, aliasId)
                    putString(CREDIT_CARD_TOKEN, creditCardToken)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = AbsaSecurityCheckSuccessfulLayoutBinding.bind(view)
        (activity as? ABSAOnlineBankingRegistrationActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        getArgs()
        binding.showLockedPinIconResult()
        navigateToFiveDigitOtpNumber()
    }

    private fun getArgs() {
        arguments?.apply {
            mAliasId =  getString(ALIAS_ID, "")
            mCreditCardToken = getString(CREDIT_CARD_TOKEN , "") ?: ""
        }
    }

    private fun navigateToFiveDigitOtpNumber() {
        CoroutineScope(Dispatchers.IO).launch {
            delay(AppConstant.DELAY_3000_MS)
            withContext(Dispatchers.Main) {
                replaceFragment(
                    fragment = AbsaFiveDigitCodeFragment.newInstance(mAliasId, mCreditCardToken),
                    tag = AbsaFiveDigitCodeFragment::class.java.simpleName,
                    containerViewId = R.id.flAbsaOnlineBankingToDevice,
                    allowStateLoss = true,
                    enterAnimation = R.anim.slide_in_from_right,
                    exitAnimation = R.anim.slide_to_left,
                    popEnterAnimation = R.anim.slide_from_left,
                    popExitAnimation = R.anim.slide_to_right
                )
            }
        }
    }

    private fun AbsaSecurityCheckSuccessfulLayoutBinding.showLockedPinIconResult() {
        imFailureIcon?.apply {
            visibility = View.VISIBLE
            setImageResource(R.drawable.ic_lock_purple)
        }
        val circularProgressIndicator = ProgressIndicator(circularProgressIndicator, progressBar, imFailureIcon, successTick)
        circularProgressIndicator?.apply {
            animationStatus = ProgressIndicator.AnimationStatus.Failure
            progressIndicatorListener { }
            stopSpinning()
        }
    }
}