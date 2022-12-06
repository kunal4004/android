package za.co.woolworths.financial.services.android.ui.fragments.credit_card_activation

import android.app.Activity
import android.graphics.Paint
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import com.awfs.coordination.databinding.CreditCardActivationProgressLayoutBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.PropertyNames.Companion.ACTION_LOWER_CASE
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.PropertyNames.Companion.activationConfirmed
import za.co.woolworths.financial.services.android.contracts.IProgressAnimationState
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.extension.findFragmentByTag
import za.co.woolworths.financial.services.android.ui.fragments.npc.ProgressStateFragment
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class CreditCardActivationProgressFragment : BaseFragmentBinding<CreditCardActivationProgressLayoutBinding>(CreditCardActivationProgressLayoutBinding::inflate), CreditCardActivationContract.CreditCardActivationView, IProgressAnimationState, View.OnClickListener {

    var presenter: CreditCardActivationContract.CreditCardActivationPresenter? = null
    lateinit var absaCardToken: String
    var isUserRetried: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        presenter = CreditCardActivationPresenterImpl(this, CreditCardActivationInteractorImpl())
        arguments?.getBundle("bundle")?.apply {
            absaCardToken = getString("absaCardToken", "")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activateCreditCard()
        binding.activationSuccessView.okGotItButton?.setOnClickListener(this)
        binding.activationProcessingLayout.processRequestTitleTextView?.text = resources.getString(R.string.credit_card_activation_processing_title)
        binding.activationFailureView.callToAction?.setOnClickListener(this)
        binding.activationFailureView.cancel?.apply {
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            setOnClickListener(this@CreditCardActivationProgressFragment)
        }
    }

    override fun activateCreditCard() {
        startProgress()
        presenter?.initCreditCardActivation(absaCardToken)
    }

    override fun getProgressState(): ProgressStateFragment? = (activity as? AppCompatActivity)?.findFragmentByTag(ProgressStateFragment::class.java.simpleName) as? ProgressStateFragment

    override fun startProgress() {
        (activity as? AppCompatActivity)?.addFragment(
                fragment = ProgressStateFragment.newInstance(this),
                tag = ProgressStateFragment::class.java.simpleName,
                containerViewId = R.id.flProgressIndicator
        )
        binding.activationProcessingLayout.root.visibility = View.VISIBLE
    }

    override fun onCreditCardActivationSuccess() {
        activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.CC_ACTIVATION_COMPLETE, hashMapOf(Pair(ACTION_LOWER_CASE, activationConfirmed)), this) }
        getProgressState()?.animateSuccessEnd(true)
        binding.activationProcessingLayout.root.visibility = View.GONE
        binding.activationSuccessView.root.visibility = View.VISIBLE
    }

    override fun onCreditCardActivationFailure() {
        getProgressState()?.animateSuccessEnd(false)
        binding.activationProcessingLayout.root.visibility = View.GONE
        binding.activationFailureView.root.visibility = View.VISIBLE
        binding.activationFailureView.failureTitleTextView.text = activity?.resources?.getString(if (isUserRetried) R.string.credit_card_activation_failure_title else R.string.credit_card_activation_retry_failure_title)
        binding.activationFailureView.callToAction.text = activity?.resources?.getString(if (isUserRetried) R.string.call_the_call_centre else R.string.retry)
        binding.activationFailureView.failureDescription.text = activity?.resources?.getString(if (isUserRetried) R.string.credit_card_activation_failure_desc else R.string.credit_card_activation_retry_failure_desc)
    }

    override fun onSessionTimeout() {

    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.clear()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.cancel -> activity?.onBackPressed()
            R.id.callToAction -> if(isUserRetried)activity?.apply { Utils.makeCall("0861 50 20 20") } else onRetryActivation()
            R.id.okGotItButton -> {
                activity?.apply {
                    setResult(Activity.RESULT_OK)
                    finish()
                    overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
                }
            }
        }
    }

    override fun onRetryActivation() {
        isUserRetried = true
        getProgressState()?.restartSpinning()
        binding.activationProcessingLayout.root.visibility = View.VISIBLE
        binding.activationFailureView.root.visibility = View.GONE
        presenter?.initCreditCardActivation(absaCardToken)
    }

}