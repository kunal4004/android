package za.co.woolworths.financial.services.android.ui.fragments.absa

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.circle_progress_layout.*
import kotlinx.coroutines.*
import za.co.woolworths.financial.services.android.ui.activities.ABSAOnlineBankingRegistrationActivity
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import za.co.woolworths.financial.services.android.ui.fragments.absa.AbsaFiveDigitCodeFragment.Companion.ALIAS_ID
import za.co.woolworths.financial.services.android.ui.fragments.absa.AbsaFiveDigitCodeFragment.Companion.CREDIT_CARD_TOKEN
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.opt_in.otp.ProgressIndicator
import za.co.woolworths.financial.services.android.util.AppConstant
import java.util.concurrent.TimeUnit

class AbsaSecurityCheckSuccessfulFragment : Fragment() {

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.absa_security_check_successful_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? ABSAOnlineBankingRegistrationActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        getArgs()
        showLockedPinIconResult()
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
            delay(TimeUnit.SECONDS.toMillis(AppConstant.DELAY_3_S))
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

    private fun showLockedPinIconResult() {
        imFailureIcon?.apply {
            visibility = View.VISIBLE
            setImageResource(R.drawable.ic_lock_purple)
        }
        val circularProgressIndicator = ProgressIndicator(circularProgressIndicator, success_frame, imFailureIcon, success_tick)
        circularProgressIndicator?.apply {
            animationStatus = ProgressIndicator.AnimationStatus.Failure
            progressIndicatorListener { }
            stopSpinning()
        }
    }

}